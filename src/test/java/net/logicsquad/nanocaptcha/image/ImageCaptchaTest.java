package net.logicsquad.nanocaptcha.image;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.content.LatinContentProducer;
import net.logicsquad.nanocaptcha.image.background.BackgroundProducer;
import net.logicsquad.nanocaptcha.image.background.FlatColorBackgroundProducer;
import net.logicsquad.nanocaptcha.image.background.GradiatedBackgroundProducer;
import net.logicsquad.nanocaptcha.image.background.SquigglesBackgroundProducer;
import net.logicsquad.nanocaptcha.image.background.TransparentBackgroundProducer;
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
	public void toStringGivesTheLengthOfTheAnswerButNotTheAnswer() {
		ImageCaptcha captcha = new ImageCaptcha.Factory.Builder(200, 50).addContent(() -> "ab3xk").build().create();
		assertEquals("[ImageCaptcha: created=" + captcha.getCreated() + " content=5 characters]", captcha.toString());
		captcha = new ImageCaptcha.Factory.Builder(200, 50).addContent(() -> "a").build().create();
		assertEquals("[ImageCaptcha: created=" + captcha.getCreated() + " content=1 character]", captcha.toString());
		return;
	}

	@Test
	public void isCorrectIgnoresCaseAndSurroundingWhitespaceUnlessAskedNotTo() {
		ImageCaptcha captcha = new ImageCaptcha.Factory.Builder(200, 50).addContent(() -> "ab3xk").build().create();
		// As a mobile keyboard or autofill might send it
		for (String answer : new String[] { "ab3xk", "Ab3xk", "AB3XK", " ab3xk", "ab3xk \t\n" }) {
			assertTrue(captcha.isCorrect(answer), "'" + answer + "'");
		}
		for (String answer : new String[] { "ab3x", "ab3xka", "ab 3xk", "", null }) {
			assertFalse(captcha.isCorrect(answer), "'" + answer + "'");
		}
		assertTrue(captcha.isCorrect("ab3xk", false));
		for (String answer : new String[] { "Ab3xk", " ab3xk", null }) {
			assertFalse(captcha.isCorrect(answer, false), "'" + answer + "'");
		}
		return;
	}

	@Test
	public void builderRefusesASecondAddContent() {
		ImageCaptcha.Factory.Builder builder = new ImageCaptcha.Factory.Builder(200, 50).addContent();
		assertThrows(IllegalStateException.class, () -> builder.addContent());
		return;
	}

	@Test
	public void builderRefusesAnImageWithNoPixels() {
		assertThrows(IllegalArgumentException.class, () -> new ImageCaptcha.Factory.Builder(0, 50));
		assertThrows(IllegalArgumentException.class, () -> new ImageCaptcha.Factory.Builder(200, -1));
		return;
	}

	@Test
	public void factoryMakesANewCaptchaEachTime() {
		AtomicInteger count = new AtomicInteger();
		ImageCaptcha.Factory factory = new ImageCaptcha.Factory.Builder(200, 50)
				.addContent(() -> "ab3x" + count.incrementAndGet()).addNoise().build();
		ImageCaptcha first = factory.create();
		byte[] png = first.toPng();
		ImageCaptcha second = factory.create();
		assertEquals("ab3x1", first.getContent());
		assertEquals("ab3x2", second.getContent());
		assertNotSame(first.getImage(), second.getImage());
		// Making the second CAPTCHA didn't touch the first
		assertArrayEquals(png, first.toPng());
		return;
	}

	@Test
	public void factoryIsntChangedByItsBuilder() {
		ImageCaptcha.Factory.Builder builder = new ImageCaptcha.Factory.Builder(200, 50);
		ImageCaptcha.Factory factory = builder.build();
		builder.addBackground(new TransparentBackgroundProducer()).addNoise(image -> image.setRGB(100, 25, Color.RED.getRGB()))
				.addBorder();
		// The default background, with no noise or border
		BufferedImage image = factory.create().getImage();
		assertEquals(Color.LIGHT_GRAY.getRGB(), image.getRGB(0, 0));
		assertEquals(Color.LIGHT_GRAY.getRGB(), image.getRGB(100, 25));
		// While the Builder now has all three
		image = builder.build().create().getImage();
		assertEquals(Color.BLACK.getRGB(), image.getRGB(0, 0));
		assertEquals(Color.RED.getRGB(), image.getRGB(100, 25));
		assertEquals(0, image.getRGB(1, 1) >>> 24);
		return;
	}

	@Test
	public void factoryDrawsInTheOrderTheBuilderWasGiven() {
		List<String> drawn = new ArrayList<>();
		ImageCaptcha.Factory factory = new ImageCaptcha.Factory.Builder(200, 50).addNoise(image -> drawn.add("noise"))
				.addContent(() -> "ab3xk", (word, image) -> drawn.add("content " + word)).addFilter(image -> drawn.add("filter"))
				.build();
		assertEquals("ab3xk", factory.create().getContent());
		assertEquals("ab3xk", factory.create().getContent());
		assertEquals(List.of("noise", "content ab3xk", "filter", "noise", "content ab3xk", "filter"), drawn);
		return;
	}

	@Test
	public void factoryMakesCaptchasOnManyThreadsAtOnce() throws InterruptedException, ExecutionException {
		ImageCaptcha.Factory factory = new ImageCaptcha.Factory.Builder(200, 50).addContent().addNoise().addFilter().addBorder()
				.build();
		ExecutorService executor = Executors.newFixedThreadPool(8);
		try {
			List<Future<ImageCaptcha>> futures = new ArrayList<>();
			for (int i = 0; i < 200; i++) {
				futures.add(executor.submit(factory::create));
			}
			Set<String> contents = new HashSet<>();
			Set<BufferedImage> images = Collections.newSetFromMap(new IdentityHashMap<>());
			for (Future<ImageCaptcha> future : futures) {
				ImageCaptcha captcha = future.get();
				assertTrue(captcha.getContent().matches("[a-hkmnprwxy2-8]{5}"), captcha.getContent());
				assertEquals(Color.BLACK.getRGB(), captcha.getImage().getRGB(0, 0));
				contents.add(captcha.getContent());
				images.add(captcha.getImage());
			}
			// A new answer, and a new image, each time
			assertTrue(contents.size() > 190, contents.size() + " different answers from 200");
			assertEquals(200, images.size());
		} finally {
			executor.shutdownNow();
		}
		return;
	}

	@Test
	public void defaultsToAnOpaqueLightGreyBackground() throws IOException {
		// create(), and a Builder with no background or the default one
		List<ImageCaptcha> captchas = Arrays.asList(ImageCaptcha.create(), new ImageCaptcha.Factory.Builder(200, 50).build()
				.create(), new ImageCaptcha.Factory.Builder(200, 50).addBackground().addContent().build().create());
		for (ImageCaptcha captcha : captchas) {
			BufferedImage image = captcha.getImage();
			assertEquals(Color.LIGHT_GRAY.getRGB(), image.getRGB(0, 0));
			assertTrue(Arrays.stream(pixels(image)).allMatch(pixel -> pixel >>> 24 == 0xff), "not opaque");
			// So it can be a JPEG (#45)
			assertTrue(ImageIO.write(image, "jpg", new ByteArrayOutputStream()), "no JPEG");
		}
		// Transparency is a choice
		BufferedImage image = new ImageCaptcha.Factory.Builder(200, 50).addBackground(new TransparentBackgroundProducer()).build()
				.create().getImage();
		assertEquals(0, image.getRGB(0, 0) >>> 24);
		return;
	}

	@Test
	public void addBorderDrawsEveryEdgePixelAndNothingElse() {
		// Wide, and tall
		for (int[] size : new int[][] { { 200, 50 }, { 60, 200 } }) {
			BufferedImage image = new ImageCaptcha.Factory.Builder(size[0], size[1]).addBorder().build().create().getImage();
			int width = image.getWidth();
			int height = image.getHeight();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					boolean edge = x == 0 || y == 0 || x == width - 1 || y == height - 1;
					assertEquals(edge ? Color.BLACK.getRGB() : Color.LIGHT_GRAY.getRGB(), image.getRGB(x, y),
							width + " x " + height + ", pixel " + x + ", " + y);
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
							BufferedImage image = new ImageCaptcha.Factory.Builder(size[0], size[1]).addBackground(background)
									.addContent(new LatinContentProducer(), renderer).addNoise(noiseProducer).addFilter(filter).build()
									.create().getImage();
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
		// The default background, a transparent one, and a gradient
		List<ImageCaptcha> captchas = Arrays.asList(ImageCaptcha.create(),
				new ImageCaptcha.Factory.Builder(200, 50).addBackground(new TransparentBackgroundProducer()).addContent()
						.addNoise().addFilter().addBorder().build().create(),
				new ImageCaptcha.Factory.Builder(200, 50).addBackground(new GradiatedBackgroundProducer()).addContent().build()
						.create());
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
		ImageCaptcha.Factory factory = new ImageCaptcha.Factory.Builder(200, 50).addContent(() -> "\u4E2D\u6587", renderer)
				.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, factory::create);
		assertTrue(e.getMessage().contains("(U+4E2D)"), e.getMessage());
		return;
	}

	/**
	 * Renders a CAPTCHA with {@code renderer} on a transparent image, and checks that something was drawn.
	 *
	 * @param renderer a {@link WordRenderer}
	 */
	private static void assertDrawsText(WordRenderer renderer) {
		ImageCaptcha captcha = new ImageCaptcha.Factory.Builder(200, 50).addBackground(new TransparentBackgroundProducer())
				.addContent(new LatinContentProducer(), renderer).build().create();
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
