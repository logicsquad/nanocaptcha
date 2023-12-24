package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * <p>
 * Based on the {@link DefaultWordRenderer}, this implementation strips down to the basics to render {@link BufferedImage}s as much as 5X
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
 * </ul>
 * 
 * <p>
 * As noted above, this class will render each glyph with a random horizontal and vertical fudge factor between (-5, 5) from the baseline.
 * The effect is that glyphs can move around and bunch together (or spread apart) more. As with {@link Font} choice, there is only limited
 * randomness here: again, we pre-compute a list of 100 random fudge values in the range, and cycle through that list repeatedly.
 * </p>
 * 
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.1
 */
public class FastWordRenderer extends AbstractWordRenderer {
	/**
	 * Horizontal space between glyphs (in pixels)
	 */
	private static final int SHIFT = 20;

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
	private static AtomicInteger idxPointer = new AtomicInteger(0);

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
	private static AtomicInteger fudgePointer = new AtomicInteger(0);

	/**
	 * Available {@link Font}s
	 */
	private static final Font[] FONTS = new Font[2];

	// Set up Font list, pre-computed values
	static {
		FONTS[0] = DEFAULT_FONTS.get(0);
		FONTS[1] = DEFAULT_FONTS.get(1);

		for (int i = 0; i < FONT_INDEX_SIZE; i++) {
			INDEXES[i] = RAND.nextInt(FONTS.length);
		}
		for (int i = 0; i < FUDGE_INDEX_SIZE; i++) {
			FUDGES[i] = RAND.nextInt((FUDGE_MAX - FUDGE_MIN) + 1) + FUDGE_MIN;
		}
	}

	/**
	 * Constructor taking x- and y-axis offsets
	 * 
	 * @param xOffset x-axis offset
	 * @param yOffset y-axis offset
	 * @since 1.4
	 */
	private FastWordRenderer(double xOffset, double yOffset, Supplier<Color> wordColorSupplier) {
		super(xOffset, yOffset, wordColorSupplier);
		return;
	}

	@Override
	public void render(final String word, BufferedImage image) {
		Graphics2D g = image.createGraphics();
		int xBaseline = (int) (image.getWidth() * xOffset());
		int yBaseline = image.getHeight() - (int) (image.getHeight() * yOffset());
		char[] chars = new char[1];
		for (char c : word.toCharArray()) {
			chars[0] = c;
			g.setColor(wordColorSupplier().get());
			g.setFont(nextFont());
			int xFudge = nextFudge();
			int yFudge = nextFudge();
			g.drawChars(chars, 0, 1, xBaseline + xFudge, yBaseline - yFudge);
			xBaseline = xBaseline + SHIFT;
		}
	}

	/**
	 * Returns the next {@link Font} to use.
	 * 
	 * @return next {@link Font}
	 */
	private Font nextFont() {
		if (FONTS.length == 1) {
			return FONTS[0];
		} else {
			return FONTS[INDEXES[idxPointer.getAndIncrement() % FONT_INDEX_SIZE]];
		}
	}

	/**
	 * Returns the next fudge value to use.
	 * 
	 * @return fudge value
	 */
	private int nextFudge() {
		return FUDGES[fudgePointer.getAndIncrement() % FUDGE_INDEX_SIZE];
	}

	/**
	 * Builder for {@link FastWordRenderer}.
	 * 
	 * @since 1.4
	 */
	public static class Builder extends AbstractWordRenderer.Builder {
		@Override
		public FastWordRenderer build() {
			return new FastWordRenderer(xOffset, yOffset, wordColorSupplier);
		}
	}
}
