package net.logicsquad.nanocaptcha.image.renderer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link DefaultWordRenderer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.3
 */
public class DefaultWordRendererTest {
	/**
	 * Characters from {@link net.logicsquad.nanocaptcha.content.LatinContentProducer}
	 */
	private static final String LATIN = "abcdefghkmnprwxy2345678";

	/**
	 * Each character in each built-in font was drawn as the same bitmap wherever it went, so the default CAPTCHA could
	 * be read by matching 46 templates.
	 */
	@Test
	public void drawsEachCharacterDifferentlyEachTime() {
		Set<IntBuffer> bitmaps = new HashSet<>();
		int renders = 0;
		for (Font font : AbstractWordRenderer.DEFAULT_FONTS) {
			DefaultWordRenderer renderer = (DefaultWordRenderer) new DefaultWordRenderer.Builder().font(font).build();
			for (char c : LATIN.toCharArray()) {
				for (int i = 0; i < 10; i++) {
					BufferedImage image = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
					renderer.render(String.valueOf(c), image, new Random(renders++));
					bitmaps.add(bitmap(image));
				}
			}
		}
		assertTrue(bitmaps.size() > renders * 9 / 10, bitmaps.size() + " different bitmaps in " + renders + " renders");
		return;
	}

	@Test
	public void sameSeedDrawsTheSameImage() {
		// One font, since the Builder's font supplier chooses its own at random
		DefaultWordRenderer renderer = (DefaultWordRenderer) new DefaultWordRenderer.Builder()
				.font(AbstractWordRenderer.DEFAULT_FONTS.get(1)).build();
		BufferedImage first = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
		renderer.render("ab3xk", first, new Random(7));
		BufferedImage second = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
		renderer.render("ab3xk", second, new Random(7));
		assertArrayEquals(pixels(first), pixels(second));
		return;
	}

	/**
	 * Returns the ink in {@code image}, cropped to its bounds.
	 *
	 * @param image an image
	 * @return width, height and pixels of the ink
	 */
	private static IntBuffer bitmap(BufferedImage image) {
		Rectangle ink = AbstractWordRendererTest.ink(image);
		int[] bitmap = new int[2 + ink.width * ink.height];
		bitmap[0] = ink.width;
		bitmap[1] = ink.height;
		image.getRGB(ink.x, ink.y, ink.width, ink.height, bitmap, 2, ink.width);
		return IntBuffer.wrap(bitmap);
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
