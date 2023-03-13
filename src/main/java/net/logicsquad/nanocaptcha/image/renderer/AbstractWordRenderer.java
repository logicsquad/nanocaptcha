package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Superclass for {@link WordRenderer} implementations.
 * 
 * @author paulh
 * @since 1.4
 */
public abstract class AbstractWordRenderer implements WordRenderer {
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
}
