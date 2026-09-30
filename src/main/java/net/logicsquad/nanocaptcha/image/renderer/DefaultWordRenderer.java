package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Renders the content onto the image.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @author <a href="mailto:botyrbojey@gmail.com">bivashy</a>
 * @since 1.0
 */
public final class DefaultWordRenderer extends AbstractWordRenderer {
	/**
	 * Constructor taking its settings from a {@link Builder}
	 *
	 * @param builder a {@link Builder}
	 * @since 2.3
	 */
	private DefaultWordRenderer(Builder builder) {
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
	 * Renders {@code word} onto {@code image}, using {@code random}, so that tests can seed it.
	 *
	 * @param word   word to render
	 * @param image  image to render onto
	 * @param random a {@link Random}
	 * @throws IllegalArgumentException if a font can't display a character in {@code word}
	 */
	void render(String word, BufferedImage image, Random random) {
		Graphics2D g = image.createGraphics();
		try {
			RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
			g.setRenderingHints(hints);

			// Choose every glyph's font first, so that the text can be measured before it's drawn
			FontRenderContext frc = g.getFontRenderContext();
			char[] chars = word.toCharArray();
			Font[] fonts = new Font[chars.length];
			float size = fontSize(image.getHeight());
			for (int i = 0; i < chars.length; i++) {
				Font font = fontSupplier().get();
				if (!font.canDisplay(chars[i])) {
					throw new IllegalArgumentException(
							cannotDisplay(font, chars[i]) + " Supply a font that can with DefaultWordRenderer.Builder.font().");
				}
				// The built-in fonts are sized to the image, and others keep their own size
				fonts[i] = defaultFonts() && font.getSize2D() != size ? font.deriveFont(size) : font;
			}
			int xBaseline = (int) Math.round(image.getWidth() * xOffset());
			// The same margin on the right, and clear of the edges, where a border would touch the text
			int width = image.getWidth() - xBaseline - Math.max(1, xBaseline);
			int height = image.getHeight() - 2;
			Line line = new Line(chars, fonts, frc);
			while (line.width > width || line.height() > height) {
				if (!shrink(fonts, Math.min((double) width / line.width, (double) height / line.height()))) {
					break;
				}
				line = new Line(chars, fonts, frc);
			}
			int yBaseline = randomYOffset() ? randomBaseline(image.getHeight(), line.ascent, line.descent, random)
					: image.getHeight() - (int) Math.round(image.getHeight() * yOffset());

			for (int i = 0; i < chars.length; i++) {
				g.setColor(colorSupplier().get());
				g.setFont(fonts[i]);
				g.drawChars(chars, i, 1, xBaseline + line.x[i], yBaseline);
			}
		} finally {
			g.dispose();
		}
	}

	/**
	 * Makes each of {@code fonts} smaller by {@code scale}, and by at least a point, in whole points, so that the
	 * JDK's glyph caches see only a few sizes.
	 *
	 * @param fonts fonts to shrink, in place
	 * @param scale scale, less than 1
	 * @return {@code false} if the fonts are all too small to shrink
	 */
	private static boolean shrink(Font[] fonts, double scale) {
		boolean shrunk = false;
		for (int i = 0; i < fonts.length; i++) {
			float size = fonts[i].getSize2D();
			if (size > 1) {
				fonts[i] = fonts[i].deriveFont((float) Math.max(1, Math.min(size - 1, Math.floor(size * scale))));
				shrunk = true;
			}
		}
		return shrunk;
	}

	/**
	 * A line of glyphs, laid out as they'll be drawn, each where the ink of the one before ends, with the pixels
	 * their ink covers.
	 */
	private static final class Line {
		/**
		 * Where each glyph goes, from the start of the line
		 */
		private final int[] x;

		/**
		 * How far the ink reaches, from the start of the line
		 */
		private final int width;

		/**
		 * Rows of ink above the baseline
		 */
		private final int ascent;

		/**
		 * Rows of ink below the baseline
		 */
		private final int descent;

		/**
		 * Constructor
		 *
		 * @param chars characters
		 * @param fonts a font for each character
		 * @param frc   {@link FontRenderContext} they'll be drawn with
		 */
		private Line(char[] chars, Font[] fonts, FontRenderContext frc) {
			x = new int[chars.length];
			int next = 0;
			int right = 0;
			int above = 0;
			int below = 0;
			for (int i = 0; i < chars.length; i++) {
				GlyphVector glyph = fonts[i].createGlyphVector(frc, new char[] { chars[i] });
				Rectangle bounds = glyph.getPixelBounds(frc, 0, 0);
				x[i] = next;
				right = Math.max(right, next + bounds.x + bounds.width);
				above = Math.max(above, -bounds.y);
				below = Math.max(below, bounds.y + bounds.height);
				next = next + (int) glyph.getVisualBounds().getWidth();
			}
			width = right;
			ascent = above;
			descent = below;
			return;
		}

		/**
		 * Returns the rows of ink.
		 *
		 * @return height of the ink
		 */
		private int height() {
			return ascent + descent;
		}
	}

	/**
	 * Builder for {@code DefaultWordRenderer}.
	 * 
	 * @since 1.4
	 */
	public static class Builder extends AbstractWordRenderer.Builder {
		@Override
		public DefaultWordRenderer build() {
			return new DefaultWordRenderer(this);
		}
	}
}
