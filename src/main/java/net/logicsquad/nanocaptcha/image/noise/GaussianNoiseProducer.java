package net.logicsquad.nanocaptcha.image.noise;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Adds Gaussian noise to the image. Gaussian noise is statistical noise having a
 * probability density function equal to that of the normal distribution, which is
 * also known as the Gaussian distribution.
 * <p>
 * The noise is drawn over each pixel as a speckle: white for positive noise and black for negative, as opaque as the
 * noise is large. An {@link net.logicsquad.nanocaptcha.image.ImageCaptcha.Factory ImageCaptcha.Factory} draws on a
 * transparent layer, and puts the background behind it last, so the speckles make a grain that shows over any
 * background, and opaque pixels stay opaque.
 * </p>
 *
 * @author <a href="mailto:botyrbojey@gmail.com">bivashy</a>
 * @see <a href="https://en.wikipedia.org/wiki/Gaussian_noise">Gaussian noise on Wikipedia</a>
 * @since 2.0
 */
public final class GaussianNoiseProducer implements NoiseProducer {
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
     * @param mean the mean of the Gaussian noise: above 0 lightens the image, and below 0 darkens it
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
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = speckle(pixels[i], random.nextGaussian() * standardDeviation + mean);
        }
        image.setRGB(0, 0, width, height, pixels, 0, width);
    }

    /**
     * Draws a speckle over a pixel: white for positive noise and black for negative, with an alpha of the noise's
     * magnitude, up to 255. Adding the noise to each channel instead would leave a transparent pixel's colour
     * invisible, and make opaque pixels partly transparent.
     *
     * @param argb  a pixel, in non-premultiplied ARGB
     * @param noise noise for the pixel
     * @return the pixel with the speckle drawn over it
     */
    private static int speckle(int argb, double noise) {
        double speckleAlpha = Math.min(Math.abs(noise), 255) / 255;
        if (speckleAlpha == 0) {
            return argb;
        }
        int speckleValue = noise > 0 ? 255 : 0;
        double alpha = (argb >>> 24) / 255.0;
        // Source over, as Graphics2D would draw it
        double resultAlpha = speckleAlpha + alpha * (1 - speckleAlpha);
        int result = (int) Math.round(resultAlpha * 255) << 24;
        for (int shift = 16; shift >= 0; shift -= 8) {
            int value = (argb >>> shift) & 0xff;
            result |= (int) Math.round((speckleValue * speckleAlpha + value * alpha * (1 - speckleAlpha)) / resultAlpha) << shift;
        }
        return result;
    }
}
