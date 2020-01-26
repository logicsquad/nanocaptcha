package net.logicsquad.nanocaptcha.content;

/**
 * Generates random strings using a subset of the Chinese alphabet.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class ChineseContentProducer extends AbstractContentProducer implements ContentProducer {
	/**
	 * Code point at start of range (inclusive)
	 */
	static final int CODE_POINT_START = 0x4E00;

	/**
	 * Code point at end of range (exclusive)
	 */
	static final int CODE_POINT_END = 0x4F6F;

	/**
	 * Array of source characters
	 */
	static final char[] CHARS;

	static {
		CHARS = new char[CODE_POINT_END - CODE_POINT_START];
		for (int i = 0; i < (CODE_POINT_END - CODE_POINT_START); i++) {
			CHARS[i] = (char) (CODE_POINT_START + i);
		}
	}

	/**
	 * Constructor for object returning content of default length.
	 */
	public ChineseContentProducer() {
		this(DEFAULT_LENGTH);
	}

	/**
	 * Constructor taking a length specifier.
	 *
	 * @param length content length
	 */
	public ChineseContentProducer(int length) {
		super(length, CHARS);
	}
}
