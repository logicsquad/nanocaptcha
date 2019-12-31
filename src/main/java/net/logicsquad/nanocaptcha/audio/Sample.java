package net.logicsquad.nanocaptcha.audio;

import java.io.*;
import java.util.Objects;

import javax.sound.sampled.*;

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
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class Sample {
	/**
	 * {@link AudioFormat} for all {@code Sample}s
	 */
	public static final AudioFormat SC_AUDIO_FORMAT = new AudioFormat(16000, // sample rate
			16, // sample size in bits
			1, // channels
			true, // signed?
			false); // big endian?;

	/**
	 * {@link AudioInputStream} for this {@code Sample}
	 */
	private final AudioInputStream audioInputStream;

	/**
	 * Constructor taking a filename.
	 *
	 * @param filename filename
	 * @throws NullPointerException if {@code filename} is {@code null}
	 */
	public Sample(String filename) {
		this(Sample.class.getResourceAsStream(Objects.requireNonNull(filename)));
	}

	/**
	 * Constructor taking an {@link InputStream}.
	 *
	 * @param is an {@link InputStream}
	 * @throws NullPointerException     if {@code is} is {@code null}
	 * @throws IllegalArgumentException if the audio format is unsupported
	 * @throws RuntimeException         if
	 *                                  {@link AudioSystem#getAudioInputStream(InputStream)}
	 *                                  is unable to read the audio stream
	 */
	public Sample(InputStream is) {
		Objects.requireNonNull(is);
		if (is instanceof AudioInputStream) {
			audioInputStream = (AudioInputStream) is;
		} else {
			try {
				audioInputStream = AudioSystem.getAudioInputStream(is);
			} catch (UnsupportedAudioFileException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (!audioInputStream.getFormat().matches(SC_AUDIO_FORMAT)) {
			throw new IllegalArgumentException("Unsupported audio format.");
		}
		return;
	}

	/**
	 * Returns {@link AudioInputStream} for this {@code Sample}.
	 *
	 * @return {@link AudioInputStream}
	 */
	public AudioInputStream getAudioInputStream() {
		return audioInputStream;
	}

	/**
	 * Returns {@link AudioFormat} for this {@code Sample}.
	 *
	 * @return {@link AudioFormat}
	 */
	private AudioFormat getFormat() {
		return audioInputStream.getFormat();
	}

	/**
	 * Return the number of samples for all channels.
	 *
	 * @return number of samples for all channels
	 */
	long getSampleCount() {
		long total = (audioInputStream.getFrameLength() * getFormat().getFrameSize() * 8)
				/ getFormat().getSampleSizeInBits();
		return total / getFormat().getChannels();
	}

	/**
	 * Returns interleaved samples for this {@code Sample}.
	 *
	 * @return interleaved samples
	 */
	double[] getInterleavedSamples() {
		double[] samples = new double[(int) getSampleCount()];
		try {
			getInterleavedSamples(0, getSampleCount(), samples);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return samples;
	}

	/**
	 * Returns the interleaved decoded samples for all channels, from sample index
	 * {@code start} (included) to sample index {@code end} (excluded) and copy them
	 * into {@code samples}. {@code end} must not exceed {@code getSampleCount()},
	 * and the number of samples must not be so large that the associated byte array
	 * cannot be allocated.
	 *
	 * @param start   start index
	 * @param end     end index
	 * @param samples destination array
	 * @throws IOException              if unable to read from
	 *                                  {@link AudioInputStream}
	 * @throws IllegalArgumentException if sample is too large
	 */
	private double[] getInterleavedSamples(long start, long end, double[] samples)
			throws IOException, IllegalArgumentException {
		long nbSamples = end - start;
		long nbBytes = nbSamples * (getFormat().getSampleSizeInBits() / 8) * getFormat().getChannels();
		if (nbBytes > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Too many samples. Try using a smaller wav.");
		}
		// allocate a byte buffer
		byte[] inBuffer = new byte[(int) nbBytes];
		// read bytes from audio file
		audioInputStream.read(inBuffer, 0, inBuffer.length);
		// decode bytes into samples.
		decodeBytes(inBuffer, samples);

		return samples;
	}

	/**
	 * Decodes audio as bytes in {@code audioBytes} into audio as samples and writes
	 * the result into {@code audioSamples}.
	 *
	 * @param audioBytes   source audio as bytes
	 * @param audioSamples destination audio as samples
	 */
	private void decodeBytes(byte[] audioBytes, double[] audioSamples) {
		int sampleSizeInBytes = getFormat().getSampleSizeInBits() / 8;
		int[] sampleBytes = new int[sampleSizeInBytes];
		int k = 0; // index in audioBytes
		for (int i = 0; i < audioSamples.length; i++) {
			// collect sample byte in big-endian order
			if (getFormat().isBigEndian()) {
				// bytes start with MSB
				for (int j = 0; j < sampleSizeInBytes; j++) {
					sampleBytes[j] = audioBytes[k++];
				}
			} else {
				// bytes start with LSB
				for (int j = sampleSizeInBytes - 1; j >= 0; j--) {
					sampleBytes[j] = audioBytes[k++];
					if (sampleBytes[j] != 0) {
						j = j + 0;
					}
				}
			}
			// get integer value from bytes
			int ival = 0;
			for (int j = 0; j < sampleSizeInBytes; j++) {
				ival += sampleBytes[j];
				if (j < sampleSizeInBytes - 1) {
					ival <<= 8;
				}
			}
			// decode value
			double ratio = Math.pow(2., getFormat().getSampleSizeInBits() - 1);
			double val = ((double) ival) / ratio;
			audioSamples[i] = val;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(26);
		sb.append("[Sample: samples=").append(getSampleCount()).append(" format=").append(getFormat()).append("]");
		return sb.toString();
	}
}
