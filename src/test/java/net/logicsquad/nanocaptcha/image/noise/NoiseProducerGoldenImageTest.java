package net.logicsquad.nanocaptcha.image.noise;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import net.logicsquad.nanocaptcha.image.GoldenImages;

/**
 * Compares the noise each {@link NoiseProducer} adds to {@link GoldenImages#input()} with its golden image.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.3
 * @see GoldenImages
 */
public class NoiseProducerGoldenImageTest {
	/**
	 * Only on Java 17 and later. Earlier JDKs stroke shapes differently: Java 9 replaced the rasteriser with Marlin,
	 * which has changed since.
	 */
	@Test
	@EnabledForJreRange(min = JRE.JAVA_17)
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
