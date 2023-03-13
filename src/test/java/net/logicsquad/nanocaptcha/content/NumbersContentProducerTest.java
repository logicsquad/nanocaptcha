package net.logicsquad.nanocaptcha.content;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link NumbersContentProducer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class NumbersContentProducerTest {
	@Test
	public void producerReturnsNumber() {
		ContentProducer producer = new NumbersContentProducer();
		for (int i = 0; i < 1000; i++) {
			assertTrue(isNumeric(producer.getContent()));
		}
		return;
	}

	@Test
	public void producerReturnsContentOfExpectedLength() {
		for (int i = 1; i <= 1000; i++) {
			ContentProducer producer = new NumbersContentProducer(i);
			String content = producer.getContent();
			assertEquals(i, content.length());
			assertTrue(isNumeric(content));
		}
		return;
	}

	@Test
	public void constructorThrowsOnZero() {
		assertThrows(IllegalArgumentException.class, () -> new NumbersContentProducer(0));
	}

	@Test
	public void constructorThrowsOnNegative() {
		assertThrows(IllegalArgumentException.class, () -> new NumbersContentProducer(-1));
	}

	/**
	 * Is {@code number} a number?
	 *
	 * @param number candidate number string
	 * @return {@code true} if {@code number} is a number, otherwise {@code false}
	 */
	private boolean isNumeric(String number) {
		return number.chars().allMatch(Character::isDigit);
	}
}
