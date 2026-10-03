package net.logicsquad.nanocaptcha.image.filter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.image.GoldenImages;

/**
 * Unit tests on {@link RippleImageFilter} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 3.0
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/96">#96</a>
 */
public class RippleImageFilterTest {
	/**
	 * The ripple used to be the same every time, so it could be undone by knowing it in advance.
	 */
	@Test
	public void ripplesDifferentlyEachTime() throws IOException {
		Set<String> ripples = new HashSet<>();
		for (int i = 0; i < 20; i++) {
			BufferedImage image = GoldenImages.input();
			new RippleImageFilter().filter(image);
			ripples.add(Arrays.toString(pixels(image)));
		}
		assertEquals(20, ripples.size());
		return;
	}

	@Test
	public void sameSeedRipplesTheSameWay() throws IOException {
		int[] first = rippled(new Random(7));
		assertArrayEquals(first, rippled(new Random(7)));
		assertFalse(Arrays.equals(first, rippled(new Random(8))));
		return;
	}

	@Test
	public void movesPixelsOnlyALittle() {
		int red = Color.RED.getRGB();
		for (int seed = 0; seed < 20; seed++) {
			BufferedImage image = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < 50; y++) {
				image.setRGB(100, y, red);
			}
			new RippleImageFilter().filter(image, new Random(seed));
			for (int y = 0; y < 50; y++) {
				boolean inked = false;
				for (int x = 0; x < 200; x++) {
					if (image.getRGB(x, y) >>> 24 != 0) {
						// No row moves more than three pixels
						assertTrue(x >= 97 && x <= 103, "seed " + seed + ": pixel " + x + ", " + y);
						inked = true;
					}
				}
				// Clear of the top and bottom, where it can be drawn from outside the image, every row keeps some line
				assertTrue(inked || y < 2 || y > 47, "seed " + seed + ": row " + y + " lost the line");
			}
		}
		return;
	}

	/**
	 * Returns the pixels of the golden images' input, rippled using {@code random}.
	 *
	 * @param random a {@link Random}
	 * @return ARGB pixels, row by row
	 * @throws IOException if the input can't be read
	 */
	private static int[] rippled(Random random) throws IOException {
		BufferedImage image = GoldenImages.input();
		new RippleImageFilter().filter(image, random);
		return pixels(image);
	}

	/**
	 * Returns the pixels of {@code image}.
	 *
	 * @param image an image
	 * @return ARGB pixels, row by row
	 */
	private static int[] pixels(BufferedImage image) {
		return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
	}
}
