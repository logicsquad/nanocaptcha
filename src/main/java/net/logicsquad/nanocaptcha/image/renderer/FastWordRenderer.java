package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Based on the {@link DefaultWordRenderer}, this implementation strips down to the basics to render {@link BufferedImage}s many times
 * faster. (This class will render almost 70,000 {@link BufferedImage}s per second on an iMac with a 4GHz Intel Core i7 CPU.) It has the
 * following restrictions compared to {@link DefaultWordRenderer}:
 * </p>
 *
 * <ul>
 * <li>{@link Font} choices are limited: renders with "Courier Prime" and "Public Sans".</li>
 * <li>Rendered text is <em>not</em> anti-aliased.</li>
 * <li>{@link DefaultWordRenderer} measures the size of each glyph it renders to calculate horizontal spacing. This class uses fixed
 * spacing, <em>but</em> will "fudge" each glyph's position horizontally and vertically: see below.</li>
 * <li>{@link Font} choice is only random for the first 100 choices: this class pre-computes a list of random indexes into the {@link Font}
 * array, and then <em>re-uses</em> those indexes by cycling through them repeatedly.</li>
 * <li>Glyphs aren't rotated or scaled, and are drawn at whole pixels, so each character in each font comes out as the same bitmap, which
 * makes the text easier to read by machine.</li>
 * </ul>
 *
 * <p>
 * As noted above, this class will render each glyph with a random horizontal and vertical fudge factor between (-5, 5) from the baseline.
 * The effect is that glyphs can move around and bunch together (or spread apart) more. As with {@link Font} choice, there is only limited
 * randomness here: again, we pre-compute a list of 100 random fudge values in the range, and cycle through that list repeatedly.
 * </p>
 *
 * <p>
 * Those sizes, and the spacing, are for the default image height of 50 pixels, where the fonts are 40 pt. In other images, they're
 * in proportion to the height, and they shrink if the text wouldn't otherwise fit across the image.
 * </p>
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @author <a href="mailto:botyrbojey@gmail.com">bivashy</a>
 * @since 1.1
 */
public final class FastWordRenderer extends AbstractWordRenderer {
	/**
	 * Horizontal space between glyphs (in pixels, at {@link AbstractWordRenderer#FONT_SIZE})
	 */
	private static final int SHIFT = 20;

	/**
	 * How far the widest glyph in the built-in fonts, "W" in Public Sans, reaches right, as a proportion of the font size
	 */
	private static final double WIDEST = 0.95;

	/**
	 * Size of list of pre-computed indexes (into {@link Font} list)
	 */
	private static final int FONT_INDEX_SIZE = 100;

	/**
	 * Pre-computed indexes into {@link Font} list
	 */
	private static final int[] INDEXES = new int[FONT_INDEX_SIZE];

	/**
	 * Current index pointer
	 */
	static final AtomicInteger idxPointer = new AtomicInteger(0);

	/**
	 * Minimum fudge value
	 */
	private static final int FUDGE_MIN = -5;

	/**
	 * Maximum fudge value
	 */
	private static final int FUDGE_MAX = 5;

	/**
	 * Size of list of pre-computed fudge values
	 */
	private static final int FUDGE_INDEX_SIZE = 100;

	/**
	 * Pre-computed fudge values
	 */
	private static final int[] FUDGES = new int[FUDGE_INDEX_SIZE];

	/**
	 * Current fudge pointer
	 */
	static final AtomicInteger fudgePointer = new AtomicInteger(0);

	/**
	 * Available {@link Font}s
	 */
	private static final Font[] FONTS = new Font[2];

	/**
	 * {@link #FONTS} at the other sizes they've been used at: one size for each image height, and smaller ones for
	 * text that has to shrink to fit
	 */
	private static final ConcurrentMap<Float, Font[]> SIZED_FONTS = new ConcurrentHashMap<>();

	// Set up Font list, pre-computed values
	static {
		FONTS[0] = DEFAULT_FONTS.get(0);
		FONTS[1] = DEFAULT_FONTS.get(1);

		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < FONT_INDEX_SIZE; i++) {
			INDEXES[i] = random.nextInt(FONTS.length);
		}
		for (int i = 0; i < FUDGE_INDEX_SIZE; i++) {
			FUDGES[i] = random.nextInt((FUDGE_MAX - FUDGE_MIN) + 1) + FUDGE_MIN;
		}
	}

	/**
	 * Constructor taking its settings from a {@link Builder}
	 *
	 * @param builder a {@link Builder}
	 * @since 2.3
	 */
	private FastWordRenderer(Builder builder) {
		super(builder);
		return;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if a font can't display a character in {@code word}
	 */
	@Override
	public void render(final String word, BufferedImage image) {
		render(word, image, ThreadLocalRandom.current());
	}

	/**
	 * Renders {@code word} onto {@code image}, using {@code random} for a random y-offset, so that tests can seed it.
	 *
	 * @param word   word to render
	 * @param image  image to render onto
	 * @param random a {@link Random}
	 * @throws IllegalArgumentException if a font can't display a character in {@code word}
	 */
	void render(String word, BufferedImage image, Random random) {
		Graphics2D g = image.createGraphics();
		try {
			char[] chars = word.toCharArray();
			int xBaseline = (int) (image.getWidth() * xOffset());
			// Room for the text at 40 pt, allowing for the widest glyph at the end and its fudge, which has to fit
			// between the same margin on each side, clear of the edges, where a border would touch it
			double needed = Math.max(0, chars.length - 1) * SHIFT + FUDGE_MAX + WIDEST * FONT_SIZE;
			int width = image.getWidth() - xBaseline - Math.max(1, xBaseline);
			float size = (float) Math.max(1, Math.min(fontSize(image.getHeight()), Math.floor(width * FONT_SIZE / needed)));
			double scale = size / FONT_SIZE;
			Font[] sized = fonts(size);
			Font[] fonts = new Font[chars.length];
			for (int i = 0; i < chars.length; i++) {
				fonts[i] = nextFont(sized);
				if (!fonts[i].canDisplay(chars[i])) {
					throw new IllegalArgumentException(cannotDisplay(fonts[i], chars[i])
							+ " FastWordRenderer only uses its built-in fonts, so use DefaultWordRenderer with a font that can.");
				}
			}
			int shift = (int) Math.round(SHIFT * scale);
			int yBaseline;
			if (randomYOffset()) {
				FontRenderContext frc = g.getFontRenderContext();
				int ascent = 0;
				int descent = 0;
				for (int i = 0; i < chars.length; i++) {
					Rectangle bounds = fonts[i].createGlyphVector(frc, new char[] { chars[i] }).getPixelBounds(frc, 0, 0);
					ascent = Math.max(ascent, -bounds.y);
					descent = Math.max(descent, bounds.y + bounds.height);
				}
				// Each glyph's fudge can move it up or down
				yBaseline = randomBaseline(image.getHeight(), ascent + FUDGE_MAX * scale, descent - FUDGE_MIN * scale, random);
			} else {
				yBaseline = image.getHeight() - (int) (image.getHeight() * yOffset());
			}
			for (int i = 0; i < chars.length; i++) {
				g.setColor(colorSupplier().get());
				g.setFont(fonts[i]);
				int xFudge = (int) Math.round(nextFudge() * scale);
				int yFudge = (int) Math.round(nextFudge() * scale);
				g.drawChars(chars, i, 1, xBaseline + xFudge, yBaseline - yFudge);
				xBaseline = xBaseline + shift;
			}
		} finally {
			g.dispose();
		}
	}

	/**
	 * Returns the next {@link Font} to use.
	 *
	 * @param fonts {@link #FONTS}, at the size to use
	 * @return next {@link Font}
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/44">#44</a>
	 */
	private Font nextFont(Font[] fonts) {
		if (fonts.length == 1) {
			return fonts[0];
		} else {
			// floorMod, not %: the pointer goes negative once it passes Integer.MAX_VALUE.
			return fonts[INDEXES[Math.floorMod(idxPointer.getAndIncrement(), FONT_INDEX_SIZE)]];
		}
	}

	/**
	 * Returns {@link #FONTS} at {@code size}.
	 *
	 * @param size font size, in whole points
	 * @return fonts
	 */
	private static Font[] fonts(float size) {
		if (size == FONT_SIZE) {
			return FONTS;
		}
		return SIZED_FONTS.computeIfAbsent(size, s -> Arrays.stream(FONTS).map(font -> font.deriveFont(s)).toArray(Font[]::new));
	}

	/**
	 * Returns the next fudge value to use.
	 *
	 * @return fudge value
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/44">#44</a>
	 */
	private int nextFudge() {
		// floorMod, not %: the pointer goes negative once it passes Integer.MAX_VALUE.
		return FUDGES[Math.floorMod(fudgePointer.getAndIncrement(), FUDGE_INDEX_SIZE)];
	}

	/**
	 * Builder for {@link FastWordRenderer}. Note that calls to the {@link Font}-related methods inherited from
	 * {@link AbstractWordRenderer.Builder} are effectively ignored: {@code FastWordRenderer} uses a fixed set of two {@link Font}s.
	 *
	 * @since 1.4
	 */
	public static class Builder extends AbstractWordRenderer.Builder {
		@Override
		public FastWordRenderer build() {
			return new FastWordRenderer(this);
		}
	}
}
