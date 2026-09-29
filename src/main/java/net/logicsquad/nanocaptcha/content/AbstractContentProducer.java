package net.logicsquad.nanocaptcha.content;

import java.security.SecureRandom;
import java.util.Arrays;

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
	 * Random number generator. The content is the CAPTCHA's answer, so this is a
	 * {@link SecureRandom}: a {@link java.util.Random}'s next values can be worked
	 * out from enough earlier ones.
	 *
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/49">#49</a>
	 */
	private static final SecureRandom RAND = new SecureRandom();

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
