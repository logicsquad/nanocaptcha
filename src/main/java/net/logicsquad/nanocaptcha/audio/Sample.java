package net.logicsquad.nanocaptcha.audio;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Class representing a sound sample, typically read in from a file. Note that
 * at this time this class only supports wav files with the following
 * characteristics:
 * </p>
 *
 * <ul>
 * <li>Sample rate: 16KHz</li>
 * <li>Sample size: 16 bits</li>
 * <li>Channels: 1</li>
 * <li>Signed: true</li>
 * <li>Big Endian: false</li>
 * </ul>
 *
 * <p>
 * Data files in other formats will cause an
 * <code>IllegalArgumentException</code> to be thrown.
 * </p>
 *
 * <p>
 * A {@code Sample} reads all of its audio when it's created, and doesn't change after that, so it can be played or
 * written any number of times.
 * </p>
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class Sample {
	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Sample.class);

	/**
	 * {@link AudioFormat} for all {@code Sample}s
	 */
	public static final AudioFormat SC_AUDIO_FORMAT = new AudioFormat(16_000, // sample rate
			16, // sample size in bits
			1, // channels
			true, // signed?
			false); // big endian?;

	/**
	 * Audio data, in {@link #SC_AUDIO_FORMAT}
	 */
	private final byte[] data;

	/**
	 * Constructor taking an {@link InputStream}, which it reads to the end but doesn't close.
	 *
	 * @param is an {@link InputStream}
	 * @throws NullPointerException     if {@code is} is {@code null}
	 * @throws IllegalArgumentException if the audio format is unsupported
	 * @throws RuntimeException         if
	 *                                  {@link AudioSystem#getAudioInputStream(InputStream)}
	 *                                  is unable to read the audio stream
	 */
	public Sample(InputStream is) {
		this(read(is));
	}

	/**
	 * Constructor taking a {@link URL}, which it opens, reads to the end and then closes. For a resource of your own, use
	 * the {@link URL} from your class's {@link Class#getResource(String)}.
	 *
	 * @param url a {@link URL}
	 * @throws NullPointerException     if {@code url} is {@code null}, as it is when {@link Class#getResource(String)}
	 *                                  can't find a resource
	 * @throws IllegalArgumentException if the audio format is unsupported
	 * @throws UncheckedIOException     if {@code url} can't be opened
	 * @throws RuntimeException         if
	 *                                  {@link AudioSystem#getAudioInputStream(InputStream)}
	 *                                  is unable to read the audio stream
	 * @since 2.2
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/50">#50</a>
	 */
	public Sample(URL url) {
		this(read(url));
	}

	/**
	 * Constructor taking audio data in {@link #SC_AUDIO_FORMAT}.
	 *
	 * @param data audio data
	 */
	private Sample(byte[] data) {
		this.data = data;
		return;
	}

	/**
	 * Returns the audio data from {@code url}, closing the stream once it's read.
	 *
	 * @param url a {@link URL}
	 * @return audio data
	 */
	private static byte[] read(URL url) {
		Objects.requireNonNull(url, "The URL is null, as Class.getResource() returns when it can't find a resource.");
		try (InputStream is = url.openStream()) {
			return read(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Returns the audio data from {@code is}, which is read to the end.
	 *
	 * @param is an {@link InputStream}
	 * @return audio data
	 */
	private static byte[] read(InputStream is) {
		Objects.requireNonNull(is);
		try {
			AudioInputStream audio = is instanceof AudioInputStream ? (AudioInputStream) is
					: AudioSystem.getAudioInputStream(new BufferedInputStream(is));
			if (!audio.getFormat().matches(SC_AUDIO_FORMAT)) {
				throw new IllegalArgumentException("Unsupported audio format.");
			}
			// A single read() can return less than the whole clip, so keep going until the end.
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int count;
			while ((count = audio.read(buffer)) != -1) {
				data.write(buffer, 0, count);
			}
			return data.toByteArray();
		} catch (UnsupportedAudioFileException | IOException e) {
			LOG.error("Unable to get audio input stream.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a new {@link AudioInputStream} for this {@code Sample}. Each call starts from the beginning, so the audio
	 * can be read more than once.
	 *
	 * @return {@link AudioInputStream}
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/40">#40</a>
	 */
	public AudioInputStream getAudioInputStream() {
		return new AudioInputStream(new ByteArrayInputStream(data), SC_AUDIO_FORMAT, getSampleCount());
	}

	/**
	 * Writes this {@code Sample} to {@code out} as a WAV file, leaving {@code out} open.
	 *
	 * @param out an {@link OutputStream}
	 * @throws IOException if unable to write to {@code out}
	 * @since 2.2
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/40">#40</a>
	 */
	public void writeWav(OutputStream out) throws IOException {
		AudioSystem.write(getAudioInputStream(), AudioFileFormat.Type.WAVE, Objects.requireNonNull(out));
	}

	/**
	 * Returns this {@code Sample} as the contents of a WAV file.
	 *
	 * @return WAV file contents
	 * @since 2.2
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/40">#40</a>
	 */
	public byte[] toWav() {
		ByteArrayOutputStream out = new ByteArrayOutputStream(data.length + 44);
		try {
			writeWav(out);
		} catch (IOException e) {
			// ByteArrayOutputStream doesn't throw this
			throw new UncheckedIOException(e);
		}
		return out.toByteArray();
	}

	/**
	 * Return the number of samples for all channels.
	 *
	 * @return number of samples for all channels
	 */
	long getSampleCount() {
		return data.length / SC_AUDIO_FORMAT.getFrameSize();
	}

	/**
	 * Returns interleaved samples for this {@code Sample}, scaled to [-1, 1).
	 *
	 * @return interleaved samples
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/41">#41</a>
	 */
	double[] getInterleavedSamples() {
		double[] samples = new double[(int) getSampleCount()];
		for (int i = 0; i < samples.length; i++) {
			// 16-bit little-endian: only the high byte carries the sign.
			samples[i] = (short) ((data[2 * i + 1] << 8) | (data[2 * i] & 0xFF)) / 32_768.0;
		}
		return samples;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(26);
		sb.append("[Sample: samples=").append(getSampleCount()).append(" format=").append(SC_AUDIO_FORMAT).append(']');
		return sb.toString();
	}
}
