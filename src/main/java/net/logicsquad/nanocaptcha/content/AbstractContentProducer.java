package net.logicsquad.nanocaptcha.content;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Parent class for {@link ContentProducer}s that produce text of a given length
 * from a given array of characters. Subclasses just need to supply the
 * permitted characters.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public abstract class AbstractContentProducer implements ContentProducer {
	/**
	 * Random number generator
	 */
	private static final Random RAND = new SecureRandom();

	/**
	 * Length of strings produced by this object
	 */
	private final int _length;

	/**
	 * Source characters
	 */
	private final char[] _srcChars;

	/**
	 * Constructor taking a length and an array of source characters.
	 * 
	 * @param length   text length
	 * @param srcChars source characters
	 */
	public AbstractContentProducer(int length, char[] srcChars) {
		_length = length;
		_srcChars = copyOf(srcChars, srcChars.length);
	}

	@Override
	public String getContent() {
		String capText = "";
		for (int i = 0; i < _length; i++) {
			capText += _srcChars[RAND.nextInt(_srcChars.length)];
		}

		return capText;
	}

	/**
	 * Copies a supplied array.
	 * 
	 * @param original  source array
	 * @param newLength length of returned array
	 * @return copy of source array
	 */
	private static char[] copyOf(char[] original, int newLength) {
		char[] copy = new char[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}
}
