package net.logicsquad.nanocaptcha.audio.noise;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;

import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * Unit tests on {@link RandomNoiseProducer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.2
 */
public class RandomNoiseProducerTest {
	@Test
	public void everyBuiltInNoiseIsAUsableSample() {
		for (String filename : RandomNoiseProducer.BUILT_IN_NOISES) {
			assertDoesNotThrow(() -> new Sample(RandomNoiseProducer.class.getResource(filename)), filename);
		}
		return;
	}

	@Test
	public void addNoiseUsesTheNoisesItsGiven() throws IOException {
		RandomNoiseProducer producer = new RandomNoiseProducer(Collections.singletonList(constant(1600, 0.5)));
		short[] mixed = pcm(producer.addNoise(Collections.singletonList(constant(1600, 0.0))));
		assertEquals(1600, mixed.length);
		for (short value : mixed) {
			// Noise is mixed in at 0.6 of its level
			assertEquals(0.3 * 32767, value, 2);
		}
		return;
	}

	@Test
	public void noiseStartsAtARandomPointWithEnoughLeft() throws IOException {
		// A rising ramp shows where each mix starts, and whether it wraps around
		byte[] data = new byte[16_000 * 2];
		for (int i = 0; i < 16_000; i++) {
			data[2 * i] = (byte) i;
			data[2 * i + 1] = (byte) (i >> 8);
		}
		Sample ramp = new Sample(new AudioInputStream(new ByteArrayInputStream(data), Sample.SC_AUDIO_FORMAT, 16_000));
		Set<Short> starts = new HashSet<>();
		for (int seed = 0; seed < 20; seed++) {
			short[] noise = pcm(RandomNoiseProducer.from(ramp, 1600, new Random(seed)));
			assertTrue(noise.length >= 1600, "only " + noise.length + " samples left");
			assertEquals(16_000 - noise.length, noise[0]);
			starts.add(noise[0]);
		}
		assertTrue(starts.size() > 15, "starts: " + starts);
		// Noise too short to spare any is used from the start
		assertSame(ramp, RandomNoiseProducer.from(ramp, 20_000, new Random(1)));
		return;
	}

	@Test
	public void listConstructorRejectsMissingNoises() {
		assertThrows(IllegalArgumentException.class, () -> new RandomNoiseProducer(Collections.emptyList()));
		assertThrows(NullPointerException.class, () -> new RandomNoiseProducer(Collections.singletonList(null)));
		return;
	}

	@Test
	@SuppressWarnings("deprecation")
	public void namesConstructorStillReadsResources() {
		assertDoesNotThrow(() -> new RandomNoiseProducer(RandomNoiseProducer.BUILT_IN_NOISES));
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> new RandomNoiseProducer(new String[] { "/no/such/noise.wav" }));
		assertTrue(e.getMessage().contains("'/no/such/noise.wav'"), e.getMessage());
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
		return new Sample(new AudioInputStream(new ByteArrayInputStream(data), Sample.SC_AUDIO_FORMAT, length));
	}

	/**
	 * Returns the 16-bit values in {@code sample}.
	 *
	 * @param sample a {@link Sample}
	 * @return values
	 * @throws IOException if the audio can't be read
	 */
	private static short[] pcm(Sample sample) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (AudioInputStream in = sample.getAudioInputStream()) {
			byte[] buffer = new byte[4096];
			int count;
			while ((count = in.read(buffer)) != -1) {
				out.write(buffer, 0, count);
			}
		}
		byte[] bytes = out.toByteArray();
		short[] values = new short[bytes.length / 2];
		for (int i = 0; i < values.length; i++) {
			values[i] = (short) ((bytes[2 * i + 1] << 8) | (bytes[2 * i] & 0xFF));
		}
		return values;
	}
}
