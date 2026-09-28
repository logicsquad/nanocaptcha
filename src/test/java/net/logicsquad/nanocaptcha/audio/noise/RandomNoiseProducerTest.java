package net.logicsquad.nanocaptcha.audio.noise;

import static org.junit.jupiter.api.Assertions.*;

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
			assertDoesNotThrow(() -> new Sample(filename), filename);
		}
		return;
	}
}
