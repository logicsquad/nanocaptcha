package net.logicsquad.nanocaptcha.image.filter;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Ripples the image: shifts each row sideways, and each column up or down, along a sine wave. The waves' phase, height
 * and length vary a little with each image, so that a ripple can't be undone by knowing it in advance.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/96">#96</a>
 */
public final class RippleImageFilter implements ImageFilter {
	/**
	 * Furthest a row moves sideways, either way (in pixels)
	 */
	private static final double ROW_SHIFT = 2.6;

	/**
	 * Distance down the image over which the rows' shifts repeat (in pixels)
	 */
	private static final double ROW_PERIOD = 2 * Math.PI * 15;

	/**
	 * Furthest a column moves up or down, either way (in pixels)
	 */
	private static final double COLUMN_SHIFT = 1.7;

	/**
	 * Distance across the image over which the columns' shifts repeat (in pixels)
	 */
	private static final double COLUMN_PERIOD = 2 * Math.PI * 5;

	/**
	 * Largest change to a wave's height or length, either way, as a proportion
	 */
	private static final double MAX_VARIATION = 0.1;

	@Override
	public void filter(BufferedImage image) {
		filter(image, ThreadLocalRandom.current());
	}

	/**
	 * Ripples {@code image}, using {@code random}, so that tests can seed it.
	 *
	 * @param image  image to ripple
	 * @param random a {@link Random}
	 */
	void filter(BufferedImage image, Random random) {
		Wave rows = new Wave(ROW_SHIFT, ROW_PERIOD, random);
		Wave columns = new Wave(COLUMN_SHIFT, COLUMN_PERIOD, random);
		int width = image.getWidth();
		int height = image.getHeight();
		double[] columnShifts = new double[width];
		for (int x = 0; x < width; x++) {
			columnShifts[x] = columns.at(x);
		}
		int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
		int[] rippled = new int[pixels.length];
		for (int y = 0; y < height; y++) {
			double rowShift = rows.at(y);
			for (int x = 0; x < width; x++) {
				// Each pixel takes its colour from the point the waves move to it
				rippled[y * width + x] = sample(pixels, width, height, x + rowShift, y + columnShifts[x]);
			}
		}
		image.setRGB(0, 0, width, height, rippled, 0, width);
		return;
	}

	/**
	 * Returns the colour at ({@code x}, {@code y}) in an image's {@code pixels}, blended from the four pixels around
	 * it. Outside the image, those pixels are transparent, in the colour of the nearest edge pixel, so that blending
	 * with them fades the edge rather than darkening it.
	 *
	 * @param pixels ARGB pixels, row by row
	 * @param width  image width
	 * @param height image height
	 * @param x      x-coordinate
	 * @param y      y-coordinate
	 * @return ARGB colour
	 */
	private static int sample(int[] pixels, int width, int height, double x, double y) {
		int left = (int) Math.floor(x);
		int top = (int) Math.floor(y);
		// How far the point is towards the pixels on the right and below
		double across = x - left;
		double down = y - top;
		int topLeft = pixel(pixels, width, height, left, top);
		int topRight = pixel(pixels, width, height, left + 1, top);
		int bottomLeft = pixel(pixels, width, height, left, top + 1);
		int bottomRight = pixel(pixels, width, height, left + 1, top + 1);
		int argb = 0;
		for (int shift = 0; shift < 32; shift += 8) {
			double upper = (1 - across) * (topLeft >>> shift & 0xff) + across * (topRight >>> shift & 0xff);
			double lower = (1 - across) * (bottomLeft >>> shift & 0xff) + across * (bottomRight >>> shift & 0xff);
			argb |= (int) Math.round((1 - down) * upper + down * lower) << shift;
		}
		return argb;
	}

	/**
	 * Returns the pixel at ({@code x}, {@code y}) in an image's {@code pixels}, or, outside the image, the nearest edge
	 * pixel made transparent.
	 *
	 * @param pixels ARGB pixels, row by row
	 * @param width  image width
	 * @param height image height
	 * @param x      x-coordinate
	 * @param y      y-coordinate
	 * @return ARGB colour
	 */
	private static int pixel(int[] pixels, int width, int height, int x, int y) {
		int edgeX = Math.max(0, Math.min(width - 1, x));
		int edgeY = Math.max(0, Math.min(height - 1, y));
		int argb = pixels[edgeY * width + edgeX];
		return edgeX == x && edgeY == y ? argb : argb & 0x00ffffff;
	}

	/**
	 * A sine wave with a random phase, and a height and length a little different from the ones it's given.
	 */
	private static final class Wave {
		/**
		 * Height (in pixels)
		 */
		private final double amplitude;

		/**
		 * Length (in pixels)
		 */
		private final double period;

		/**
		 * Phase (in radians)
		 */
		private final double phase;

		/**
		 * Constructor
		 *
		 * @param amplitude height (in pixels), before it's varied
		 * @param period    length (in pixels), before it's varied
		 * @param random    a {@link Random}
		 */
		private Wave(double amplitude, double period, Random random) {
			this.amplitude = amplitude * (1 + (2 * random.nextDouble() - 1) * MAX_VARIATION);
			this.period = period * (1 + (2 * random.nextDouble() - 1) * MAX_VARIATION);
			phase = 2 * Math.PI * random.nextDouble();
			return;
		}

		/**
		 * Returns the wave's height at {@code position}.
		 *
		 * @param position distance along the wave (in pixels)
		 * @return height (in pixels)
		 */
		private double at(int position) {
			return amplitude * Math.sin(2 * Math.PI * position / period + phase);
		}
	}
}
