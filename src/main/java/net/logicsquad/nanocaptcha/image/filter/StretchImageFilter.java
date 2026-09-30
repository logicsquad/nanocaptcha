package net.logicsquad.nanocaptcha.image.filter;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Draws a stretched copy of the image over it. The image keeps its size, so
 * the default x-axis scale of 1.0 and y-axis scale of 3.0 don't make it tall:
 * they stretch the top third of the image to its full height, and draw that
 * over the original.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 * @deprecated An image can't be stretched in place, so this filter draws part
 *             of the image over the rest, which doubles the text rather than
 *             distorting it, and a uniform stretch is easy for OCR to undo.
 *             Use {@link RippleImageFilter} or {@link ShearImageFilter}
 *             instead. This class will be removed in 3.0.
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/57">#57</a>
 */
@Deprecated
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
		// Drawn onto itself, the image would be read from pixels the same draw has already written
		BufferedImage copy = new BufferedImage(image.getColorModel(), image.copyData(null), image.isAlphaPremultiplied(), null);
		Graphics2D g = image.createGraphics();
		AffineTransform at = new AffineTransform();
		at.scale(xScale, yScale);
		g.drawRenderedImage(copy, at);
		g.dispose();
	}
}
