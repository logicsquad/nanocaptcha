package net.logicsquad.nanocaptcha.image.noise;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import net.logicsquad.nanocaptcha.image.background.FlatColorBackgroundProducer;

/**
 * Unit tests on {@link GaussianNoiseProducer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.3
 */
public class GaussianNoiseProducerTest {
	private static final int WHITE = 0xffffff;

	private static final int BLACK = 0x000000;

	@Test
	public void leavesOpaquePixelsOpaque() {
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		int[] opaque = new int[100 * 100];
		Arrays.fill(opaque, 0xff336699);
		image.setRGB(0, 0, 100, 100, opaque, 0, 100);
		new GaussianNoiseProducer().makeNoise(image, new Random(1));
		int[] pixels = pixels(image);
		for (int pixel : pixels) {
			assertEquals(0xff, pixel >>> 24);
		}
		assertTrue(Arrays.stream(pixels).distinct().count() > 1, "no noise");
		return;
	}

	@Test
	public void drawsWhiteAndBlackSpecklesOnATransparentLayer() {
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		new GaussianNoiseProducer().makeNoise(image, new Random(1));
		int[] counts = speckles(image);
		// With a mean of 0, about half each way, less the few pixels with noise too small to show
		assertTrue(counts[0] > 4500 && counts[1] > 4500, Arrays.toString(counts));
		return;
	}

	@Test
	public void positiveMeanMakesMostSpecklesWhite() {
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		new GaussianNoiseProducer(20, 40).makeNoise(image, new Random(1));
		int[] counts = speckles(image);
		assertTrue(counts[0] > 9 * counts[1], Arrays.toString(counts));
		return;
	}

	@Test
	public void grainShowsOverABackground() {
		ImageCaptcha.Factory factory = new ImageCaptcha.Factory.Builder(200, 50)
				.addBackground(new FlatColorBackgroundProducer(Color.WHITE)).addNoise(new GaussianNoiseProducer()).build();
		BufferedImage image = factory.create().getImage();
		long grey = Arrays.stream(pixels(image)).filter(p -> (p & WHITE) != WHITE).count();
		// The black speckles, about half of them
		assertTrue(grey > 200 * 50 / 3, grey + " pixels aren't white");
		return;
	}

	/**
	 * Counts the white and black speckles on a layer that was transparent, and checks that there are no others.
	 *
	 * @param image image
	 * @return counts of white and black speckles
	 */
	private static int[] speckles(BufferedImage image) {
		int[] counts = new int[2];
		for (int pixel : pixels(image)) {
			if ((pixel >>> 24) == 0) {
				continue;
			}
			int rgb = pixel & WHITE;
			assertTrue(rgb == WHITE || rgb == BLACK, String.format("Speckle %08x isn't white or black", pixel));
			counts[rgb == WHITE ? 0 : 1]++;
		}
		return counts;
	}

	/**
	 * Returns the pixels of {@code image} in ARGB.
	 *
	 * @param image image
	 * @return pixels
	 */
	private static int[] pixels(BufferedImage image) {
		return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
	}
}
