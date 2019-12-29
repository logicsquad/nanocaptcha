package net.logicsquad.nanocaptcha.content;

/**
 * Generates random strings using a subset of the Latin alphabet.
 * 
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class LatinContentProducer extends AbstractContentProducer implements ContentProducer {
    private static final int DEFAULT_LENGTH = 5;
    private static final char[] DEFAULT_CHARS = new char[] { 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'k', 'm', 'n', 'p', 'r', 'w', 'x', 'y',
            '2', '3', '4', '5', '6', '7', '8', };

    public LatinContentProducer() {
    	super(DEFAULT_LENGTH, DEFAULT_CHARS);
    }
}
