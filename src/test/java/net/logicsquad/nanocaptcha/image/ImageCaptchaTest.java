package net.logicsquad.nanocaptcha.image;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.content.LatinContentProducer;
import net.logicsquad.nanocaptcha.image.renderer.DefaultWordRenderer;
import net.logicsquad.nanocaptcha.image.renderer.FastWordRenderer;
import net.logicsquad.nanocaptcha.image.renderer.WordRenderer;

/**
 * Unit tests on {@link ImageCaptcha} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.2
 */
public class ImageCaptchaTest {
	@Test
	public void defaultWordRendererDrawsText() {
		assertDrawsText(new DefaultWordRenderer.Builder().build());
		return;
	}

	@Test
	public void fastWordRendererDrawsText() {
		assertDrawsText(new FastWordRenderer.Builder().build());
		return;
	}

	/**
	 * Renders a CAPTCHA with {@code renderer} on a transparent image, and checks that something was drawn.
	 *
	 * @param renderer a {@link WordRenderer}
	 */
	private static void assertDrawsText(WordRenderer renderer) {
		ImageCaptcha captcha = new ImageCaptcha.Builder(200, 50).addContent(new LatinContentProducer(), renderer).build();
		BufferedImage image = captcha.getImage();
		assertEquals(200, image.getWidth());
		assertEquals(50, image.getHeight());
		int drawn = 0;
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if ((image.getRGB(x, y) >>> 24) != 0) {
					drawn++;
				}
			}
		}
		assertTrue(drawn > 100, "only " + drawn + " pixels drawn");
		return;
	}
}
