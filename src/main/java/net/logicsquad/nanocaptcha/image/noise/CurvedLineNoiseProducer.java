package net.logicsquad.nanocaptcha.image.noise;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Adds a randomly curved line to the image.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class CurvedLineNoiseProducer implements NoiseProducer {
	/**
	 * Random number generator
	 */
	private static final Random RAND = new SecureRandom();

	/**
	 * Default line {@link Color}
	 */
	private static final Color DEFAULT_COLOR = Color.BLACK;

	/**
	 * Default line width
	 */
	private static final float DEFAULT_WIDTH = 3.0f;

	/**
	 * Line {@link Color}
	 */
	private final Color lineColor;

	/**
	 * Line width
	 */
	private final float lineWidth;

	/**
	 * Constructor using default {@link Color} and width.
	 */
	public CurvedLineNoiseProducer() {
		this(DEFAULT_COLOR, DEFAULT_WIDTH);
		return;
	}

	/**
	 * Constructor taking {@link Color} and width.
	 * 
	 * @param lineColor line {@link Color}
	 * @param lineWidth line width
	 */
	public CurvedLineNoiseProducer(Color lineColor, float lineWidth) {
		this.lineColor = lineColor;
		this.lineWidth = lineWidth;
		return;
	}

	@Override
	public void makeNoise(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		// the curve from where the points are taken
		CubicCurve2D cc = new CubicCurve2D.Float(width * .1f, height * RAND.nextFloat(), width * .1f,
				height * RAND.nextFloat(), width * .25f, height * RAND.nextFloat(), width * .9f,
				height * RAND.nextFloat());

		// creates an iterator to define the boundary of the flattened curve
		PathIterator pi = cc.getPathIterator(null, 2);
		Point2D tmp[] = new Point2D[200];
		int i = 0;

		// while pi is iterating the curve, adds points to tmp array
		while (!pi.isDone()) {
			float[] coords = new float[6];
			switch (pi.currentSegment(coords)) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				tmp[i] = new Point2D.Float(coords[0], coords[1]);
			}
			i++;
			pi.next();
		}

		// the points where the line changes the stroke and direction
		Point2D[] pts = new Point2D[i];
		// copies points from tmp to pts
		System.arraycopy(tmp, 0, pts, 0, i);

		Graphics2D graph = (Graphics2D) image.getGraphics();
		graph.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		graph.setColor(lineColor);

		// for the maximum 3 point change the stroke and direction
		for (i = 0; i < pts.length - 1; i++) {
			if (i < 3) {
				graph.setStroke(new BasicStroke(lineWidth));
			}
			graph.drawLine((int) pts[i].getX(), (int) pts[i].getY(), (int) pts[i + 1].getX(), (int) pts[i + 1].getY());
		}
		graph.dispose();
	}
}
