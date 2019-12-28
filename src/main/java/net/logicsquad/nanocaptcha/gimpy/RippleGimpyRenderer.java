package net.logicsquad.nanocaptcha.gimpy;

import java.awt.image.BufferedImage;

import com.jhlabs.image.RippleFilter;

/**
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 */
public class RippleGimpyRenderer implements GimpyRenderer {

    /**
     * Apply a RippleFilter to the image.
     * 
     * @param image The image to be distorted
     */
	@Override
    public void gimp(BufferedImage image) {
        RippleFilter filter = new RippleFilter();
        filter.setWaveType(RippleFilter.SINE);
        filter.setXAmplitude(2.6f);
        filter.setYAmplitude(1.7f);
        filter.setXWavelength(15);
        filter.setYWavelength(5);
        GimpyRenderer.applyFilter(image, filter);
    }
}