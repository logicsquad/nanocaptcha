package net.logicsquad.nanocaptcha.content;

/**
 * Object that can generate text content for a CAPTCHA. The content is the
 * CAPTCHA's answer, so implementations should choose it with a
 * {@link java.security.SecureRandom} rather than a {@link java.util.Random},
 * whose next values can be worked out from enough earlier ones.
 * <p>
 * A factory, {@link net.logicsquad.nanocaptcha.image.ImageCaptcha.Factory ImageCaptcha.Factory} or
 * {@link net.logicsquad.nanocaptcha.audio.AudioCaptcha.Factory AudioCaptcha.Factory}, shares an implementation between
 * every CAPTCHA it makes, on whichever threads ask, so it has to be thread-safe. NanoCaptcha's own keep no state that
 * changes, and share one {@link java.security.SecureRandom}, which is thread-safe.
 * </p>
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/49">#49</a>
 */
public interface ContentProducer {
	/**
	 * Returns a string of characters to be used as the answer for the CAPTCHA.
	 *
	 * @return CAPTCHA content
	 */
	String getContent();
}
