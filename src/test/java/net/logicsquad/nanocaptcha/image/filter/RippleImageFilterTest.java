package net.logicsquad.nanocaptcha.image.filter;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link RippleImageFilter} class.
 * 
 * @author paulh
 * @since 1.2
 */
public class RippleImageFilterTest {
	private ImageFilter rippleImageFilter;

	@BeforeEach
	public void setup() {
		rippleImageFilter = new RippleImageFilter();
		return;
	}

	/**
	 * Compares a known expected result to an actual transformation. This method is
	 * to allow us to make changes to the implementation of
	 * {@link RippleImageFilter} and confirm its functionality is unchanged.
	 * 
	 * @throws IOException if there is a problem reading images
	 */
	@Test
	public void rippleImageFilterProducesExpectedTransformation() throws IOException {
		BufferedImage input = ImageIO.read(getClass().getClassLoader().getResourceAsStream("input.png"));
		BufferedImage expected = ImageIO.read(getClass().getClassLoader().getResourceAsStream("output.png"));
		rippleImageFilter.filter(input);
		assertTrue(bufferedImagesEqual(expected, input));
		return;
	}

	/**
	 * 
	 * @param expected expected {@link BufferedImage}
	 * @param actual   actual {@link BufferedImage}
	 * @return {@code true} if {@code expected} and {@code actual} are the same,
	 *         otherwise {@code false}
	 * @see <a href=
	 *      "https://stackoverflow.com/questions/15305037/java-compare-one-bufferedimage-to-another">Stack
	 *      Overflow</a>
	 */
	private boolean bufferedImagesEqual(BufferedImage expected, BufferedImage actual) {
		if (expected.getWidth() == actual.getWidth() && expected.getHeight() == actual.getHeight()) {
			for (int x = 0; x < expected.getWidth(); x++) {
				for (int y = 0; y < expected.getHeight(); y++) {
					if (expected.getRGB(x, y) != actual.getRGB(x, y))
						return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}
}
