package net.logicsquad.nanocaptcha.image;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.content.LatinContentProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.GradiatedBackgroundProducer;
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

	@Test
	public void defaultWordRendererRejectsCharactersItsFontCantDisplay() {
		assertRejectsChinese(new DefaultWordRenderer.Builder().build());
		return;
	}

	@Test
	public void fastWordRendererRejectsCharactersItsFontsCantDisplay() {
		assertRejectsChinese(new FastWordRenderer.Builder().build());
		return;
	}

	@Test
	public void toPngHoldsTheImage() throws IOException {
		// Transparent, with a transparent background, and opaque
		List<ImageCaptcha> captchas = Arrays.asList(ImageCaptcha.create(),
				new ImageCaptcha.Builder(200, 50).addBackground().addContent().addNoise().addFilter().addBorder().build(),
				new ImageCaptcha.Builder(200, 50).addBackground(new GradiatedBackgroundProducer()).addContent().build());
		for (ImageCaptcha captcha : captchas) {
			assertHoldsImage(captcha.toPng(), captcha.getImage());
		}
		return;
	}

	@Test
	public void writePngWritesThePngAndLeavesTheStreamOpen() throws IOException {
		ImageCaptcha captcha = ImageCaptcha.create();
		AtomicBoolean closed = new AtomicBoolean();
		ByteArrayOutputStream out = new ByteArrayOutputStream() {
			@Override
			public void close() {
				closed.set(true);
			}
		};
		captcha.writePng(out);
		assertFalse(closed.get(), "writePng() closed the stream");
		assertHoldsImage(out.toByteArray(), captcha.getImage());
		return;
	}

	@Test
	public void toDataUriHoldsAPng() throws IOException {
		ImageCaptcha captcha = ImageCaptcha.create();
		String prefix = "data:image/png;base64,";
		String uri = captcha.toDataUri();
		assertTrue(uri.startsWith(prefix), uri);
		assertHoldsImage(Base64.getDecoder().decode(uri.substring(prefix.length())), captcha.getImage());
		return;
	}

	/**
	 * Checks that {@code png} is a PNG file holding exactly {@code image}, transparency included.
	 *
	 * @param png   PNG file contents
	 * @param image expected image
	 * @throws IOException if {@code png} can't be read
	 */
	private static void assertHoldsImage(byte[] png, BufferedImage image) throws IOException {
		byte[] signature = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n' };
		assertArrayEquals(signature, Arrays.copyOf(png, signature.length));
		BufferedImage read = ImageIO.read(new ByteArrayInputStream(png));
		assertEquals(image.getWidth(), read.getWidth());
		assertEquals(image.getHeight(), read.getHeight());
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				assertEquals(image.getRGB(x, y), read.getRGB(x, y), "pixel " + x + ", " + y);
			}
		}
		return;
	}

	/**
	 * Checks that {@code renderer} throws on Chinese content, which the built-in fonts can't display, instead of
	 * drawing empty boxes.
	 *
	 * @param renderer a {@link WordRenderer} using the built-in fonts
	 */
	private static void assertRejectsChinese(WordRenderer renderer) {
		ImageCaptcha.Builder builder = new ImageCaptcha.Builder(200, 50);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> builder.addContent(() -> "\u4E2D\u6587", renderer));
		assertTrue(e.getMessage().contains("(U+4E2D)"), e.getMessage());
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
