package net.logicsquad.nanocaptcha.content;

import org.junit.Test;

/**
 * Unit tests on {@link FiveLetterFirstNameContentProducer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class FiveLetterFirstNameContentProducerTest {
	private ContentProducer producer = new FiveLetterFirstNameContentProducer();

	// All we're doing here is making sure getContent() probably doesn't make an
	// index calculation error.
	@Test
	public void getContentShouldSucceed() {
		for (int i = 0; i < 200_000; i++) {
			producer.getContent();
		}
	}
}
