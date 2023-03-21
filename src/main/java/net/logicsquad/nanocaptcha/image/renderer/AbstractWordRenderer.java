package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for {@link WordRenderer} implementations.
 * 
 * @author paulh
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
	 * Default {@link Color}s
	 */
	protected static final List<Color> DEFAULT_COLORS = new ArrayList<>();

	/**
	 * Default fonts
	 */
	protected static final List<Font> DEFAULT_FONTS = new ArrayList<>();

	// Set up default Colors, Fonts
	static {
		DEFAULT_COLORS.add(Color.BLACK);
		DEFAULT_FONTS.add(fontFromResource(COURIER_PRIME_FONT));
		DEFAULT_FONTS.add(fontFromResource(PUBLIC_SANS_FONT));
	}

	/**
	 * Font size (in points)
	 */
	protected static final int FONT_SIZE = 40;

	/**
	 * Random number generator
	 */
	protected static final Random RAND = new Random();

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
	 * Constructor
	 */
	protected AbstractWordRenderer() {
		this.xOffset = X_OFFSET_DEFAULT;
		this.yOffset = Y_OFFSET_DEFAULT;
		return;
	}

	/**
	 * Constructor taking x- and y-offset overrides
	 * 
	 * @param xOffset x-axis offset
	 * @param yOffset y-axis offset
	 */
	protected AbstractWordRenderer(double xOffset, double yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		return;
	}

	@Override
	public abstract void render(String word, BufferedImage image);

	/**
	 * Builder for {@code AbstractWordRenderer}.
	 */
	public static abstract class Builder implements net.logicsquad.nanocaptcha.Builder<AbstractWordRenderer> {
		/**
		 * X-axis offset
		 */
		protected double xOffset;

		/**
		 * Y-axis offset
		 */
		protected double yOffset;

		/**
		 * Constructor
		 */
		protected Builder() {
			xOffset = X_OFFSET_DEFAULT;
			yOffset = Y_OFFSET_DEFAULT;
			return;
		}

		/**
		 * Sets y-offset value.
		 * 
		 * @param yOffset y-offset (in [0, 1])
		 * @return this
		 */
		public Builder setYOffset(double yOffset) {
			this.yOffset = yOffset;
			return this;
		}

		/**
		 * Sets x-offset value.
		 * 
		 * @param xOffset x-offset (in [0, 1])
		 * @return this
		 */
		public Builder setXOffset(double xOffset) {
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
