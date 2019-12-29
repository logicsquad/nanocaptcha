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
	 * Default length for produced content
	 */
	protected static final int DEFAULT_LENGTH = 5;

	/**
	 * Random number generator
	 */
	private static final Random RAND = new SecureRandom();

	/**
	 * Length of strings produced by this object
	 */
	private final int length;

	/**
	 * Source characters
	 */
	private final char[] srcChars;

	/**
	 * Constructor taking a length and an array of source characters.
	 * 
	 * @param length   text length
	 * @param srcChars source characters
	 */
	public AbstractContentProducer(int length, char[] srcChars) {
		this.length = length;
		this.srcChars = copyOf(srcChars, srcChars.length);
	}

	@Override
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(srcChars[RAND.nextInt(srcChars.length)]);
		}
		return sb.toString();
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
