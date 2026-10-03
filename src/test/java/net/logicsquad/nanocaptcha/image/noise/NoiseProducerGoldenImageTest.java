package net.logicsquad.nanocaptcha.image.noise;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.image.GoldenImages;

/**
 * Compares the noise each {@link NoiseProducer} adds to {@link GoldenImages#input()} with its golden image.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.3
 * @see GoldenImages
 */
public class NoiseProducerGoldenImageTest {
	@Test
	public void curvedLineNoiseProducerMatchesGoldenImage() throws IOException {
		BufferedImage image = GoldenImages.input();
		new CurvedLineNoiseProducer().makeNoise(image, new Random(GoldenImages.SEED));
		GoldenImages.assertMatches("noise/CurvedLineNoiseProducer", image);
		return;
	}

	@Test
	public void gaussianNoiseProducerMatchesGoldenImage() throws IOException {
		BufferedImage image = GoldenImages.input();
		new GaussianNoiseProducer().makeNoise(image, new Random(GoldenImages.SEED));
		GoldenImages.assertMatches("noise/GaussianNoiseProducer", image);
		return;
	}

	@Test
	public void saltAndPepperNoiseProducerMatchesGoldenImage() throws IOException {
		BufferedImage image = GoldenImages.input();
		new SaltAndPepperNoiseProducer().makeNoise(image, new Random(GoldenImages.SEED));
		GoldenImages.assertMatches("noise/SaltAndPepperNoiseProducer", image);
		return;
	}

	@Test
	public void straightLineNoiseProducerMatchesGoldenImage() throws IOException {
		BufferedImage image = GoldenImages.input();
		new StraightLineNoiseProducer().makeNoise(image, new Random(GoldenImages.SEED));
		GoldenImages.assertMatches("noise/StraightLineNoiseProducer", image);
		return;
	}
}
