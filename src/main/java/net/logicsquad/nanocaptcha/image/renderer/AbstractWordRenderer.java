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
     * Default supplier of the {@link Color}
     */
    protected static final Supplier<Color> DEFAULT_COLOR_SUPPLIER = () -> DEFAULT_COLORS.get(RAND.nextInt(DEFAULT_COLORS.size()));

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
	 * Constructor
	 */
	protected AbstractWordRenderer() {
		this.xOffset = X_OFFSET_DEFAULT;
		this.yOffset = Y_OFFSET_DEFAULT;
		this.colorSupplier = DEFAULT_COLOR_SUPPLIER;
		return;
	}

	/**
	 * Constructor taking x- and y-offset overrides
	 *
	 * @param xOffset       x-axis offset
	 * @param yOffset       y-axis offset
	 * @param colorSupplier {@link Color} supplier
	 */
	protected AbstractWordRenderer(double xOffset, double yOffset, Supplier<Color> colorSupplier) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
        this.colorSupplier = colorSupplier;
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
         * Supplier of {@link Color}
         */
        protected Supplier<Color> colorSupplier;

		/**
		 * Constructor
		 */
		protected Builder() {
			xOffset = X_OFFSET_DEFAULT;
			yOffset = Y_OFFSET_DEFAULT;
            colorSupplier = DEFAULT_COLOR_SUPPLIER;
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
		 * Sets the supplier to randomly select a color from the given colors.
		 *
		 * @param color the first color
		 * @param colors additional colors (optional)
		 * @return this
		 * @since 2.0
		 */
		public Builder randomColor(Color color, Color... colors) {
			List<Color> colorList = new ArrayList<>();
			colorList.add(color);
			Collections.addAll(colorList, colors);
			randomColor(colorList);
			return this;
		}

		/**
		 * Assigns a supplier that randomly selects a color from the provided list.
		 * If the list is empty, no changes are made to the current color supplier.
		 *
		 * @param colors the list of colors to choose from
		 * @return this
		 * @since 2.0
		 */
		public Builder randomColor(List<Color> colors) {
			if (!colors.isEmpty()) {
				this.colorSupplier = () -> colors.get(RAND.nextInt(colors.size()));
			}
			return this;
		}

		/**
		 * Sets the supplier to provide a specified color.
		 *
		 * @param color the color to be provided by the supplier
		 * @return this
		 * @since 2.0
		 */
		public Builder color(Color color) {
			this.colorSupplier = () -> color;
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
