package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

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
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/85">#85</a>
 */
public final class DefaultWordRenderer implements WordRenderer {
	/**
	 * Resource path to "Courier Prime"
	 */
	private static final String COURIER_PRIME_FONT = "/net/logicsquad/nanocaptcha/fonts/CourierPrime-Bold.ttf";

	/**
	 * Resource path to "Public Sans"
	 */
	private static final String PUBLIC_SANS_FONT = "/net/logicsquad/nanocaptcha/fonts/PublicSans-Bold.ttf";

	/**
	 * Font size (in points) of {@link #DEFAULT_FONTS}. They're sized to the image instead: this size in an image of the
	 * default height, 50 pixels, and in proportion otherwise.
	 */
	private static final int FONT_SIZE = 40;

	/**
	 * Height of image (in pixels) that {@link #FONT_SIZE} suits
	 */
	private static final int FONT_SIZE_HEIGHT = 50;

	/**
	 * Built-in fonts, which can display everything NanoCaptcha's own content producers generate
	 */
	static final List<Font> DEFAULT_FONTS = List.of(fontFromResource(COURIER_PRIME_FONT), fontFromResource(PUBLIC_SANS_FONT));

	/**
	 * Default supplier for {@link Color}
	 */
	private static final Supplier<Color> DEFAULT_COLOR_SUPPLIER = () -> Color.BLACK;

	/**
	 * Default supplier for {@link Font}: one of {@link #DEFAULT_FONTS} at random
	 */
	private static final Supplier<Font> DEFAULT_FONT_SUPPLIER = () -> DEFAULT_FONTS.get(ThreadLocalRandom.current().nextInt(DEFAULT_FONTS.size()));

	/**
	 * Default percentage offset along x-axis
	 */
	private static final double X_OFFSET_DEFAULT = 0.05;

	/**
	 * Default percentage offset along y-axis
	 */
	private static final double Y_OFFSET_DEFAULT = 0.25;

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
	 * Percentage offset along x-axis
	 */
	private final double xOffset;

	/**
	 * Percentage offset along y-axis
	 */
	private final double yOffset;

	/**
	 * Whether to choose the y-offset at random for each render
	 */
	private final boolean randomYOffset;

	/**
	 * Whether {@link #fontSupplier} supplies {@link #DEFAULT_FONTS}, which are sized to the image
	 */
	private final boolean defaultFonts;

	/**
	 * Supplier of {@link Color}
	 */
	private final Supplier<Color> colorSupplier;

	/**
	 * Supplier for {@link Font}
	 */
	private final Supplier<Font> fontSupplier;

	/**
	 * Constructor taking its settings from a {@link Builder}
	 *
	 * @param builder a {@link Builder}
	 * @since 2.3
	 */
	private DefaultWordRenderer(Builder builder) {
		xOffset = builder.xOffset;
		yOffset = builder.yOffset;
		randomYOffset = builder.randomYOffset;
		colorSupplier = builder.colorSupplier;
		fontSupplier = builder.fontSupplier;
		defaultFonts = fontSupplier == DEFAULT_FONT_SUPPLIER;
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
				Font font = fontSupplier.get();
				if (!font.canDisplay(chars[i])) {
					throw new IllegalArgumentException(
							cannotDisplay(font, chars[i]) + " Supply a font that can with DefaultWordRenderer.Builder.font().");
				}
				// The built-in fonts are sized to the image, and others keep their own size
				fonts[i] = defaultFonts && font.getSize2D() != size ? font.deriveFont(size) : font;
				variations[i] = new Variation(random);
			}
			int xBaseline = (int) Math.round(image.getWidth() * xOffset);
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
			int yBaseline = randomYOffset ? randomBaseline(image.getHeight(), line.ascent, line.descent, random)
					: image.getHeight() - (int) Math.round(image.getHeight() * yOffset);

			g.translate(xBaseline, yBaseline);
			for (Shape glyph : line.glyphs) {
				g.setColor(colorSupplier.get());
				g.fill(glyph);
			}
		} finally {
			g.dispose();
		}
	}

	/**
	 * Returns the size of {@link #DEFAULT_FONTS} in an image {@code height} pixels high: {@link #FONT_SIZE} at the
	 * default height, and in proportion otherwise, in whole points.
	 *
	 * @param height image height
	 * @return font size
	 * @since 2.3
	 */
	static float fontSize(int height) {
		return (float) Math.max(1, Math.floor((double) height * FONT_SIZE / FONT_SIZE_HEIGHT));
	}

	/**
	 * Returns a random y-coordinate for the baseline of text that reaches {@code ascent} above it and {@code descent}
	 * below it, anywhere the text fits in an image {@code height} pixels high. It keeps the text off the top and
	 * bottom rows, where a border would touch it. Text too tall to fit is centred.
	 *
	 * @param height  image height
	 * @param ascent  how far the text reaches above the baseline, such as the height of the pixels it inks
	 * @param descent how far the text reaches below the baseline
	 * @param random  a {@link Random}
	 * @return y-coordinate of baseline
	 * @since 2.3
	 */
	static int randomBaseline(int height, double ascent, double descent, Random random) {
		int highest = (int) Math.ceil(ascent) + 1;
		int lowest = height - 1 - (int) Math.ceil(descent);
		if (lowest < highest) {
			return (highest + lowest) / 2;
		}
		return highest + random.nextInt(lowest - highest + 1);
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
	 * Returns a {@link Font} loaded from supplied {@code resourceName}.
	 *
	 * @param resourceName path to resource
	 * @return loaded {@link Font}
	 * @throws IllegalStateException if the font can't be loaded
	 * @since 1.5
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/37">#37</a>
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/77">#77</a>
	 */
	private static Font fontFromResource(String resourceName) {
		try (InputStream is = DefaultWordRenderer.class.getResourceAsStream(resourceName)) {
			if (is == null) {
				throw new IllegalStateException("NanoCaptcha's font '" + resourceName + "' is missing from the classpath.");
			}
			return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont((float) FONT_SIZE);
		} catch (IOException | FontFormatException e) {
			throw cannotLoadFont(resourceName, e, Paths.get(System.getProperty("java.io.tmpdir")));
		}
	}

	/**
	 * Returns an exception explaining why the font {@code resourceName} couldn't be loaded. Java copies a font to a
	 * temporary file before loading it, so this first checks that a file can be created in {@code temporaryDirectory}.
	 * If it can, the likeliest cause is that the JDK can't use fonts at all.
	 *
	 * @param resourceName       path to resource
	 * @param cause              what went wrong loading the font
	 * @param temporaryDirectory the JVM's temporary directory
	 * @return exception to throw
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/77">#77</a>
	 */
	static IllegalStateException cannotLoadFont(String resourceName, Exception cause, Path temporaryDirectory) {
		try {
			Files.delete(Files.createTempFile(temporaryDirectory, "nanocaptcha", ".tmp"));
		} catch (IOException | SecurityException e) {
			IllegalStateException exception = new IllegalStateException("NanoCaptcha can't load its font '" + resourceName
					+ "', because Java copies fonts to a temporary file before loading them, and it can't create one in '"
					+ temporaryDirectory + "'. Mount a writable directory at /tmp, or set -Djava.io.tmpdir to one. "
					+ "See https://github.com/logicsquad/nanocaptcha#running-in-containers", cause);
			exception.addSuppressed(e);
			return exception;
		}
		return new IllegalStateException("NanoCaptcha can't load its font '" + resourceName + "'. This usually means the JDK "
				+ "can't use fonts at all, as in slim and Alpine container images: install fontconfig and a font package, "
				+ "or use a JDK image that includes them. "
				+ "See https://github.com/logicsquad/nanocaptcha#running-in-containers", cause);
	}

	/**
	 * Returns a message saying that {@code font} can't display {@code c}.
	 *
	 * @param font a {@link Font}
	 * @param c    a character {@code font} can't display
	 * @return message
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/38">#38</a>
	 */
	static String cannotDisplay(Font font, char c) {
		return String.format("Font '%s' can't display '%c' (U+%04X).", font.getFontName(), c, (int) c);
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
	public static final class Builder {
		/**
		 * X-axis offset
		 */
		private double xOffset;

		/**
		 * Y-axis offset
		 */
		private double yOffset;

		/**
		 * Whether to choose the y-offset at random for each render
		 */
		private boolean randomYOffset;

		/**
		 * Supplier for {@link Color}
		 */
		private Supplier<Color> colorSupplier;

		/**
		 * Supplier for {@link Font}
		 */
		private Supplier<Font> fontSupplier;

		/**
		 * Constructor
		 */
		public Builder() {
			xOffset = X_OFFSET_DEFAULT;
			yOffset = Y_OFFSET_DEFAULT;
			colorSupplier = DEFAULT_COLOR_SUPPLIER;
			fontSupplier = DEFAULT_FONT_SUPPLIER;
			return;
		}

		/**
		 * Sets y-offset value.
		 *
		 * @param yOffset y-offset (in [0, 1])
		 * @return this
		 */
		public Builder yOffset(double yOffset) {
			this.yOffset = yOffset;
			randomYOffset = false;
			return this;
		}

		/**
		 * Sets x-offset value.
		 *
		 * @param xOffset x-offset (in [0, 1])
		 * @return this
		 */
		public Builder xOffset(double xOffset) {
			this.xOffset = xOffset;
			return this;
		}

		/**
		 * Chooses the y-offset at random each time the renderer renders, anywhere the text fits in the image.
		 *
		 * @return this
		 */
		public Builder randomiseYOffset() {
			randomYOffset = true;
			return this;
		}

		/**
		 * Chooses each glyph's {@link Color} at random from the given {@link Color}s.
		 *
		 * @param color  the first {@link Color}
		 * @param colors additional {@link Color}s (optional)
		 * @return this
		 * @since 2.0
		 */
		public Builder randomColor(Color color, Color... colors) {
			List<Color> colorList = new ArrayList<>();
			colorList.add(color);
			Collections.addAll(colorList, colors);
			return randomColor(colorList);
		}

		/**
		 * Chooses each glyph's {@link Color} at random from {@code colors}. If the list is empty, the colours stay as they
		 * were. The renderer keeps its own copy of the list, so changing {@code colors} afterwards doesn't change the
		 * renderer.
		 *
		 * @param colors the list of {@link Color}s to choose from
		 * @return this
		 * @since 2.0
		 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/64">#64</a>
		 */
		public Builder randomColor(List<Color> colors) {
			if (!colors.isEmpty()) {
				List<Color> copy = new ArrayList<>(colors);
				colorSupplier = () -> copy.get(ThreadLocalRandom.current().nextInt(copy.size()));
			}
			return this;
		}

		/**
		 * Draws every glyph in {@code color}.
		 *
		 * @param color a {@link Color}
		 * @return this
		 * @since 2.0
		 */
		public Builder color(Color color) {
			colorSupplier = () -> color;
			return this;
		}

		/**
		 * Chooses each glyph's {@link Font} at random from the given {@link Font}s.
		 *
		 * @param font  the first {@link Font}
		 * @param fonts additional {@link Font}s (optional)
		 * @return this
		 * @since 2.1
		 */
		public Builder randomFont(Font font, Font... fonts) {
			List<Font> fontList = new ArrayList<>();
			fontList.add(font);
			Collections.addAll(fontList, fonts);
			return randomFont(fontList);
		}

		/**
		 * Chooses each glyph's {@link Font} at random from {@code fonts}. If the list is empty, the fonts stay as they
		 * were. The renderer keeps its own copy of the list, so changing {@code fonts} afterwards doesn't change the
		 * renderer.
		 *
		 * @param fonts the list of {@link Font}s to choose from
		 * @return this
		 * @since 2.1
		 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/64">#64</a>
		 */
		public Builder randomFont(List<Font> fonts) {
			if (!fonts.isEmpty()) {
				List<Font> copy = new ArrayList<>(fonts);
				fontSupplier = () -> copy.get(ThreadLocalRandom.current().nextInt(copy.size()));
			}
			return this;
		}

		/**
		 * Draws every glyph in {@code font}, at its own size.
		 *
		 * @param font a {@link Font}
		 * @return this
		 * @since 2.1
		 */
		public Builder font(Font font) {
			fontSupplier = () -> font;
			return this;
		}

		/**
		 * Builds the renderer described by this {@code Builder}.
		 *
		 * @return new {@link DefaultWordRenderer}
		 */
		public DefaultWordRenderer build() {
			return new DefaultWordRenderer(this);
		}
	}
}
