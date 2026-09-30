package net.logicsquad.nanocaptcha.image.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link StretchImageFilter} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.3
 */
public class StretchImageFilterTest {
	/**
	 * A line along the top edge is stretched to three rows, and no further. When
	 * the image was stretched onto itself, each row was drawn from rows the same
	 * draw had already written, which smeared the line down the whole image.
	 */
	@Test
	public void stretchesATopLineToThreeRows() {
		BufferedImage image = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
		int red = Color.RED.getRGB();
		for (int x = 0; x < image.getWidth(); x++) {
			image.setRGB(x, 0, red);
		}
		new StretchImageFilter().filter(image);
		for (int y = 0; y < image.getHeight(); y++) {
			int expected = y < 3 ? red : 0;
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(expected, image.getRGB(x, y), "(" + x + ", " + y + ")");
			}
		}
		return;
	}
}
