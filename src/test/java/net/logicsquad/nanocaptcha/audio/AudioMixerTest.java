package net.logicsquad.nanocaptcha.audio;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;

import javax.sound.sampled.AudioInputStream;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link AudioMixer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.2
 */
public class AudioMixerTest {
	@Test
	public void mixClipsInsteadOfWrapping() {
		Sample loud = constant(1000, 0.8);
		for (double value : AudioMixer.mix(loud, 1.0, loud, 1.0).getInterleavedSamples()) {
			assertTrue(value > 0.99, "sample wrapped to " + value);
		}
		return;
	}

	@Test
	public void mixRepeatsAShorterSecondSample() {
		double[] mixed = AudioMixer.mix(constant(16000, 0.0), 1.0, constant(4000, 0.5), 1.0).getInterleavedSamples();
		assertEquals(16000, mixed.length);
		// A constant crossfaded into itself stays constant, so every sample should hold it.
		for (int i = 0; i < mixed.length; i++) {
			assertEquals(0.5, mixed[i], 1e-3, "sample " + i);
		}
		return;
	}

	@Test
	public void mixScalesAllOfTheFirstSample() {
		double[] mixed = AudioMixer.mix(constant(16000, 0.5), 0.5, constant(4000, 0.0), 1.0).getInterleavedSamples();
		for (int i = 0; i < mixed.length; i++) {
			assertEquals(0.25, mixed[i], 1e-3, "sample " + i);
		}
		return;
	}

	@Test
	public void mixIsAsLongAsTheFirstSample() {
		assertEquals(4000, AudioMixer.mix(constant(4000, 0.1), 1.0, constant(16000, 0.1), 1.0).getSampleCount());
		assertEquals(16000, AudioMixer.mix(constant(16000, 0.1), 1.0, constant(4000, 0.1), 1.0).getSampleCount());
		return;
	}

	/**
	 * Returns a {@link Sample} of {@code length} samples, all with {@code value}.
	 *
	 * @param length number of samples
	 * @param value  value of every sample, in [-1, 1]
	 * @return {@link Sample}
	 */
	private static Sample constant(int length, double value) {
		byte[] data = new byte[length * 2];
		short pcm = (short) Math.round(value * 32767);
		for (int i = 0; i < length; i++) {
			data[2 * i] = (byte) pcm;
			data[2 * i + 1] = (byte) (pcm >> 8);
		}
		return new Sample(new AudioInputStream(new ByteArrayInputStream(data), Sample.FORMAT, length));
	}
}
