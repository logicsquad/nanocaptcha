package net.logicsquad.nanocaptcha.image.noise;

import java.awt.image.BufferedImage;

/**
 * An object that can add noise to an image.
 * <p>
 * An {@link net.logicsquad.nanocaptcha.image.ImageCaptcha.Factory ImageCaptcha.Factory} shares an implementation
 * between every CAPTCHA it makes, on whichever threads ask, so it has to be thread-safe. NanoCaptcha's own keep no
 * state that changes, and take any randomness from {@link java.util.concurrent.ThreadLocalRandom} on each call.
 * </p>
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @since 1.0
 */
public interface NoiseProducer {
	/**
	 * Adds noise to {@code image}.
	 *
	 * @param image a {@link BufferedImage}
	 */
	void makeNoise(BufferedImage image);
}
