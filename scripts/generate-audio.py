#!/usr/bin/env python3
"""Generates NanoCaptcha's spoken digits and audio test fixtures with Piper.

Every file under src/main/resources/sounds/<language>/numbers comes from this
script, as do the audio fixtures in src/test/resources. It uses only Piper
voices trained entirely on public-domain or CC BY recordings; NOTICE credits
them. The Whisper speech recogniser checks every digit, and any take
it doesn't hear as the right digit is replaced.

Run it from the project root, in a virtual environment:

    python3 -m venv .venv
    .venv/bin/pip install -r scripts/requirements-audio.txt
    .venv/bin/python scripts/generate-audio.py

Piper's voice models and Whisper's (about 830 MB together) are downloaded
into .audio-models on first use. Piper's randomness is seeded, so a run
repeats the same takes on the same platform with the same package versions.
"""

import argparse
import contextlib
import os
import re
import shutil
import sys
import wave
from pathlib import Path

import _pywhispercpp as whisper_cpp
import numpy as np
import onnxruntime
from piper import PiperVoice, SynthesisConfig
from piper.const import BOS, PAD
from piper.download_voices import download_voice
from piper.phonemize_espeak import ESPEAK_DATA_DIR
from pywhispercpp.model import Model
from pywhispercpp.utils import download_model

ROOT = Path(__file__).resolve().parent.parent
SOUNDS = ROOT / "src" / "main" / "resources" / "sounds"
FIXTURES = ROOT / "src" / "test" / "resources"

# The format Sample requires: 16 kHz, 16-bit, mono, little-endian.
SAMPLE_RATE = 16_000

# Every clip is scaled to this loudness (RMS of its voiced part), unless that
# would push its peak above the ceiling.
TARGET_RMS_DBFS = -18.0
PEAK_CEILING_DBFS = -3.0

# Silence before and after each word, so concatenated digits stay distinct.
LEAD_SECONDS = 0.06
TAIL_SECONDS = 0.10

# Piper's voices were trained on whole sentences, and often babble when asked
# for a single word. Much less noise than their defaults makes these two far
# more reliable. The others read a list instead (see read_list); LJSpeech, for
# one, clips "nine" when it's said alone.
SINGLE_WORD_VOICES = {"en_GB-cori-medium", "en_US-john-medium"}
NOISE = 0.1

# Piper's output is random, so a take that isn't a single word, or that
# Whisper doesn't hear as the right digit, gets retried.
ATTEMPTS = 8
MAX_WORD_SECONDS = 1.1

# The other voices read all ten digits in one utterance, a little slower than
# normal, and the best of several readings is kept for each digit. A digit
# Whisper hasn't heard correctly by then gets more readings, because some
# speakers get their hardest digit right only rarely.
LIST_LENGTH_SCALE = 1.2
LIST_TAKES = 6
MAX_LIST_TAKES = 200

# Seeds Piper's randomness, so runs are repeatable
SEED = 35

WORDS = {
    "en": ["zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"],
    "de": ["null", "eins", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht", "neun"],
    "fr": ["zéro", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf"],
}

# Whisper sometimes writes a lone French digit as a word that sounds the same.
HOMOPHONES = {
    "fr": {"zero": 0, "hein": 1, "de": 2, "d'eux": 2, "sis": 6, "cisse": 6, "set": 7, "cette": 7, "sète": 7},
}

# (language, suffix, Piper voice, speaker). The suffix is the letter after the
# digit in each file name, and must match RandomNumberVoiceProducer. The
# speaker is the Multilingual LibriSpeech speaker ID for multi-speaker voices.
VOICES = [
    ("en", "a", "en_US-ljspeech-medium", None),
    ("en", "b", "en_GB-cori-medium", None),
    ("en", "c", "en_US-john-medium", None),
    ("de", "a", "de_DE-mls-medium", "10191"),
    ("de", "b", "de_DE-mls-medium", "5055"),
    ("fr", "a", "fr_FR-mls-medium", "94"),
]

# Phonemes that separate words
BREAKS = {" ", ",", ".", ";", ":", "!", "?"}


def load(name, models_dir):
    """Downloads the named Piper voice if necessary, and loads it."""
    models_dir.mkdir(parents=True, exist_ok=True)
    download_voice(name, models_dir)
    # espeak-ng exits if its data path is too long (168 characters was), so
    # pass whichever form of the path is shorter.
    espeak = min(str(ESPEAK_DATA_DIR), os.path.relpath(ESPEAK_DATA_DIR), key=len)
    # Alignments report the samples spent on each phoneme (needs onnx).
    return PiperVoice.load(models_dir / f"{name}.onnx", espeak_data_dir=espeak, include_alignments=True)


@contextlib.contextmanager
def quiet_stderr():
    """Hides whisper.cpp's start-up messages; pywhispercpp's own option never restores stderr."""
    sys.stderr.flush()
    saved = os.dup(2)
    try:
        with open(os.devnull, "w") as devnull:
            os.dup2(devnull.fileno(), 2)
            yield
    finally:
        os.dup2(saved, 2)
        os.close(saved)


class Listener:
    """Whisper, which must hear each digit correctly for it to be kept."""

    def __init__(self, models_dir):
        path = download_model("small", str(models_dir))
        with quiet_stderr():
            self.model = Model(path, print_progress=False, print_realtime=False)

    def hear(self, clip, language):
        """Returns the digits Whisper hears in a clip, and the probability of its least likely word."""
        # whisper.cpp ignores anything shorter than a second.
        pad = max(2 * SAMPLE_RATE - len(clip), 0)
        audio = np.pad(clip, (pad // 2, pad - pad // 2)).astype(np.float32)
        segments = self.model.transcribe(audio, language=language, single_segment=True, no_context=True)
        # Token probabilities are only available from whisper.cpp itself.
        context, probabilities = self.model._ctx, []
        for i in range(whisper_cpp.whisper_full_n_segments(context)):
            for j in range(whisper_cpp.whisper_full_n_tokens(context, i)):
                token = whisper_cpp.whisper_full_get_token_text(context, i, j)
                token = token.decode("utf-8", "replace") if isinstance(token, bytes) else token
                if not token.startswith(("[_", "<|")) and re.search(r"\w", token):
                    probabilities.append(whisper_cpp.whisper_full_get_token_p(context, i, j))
        return digits_in(" ".join(s.text for s in segments), language), min(probabilities, default=0.0)


def digits_in(text, language):
    """Returns the digits in a transcript, in numerals or words, with None for any other word."""
    words, homophones = WORDS[language], HOMOPHONES.get(language, {})
    found = []
    for token in re.findall(r"\d|[^\W\d_]+(?:'[^\W\d_]+)?", text.lower().replace("’", "'")):
        found.append(int(token) if token.isdigit() else words.index(token) if token in words else homophones.get(token))
    return found


def speak(voice, text):
    """Returns Piper's audio for text, as floats in [-1, 1] at the voice's rate."""
    config = SynthesisConfig(normalize_audio=False, noise_scale=NOISE, noise_w_scale=NOISE)
    return np.concatenate([chunk.audio_float_array for chunk in voice.synthesize(text, config)])


def is_single_word(audio, rate):
    """Is the loud part of audio one short burst, rather than several?"""
    frame = rate // 20
    frames = audio[:len(audio) // frame * frame].reshape(-1, frame)
    level = 20 * np.log10(np.sqrt(np.mean(frames ** 2, axis=1)) + 1e-9)
    loud = np.flatnonzero(level > level.max() - 18)
    # Gaps of more than 150 ms between loud frames mean separate bursts.
    return not np.any(np.diff(loud) > 3) and (loud[-1] - loud[0] + 1) / 20 <= MAX_WORD_SECONDS


def say_digits(voice, language, listener):
    """Returns a clip of each digit, spoken on its own."""
    rate = voice.config.sample_rate
    clips = []
    for number, word in enumerate(WORDS[language]):
        for _ in range(ATTEMPTS):
            audio = speak(voice, word)
            if is_single_word(audio, rate):
                clip = finish(audio, rate)
                if listener.hear(clip, language)[0] == [number]:
                    clips.append(clip)
                    break
        else:
            raise RuntimeError(f"no clean take of {word!r}")
    return clips


def read_list(voice, language, speaker):
    """Reads every digit in one utterance, and returns the audio of each.

    Each digit is a sentence of its own, with an extra digit at either end so
    that all ten sound mid-list. Piper would synthesise separate sentences
    separately, so this builds the phonemes itself.
    """
    words = WORDS[language]
    id_map = voice.config.phoneme_id_map
    phonemes = []
    for word in [words[9], *words, words[0]]:
        phonemes += [p for sentence in voice.phonemize(word) for p in sentence if p in id_map and p not in BREAKS]
        phonemes += [".", " "]
    phonemes.pop()
    speaker_id = voice.config.speaker_id_map[speaker] if speaker else None
    config = SynthesisConfig(speaker_id=speaker_id, length_scale=LIST_LENGTH_SCALE)
    audio, samples = voice.phoneme_ids_to_audio(voice.phonemes_to_ids(phonemes), config, include_alignments=True)
    # Find where each word starts and ends, from the samples spent on each
    # phoneme id; every phoneme is followed by a PAD.
    index = len(id_map[BOS]) + len(id_map[PAD])
    position = int(samples[:index].sum())
    spans, start = [], None
    for phoneme in phonemes:
        ids = len(id_map[phoneme]) + len(id_map[PAD])
        if phoneme in BREAKS:
            if start is not None:
                spans.append((start, position))
                start = None
        elif start is None:
            start = position
        position += int(samples[index:index + ids].sum())
        index += ids
    # Cut each pause at its quietest point.
    level = envelope(audio, voice.config.sample_rate, 0.01)
    cuts = [end + int(np.argmin(level[end:start])) if start > end else end for (_, end), (start, _) in zip(spans, spans[1:])]
    return [audio[a:b] for a, b in zip(cuts, cuts[1:])]


def read_digits(voice, language, speaker, listener):
    """Returns a clip of each digit, the clearest from several readings of the list."""
    rate = voice.config.sample_rate
    best = [(0.0, None)] * 10
    for take in range(MAX_LIST_TAKES):
        missing = {number for number, (_, clip) in enumerate(best) if clip is None}
        if take >= LIST_TAKES and not missing:
            break
        for number, audio in enumerate(read_list(voice, language, speaker)):
            if take >= LIST_TAKES and number not in missing:
                continue
            clip = finish(audio, rate)
            heard, probability = listener.hear(clip, language)
            if heard == [number] and probability > best[number][0]:
                best[number] = (probability, clip)
    missing = [WORDS[language][number] for number, (_, clip) in enumerate(best) if clip is None]
    if missing:
        raise RuntimeError(f"no clean take of {', '.join(missing)}" + (f" from speaker {speaker}" if speaker else ""))
    return [clip for _, clip in best]


def envelope(audio, rate, seconds):
    """Returns the RMS of each sample's surrounding window."""
    width = max(int(rate * seconds), 1)
    return np.sqrt(np.convolve(audio ** 2, np.ones(width) / width, mode="same"))


def trim(audio, rate):
    """Cuts leading and trailing silence, then pads with exact amounts of it."""
    level = envelope(audio, rate, 0.01)
    voiced = np.flatnonzero(level > level.max() * 10 ** (-45 / 20))
    # A little extra either side keeps soft consonants, like the "f" in "five".
    margin = int(rate * 0.02)
    clip = audio[max(voiced[0] - margin, 0):voiced[-1] + margin].copy()
    fade = int(rate * 0.01)
    clip[:fade] *= np.linspace(0, 1, fade)
    clip[-fade:] *= np.linspace(1, 0, fade)
    return np.concatenate([np.zeros(int(rate * LEAD_SECONDS)), clip, np.zeros(int(rate * TAIL_SECONDS))])


def resample(audio, source_rate, target_rate):
    """Resamples by truncating the spectrum, which is fine for padded clips."""
    length = int(round(len(audio) * target_rate / source_rate))
    return np.fft.irfft(np.fft.rfft(audio), length) * (length / len(audio))


def normalise(audio, rate):
    """Scales audio to the target loudness, within the peak ceiling."""
    level = envelope(audio, rate, 0.02)
    voiced = audio[level > level.max() * 10 ** (-35 / 20)]
    gain = 10 ** (TARGET_RMS_DBFS / 20) / np.sqrt(np.mean(voiced ** 2))
    gain = min(gain, 10 ** (PEAK_CEILING_DBFS / 20) / np.abs(audio).max())
    return audio * gain


def finish(audio, rate):
    """Returns a finished clip at SAMPLE_RATE: trimmed, resampled and normalised."""
    return normalise(resample(trim(audio.astype(np.float64), rate), rate, SAMPLE_RATE), SAMPLE_RATE)


def write_wav(path, audio, rate):
    """Writes audio as a 16-bit mono WAV file."""
    pcm = np.clip(np.round(audio * 32767), -32768, 32767).astype("<i2")
    with wave.open(str(path), "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(rate)
        out.writeframes(pcm.tobytes())


def generate_digits(models_dir):
    """Writes every digit for every voice, removing any files left over."""
    onnxruntime.set_seed(SEED)
    listener = Listener(models_dir)
    loaded = {}
    written = set()
    for language, suffix, name, speaker in VOICES:
        voice = loaded.get(name) or loaded.setdefault(name, load(name, models_dir))
        try:
            if name in SINGLE_WORD_VOICES:
                clips = say_digits(voice, language, listener)
            else:
                clips = read_digits(voice, language, speaker, listener)
        except RuntimeError as e:
            raise RuntimeError(f"{name}: {e}") from None
        directory = SOUNDS / language / "numbers"
        directory.mkdir(parents=True, exist_ok=True)
        for number, clip in enumerate(clips):
            path = directory / f"{number}_{suffix}.wav"
            write_wav(path, clip, SAMPLE_RATE)
            written.add(path)
        print(f"{language} {suffix}: {name}" + (f" speaker {speaker}" if speaker else ""))
    for language in sorted({language for language, *_ in VOICES}):
        for stale in sorted((SOUNDS / language / "numbers").glob("*.wav")):
            if stale not in written:
                stale.unlink()
                print(f"removed {stale.relative_to(ROOT)}")
    return loaded


def generate_fixtures(loaded, models_dir):
    """Writes the audio fixtures SampleTest uses."""
    import soundfile  # only needed here, for MP3

    _, _, name, _ = VOICES[0]
    voice = loaded.get(name) or load(name, models_dir)
    rate = voice.config.sample_rate
    hello = normalise(trim(speak(voice, "Hello."), rate), rate)
    # A valid WAV file in a format Sample rejects (8 kHz)
    write_wav(FIXTURES / "hello.wav", resample(hello, rate, 8_000), 8_000)
    # A format AudioSystem can't read at all
    soundfile.write(str(FIXTURES / "hello.mp3"), hello, rate, format="MP3", subtype="MPEG_LAYER_III")
    # A copy of one of the real samples
    shutil.copyfile(SOUNDS / "en" / "numbers" / "0_a.wav", FIXTURES / "0_a.wav")
    print("fixtures: hello.wav, hello.mp3, 0_a.wav")


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--models", type=Path, default=ROOT / ".audio-models",
                        help="directory for downloaded Piper and Whisper models (default: .audio-models)")
    args = parser.parse_args()
    loaded = generate_digits(args.models)
    generate_fixtures(loaded, args.models)


if __name__ == "__main__":
    main()
