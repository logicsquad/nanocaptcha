package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Based on the {@link DefaultWordRenderer}, this implementation strips down to
 * the basics to render {@link BufferedImage}s as much as 5X faster. (This class
 * will render almost 70,000 {@link BufferedImage}s per second on an iMac with a
 * 4GHx Intel Core i7 CPU.) It has the following restrictions compared to
 * {@link DefaultWordRenderer}:
 * </p>
 * 
 * <ul>
 * <li>{@link Color} choices are limited: text is rendered in black.</li>
 * <li>{@link Font} choices are limited: renders with Arial and Courier.</li>
 * <li>Rendered text is <em>not</em> anti-aliased.</li>
 * <li>{@link DefaultWordRenderer} measures the size of each glyph it renders to
 * calculate horizontal spacing. This class uses fixed spacing, <em>but</em>
 * will "fudge" each glyph's position horizontally and vertically: see
 * below.</li>
 * <li>{@link Font} choice is only random for the first 100 choices: this class
 * pre-computes a list of random indexes into the {@link Font} array, and then
 * <em>re-uses</em> those indexes by cycling through them repeatedly.</li>
 * </ul>
 * 
 * <p>
 * As noted above, this class will render each glyph with a random horizontal
 * and vertical fudge factor between (-5, 5) from the baseline. The effect is
 * that glyphs can move around and bunch together (or spread apart) more. As
 * with {@link Font} choice, there is only limited randomness here: again, we
 * pre-compute a list of 100 random fudge values in the range, and cycle through
 * that list repeatedly.
 * </p>
 * 
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.1
 */
public class FastWordRenderer implements WordRenderer {
	/**
	 * Font size (in points)
	 */
	private static final int FONT_SIZE = 40;

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
	 * Random number generator
	 */
	private static final Random RAND = new Random();

	/**
	 * Available {@link Color}
	 */
	private static final Color COLOR = Color.BLACK;

	/**
	 * Available {@link Font}s
	 */
	private static final Font[] FONTS = new Font[2];

	// Set up Font list
	static {
		FONTS[0] = new Font("Arial", Font.BOLD, FONT_SIZE);
		FONTS[1] = new Font("Courier", Font.BOLD, FONT_SIZE);
	}

	// The text will be rendered 25%/5% of the image height/width from the X and Y
	// axes
	/**
	 * Percentage offset along y-axis
	 */
	private static final double YOFFSET = 0.25;

	/**
	 * Percentage offset along x-axis
	 */
	private static final double XOFFSET = 0.05;

	/**
	 * Constructor
	 */
	public FastWordRenderer() {
		for (int i = 0; i < FONT_INDEX_SIZE; i++) {
			INDEXES[i] = RAND.nextInt(FONTS.length);
		}
		for (int i = 0; i < FUDGE_INDEX_SIZE; i++) {
			FUDGES[i] = RAND.nextInt((FUDGE_MAX - FUDGE_MIN) + 1) + FUDGE_MIN;
		}
		return;
	}

	@Override
	public void render(final String word, BufferedImage image) {
		Graphics2D g = image.createGraphics();
		int xBaseline = (int) (image.getWidth() * XOFFSET);
		int yBaseline = image.getHeight() - (int) (image.getHeight() * YOFFSET);
		char[] chars = new char[1];
		for (char c : word.toCharArray()) {
			chars[0] = c;
			g.setColor(COLOR);
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
}
