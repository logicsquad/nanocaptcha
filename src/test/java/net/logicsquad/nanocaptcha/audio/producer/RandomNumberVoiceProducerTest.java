package net.logicsquad.nanocaptcha.audio.producer;

import org.junit.Test;

/**
 * Unit tests on {@link RandomNumberVoiceProducer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class RandomNumberVoiceProducerTest {
	@Test(expected = NullPointerException.class)
	public void constructorThrowsOnNull() {
		new RandomNumberVoiceProducer(null);
		return;
	}
}
