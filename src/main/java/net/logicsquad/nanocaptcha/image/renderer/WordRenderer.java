package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.image.BufferedImage;

/**
 * Renders the content for the CAPTCHA onto the image.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public interface WordRenderer {
	/**
	 * Renders {@code word} to a {@link BufferedImage}.
	 *
	 * @param word  string to be rendered
	 * @param image image onto which the word will be rendered
	 */
	void render(String word, BufferedImage image);
}
