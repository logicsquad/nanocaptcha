package net.logicsquad.nanocaptcha.image.noise;

import java.awt.image.BufferedImage;

/**
 * An object that can add noise to an image.
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
