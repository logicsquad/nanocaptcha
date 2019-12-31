package net.logicsquad.nanocaptcha.image.filter;

import java.awt.image.BufferedImage;

import com.jhlabs.image.RippleFilter;

/**
 * Applies a {@link RippleFilter} to the image.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class RippleImageFilter implements ImageFilter {
	@Override
	public void filter(BufferedImage image) {
		RippleFilter filter = new RippleFilter();
		filter.setWaveType(RippleFilter.SINE);
		filter.setXAmplitude(2.6f);
		filter.setYAmplitude(1.7f);
		filter.setXWavelength(15);
		filter.setYWavelength(5);
		ImageFilter.applyFilter(image, filter);
	}
}
