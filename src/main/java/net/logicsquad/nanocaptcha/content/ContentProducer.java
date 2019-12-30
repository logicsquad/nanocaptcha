package net.logicsquad.nanocaptcha.content;

/**
 * Object that can generate text content for a CAPTCHA.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public interface ContentProducer {
	/**
	 * Returns a string of characters to be used as the answer for the CAPTCHA.
	 * 
	 * @return CAPTCHA content
	 */
	String getContent();
}
