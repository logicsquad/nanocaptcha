package net.logicsquad.nanocaptcha.image.backgrounds;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public final class FlatColorBackgroundProducer implements BackgroundProducer {

	private Color _color = Color.GRAY;

	public FlatColorBackgroundProducer() {
		this(Color.GRAY);
	}

	public FlatColorBackgroundProducer(Color color) {
		_color = color;
	}

	@Override
	public BufferedImage getBackground(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D graphics = img.createGraphics();
		graphics.setPaint(_color);
		graphics.fill(new Rectangle2D.Double(0, 0, width, height));
		graphics.drawImage(img, 0, 0, null);
		graphics.dispose();

		return img;
	}
}
