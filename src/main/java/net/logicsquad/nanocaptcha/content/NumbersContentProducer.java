package net.logicsquad.nanocaptcha.content;

/**
 * {@link ContentProducer} implementation that will return a series of numbers.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class NumbersContentProducer extends DefaultContentProducer implements ContentProducer {
	/**
	 * Default length
	 */
	private static final int DEFAULT_LENGTH = 5;

	/**
	 * Character set
	 */
	private static final char[] NUMBERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	/**
	 * Constructor producing an object that will return content strings of
	 * {@link #DEFAULT_LENGTH}.
	 */
	public NumbersContentProducer() {
		this(DEFAULT_LENGTH);
	}

	/**
	 * Constructor taking a {@code length} specifier.
	 * 
	 * @param length length of returned content strings
	 * @throws IllegalArgumentException if {@code length} is not positive
	 */
	public NumbersContentProducer(int length) {
		super(length, NUMBERS);
	}
}
