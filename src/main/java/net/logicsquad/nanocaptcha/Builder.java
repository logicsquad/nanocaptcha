package net.logicsquad.nanocaptcha;

/**
 * Generic object builder.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @param <T> concrete class
 * @since 1.4
 */
public interface Builder<T> {
	/**
	 * Builds and returns an object of type {@code T} based on the state of this object.
	 *
	 * @return object of type {@code T}
	 */
	T build();
}
