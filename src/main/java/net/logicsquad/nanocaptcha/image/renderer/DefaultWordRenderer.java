package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Renders the content onto the image. Each glyph gets a small random rotation, scale, vertical shift and sub-pixel
 * position, chosen afresh for each CAPTCHA, and overlaps the one before it slightly, so that a character isn't drawn as
 * the same bitmap twice.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @author <a href="mailto:botyrbojey@gmail.com">bivashy</a>
 * @since 1.0
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/75">#75</a>
 */
public final class DefaultWordRenderer extends AbstractWordRenderer {
	/**
	 * Largest rotation of a glyph, either way (in radians)
	 */
	private static final double MAX_ROTATION = Math.toRadians(10);

	/**
	 * Largest change to a glyph's width or height, either way, as a proportion
	 */
	private static final double MAX_SCALE = 0.1;

	/**
	 * Largest vertical shift of a glyph, either way, as a proportion of the font size
	 */
	private static final double MAX_RISE = 0.05;

	/**
	 * Largest overlap of a glyph with the one before it, as a proportion of the font size
	 */
	private static final double MAX_OVERLAP = 0.08;

	/**
	 * Flatness for measuring the curves in glyph outlines (in pixels)
	 */
	private static final double FLATNESS = 0.05;

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
			// Normalising the outlines would snap them to whole pixels
			hints.add(new RenderingHints(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE));
			g.setRenderingHints(hints);

			// Choose every glyph's font and variation first, so that the text can be measured before it's drawn
			FontRenderContext frc = g.getFontRenderContext();
			char[] chars = word.toCharArray();
			Font[] fonts = new Font[chars.length];
			Variation[] variations = new Variation[chars.length];
			float size = fontSize(image.getHeight());
			for (int i = 0; i < chars.length; i++) {
				Font font = fontSupplier().get();
				if (!font.canDisplay(chars[i])) {
					throw new IllegalArgumentException(
							cannotDisplay(font, chars[i]) + " Supply a font that can with DefaultWordRenderer.Builder.font().");
				}
				// The built-in fonts are sized to the image, and others keep their own size
				fonts[i] = defaultFonts() && font.getSize2D() != size ? font.deriveFont(size) : font;
				variations[i] = new Variation(random);
			}
			int xBaseline = (int) Math.round(image.getWidth() * xOffset());
			// The same margin on the right, and clear of the edges, where a border would touch the text
			int width = image.getWidth() - xBaseline - Math.max(1, xBaseline);
			int height = image.getHeight() - 2;
			Line line = new Line(chars, fonts, variations, frc);
			while (line.width > width || line.height() > height) {
				if (!shrink(fonts, Math.min((double) width / line.width, (double) height / line.height()))) {
					break;
				}
				line = new Line(chars, fonts, variations, frc);
			}
			int yBaseline = randomYOffset() ? randomBaseline(image.getHeight(), line.ascent, line.descent, random)
					: image.getHeight() - (int) Math.round(image.getHeight() * yOffset());

			g.translate(xBaseline, yBaseline);
			for (Shape glyph : line.glyphs) {
				g.setColor(colorSupplier().get());
				g.fill(glyph);
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
	 * Returns the bounds of {@code shape} as {@code {minX, minY, maxX, maxY}}, allowing for the flattening of its
	 * curves. ({@link Shape#getBounds2D()} includes the control points of curves in older JDKs.)
	 *
	 * @param shape a {@link Shape}
	 * @return bounds, all 0 for an empty shape
	 */
	private static double[] bounds(Shape shape) {
		double[] bounds = { Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE };
		double[] coords = new double[6];
		for (PathIterator pi = shape.getPathIterator(null, FLATNESS); !pi.isDone(); pi.next()) {
			if (pi.currentSegment(coords) != PathIterator.SEG_CLOSE) {
				bounds[0] = Math.min(bounds[0], coords[0] - FLATNESS);
				bounds[1] = Math.min(bounds[1], coords[1] - FLATNESS);
				bounds[2] = Math.max(bounds[2], coords[0] + FLATNESS);
				bounds[3] = Math.max(bounds[3], coords[1] + FLATNESS);
			}
		}
		return bounds[0] > bounds[2] ? new double[4] : bounds;
	}

	/**
	 * Random changes to one glyph. Distances are proportions of the font size, so that they still suit if the text
	 * shrinks to fit.
	 */
	private static final class Variation {
		/**
		 * Rotation (in radians)
		 */
		private final double rotation;

		/**
		 * Horizontal scale
		 */
		private final double scaleX;

		/**
		 * Vertical scale
		 */
		private final double scaleY;

		/**
		 * Shift upwards, as a proportion of the font size
		 */
		private final double rise;

		/**
		 * Overlap with the glyph before, as a proportion of the font size
		 */
		private final double overlap;

		/**
		 * Shift to the right (in pixels, less than 1)
		 */
		private final double shift;

		/**
		 * Constructor
		 *
		 * @param random a {@link Random}
		 */
		private Variation(Random random) {
			rotation = (2 * random.nextDouble() - 1) * MAX_ROTATION;
			scaleX = 1 + (2 * random.nextDouble() - 1) * MAX_SCALE;
			scaleY = 1 + (2 * random.nextDouble() - 1) * MAX_SCALE;
			rise = (2 * random.nextDouble() - 1) * MAX_RISE;
			overlap = random.nextDouble() * MAX_OVERLAP;
			shift = random.nextDouble();
			return;
		}
	}

	/**
	 * A line of glyphs, laid out as they'll be drawn, with the pixels their ink covers. Each glyph starts where the ink
	 * of the one before it ends, less its overlap.
	 */
	private static final class Line {
		/**
		 * Outline of each glyph, from the start of the line, with the baseline at 0
		 */
		private final Shape[] glyphs;

		/**
		 * Columns of ink, from the start of the line
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
		 * @param chars      characters
		 * @param fonts      a font for each character
		 * @param variations a {@link Variation} for each character
		 * @param frc        {@link FontRenderContext} they'll be drawn with
		 */
		private Line(char[] chars, Font[] fonts, Variation[] variations, FontRenderContext frc) {
			glyphs = new Shape[chars.length];
			double next = 0;
			double right = 0;
			double top = 0;
			double bottom = 0;
			for (int i = 0; i < chars.length; i++) {
				Variation variation = variations[i];
				float size = fonts[i].getSize2D();
				Shape outline = fonts[i].createGlyphVector(frc, new char[] { chars[i] }).getGlyphOutline(0);
				double[] bounds = bounds(outline);
				double x = (bounds[0] + bounds[2]) / 2;
				double y = (bounds[1] + bounds[3]) / 2;
				// Rotate and scale the glyph about its centre
				AffineTransform transform = AffineTransform.getTranslateInstance(x, y - variation.rise * size);
				transform.rotate(variation.rotation);
				transform.scale(variation.scaleX, variation.scaleY);
				transform.translate(-x, -y);
				Shape varied = transform.createTransformedShape(outline);
				bounds = bounds(varied);
				double start = (i == 0 ? 0 : next - variation.overlap * size) + variation.shift;
				glyphs[i] = AffineTransform.getTranslateInstance(start - bounds[0], 0).createTransformedShape(varied);
				next = start + bounds[2] - bounds[0];
				right = Math.max(right, next);
				top = Math.min(top, bounds[1]);
				bottom = Math.max(bottom, bounds[3]);
			}
			width = (int) Math.ceil(right);
			ascent = (int) Math.ceil(-top);
			descent = (int) Math.ceil(bottom);
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
