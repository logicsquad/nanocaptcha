package net.logicsquad.nanocaptcha.image.background;

import java.awt.image.BufferedImage;

/**
 * An object that can produce a background image.
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
public interface BackgroundProducer {
	/**
	 * Returns a background image that can be added to a CAPTCHA image.
	 *
	 * @param width  required width
	 * @param height required height
	 * @return background image of {@code width} x {@code height} pixels
	 */
	BufferedImage getBackground(int width, int height);
}
