package net.logicsquad.nanocaptcha.content;

/**
 * Generates random strings using a subset of the Latin alphabet.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class LatinContentProducer extends AbstractContentProducer {
    /**
     * Source characters
     */
    private static final char[] DEFAULT_CHARS = new char[] { 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'k', 'm', 'n', 'p', 'r', 'w', 'x', 'y',
            '2', '3', '4', '5', '6', '7', '8', };

	/**
	 * Constructor for object returning content of default length.
	 */
	public LatinContentProducer() {
		this(DEFAULT_LENGTH);
	}

	/**
	 * Constructor taking a length specifier.
	 *
	 * @param length content length
	 */
	public LatinContentProducer(int length) {
		super(length, DEFAULT_CHARS);
	}
}
