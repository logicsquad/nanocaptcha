package net.logicsquad.nanocaptcha.content;

import java.util.Arrays;
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
	private static final Random RAND = new Random();

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
	 * @throws IllegalArgumentException if {@code length} is not positive
	 */
	public AbstractContentProducer(int length, char[] srcChars) {
		if (length <= 0) {
			throw new IllegalArgumentException("Content length must be positive.");
		}
		this.length = length;
		this.srcChars = Arrays.copyOf(srcChars, srcChars.length);
		return;
	}

	@Override
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(srcChars[RAND.nextInt(srcChars.length)]);
		}
		return sb.toString();
	}
}
