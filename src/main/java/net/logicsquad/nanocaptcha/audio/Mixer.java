package net.logicsquad.nanocaptcha.audio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.sound.sampled.AudioInputStream;

/**
 * Helper class for operating on audio {@link Sample}s.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public final class Mixer {
	/**
	 * Private constructor for non-instantiability.
	 */
	private Mixer() {
		throw new AssertionError();
	}

	/**
	 * Returns the concatenation of the supplied {@link Sample}s as a new
	 * {@link Sample}. If {@code samples} is empty, this method returns a new, empty
	 * {@link Sample}.
	 *
	 * @param samples a list of {@link Sample}s
	 * @return concatenation {@link Sample}
	 * @throws NullPointerException if {@code samples} is {@code null}
	 */
	public static Sample concatenate(List<Sample> samples) {
		Objects.requireNonNull(samples);

		// If we have no samples, return an empty Sample
		if (samples.isEmpty()) {
			return buildSample(0, new double[0]);
		} else {
			int sampleCount = 0;
			// append voices to each other
			double[] first = samples.get(0).getInterleavedSamples();
			sampleCount += samples.get(0).getSampleCount();
			double[][] samplesArray = new double[samples.size() - 1][];
			for (int i = 0; i < samplesArray.length; i++) {
				samplesArray[i] = samples.get(i + 1).getInterleavedSamples();
				sampleCount += samples.get(i + 1).getSampleCount();
			}
			double[] appended = concatenate(first, samplesArray);
			return buildSample(sampleCount, appended);
		}
	}

	/**
	 * Returns {@code sample1} mixed with {@code sample2} as a new {@link Sample}.
	 * Additionally, {@code sample1}'s volume is adjusted by the multiplier
	 * {@code volume1}, and {@code sample2}'s by {@code volume2}.
	 *
	 * @param sample1 first {@link Sample}
	 * @param volume1 first multiplier
	 * @param sample2 second {@link Sample}
	 * @param volume2 second multiplier
	 * @return mixed {@link Sample}
	 * @throws NullPointerException if {@code sample1} or {@code sample2} is
	 *                              {@code null}
	 */
	public static Sample mix(Sample sample1, double volume1, Sample sample2, double volume2) {
		Objects.requireNonNull(sample1);
		Objects.requireNonNull(sample2);
		double[] s1Array = sample1.getInterleavedSamples();
		double[] s2Array = sample2.getInterleavedSamples();
		double[] mixed = mix(s1Array, volume1, s2Array, volume2);
		return buildSample(sample1.getSampleCount(), mixed);
	}

	/**
	 * Concatenates the supplied arrays of {@code double}s and returns the resulting
	 * array.
	 *
	 * @param first an array of {@code double}s
	 * @param rest  additional arrays of {@code double}s
	 * @return concatenated array
	 */
	private static double[] concatenate(double[] first, double[]... rest) {
		int totalLength = first.length;
		for (double[] array : rest) {
			totalLength += array.length;
		}
		double[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (double[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	/**
	 * Returns {@code sample1} mixed with {@code sample2} as a new raw array of
	 * {@code double}s. Additionally, {@code sample1}'s volume is adjusted by the
	 * multiplier {@code volume1}, and {@code sample2}'s by {@code volume2}.
	 *
	 * @param sample1 first sample
	 * @param volume1 first multiplier
	 * @param sample2 second sample
	 * @param volume2 second multiplier
	 * @return mixed sample
	 */
	private static double[] mix(double[] sample1, double volume1, double[] sample2, double volume2) {
		for (int i = 0; i < sample1.length; i++) {
			if (i >= sample2.length) {
				sample1[i] = 0;
				break;
			}
			sample1[i] = sample1[i] * volume1 + sample2[i] * volume2;
		}
		return sample1;
	}

	/**
	 * Returns a {@link Sample} created from the raw {@code sample} data.
	 *
	 * @param sampleCount number of samples
	 * @param sample      raw sample data
	 * @return {@link Sample} from raw samples
	 */
	private static Sample buildSample(long sampleCount, double[] sample) {
		// I'm reasonably sure we don't need to ask for sampleCount here: it's just
		// going to match sample.length, isn't it?
		byte[] buffer = asByteArray(sampleCount, sample);
		InputStream bais = new ByteArrayInputStream(buffer);
		AudioInputStream ais = new AudioInputStream(bais, Sample.SC_AUDIO_FORMAT, sampleCount);
		return new Sample(ais);
	}

	/**
	 * Returns a sample encoded as {@code double[]} as a {@code byte[]}.
	 *
	 * @param sampleCount number of samples
	 * @param sample      raw sample data
	 * @return sample encoded as {@code byte[]}
	 */
	private static byte[] asByteArray(long sampleCount, double[] sample) {
		int bufferLength = (int) sampleCount * (Sample.SC_AUDIO_FORMAT.getSampleSizeInBits() / 8);
		byte[] buffer = new byte[bufferLength];
		int in;
		for (int i = 0; i < sample.length; i++) {
			in = (int) (sample[i] * 32767);
			buffer[2 * i] = (byte) (in & 255);
			buffer[2 * i + 1] = (byte) (in >> 8);
		}
		return buffer;
	}
}
