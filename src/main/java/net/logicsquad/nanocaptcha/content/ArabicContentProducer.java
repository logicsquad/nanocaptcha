package net.logicsquad.nanocaptcha.content;

/**
 * Generates random strings using a subset of the Arabic alphabet.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class ArabicContentProducer extends AbstractContentProducer implements ContentProducer {
	/**
	 * Default length for produced content
	 */
	private static final int DEFAULT_LENGTH = 5;

	/**
	 * Array containing some Arabic characters
	 */
	private static char[] ARABIC_CHARS = { '\u0627', '\u0628', '\u062a', '\u062b', '\u062c', '\u062d', '\u062e',
			'\u062f', '\u0630', '\u0631', '\u0632', '\u0633', '\u0634', '\u0635', '\u0636', '\u0637', '\u0638',
			'\u0639', '\u063a', '\u0641', '\u0642', '\u0643', '\u0644', '\u0645', '\u0646', '\u0647', '\u0648',
			'\u064a' };

	/**
	 * Constructor for object returning content of default length.
	 */
	public ArabicContentProducer() {
		this(DEFAULT_LENGTH);
	}

	/**
	 * Constructor taking a length specifier.
	 * 
	 * @param length content length
	 */
	public ArabicContentProducer(int length) {
		super(length, ARABIC_CHARS);
	}
}
