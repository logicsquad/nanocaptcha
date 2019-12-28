package net.logicsquad.nanocaptcha.gimpy;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageFilter;
import java.awt.image.BufferedImageOp;
import java.awt.image.FilteredImageSource;

/**
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */

public interface GimpyRenderer {
	public void gimp(BufferedImage image);

	static void applyFilter(BufferedImage img, BufferedImageOp filter) {
		FilteredImageSource src = new FilteredImageSource(img.getSource(), new BufferedImageFilter(filter));
		Image fImg = Toolkit.getDefaultToolkit().createImage(src);
		Graphics2D g = img.createGraphics();
		g.drawImage(fImg, 0, 0, null, null);
		g.dispose();
	}
}
