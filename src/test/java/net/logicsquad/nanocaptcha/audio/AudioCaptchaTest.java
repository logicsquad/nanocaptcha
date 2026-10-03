package net.logicsquad.nanocaptcha.audio;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.audio.producer.RandomNumberVoiceProducer;

/**
 * Unit tests on {@link AudioCaptcha} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.2
 */
public class AudioCaptchaTest {
	@Test
	public void audioCanBeWrittenTwice() throws IOException {
		assertWritesTwice(AudioCaptcha.create());
		assertWritesTwice(new AudioCaptcha.Factory.Builder().addContent().addVoice().addNoise().build().create());
		return;
	}

	@Test
	public void everyLanguageMakesAudioFromItsDigits() {
		for (Locale language : Arrays.asList(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH)) {
			for (boolean noisy : new boolean[] { false, true }) {
				String what = language + (noisy ? ", with noise" : "");
				RandomNumberVoiceProducer voice = new RandomNumberVoiceProducer(language);
				// Keep the digits the CAPTCHA is made from, to check its length against
				List<Sample> digits = new ArrayList<>();
				AudioCaptcha.Factory.Builder builder = new AudioCaptcha.Factory.Builder().addContent().addVoice(c -> {
					Sample digit = voice.getVocalization(c);
					digits.add(digit);
					return digit;
				});
				if (noisy) {
					builder.addNoise();
				}
				AudioCaptcha captcha = builder.build().create();
				assertTrue(captcha.getContent().matches("[0-9]{5}"), what + ": " + captcha.getContent());
				assertEquals(5, digits.size(), what);
				Sample audio = captcha.getAudio();
				// The digits, and up to a quarter of a second after each but the last
				long length = digits.stream().mapToLong(Sample::getSampleCount).sum();
				assertTrue(audio.getSampleCount() >= length && audio.getSampleCount() <= length + 4 * 4000,
						what + ": " + audio.getSampleCount() + " samples from " + length);
				double peak = Arrays.stream(audio.getInterleavedSamples()).map(Math::abs).max().getAsDouble();
				assertTrue(peak > 0.1, what + ": peak " + peak);
				assertArrayEquals(audio.toWav(), audio.toWav(), what);
			}
		}
		return;
	}

	@Test
	public void eachDigitGetsARandomGapAndVolume() {
		// The same clip for every digit, so that only the gaps and volumes can vary
		Sample one = new RandomNumberVoiceProducer(Locale.ENGLISH).getVocalization('1');
		AudioCaptcha.Factory factory = new AudioCaptcha.Factory.Builder().addContent(() -> "11111").addVoice(c -> one).build();
		Set<Long> lengths = new HashSet<>();
		Set<Double> peaks = new HashSet<>();
		for (int i = 0; i < 20; i++) {
			Sample audio = factory.create().getAudio();
			lengths.add(audio.getSampleCount());
			peaks.add(Arrays.stream(audio.getInterleavedSamples()).map(Math::abs).max().getAsDouble());
		}
		assertTrue(lengths.size() > 15, "lengths: " + lengths);
		assertTrue(peaks.size() > 15, "peaks: " + peaks);
		return;
	}

	@Test
	public void factoryMakesANewCaptchaEachTime() {
		AtomicInteger count = new AtomicInteger();
		AudioCaptcha.Factory factory = new AudioCaptcha.Factory.Builder().addContent(() -> "1234" + count.incrementAndGet())
				.addNoise().build();
		AudioCaptcha first = factory.create();
		byte[] wav = first.getAudio().toWav();
		AudioCaptcha second = factory.create();
		assertEquals("12341", first.getContent());
		assertEquals("12342", second.getContent());
		// Making the second CAPTCHA didn't touch the first
		assertArrayEquals(wav, first.getAudio().toWav());
		return;
	}

	@Test
	public void factoryIsntChangedByItsBuilder() {
		AudioCaptcha.Factory.Builder builder = new AudioCaptcha.Factory.Builder().addContent(() -> "12");
		AudioCaptcha.Factory factory = builder.build();
		builder.addContent(() -> "34");
		assertEquals("12", factory.create().getContent());
		assertEquals("1234", builder.build().create().getContent());
		return;
	}

	@Test
	public void factoryMakesCaptchasOnManyThreadsAtOnce() throws InterruptedException, ExecutionException {
		AudioCaptcha.Factory factory = new AudioCaptcha.Factory.Builder().addContent().addVoice().addNoise().build();
		ExecutorService executor = Executors.newFixedThreadPool(8);
		try {
			List<Future<AudioCaptcha>> futures = new ArrayList<>();
			for (int i = 0; i < 100; i++) {
				futures.add(executor.submit(factory::create));
			}
			Set<String> contents = new HashSet<>();
			for (Future<AudioCaptcha> future : futures) {
				AudioCaptcha captcha = future.get();
				assertTrue(captcha.getContent().matches("[0-9]{5}"), captcha.getContent());
				// At least a second of audio
				assertTrue(captcha.getAudio().getSampleCount() > 16_000, captcha.getAudio().getSampleCount() + " samples");
				contents.add(captcha.getContent());
			}
			// A new answer each time
			assertTrue(contents.size() > 90, contents.size() + " different answers from 100");
		} finally {
			executor.shutdownNow();
		}
		return;
	}

	@Test
	public void toStringGivesTheLengthOfTheAnswerButNotTheAnswer() {
		AudioCaptcha captcha = new AudioCaptcha.Factory.Builder().addContent(() -> "12345").build().create();
		assertEquals("[AudioCaptcha: created=" + captcha.getCreated() + " content=5 characters]", captcha.toString());
		return;
	}

	@Test
	public void isCorrectIgnoresSurroundingWhitespaceUnlessAskedNotTo() {
		AudioCaptcha captcha = new AudioCaptcha.Factory.Builder().addContent(() -> "12345").build().create();
		for (String answer : new String[] { "12345", " 12345", "12345 \n" }) {
			assertTrue(captcha.isCorrect(answer), "'" + answer + "'");
		}
		for (String answer : new String[] { "1234", "123450", "123 45", "", null }) {
			assertFalse(captcha.isCorrect(answer), "'" + answer + "'");
		}
		assertTrue(captcha.isCorrect("12345", false));
		assertFalse(captcha.isCorrect(" 12345", false));
		assertFalse(captcha.isCorrect(null, false));
		return;
	}

	/**
	 * Writes {@code captcha}'s audio twice, and checks that both copies hold the same audio (#40).
	 *
	 * @param captcha an {@link AudioCaptcha}
	 * @throws IOException if unable to write the audio
	 */
	private static void assertWritesTwice(AudioCaptcha captcha) throws IOException {
		ByteArrayOutputStream first = new ByteArrayOutputStream();
		ByteArrayOutputStream second = new ByteArrayOutputStream();
		AudioSystem.write(captcha.getAudio().getAudioInputStream(), AudioFileFormat.Type.WAVE, first);
		AudioSystem.write(captcha.getAudio().getAudioInputStream(), AudioFileFormat.Type.WAVE, second);
		assertTrue(first.size() > 44, "only " + first.size() + " bytes");
		assertArrayEquals(first.toByteArray(), second.toByteArray());
		return;
	}
}
