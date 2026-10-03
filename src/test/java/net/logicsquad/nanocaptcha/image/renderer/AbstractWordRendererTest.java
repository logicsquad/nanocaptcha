package net.logicsquad.nanocaptcha.image.renderer;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
		WordRenderer renderer = new DefaultWordRenderer.Builder().randomiseYOffset().build();
		Set<Integer> tops = new TreeSet<>();
		for (int i = 0; i < 200; i++) {
			String word = new LatinContentProducer().getContent();
			Rectangle ink = render(renderer, word, 200, 50);
			int bottom = ink.y + ink.height - 1;
			// Clear of the top and bottom rows, where a glyph might have been cut off
			assertTrue(ink.y > 0 && bottom < 49, "'" + word + "' reaches from row " + ink.y + " to " + bottom);
			tops.add(ink.y);
		}
		assertTrue(tops.size() >= 5, "The text starts on only these rows: " + tops);
		return;
	}

	@Test
	public void builtInFontsScaleWithTheImageHeight() {
		assertEquals(40, AbstractWordRenderer.fontSize(50));
		assertEquals(80, AbstractWordRenderer.fontSize(100));
		assertEquals(1, AbstractWordRenderer.fontSize(1));
		// At 40 pt, digits are under 45 pixels high
		Rectangle ink = render(new DefaultWordRenderer.Builder().build(), "23456", 400, 100);
		assertTrue(ink.height >= 45, "The digits are " + ink.height + " pixels high in a 100-pixel image");
		return;
	}

	@Test
	public void otherFontsKeepTheirSize() {
		DefaultWordRenderer renderer = (DefaultWordRenderer) new DefaultWordRenderer.Builder()
				.font(AbstractWordRenderer.DEFAULT_FONTS.get(0).deriveFont(20f)).build();
		// The same seed varies the glyphs in the same way
		BufferedImage small = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
		renderer.render("23456", small, new Random(1));
		BufferedImage large = new BufferedImage(400, 100, BufferedImage.TYPE_INT_ARGB);
		renderer.render("23456", large, new Random(1));
		assertEquals(ink(small).height, ink(large).height);
		return;
	}

	@Test
	public void longContentShrinksToFitTheWidth() {
		WordRenderer renderer = new DefaultWordRenderer.Builder().build();
		for (int i = 0; i < 100; i++) {
			// Ten characters used to run off the right-hand edge of the default image
			String word = new LatinContentProducer(10).getContent();
			Rectangle ink = render(renderer, word, 200, 50);
			// Inside the margin on the right
			assertTrue(ink.x + ink.width <= 190, "'" + word + "' covers " + ink);
			// A tall image, whose height would make the default text far too wide
			word = new LatinContentProducer().getContent();
			ink = render(renderer, word, 60, 200);
			assertTrue(ink.x + ink.width <= 57, "'" + word + "' covers " + ink);
		}
		return;
	}

	@Test
	public void keepsItsOwnCopiesOfTheColoursAndFonts() {
		List<Color> colors = new ArrayList<>(List.of(Color.RED));
		List<Font> fonts = new ArrayList<>(List.of(AbstractWordRenderer.DEFAULT_FONTS.get(0)));
		WordRenderer renderer = new DefaultWordRenderer.Builder().randomColor(colors).randomFont(fonts).build();
		// A factory shares the renderer between threads, so the lists mustn't change it
		colors.set(0, Color.BLUE);
		fonts.clear();
		BufferedImage image = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
		renderer.render("ab3xk", image);
		int[] ink = Arrays.stream(image.getRGB(0, 0, 200, 50, null, 0, 200)).filter(pixel -> pixel >>> 24 != 0).toArray();
		assertTrue(ink.length > 100, "only " + ink.length + " pixels drawn");
		assertTrue(Arrays.stream(ink).allMatch(pixel -> (pixel & 0xff) == 0), "drawn in blue");
		return;
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
		return ink(image);
	}

	/**
	 * Returns the bounds of the pixels in {@code image} that aren't transparent.
	 *
	 * @param image an image
	 * @return bounds of ink
	 */
	static Rectangle ink(BufferedImage image) {
		Rectangle ink = null;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getRGB(x, y) >>> 24) != 0) {
					Rectangle pixel = new Rectangle(x, y, 1, 1);
					ink = ink == null ? pixel : ink.union(pixel);
				}
			}
		}
		assertNotNull(ink, "Nothing was drawn");
		return ink;
	}
}
