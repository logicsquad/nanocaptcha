package net.logicsquad.nanocaptcha;

/**
 * Generic object builder.
 * 
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @param <T> concrete class
 * @since 1.4
 */
public interface Builder<T> {
	T build();
}
