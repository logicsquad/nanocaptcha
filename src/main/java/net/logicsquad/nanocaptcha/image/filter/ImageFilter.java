package net.logicsquad.nanocaptcha.image.filter;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * A filter that can distort an image CAPTCHA in some way.
 * <p>
 * An {@link net.logicsquad.nanocaptcha.image.ImageCaptcha.Factory ImageCaptcha.Factory} shares an implementation
 * between every CAPTCHA it makes, on whichever threads ask, so it has to be thread-safe. NanoCaptcha's own keep no
 * state that changes, and take any randomness from {@link java.util.concurrent.ThreadLocalRandom} on each call.
 * </p>
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
	 * Applies {@code filter} to {@code img}, replacing its pixels with the filtered ones.
	 *
	 * @param img    a {@link BufferedImage}
	 * @param filter a {@link BufferedImageOp}
	 */
	static void applyFilter(BufferedImage img, BufferedImageOp filter) {
		BufferedImage filtered = filter.filter(img, null);
		Graphics2D g = img.createGraphics();
		// Drawn over the original, the filtered image would leave the original showing wherever it's transparent
		g.setComposite(AlphaComposite.Src);
		g.drawImage(filtered, 0, 0, null);
		g.dispose();
	}
}
