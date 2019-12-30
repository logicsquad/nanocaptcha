package net.logicsquad.nanocaptcha.image.noise;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @since 1.0
 */
public interface NoiseProducer {
    void makeNoise(BufferedImage image);
}
