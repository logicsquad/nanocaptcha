package net.logicsquad.nanocaptcha.content;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link ChineseContentProducer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.1
 */
public class ChineseContentProducerTest {
	// All we're doing here is checking that the static char array is initialized as
	// expected.
	@Test
	public void confirmCharactersArrayIsLoadedAsExpected() {
		assertEquals(ChineseContentProducer.CODE_POINT_END - ChineseContentProducer.CODE_POINT_START,
				ChineseContentProducer.CHARS.length);
		for (int i = 0; i < ChineseContentProducer.CHARS.length; i++) {
			assertEquals((char) (ChineseContentProducer.CODE_POINT_START + i), ChineseContentProducer.CHARS[i]);
		}
		return;
	}
}
