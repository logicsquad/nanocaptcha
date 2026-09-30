package net.logicsquad.nanocaptcha.image.renderer;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.logicsquad.nanocaptcha.content.ContentProducer;
import net.logicsquad.nanocaptcha.content.FiveLetterFirstNameContentProducer;
import net.logicsquad.nanocaptcha.content.LatinContentProducer;
import net.logicsquad.nanocaptcha.content.NumbersContentProducer;

/**
 * Unit tests on {@link AbstractWordRenderer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.2
 */
public class AbstractWordRendererTest {
	@Test
	public void defaultFontsCanDisplayEveryCharacterFromTheBuiltInProducers() {
		List<ContentProducer> producers = Arrays.asList(new LatinContentProducer(), new NumbersContentProducer(),
				new FiveLetterFirstNameContentProducer());
		for (ContentProducer producer : producers) {
			// Enough samples to see every character many times over
			for (int i = 0; i < 1000; i++) {
				for (char c : producer.getContent().toCharArray()) {
					for (Font font : AbstractWordRenderer.DEFAULT_FONTS) {
						assertTrue(font.canDisplay(c), AbstractWordRenderer.cannotDisplay(font, c));
					}
				}
			}
		}
		return;
	}

	@Test
	public void cannotLoadFontBlamesATemporaryDirectoryItCantWriteTo(@TempDir Path directory) {
		// A missing directory, since a read-only one is still writable for root, as in the container tests
		Path missing = directory.resolve("missing");
		IllegalStateException e = AbstractWordRenderer.cannotLoadFont("/fonts/Example.ttf", new IOException("Problem reading font data."),
				missing);
		assertTrue(e.getMessage().contains("can't create one in '" + missing + "'"), e.getMessage());
		assertTrue(e.getMessage().contains("-Djava.io.tmpdir"), e.getMessage());
		assertEquals(1, e.getSuppressed().length);
		return;
	}

	@Test
	public void cannotLoadFontBlamesFontSupportOtherwise(@TempDir Path directory) throws IOException {
		IllegalStateException e = AbstractWordRenderer.cannotLoadFont("/fonts/Example.ttf", new IOException("Problem reading font data."),
				directory);
		assertTrue(e.getMessage().contains("fontconfig"), e.getMessage());
		// The check leaves nothing behind
		try (Stream<Path> files = Files.list(directory)) {
			assertEquals(0, files.count());
		}
		return;
	}

	@Test
	public void randomBaselineKeepsTextClearOfTheEdges() {
		Random random = new Random(1);
		Set<Integer> baselines = new TreeSet<>();
		for (int i = 0; i < 1000; i++) {
			int baseline = AbstractWordRenderer.randomBaseline(50, 29.6, 9.2, random);
			assertTrue(baseline - 29.6 >= 1 && baseline + 9.2 <= 49, "baseline " + baseline);
			baselines.add(baseline);
		}
		// Every whole-pixel baseline that fits
		assertEquals(9, baselines.size(), baselines.toString());
		// Text too tall to fit is centred
		assertEquals(35, AbstractWordRenderer.randomBaseline(50, 40, 20, random));
		return;
	}

	@Test
	public void randomisedYOffsetKeepsGlyphsInTheImageAndChangesEachTime() {
		// FastWordRenderer's fudge moves each glyph up to 5 pixels either way, so it needs a taller image to vary
		assertRandomYOffsetKeepsGlyphsInTheImage(new DefaultWordRenderer.Builder().randomiseYOffset().build(), 50);
		assertRandomYOffsetKeepsGlyphsInTheImage(new FastWordRenderer.Builder().randomiseYOffset().build(), 70);
		return;
	}

	/**
	 * Renders 200 CAPTCHAs with {@code renderer} on a 200-pixel-wide image, and checks that no glyph reaches the top or
	 * bottom row, where it might have been cut off, and that the height of the text varies.
	 *
	 * @param renderer a {@link WordRenderer} with a random y-offset
	 * @param height   image height
	 */
	private static void assertRandomYOffsetKeepsGlyphsInTheImage(WordRenderer renderer, int height) {
		String name = renderer.getClass().getSimpleName();
		Set<Integer> tops = new TreeSet<>();
		for (int i = 0; i < 200; i++) {
			String word = new LatinContentProducer().getContent();
			Rectangle ink = render(renderer, word, 200, height);
			int bottom = ink.y + ink.height - 1;
			assertTrue(ink.y > 0 && bottom < height - 1, name + ": '" + word + "' reaches from row " + ink.y + " to " + bottom);
			tops.add(ink.y);
		}
		assertTrue(tops.size() >= 5, name + ": the text starts on only these rows: " + tops);
		return;
	}

	@Test
	public void builtInFontsScaleWithTheImageHeight() {
		assertEquals(40, AbstractWordRenderer.fontSize(50));
		assertEquals(80, AbstractWordRenderer.fontSize(100));
		assertEquals(1, AbstractWordRenderer.fontSize(1));
		for (WordRenderer renderer : renderers()) {
			// Digits are at most 44 pixels high at 40 pt, even with FastWordRenderer's fudge
			Rectangle ink = render(renderer, "23456", 400, 100);
			assertTrue(ink.height >= 45, name(renderer) + ": the digits are " + ink.height + " pixels high in a 100-pixel image");
		}
		return;
	}

	@Test
	public void otherFontsKeepTheirSize() {
		WordRenderer renderer = new DefaultWordRenderer.Builder().font(AbstractWordRenderer.DEFAULT_FONTS.get(0).deriveFont(20f))
				.build();
		assertEquals(render(renderer, "23456", 200, 50).height, render(renderer, "23456", 400, 100).height);
		return;
	}

	@Test
	public void longContentShrinksToFitTheWidth() {
		for (WordRenderer renderer : renderers()) {
			for (int i = 0; i < 100; i++) {
				// Ten characters used to run off the right-hand edge of the default image
				String word = new LatinContentProducer(10).getContent();
				Rectangle ink = render(renderer, word, 200, 50);
				// Inside the margin on the right
				assertTrue(ink.x + ink.width <= 190, name(renderer) + ": '" + word + "' covers " + ink);
				// A tall image, whose height would make the default text far too wide
				word = new LatinContentProducer().getContent();
				ink = render(renderer, word, 60, 200);
				assertTrue(ink.x + ink.width <= 57, name(renderer) + ": '" + word + "' covers " + ink);
			}
		}
		return;
	}

	/**
	 * Returns NanoCaptcha's renderers, with their default settings.
	 *
	 * @return renderers
	 */
	private static List<WordRenderer> renderers() {
		return Arrays.asList(new DefaultWordRenderer.Builder().build(), new FastWordRenderer.Builder().build());
	}

	/**
	 * Returns the class name of {@code renderer}.
	 *
	 * @param renderer a {@link WordRenderer}
	 * @return name
	 */
	private static String name(WordRenderer renderer) {
		return renderer.getClass().getSimpleName();
	}

	/**
	 * Renders {@code word} with {@code renderer} on a transparent image, and returns the bounds of the pixels it inks.
	 *
	 * @param renderer a {@link WordRenderer}
	 * @param word     word to render
	 * @param width    image width
	 * @param height   image height
	 * @return bounds of ink
	 */
	private static Rectangle render(WordRenderer renderer, String word, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		renderer.render(word, image);
		Rectangle ink = null;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if ((image.getRGB(x, y) >>> 24) != 0) {
					Rectangle pixel = new Rectangle(x, y, 1, 1);
					ink = ink == null ? pixel : ink.union(pixel);
				}
			}
		}
		assertNotNull(ink, name(renderer) + " drew nothing");
		return ink;
	}
}
