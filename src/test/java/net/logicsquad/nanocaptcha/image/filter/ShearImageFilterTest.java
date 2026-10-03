package net.logicsquad.nanocaptcha.image.filter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link ShearImageFilter} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 3.0
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/84">#84</a>
 */
public class ShearImageFilterTest {
	private static final int RED = Color.RED.getRGB();

	/**
	 * On a transparent image, a red line comes out no longer than it went in. When the rows were drawn over the old ones,
	 * every row left its original pixel behind as well.
	 */
	@Test
	public void movesPixelsWithoutLeavingAGhost() {
		for (int seed = 0; seed < 20; seed++) {
			BufferedImage image = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < 50; y++) {
				image.setRGB(100, y, RED);
			}
			new ShearImageFilter().filter(image, new Random(seed));
			long red = Arrays.stream(pixels(image)).filter(pixel -> pixel == RED).count();
			assertTrue(red <= 50, "seed " + seed + ": " + red + " red pixels from 50");
		}
		return;
	}

	/**
	 * An opaque image comes out the same whether or not its type has alpha. On Alpine, shearing an opaque image used to
	 * kill the JVM.
	 */
	@Test
	public void shearsOpaqueImagesAsItShearsTranslucentOnes() {
		for (int seed = 0; seed < 20; seed++) {
			BufferedImage opaque = new BufferedImage(200, 50, BufferedImage.TYPE_INT_RGB);
			BufferedImage translucent = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
			for (int x = 0; x < 200; x++) {
				for (int y = 0; y < 50; y++) {
					// Stripes, opaque everywhere
					int rgb = (x / 7 + y / 5) % 2 == 0 ? 0xff000000 : 0xffffffff;
					opaque.setRGB(x, y, rgb);
					translucent.setRGB(x, y, rgb);
				}
			}
			new ShearImageFilter().filter(opaque, new Random(seed));
			new ShearImageFilter().filter(translucent, new Random(seed));
			assertArrayEquals(pixels(translucent), pixels(opaque), "seed " + seed);
		}
		return;
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
