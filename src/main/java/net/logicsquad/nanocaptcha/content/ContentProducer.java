package net.logicsquad.nanocaptcha.content;

/**
 * Generate an answer for the CAPTCHA.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public interface ContentProducer {

	/**
	 * Generate a series of characters to be used as the answer for the CAPTCHA.
	 * 
	 * @return The answer for the CAPTCHA.
	 */
	public String getContent();
}
