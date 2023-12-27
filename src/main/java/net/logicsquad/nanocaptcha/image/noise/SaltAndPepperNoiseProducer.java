package net.logicsquad.nanocaptcha.image.noise;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Applies salt and pepper noise to an image. This noise type randomly changes some of the pixels to black or white, creating a 'salt and
 * pepper' effect.
 *
 * @author <a href="mailto:botyrbojey@gmail.com">bivashy</a>
 * @see <a href="https://en.wikipedia.org/wiki/Salt-and-pepper_noise">Salt and pepper on Wikipedia</a>
 * @since 2.0
 */
public class SaltAndPepperNoiseProducer implements NoiseProducer {
    /**
     * Random number generator.
     */
    private static final Random RAND = new Random();

    /**
     * Default noise density.
     */
    private static final double DEFAULT_NOISE_DENSITY = 0.15;

    /**
     * RGB value of the "pepper".
     */
    private static final int PEPPER = Color.BLACK.getRGB();

    /**
     * RGB value of the "salt".
     */
    private static final int SALT = Color.WHITE.getRGB();

    /**
     * Noise density.
     */
    private final double noiseDensity;

    /**
     * Constructor using default standard deviation and mean.
     */
    public SaltAndPepperNoiseProducer() {
        this(DEFAULT_NOISE_DENSITY);
        return;
    }

    /**
     * Constructor for salt and pepper noise producer.
     *
     * @param noiseDensity The density of the noise to be applied (0 to 1 range).
     * @throws IllegalArgumentException when the noise density is not in the range of 0 to 1.
     */
    public SaltAndPepperNoiseProducer(double noiseDensity) {
        if (noiseDensity < 0 || noiseDensity > 1) {
            throw new IllegalArgumentException("Noise density must be between 0 and 1.");
        }
        this.noiseDensity = noiseDensity;
        return;
    }

    /**
     * Applies salt and pepper noise to the given image.
     *
     * @param image The BufferedImage to apply noise to
     */
    @Override
    public void makeNoise(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (RAND.nextDouble() < noiseDensity) {
                    int color = RAND.nextBoolean() ? PEPPER : SALT;
                    image.setRGB(x, y, color);
                }
            }
        }
    }
}
