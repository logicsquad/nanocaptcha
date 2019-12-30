package net.logicsquad.nanocaptcha.image.backgrounds;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * A {@link BackgroundProducer} that generates a solid colour background.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public final class FlatColorBackgroundProducer implements BackgroundProducer {
	/**
	 * Default {@link Color}
	 */
	private static final Color DEFAULT_COLOR = Color.GRAY;

	/**
	 * {@link Color} for this producer
	 */
	private final Color color;

	/**
	 * Constructor: creates a {@link BackgroundProducer} that uses the default
	 * {@link Color}.
	 */
	public FlatColorBackgroundProducer() {
		this(DEFAULT_COLOR);
		return;
	}

	/**
	 * Constructor taking a {@link Color}.
	 * 
	 * @param color {@link Color} to set for this {@link BackgroundProducer}
	 */
	public FlatColorBackgroundProducer(Color color) {
		this.color = color;
		return;
	}

	@Override
	public BufferedImage getBackground(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = img.createGraphics();
		graphics.setPaint(color);
		graphics.fill(new Rectangle2D.Double(0, 0, width, height));
		graphics.drawImage(img, 0, 0, null);
		graphics.dispose();
		return img;
	}
}
