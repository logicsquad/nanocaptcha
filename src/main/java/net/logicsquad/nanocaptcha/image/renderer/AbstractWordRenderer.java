package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Superclass for {@link WordRenderer} implementations.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @author <a href="mailto:botyrbojey@gmail.com">bivashy</a>
 * @since 1.4
 */
public abstract class AbstractWordRenderer implements WordRenderer {
	/**
	 * Resource path to "Courier Prime"
	 */
	private static final String COURIER_PRIME_FONT = "/net/logicsquad/nanocaptcha/fonts/CourierPrime-Bold.ttf";

	/**
	 * Resource path to "Public Sans"
	 */
	private static final String PUBLIC_SANS_FONT = "/net/logicsquad/nanocaptcha/fonts/PublicSans-Bold.ttf";

	/**
	 * Default {@link Color}s
	 */
	protected static final List<Color> DEFAULT_COLORS;

	/**
	 * Default fonts
	 */
	protected static final List<Font> DEFAULT_FONTS;

	// Set up default Colors, Fonts
	static {
		List<Color> defaultColors = Arrays.asList(Color.BLACK);
		DEFAULT_COLORS = Collections.unmodifiableList(defaultColors);
		List<Font> defaultFonts = Arrays.asList(fontFromResource(COURIER_PRIME_FONT), fontFromResource(PUBLIC_SANS_FONT));
		DEFAULT_FONTS = Collections.unmodifiableList(defaultFonts);
	}

    /**
     * Default supplier for {@link Color}
     */
    protected static final Supplier<Color> DEFAULT_COLOR_SUPPLIER = () -> DEFAULT_COLORS.get(ThreadLocalRandom.current().nextInt(DEFAULT_COLORS.size()));

    /**
     * Default supplier for {@link Font}
     */
    protected static final Supplier<Font> DEFAULT_FONT_SUPPLIER = () -> DEFAULT_FONTS.get(ThreadLocalRandom.current().nextInt(DEFAULT_FONTS.size()));

	/**
	 * Font size (in points) of {@link #DEFAULT_FONTS}. {@link DefaultWordRenderer} sizes them to the image instead: this
	 * size in an image of the default height, 50 pixels, and in proportion otherwise.
	 */
	protected static final int FONT_SIZE = 40;

	/**
	 * Height of image (in pixels) that {@link #FONT_SIZE} suits
	 */
	private static final int FONT_SIZE_HEIGHT = 50;

	/**
	 * Default percentage offset along x-axis
	 */
	protected static final double X_OFFSET_DEFAULT = 0.05;

	/**
	 * Default percentage offset along y-axis
	 */
	protected static final double Y_OFFSET_DEFAULT = 0.25;

	/**
	 * Minimum for y-offset if randomised
	 */
	private static final double Y_OFFSET_MIN = 0.0;

	/**
	 * Maximum for y-offset if randomised
	 */
	private static final double Y_OFFSET_MAX = 0.75;

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
	 * Constructor taking x- and y-offset overrides
	 *
	 * @param xOffset       x-axis offset
	 * @param yOffset       y-axis offset
	 * @param colorSupplier {@link Color} supplier
	 * @param fontSupplier  {@link Font} supplier
	 */
	protected AbstractWordRenderer(double xOffset, double yOffset, Supplier<Color> colorSupplier, Supplier<Font> fontSupplier) {
		this(xOffset, yOffset, false, colorSupplier, fontSupplier);
		return;
	}

	/**
	 * Constructor taking its settings from a {@link Builder}
	 *
	 * @param builder a {@link Builder}
	 * @since 2.3
	 */
	AbstractWordRenderer(Builder builder) {
		this(builder.xOffset, builder.yOffset, builder.randomYOffset, builder.colorSupplier, builder.fontSupplier);
		return;
	}

	/**
	 * Constructor taking every setting
	 *
	 * @param xOffset       x-axis offset
	 * @param yOffset       y-axis offset
	 * @param randomYOffset whether to choose the y-offset at random for each render
	 * @param colorSupplier {@link Color} supplier
	 * @param fontSupplier  {@link Font} supplier
	 */
	private AbstractWordRenderer(double xOffset, double yOffset, boolean randomYOffset, Supplier<Color> colorSupplier,
			Supplier<Font> fontSupplier) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.randomYOffset = randomYOffset;
		this.colorSupplier = colorSupplier;
		this.fontSupplier = fontSupplier;
		this.defaultFonts = fontSupplier == DEFAULT_FONT_SUPPLIER;
		return;
	}

	@Override
	public abstract void render(String word, BufferedImage image);

	/**
	 * Builder for {@code AbstractWordRenderer}.
	 */
	public abstract static class Builder {
		/**
		 * X-axis offset
		 */
		protected double xOffset;

		/**
		 * Y-axis offset
		 */
		protected double yOffset;

		/**
		 * Whether to choose the y-offset at random for each render
		 */
		boolean randomYOffset;

        /**
         * Supplier for {@link Color}
         */
        protected Supplier<Color> colorSupplier;

        /**
         * Supplier for {@link Font}
         */
        protected Supplier<Font> fontSupplier;

		/**
		 * Constructor
		 */
		protected Builder() {
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
		 * Selects a random value for y-offset. {@link DefaultWordRenderer} chooses a new one each time it renders,
		 * anywhere the text fits in the image. For other subclasses, {@link AbstractWordRenderer#yOffset()} returns a value
		 * chosen here, between 0 and 0.75.
		 *
		 * @return this
		 */
		public Builder randomiseYOffset() {
			this.yOffset = Y_OFFSET_MIN + (Y_OFFSET_MAX - Y_OFFSET_MIN) * ThreadLocalRandom.current().nextDouble();
			randomYOffset = true;
			return this;
		}

		/**
		 * Sets {@link #colorSupplier} to randomly select a {@link Color} from the given {@link Color}s.
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
		 * Sets {@link #colorSupplier} to randomly select a {@link Color} from the provided {@code colors}. If the list is empty, no changes are
		 * made to the current {@link #colorSupplier}. The renderer keeps its own copy of the list, so changing
		 * {@code colors} afterwards doesn't change the renderer.
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
		 * Sets {@link #colorSupplier} to provide a specified {@link Color}.
		 *
		 * @param color the {@link Color} to be supplied by {@link #colorSupplier}
		 * @return this
		 * @since 2.0
		 */
		public Builder color(Color color) {
			colorSupplier = () -> color;
			return this;
		}

		/**
		 * Sets {@link #fontSupplier} to randomly select a {@link Font} from the given {@link Font}s.
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
		 * Sets {@link #fontSupplier} to randomly select a {@link Font} from the provided {@code fonts}. If the list is empty, no changes are made
		 * to the current {@link #fontSupplier}. The renderer keeps its own copy of the list, so changing {@code fonts}
		 * afterwards doesn't change the renderer.
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
		 * Sets {@link #fontSupplier} to provide a specified {@link Font}.
		 *
		 * @param font the {@link Font} to be supplied by {@link #fontSupplier}
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
		 * @return new renderer
		 */
		public abstract AbstractWordRenderer build();
	}

	/**
	 * Returns x-axis offset.
	 *
	 * @return x-axis offset
	 */
	protected double xOffset() {
		return xOffset;
	}

	/**
	 * Returns y-axis offset.
	 *
	 * @return y-axis offset
	 */
	protected double yOffset() {
		return yOffset;
	}

	/**
	 * Returns whether to choose the y-offset at random for each render.
	 *
	 * @return {@code true} if the y-offset is random
	 * @since 2.3
	 */
	boolean randomYOffset() {
		return randomYOffset;
	}

	/**
	 * Returns whether this renderer uses {@link #DEFAULT_FONTS}, which it sizes to the image.
	 *
	 * @return {@code true} if the fonts are the defaults
	 * @since 2.3
	 */
	boolean defaultFonts() {
		return defaultFonts;
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
	 * Returns {@link Color} supplier.
	 *
	 * @return {@link Color} supplier
	 * @since 2.0
	 */
	protected Supplier<Color> colorSupplier() {
		return colorSupplier;
	}

	/**
	 * Returns {@link Font} supplier.
	 * 
	 * @return {@link Font} supplier
	 * @since 2.1
	 */
	protected Supplier<Font> fontSupplier() {
		return fontSupplier;
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
}
