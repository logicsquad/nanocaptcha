package net.logicsquad.nanocaptcha.image.filter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Random;

import com.jhlabs.image.ShearFilter;

/**
 * Applies a {@link ShearFilter} to the image.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class ShearImageFilter implements ImageFilter {
	/**
	 * Default {@link Color}
	 */
	private static final Color DEFAULT_COLOR = Color.GRAY;

	/**
	 * Random number generator
	 */
	private static final Random RAND = new SecureRandom();

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
		Graphics2D g = bi.createGraphics();
		shearX(g, bi.getWidth(), bi.getHeight());
		shearY(g, bi.getWidth(), bi.getHeight());
		g.dispose();
	}

	private void shearX(Graphics2D g, int w1, int h1) {
		int period = RAND.nextInt(10) + 5;
		boolean borderGap = true;
		int frames = 15;
		int phase = RAND.nextInt(5) + 2;
		for (int i = 0; i < h1; i++) {
			double d = (period >> 1) * Math.sin((double) i / (double) period + (6.2831853071795862D * phase) / frames);
			g.copyArea(0, i, w1, 1, (int) d, 0);
			if (borderGap) {
				g.setColor(color);
				g.drawLine((int) d, i, 0, i);
				g.drawLine((int) d + w1, i, w1, i);
			}
		}
	}

	private void shearY(Graphics2D g, int w1, int h1) {
		int period = RAND.nextInt(30) + 10; // 50;
		boolean borderGap = true;
		int frames = 15;
		int phase = 7;
		for (int i = 0; i < w1; i++) {
			double d = (period >> 1) * Math.sin((float) i / period + (6.2831853071795862D * phase) / frames);
			g.copyArea(i, 0, 1, h1, 0, (int) d);
			if (borderGap) {
				g.setColor(color);
				g.drawLine(i, (int) d, i, 0);
				g.drawLine(i, (int) d + h1, i, h1);
			}
		}
	}
}
