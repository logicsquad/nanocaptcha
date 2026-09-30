package net.logicsquad.nanocaptcha.image.noise;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Adds Gaussian noise to the image. Gaussian noise is statistical noise having a
 * probability density function equal to that of the normal distribution, which is
 * also known as the Gaussian distribution.
 *
 * @author <a href="mailto:botyrbojey@gmail.com">bivashy</a>
 * @see <a href="https://en.wikipedia.org/wiki/Gaussian_noise">Gaussian noise on Wikipedia</a>
 * @since 2.0
 */
public class GaussianNoiseProducer implements NoiseProducer {
    /**
     * Default standard deviation.
     */
    private static final int DEFAULT_STANDARD_DEVIATION = 20;

    /**
     * Default mean.
     */
    private static final int DEFAULT_MEAN = 0;

    /**
     * Standard deviation for the Gaussian noise.
     */
    private final int standardDeviation;

    /**
     * Mean for the Gaussian noise.
     */
    private final int mean;

    /**
     * Constructor using default standard deviation and mean.
     */
    public GaussianNoiseProducer() {
        this(DEFAULT_STANDARD_DEVIATION, DEFAULT_MEAN);
        return;
    }

    /**
     * Constructor to create a Gaussian noise producer with specified standard deviation and mean.
     *
     * @param standardDeviation the standard deviation of the Gaussian noise
     * @param mean the mean of the Gaussian noise
     */
    public GaussianNoiseProducer(int standardDeviation, int mean) {
        this.standardDeviation = standardDeviation;
        this.mean = mean;
        return;
    }

    /**
     * Applies Gaussian noise to a BufferedImage.
     *
     * @param image the BufferedImage to which the noise is to be applied
     */
    @Override
    public void makeNoise(BufferedImage image) {
        makeNoise(image, ThreadLocalRandom.current());
    }

    /**
     * Adds noise to {@code image}, using {@code random}, so that tests can seed it.
     *
     * @param image  a {@link BufferedImage}
     * @param random a {@link Random}
     */
    void makeNoise(BufferedImage image, Random random) {
        WritableRaster raster = image.getRaster();
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                int[] pixelSamples = raster.getPixel(x, y, (int[]) null);

                for (int i = 0; i < pixelSamples.length; i++) {
                    pixelSamples[i] = clamp((int) (pixelSamples[i] + random.nextGaussian() * standardDeviation + mean), 0, 255);
                }

                raster.setPixel(x, y, pixelSamples);
            }
        }
    }

    /**
     * Clamp a value to an interval.
     *
     * @param a the lower clamp threshold
     * @param b the upper clamp threshold
     * @param x the input parameter
     * @return the clamped value
     */
    private static int clamp(int x, int a, int b) {
        return (x < a) ? a : (x > b) ? b : x;
    }
}
