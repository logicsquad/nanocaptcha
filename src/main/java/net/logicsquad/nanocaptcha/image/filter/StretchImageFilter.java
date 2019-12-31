package net.logicsquad.nanocaptcha.image.filter;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Stretches the given image over the x- and y-axes. If no scale is given,
 * defaults to an x-axis scale of 1.0 and a y-axis scale of 3.0 (i.e. make the
 * image tall but do not affect the width).
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class StretchImageFilter implements ImageFilter {
	/**
	 * Default x-axis multiplier
	 */
	private static final double XDEFAULT = 1.0;

	/**
	 * Default y-axis multiplier
	 */
	private static final double YDEFAULT = 3.0;

	/**
	 * x-axis multiplier
	 */
	private final double xScale;

	/**
	 * y-axis multiplier
	 */
	private final double yScale;

	/**
	 * Constructor using default scale multipliers.
	 */
	public StretchImageFilter() {
		this(XDEFAULT, YDEFAULT);
	}

	/**
	 * Constructor taking x- and y-axis scale multipliers.
	 * 
	 * @param xScale x-axis scale
	 * @param yScale y-axis scale
	 */
	public StretchImageFilter(double xScale, double yScale) {
		this.xScale = xScale;
		this.yScale = yScale;
		return;
	}

	@Override
	public void filter(BufferedImage image) {
		Graphics2D g = image.createGraphics();
		AffineTransform at = new AffineTransform();
		at.scale(xScale, yScale);
		g.drawRenderedImage(image, at);
	}
}
