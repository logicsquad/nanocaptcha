package net.logicsquad.nanocaptcha.image.filter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.image.GoldenImages;

/**
 * Compares the result of applying each {@link ImageFilter} to {@link GoldenImages#input()} with its golden image.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.3
 * @see GoldenImages
 */
public class ImageFilterGoldenImageTest {
	@Test
	public void fishEyeImageFilterMatchesGoldenImage() throws IOException {
		BufferedImage image = GoldenImages.input();
		new FishEyeImageFilter().filter(image, new Random(GoldenImages.SEED));
		GoldenImages.assertMatches("filter/FishEyeImageFilter", image);
		return;
	}

	@Test
	public void rippleImageFilterMatchesGoldenImage() throws IOException {
		BufferedImage image = GoldenImages.input();
		new RippleImageFilter().filter(image);
		GoldenImages.assertMatches("filter/RippleImageFilter", image);
		return;
	}

	@Test
	public void shearImageFilterMatchesGoldenImage() throws IOException {
		BufferedImage image = GoldenImages.input();
		new ShearImageFilter().filter(image, new Random(GoldenImages.SEED));
		GoldenImages.assertMatches("filter/ShearImageFilter", image);
		return;
	}
}
