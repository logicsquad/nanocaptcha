package net.logicsquad.nanocaptcha.image.filter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Applies a shear effect to the image.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class ShearImageFilter implements ImageFilter {
	/**
	 * 2 * pi
	 */
	private static final double TWO_PI = 6.2831853071795862;

	/**
	 * Default {@link Color}
	 */
	private static final Color DEFAULT_COLOR = Color.GRAY;

	/**
	 * {@link Color} to use in filter
	 */
	private final Color color;

	/**
	 * Constructor using default {@link Color}.
	 */
	public ShearImageFilter() {
		this(DEFAULT_COLOR);
		return;
	}

	/**
	 * Constructor taking a {@link Color} for the effect.
	 *
	 * @param color effect {@link Color}
	 */
	public ShearImageFilter(Color color) {
		this.color = color;
		return;
	}

	@Override
	public void filter(BufferedImage bi) {
		filter(bi, ThreadLocalRandom.current());
	}

	/**
	 * Transforms {@code bi} in place, using {@code random}, so that tests can seed it.
	 *
	 * @param bi     a {@link BufferedImage}
	 * @param random a {@link Random}
	 */
	void filter(BufferedImage bi, Random random) {
		Graphics2D g = bi.createGraphics();
		shearX(g, bi, random);
		shearY(g, bi, random);
		g.dispose();
	}

	private void shearX(Graphics2D g, BufferedImage bi, Random random) {
		int w1 = bi.getWidth();
		int h1 = bi.getHeight();
		int period = random.nextInt(10) + 5;
		boolean borderGap = true;
		int frames = 15;
		int phase = random.nextInt(5) + 2;
		for (int i = 0; i < h1; i++) {
			double d = (period >> 1) * Math.sin((double) i / (double) period + (TWO_PI * phase) / frames);
			move(bi, 0, i, w1, 1, (int) d, 0);
			if (borderGap) {
				g.setColor(color);
				g.drawLine((int) d, i, 0, i);
				g.drawLine((int) d + w1, i, w1, i);
			}
		}
	}

	private void shearY(Graphics2D g, BufferedImage bi, Random random) {
		int w1 = bi.getWidth();
		int h1 = bi.getHeight();
		int period = random.nextInt(30) + 10;
		boolean borderGap = true;
		int frames = 15;
		int phase = 7;
		for (int i = 0; i < w1; i++) {
			double d = (period >> 1) * Math.sin((float) i / period + (TWO_PI * phase) / frames);
			move(bi, i, 0, 1, h1, 0, (int) d);
			if (borderGap) {
				g.setColor(color);
				g.drawLine(i, (int) d, i, 0);
				g.drawLine(i, (int) d + h1, i, h1);
			}
		}
	}

	/**
	 * Moves the {@code width} x {@code height} area of {@code image} at ({@code x}, {@code y}) by ({@code dx},
	 * {@code dy}), replacing the pixels it lands on. {@link Graphics2D#copyArea(int, int, int, int, int, int)} draws
	 * over them instead, which leaves the original showing through any transparent pixels, and it copies in place,
	 * which on Alpine kills the JVM when it moves an opaque image left.
	 *
	 * @param image  image
	 * @param x      x-coordinate of area
	 * @param y      y-coordinate of area
	 * @param width  width of area
	 * @param height height of area
	 * @param dx     distance to move right
	 * @param dy     distance to move down
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/84">#84</a>
	 */
	private static void move(BufferedImage image, int x, int y, int width, int height, int dx, int dy) {
		Rectangle bounds = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		// Where the part of the area inside the image ends up, inside the image
		Rectangle target = new Rectangle(x, y, width, height).intersection(bounds);
		target.translate(dx, dy);
		target = target.intersection(bounds);
		if (target.isEmpty()) {
			return;
		}
		WritableRaster raster = image.getRaster();
		Object pixels = raster.getDataElements(target.x - dx, target.y - dy, target.width, target.height, null);
		raster.setDataElements(target.x, target.y, target.width, target.height, pixels);
	}
}
