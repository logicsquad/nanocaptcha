package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.image.BufferedImage;

/**
 * Renders the content for the CAPTCHA onto the image.
 * <p>
 * An {@link net.logicsquad.nanocaptcha.image.ImageCaptcha.Factory ImageCaptcha.Factory} shares an implementation
 * between every CAPTCHA it makes, on whichever threads ask, so it has to be thread-safe. NanoCaptcha's own keep no
 * state that changes, and take any randomness from {@link java.util.concurrent.ThreadLocalRandom} on each call.
 * </p>
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public interface WordRenderer {
	/**
	 * Renders {@code word} to a {@link BufferedImage}.
	 *
	 * @param word  string to be rendered
	 * @param image image onto which the word will be rendered
	 */
	void render(String word, BufferedImage image);
}
