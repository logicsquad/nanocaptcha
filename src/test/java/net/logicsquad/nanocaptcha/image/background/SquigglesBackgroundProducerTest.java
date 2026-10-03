package net.logicsquad.nanocaptcha.image.background;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link SquigglesBackgroundProducer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 3.0
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/94">#94</a>
 */
public class SquigglesBackgroundProducerTest {
	/**
	 * Every background of a given size used to be the same, so an attacker who had seen one could subtract it.
	 */
	@Test
	public void drawsADifferentBackgroundEachTime() {
		SquigglesBackgroundProducer producer = new SquigglesBackgroundProducer();
		Set<String> backgrounds = new HashSet<>();
		for (int i = 0; i < 20; i++) {
			backgrounds.add(Arrays.toString(pixels(producer.getBackground(200, 50))));
		}
		assertEquals(20, backgrounds.size());
		return;
	}

	@Test
	public void sameSeedDrawsTheSameBackground() {
		SquigglesBackgroundProducer producer = new SquigglesBackgroundProducer();
		int[] first = pixels(producer.getBackground(200, 50, new Random(7)));
		assertArrayEquals(first, pixels(producer.getBackground(200, 50, new Random(7))));
		assertFalse(Arrays.equals(first, pixels(producer.getBackground(200, 50, new Random(8)))));
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
