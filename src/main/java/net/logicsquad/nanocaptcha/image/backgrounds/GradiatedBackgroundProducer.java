package net.logicsquad.nanocaptcha.image.backgrounds;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Creates a gradiated background between two {@link Color} values. Default
 * {@link Color}s are dark gray and white.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class GradiatedBackgroundProducer implements BackgroundProducer {
	/**
	 * Default from {@link Color}
	 */
	private static final Color DEFAULT_FROM_COLOR = Color.DARK_GRAY;

	/**
	 * Default to {@link Color}
	 */
	private static final Color DEFAULT_TO_COLOR = Color.WHITE;

	/**
	 * From {@link Color} for this producer
	 */
	private final Color fromColor;

	/**
	 * To {@link Color} for this producer
	 */
	private final Color toColor;

	/**
	 * Constructor creating a producer with default {@link Color}s.
	 */
	public GradiatedBackgroundProducer() {
		this(DEFAULT_FROM_COLOR, DEFAULT_TO_COLOR);
		return;
	}

	/**
	 * Constructor taking from and to {@link Color}s.
	 * 
	 * @param fromColor from {@link Color}
	 * @param toColor   to {@link Color}
	 */
	public GradiatedBackgroundProducer(Color fromColor, Color toColor) {
		this.fromColor = fromColor;
		this.toColor = toColor;
		return;
	}

	@Override
	public BufferedImage getBackground(int width, int height) {
		// create an opaque image
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D g = img.createGraphics();
		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setRenderingHints(hints);

		// create the gradient color
		GradientPaint ytow = new GradientPaint(0, 0, fromColor, width, height, toColor);

		g.setPaint(ytow);
		// draw gradient color
		g.fill(new Rectangle2D.Double(0, 0, width, height));

		// draw the transparent image over the background
		g.drawImage(img, 0, 0, null);
		g.dispose();

		return img;
	}
}
