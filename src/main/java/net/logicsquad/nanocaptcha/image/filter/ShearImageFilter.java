package net.logicsquad.nanocaptcha.image.filter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
		shearX(g, bi.getWidth(), bi.getHeight(), random);
		shearY(g, bi.getWidth(), bi.getHeight(), random);
		g.dispose();
	}

	private void shearX(Graphics2D g, int w1, int h1, Random random) {
		int period = random.nextInt(10) + 5;
		boolean borderGap = true;
		int frames = 15;
		int phase = random.nextInt(5) + 2;
		for (int i = 0; i < h1; i++) {
			double d = (period >> 1) * Math.sin((double) i / (double) period + (TWO_PI * phase) / frames);
			g.copyArea(0, i, w1, 1, (int) d, 0);
			if (borderGap) {
				g.setColor(color);
				g.drawLine((int) d, i, 0, i);
				g.drawLine((int) d + w1, i, w1, i);
			}
		}
	}

	private void shearY(Graphics2D g, int w1, int h1, Random random) {
		int period = random.nextInt(30) + 10;
		boolean borderGap = true;
		int frames = 15;
		int phase = 7;
		for (int i = 0; i < w1; i++) {
			double d = (period >> 1) * Math.sin((float) i / period + (TWO_PI * phase) / frames);
			g.copyArea(i, 0, 1, h1, 0, (int) d);
			if (borderGap) {
				g.setColor(color);
				g.drawLine(i, (int) d, i, 0);
				g.drawLine(i, (int) d + h1, i, h1);
			}
		}
	}
}
