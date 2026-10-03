package net.logicsquad.nanocaptcha.image;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
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
import net.logicsquad.nanocaptcha.image.backgrounds.BackgroundProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.FlatColorBackgroundProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.GradiatedBackgroundProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.SquigglesBackgroundProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.TransparentBackgroundProducer;
import net.logicsquad.nanocaptcha.image.filter.FishEyeImageFilter;
import net.logicsquad.nanocaptcha.image.filter.ImageFilter;
import net.logicsquad.nanocaptcha.image.filter.RippleImageFilter;
import net.logicsquad.nanocaptcha.image.filter.ShearImageFilter;
import net.logicsquad.nanocaptcha.image.noise.CurvedLineNoiseProducer;
import net.logicsquad.nanocaptcha.image.noise.GaussianNoiseProducer;
import net.logicsquad.nanocaptcha.image.noise.NoiseProducer;
import net.logicsquad.nanocaptcha.image.noise.SaltAndPepperNoiseProducer;
import net.logicsquad.nanocaptcha.image.noise.StraightLineNoiseProducer;
import net.logicsquad.nanocaptcha.image.renderer.DefaultWordRenderer;
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
	public void defaultWordRendererRejectsCharactersItsFontCantDisplay() {
		assertRejectsChinese(new DefaultWordRenderer.Builder().build());
		return;
	}

	@Test
	public void isCorrectAcceptsOnlyTheContent() {
		ImageCaptcha captcha = ImageCaptcha.create();
		assertTrue(captcha.isCorrect(captcha.getContent()));
		assertFalse(captcha.isCorrect(captcha.getContent() + "a"));
		assertFalse(captcha.isCorrect(null));
		return;
	}

	@Test
	public void builderRefusesASecondAddContent() {
		ImageCaptcha.Builder builder = new ImageCaptcha.Builder(200, 50).addContent();
		assertThrows(IllegalStateException.class, () -> builder.addContent());
		return;
	}

	@Test
	public void builderRefusesEverythingAfterBuild() {
		ImageCaptcha.Builder builder = new ImageCaptcha.Builder(200, 50).addContent();
		ImageCaptcha captcha = builder.build();
		byte[] png = captcha.toPng();
		assertThrows(IllegalStateException.class, () -> builder.build());
		assertThrows(IllegalStateException.class, () -> builder.addBackground());
		assertThrows(IllegalStateException.class, () -> builder.addContent());
		assertThrows(IllegalStateException.class, () -> builder.addNoise());
		assertThrows(IllegalStateException.class, () -> builder.addFilter());
		assertThrows(IllegalStateException.class, () -> builder.addBorder());
		// None of them touched the CAPTCHA already built
		assertArrayEquals(png, captcha.toPng());
		return;
	}

	@Test
	public void addBorderDrawsEveryEdgePixelAndNothingElse() {
		// Wide, and tall
		for (int[] size : new int[][] { { 200, 50 }, { 60, 200 } }) {
			BufferedImage image = new ImageCaptcha.Builder(size[0], size[1]).addBorder().build().getImage();
			int width = image.getWidth();
			int height = image.getHeight();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					boolean edge = x == 0 || y == 0 || x == width - 1 || y == height - 1;
					assertEquals(edge ? Color.BLACK.getRGB() : 0, image.getRGB(x, y), width + " x " + height + ", pixel " + x + ", " + y);
				}
			}
		}
		return;
	}

	@Test
	public void randomNoiseProducersAndFiltersChangeTheImage() {
		List<NoiseProducer> noiseProducers = Arrays.asList(new CurvedLineNoiseProducer(), new StraightLineNoiseProducer(),
				new GaussianNoiseProducer(), new SaltAndPepperNoiseProducer());
		for (NoiseProducer noiseProducer : noiseProducers) {
			BufferedImage image = ImageCaptcha.create().getImage();
			int[] before = pixels(image);
			noiseProducer.makeNoise(image);
			assertFalse(Arrays.equals(before, pixels(image)), noiseProducer.getClass().getSimpleName() + " changed nothing");
		}
		List<ImageFilter> filters = Arrays.asList(new RippleImageFilter(), new ShearImageFilter());
		for (ImageFilter filter : filters) {
			BufferedImage image = ImageCaptcha.create().getImage();
			int[] before = pixels(image);
			filter.filter(image);
			assertFalse(Arrays.equals(before, pixels(image)), filter.getClass().getSimpleName() + " changed nothing");
		}
		return;
	}

	@Test
	public void everyBackgroundNoiseProducerFilterAndRendererWorkTogether() {
		List<BackgroundProducer> backgrounds = Arrays.asList(new TransparentBackgroundProducer(), new FlatColorBackgroundProducer(),
				new GradiatedBackgroundProducer(), new SquigglesBackgroundProducer());
		List<NoiseProducer> noiseProducers = Arrays.asList(new CurvedLineNoiseProducer(), new StraightLineNoiseProducer(),
				new GaussianNoiseProducer(), new SaltAndPepperNoiseProducer());
		List<ImageFilter> filters = Arrays.asList(new RippleImageFilter(), new ShearImageFilter(), new FishEyeImageFilter());
		List<WordRenderer> renderers = Arrays.asList(new DefaultWordRenderer.Builder().build());
		// Wide, and tall
		for (int[] size : new int[][] { { 200, 50 }, { 60, 200 } }) {
			for (BackgroundProducer background : backgrounds) {
				for (NoiseProducer noiseProducer : noiseProducers) {
					for (ImageFilter filter : filters) {
						for (WordRenderer renderer : renderers) {
							String what = size[0] + " x " + size[1] + ": " + name(background) + ", " + name(noiseProducer) + ", "
									+ name(filter) + ", " + name(renderer);
							BufferedImage image = new ImageCaptcha.Builder(size[0], size[1]).addBackground(background)
									.addContent(new LatinContentProducer(), renderer).addNoise(noiseProducer).addFilter(filter).build()
									.getImage();
							assertEquals(size[0], image.getWidth(), what);
							assertEquals(size[1], image.getHeight(), what);
							assertTrue(Arrays.stream(pixels(image)).distinct().count() > 1, what + ": one colour");
						}
					}
				}
			}
		}
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

	/**
	 * Returns a copy of {@code image}'s pixels.
	 *
	 * @param image a {@link BufferedImage}
	 * @return ARGB pixels, row by row
	 */
	private static int[] pixels(BufferedImage image) {
		return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
	}

	/**
	 * Returns the simple name of {@code object}'s class, to say which combination failed.
	 *
	 * @param object an object
	 * @return class name
	 */
	private static String name(Object object) {
		return object.getClass().getSimpleName();
	}
}
