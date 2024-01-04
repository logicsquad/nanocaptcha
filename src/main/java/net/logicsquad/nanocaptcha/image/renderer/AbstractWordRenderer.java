package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for {@link WordRenderer} implementations.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @author <a href="mailto:botyrbojey@gmail.com">bivashy</a>
 * @since 1.4
 */
public abstract class AbstractWordRenderer implements WordRenderer {
	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AbstractWordRenderer.class);

	/**
	 * Resource path to "Courier Prime"
	 */
	private static final String COURIER_PRIME_FONT = "/fonts/CourierPrime-Bold.ttf";

	/**
	 * Resource path to "Public Sans"
	 */
	private static final String PUBLIC_SANS_FONT = "/fonts/PublicSans-Bold.ttf";

    /**
     * Random number generator
     */
    protected static final Random RAND = new Random();

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
    protected static final Supplier<Color> DEFAULT_COLOR_SUPPLIER = () -> DEFAULT_COLORS.get(RAND.nextInt(DEFAULT_COLORS.size()));

    /**
     * Default supplier for {@link Font}
     */
    protected static final Supplier<Font> DEFAULT_FONT_SUPPLIER = () -> DEFAULT_FONTS.get(RAND.nextInt(DEFAULT_FONTS.size()));

	/**
	 * Font size (in points)
	 */
	protected static final int FONT_SIZE = 40;

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
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.colorSupplier = colorSupplier;
		this.fontSupplier = fontSupplier;
		return;
	}

	@Override
	public abstract void render(String word, BufferedImage image);

	/**
	 * Builder for {@code AbstractWordRenderer}.
	 */
	public abstract static class Builder implements net.logicsquad.nanocaptcha.Builder<AbstractWordRenderer> {
		/**
		 * X-axis offset
		 */
		protected double xOffset;

		/**
		 * Y-axis offset
		 */
		protected double yOffset;

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
		 * Selects a random value for y-offset.
		 *
		 * @return this
		 */
		public Builder randomiseYOffset() {
			this.yOffset = Y_OFFSET_MIN + (Y_OFFSET_MAX - Y_OFFSET_MIN) * RAND.nextDouble();
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
		 * made to the current {@link #colorSupplier}.
		 *
		 * @param colors the list of {@link Color}s to choose from
		 * @return this
		 * @since 2.0
		 */
		public Builder randomColor(List<Color> colors) {
			if (!colors.isEmpty()) {
				colorSupplier = () -> colors.get(RAND.nextInt(colors.size()));
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
		 * to the current {@link #fontSupplier}.
		 *
		 * @param fonts the list of {@link Font}s to choose from
		 * @return this
		 * @since 2.1
		 */
		public Builder randomFont(List<Font> fonts) {
			if (!fonts.isEmpty()) {
				fontSupplier = () -> fonts.get(RAND.nextInt(fonts.size()));
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
	 * Returns a {@link Font} loaded from supplied {@code resourceName}, or {@code null} if unable to load the
	 * resource.
	 *
	 * @param resourceName path to resource
	 * @return loaded {@link Font}
	 * @since 1.5
	 */
	private static Font fontFromResource(String resourceName) {
		try (InputStream is = DefaultWordRenderer.class.getResourceAsStream(resourceName)) {
			return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont((long) FONT_SIZE);
		} catch (IOException | FontFormatException e) {
			LOG.error("Unable to load font '{}'.", resourceName, e);
			return null;
		}
	}
}
