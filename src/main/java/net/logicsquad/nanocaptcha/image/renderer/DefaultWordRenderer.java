package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Renders the content onto the image.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class DefaultWordRenderer implements WordRenderer {
	/**
	 * Font size in points
	 */
	private static final int FONT_SIZE = 40;

	/**
	 * Random number generator
	 */
	private static final Random RAND = new Random();

	/**
	 * Default {@link Color}s
	 */
	private static final List<Color> DEFAULT_COLORS = new ArrayList<>();

	/**
	 * Default fonts
	 */
	private static final List<Font> DEFAULT_FONTS = new ArrayList<>();

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

	// Set up default Colors, Fonts
	static {
		DEFAULT_COLORS.add(Color.BLACK);
		DEFAULT_FONTS.add(new Font("Arial", Font.BOLD, FONT_SIZE));
		DEFAULT_FONTS.add(new Font("Courier", Font.BOLD, FONT_SIZE));
	}

	/**
	 * List of available {@link Color}s
	 */
	private final List<Color> colors = new ArrayList<>();

	/**
	 * List of available {@link Font}s
	 */
	private final List<Font> fonts = new ArrayList<>();

	/**
	 * Constructor using default {@link Color} (black) and {@link Font}s (Arial and
	 * Courier).
	 */
	public DefaultWordRenderer() {
		this(DEFAULT_COLORS, DEFAULT_FONTS);
	}

	/**
	 * Constructor taking a list of {@link Color}s and {@link Font}s to choose from.
	 *
	 * @param colors {@link Color}s
	 * @param fonts  {@link Font}s
	 */
	public DefaultWordRenderer(List<Color> colors, List<Font> fonts) {
		this.colors.addAll(colors);
		this.fonts.addAll(fonts);
		return;
	}

	@Override
	public void render(final String word, BufferedImage image) {
		Graphics2D g = image.createGraphics();

		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
		g.setRenderingHints(hints);

		FontRenderContext frc = g.getFontRenderContext();
		int xBaseline = (int) Math.round(image.getWidth() * XOFFSET);
		int yBaseline = image.getHeight() - (int) Math.round(image.getHeight() * YOFFSET);

		char[] chars = new char[1];
		for (char c : word.toCharArray()) {
			chars[0] = c;

			g.setColor(colors.get(RAND.nextInt(colors.size())));

			int choiceFont = RAND.nextInt(fonts.size());
			Font font = fonts.get(choiceFont);
			g.setFont(font);

			GlyphVector gv = font.createGlyphVector(frc, chars);
			g.drawChars(chars, 0, chars.length, xBaseline, yBaseline);

			int width = (int) gv.getVisualBounds().getWidth();
			xBaseline = xBaseline + width;
		}
	}
}
