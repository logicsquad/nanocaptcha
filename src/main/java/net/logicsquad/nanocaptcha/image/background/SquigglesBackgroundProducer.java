package net.logicsquad.nanocaptcha.image.background;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Draws a row of overlapping dashed ellipses. Their size, spacing and dashes, and where they start, vary a little with
 * each background, so that one background can't be subtracted from another.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/94">#94</a>
 */
public class SquigglesBackgroundProducer implements BackgroundProducer {
	/**
	 * Alpha value of background
	 */
	private static final float ALPHA = 0.75f;

	/**
	 * Length of each dash, and of each gap between them
	 */
	private static final float DASH = 2.0f;

	/**
	 * Smallest distance between one ellipse and the next (in pixels)
	 */
	private static final double MIN_SPACING = 4.0;

	/**
	 * Largest distance between one ellipse and the next (in pixels)
	 */
	private static final double MAX_SPACING = 6.0;

	/**
	 * Largest change to the ellipses' width or height, either way, as a proportion of the image's
	 */
	private static final double MAX_SCALE = 0.1;

	@Override
	public BufferedImage getBackground(int width, int height) {
		return getBackground(width, height, ThreadLocalRandom.current());
	}

	/**
	 * Returns a background {@code width} x {@code height} pixels, using {@code random}, so that tests can seed it.
	 *
	 * @param width  image width
	 * @param height image height
	 * @param random a {@link Random}
	 * @return background
	 */
	BufferedImage getBackground(int width, int height, Random random) {
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = result.createGraphics();

		// Dashes that start anywhere in their pattern
		BasicStroke bs = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f,
				new float[] { DASH, DASH }, 2 * DASH * random.nextFloat());
		graphics.setStroke(bs);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ALPHA);
		graphics.setComposite(ac);

		// Ellipses about the size of the image, a few pixels apart, from just off the left-hand edge to the right
		double spacing = MIN_SPACING + (MAX_SPACING - MIN_SPACING) * random.nextDouble();
		double ellipseWidth = width * (1 + (2 * random.nextDouble() - 1) * MAX_SCALE);
		double ellipseHeight = height * (1 + (2 * random.nextDouble() - 1) * MAX_SCALE);
		double y = (height - ellipseHeight) * random.nextDouble();
		Arc2D arc = new Arc2D.Double();
		for (double x = -ellipseWidth - spacing * random.nextDouble(); x < width; x += spacing) {
			arc.setArc(x, y, ellipseWidth, ellipseHeight, 0.0, 360.0, Arc2D.OPEN);
			graphics.draw(arc);
		}
		graphics.dispose();
		return result;
	}
}
