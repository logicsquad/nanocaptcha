package net.logicsquad.nanocaptcha.image.filter;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageFilter;
import java.awt.image.BufferedImageOp;
import java.awt.image.FilteredImageSource;

/**
 * A filter that can distort an image CAPTCHA in some way.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public interface ImageFilter {
	/**
	 * Transforms {@code image} in-place.
	 * 
	 * @param image {@link BufferedImage} to transform
	 */
	void filter(BufferedImage image);

	/**
	 * Applies {@code filter} to {@code img}.
	 * 
	 * @param img    a {@link BufferedImage}
	 * @param filter a {@link BufferedImageOp}
	 */
	static void applyFilter(BufferedImage img, BufferedImageOp filter) {
		FilteredImageSource src = new FilteredImageSource(img.getSource(), new BufferedImageFilter(filter));
		Image fImg = Toolkit.getDefaultToolkit().createImage(src);
		Graphics2D g = img.createGraphics();
		g.drawImage(fImg, 0, 0, null, null);
		g.dispose();
	}
}
