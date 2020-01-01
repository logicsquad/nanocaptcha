package net.logicsquad.nanocaptcha.image.noise;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

/**
 * Draws a straight line through the given image.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class StraightLineNoiseProducer implements NoiseProducer {
	/**
	 * Random number generator
	 */
	private static final SecureRandom RAND = new SecureRandom();

	/**
	 * Default line {@link Color}
	 */
	private static final Color DEFAULT_COLOR = Color.RED;

	/**
	 * Default line width
	 */
	private static final int DEFAULT_WIDTH = 4;

	/**
	 * Line {@link Color}
	 */
	private final Color lineColor;

	/**
	 * Line width
	 */
	private final int lineWidth;

	/**
	 * Constructor using default values.
	 */
	public StraightLineNoiseProducer() {
		this(DEFAULT_COLOR, DEFAULT_WIDTH);
		return;
	}

	/**
	 * Constructor taking a line {@link Color} and line width.
	 *
	 * @param lineColor line {@link Color}
	 * @param lineWidth line width
	 */
	public StraightLineNoiseProducer(Color lineColor, int lineWidth) {
		this.lineColor = lineColor;
		this.lineWidth = lineWidth;
		return;
	}

	@Override
	public void makeNoise(BufferedImage image) {
		Graphics2D graphics = image.createGraphics();
		int height = image.getHeight();
		int width = image.getWidth();
		int y1 = RAND.nextInt(height) + 1;
		int y2 = RAND.nextInt(height) + 1;
		drawLine(graphics, y1, width, y2);
	}

	private void drawLine(Graphics g, int y1, int x2, int y2) {
		int x1 = 0;

		// The thick line is in fact a filled polygon
		g.setColor(lineColor);
		int dX = x2 - x1;
		int dY = y2 - y1;
		// line length
		double lineLength = Math.sqrt(dX * dX + dY * dY);

		double scale = lineWidth / (2 * lineLength);

		// The x and y increments from an endpoint needed to create a
		// rectangle...
		double ddx = -scale * dY;
		double ddy = scale * dX;
		ddx += ddx > 0 ? 0.5 : -0.5;
		ddy += ddy > 0 ? 0.5 : -0.5;
		int dx = (int) ddx;
		int dy = (int) ddy;

		// Now we can compute the corner points...
		int[] xPoints = new int[4];
		int[] yPoints = new int[4];

		xPoints[0] = x1 + dx;
		yPoints[0] = y1 + dy;
		xPoints[1] = x1 - dx;
		yPoints[1] = y1 - dy;
		xPoints[2] = x2 - dx;
		yPoints[2] = y2 - dy;
		xPoints[3] = x2 + dx;
		yPoints[3] = y2 + dy;

		g.fillPolygon(xPoints, yPoints, 4);
	}
}
