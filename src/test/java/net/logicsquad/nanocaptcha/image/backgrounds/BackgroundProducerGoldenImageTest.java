package net.logicsquad.nanocaptcha.image.backgrounds;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import net.logicsquad.nanocaptcha.image.GoldenImages;

/**
 * Compares each {@link BackgroundProducer} with its golden image.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.3
 * @see GoldenImages
 */
public class BackgroundProducerGoldenImageTest {
	@Test
	public void flatColorBackgroundProducerMatchesGoldenImage() throws IOException {
		assertMatches(new FlatColorBackgroundProducer());
		return;
	}

	@Test
	public void gradiatedBackgroundProducerMatchesGoldenImage() throws IOException {
		assertMatches(new GradiatedBackgroundProducer());
		return;
	}

	/**
	 * Only on Java 17 and later. Earlier JDKs stroke shapes differently: Java 9 replaced the rasteriser with Marlin,
	 * which has changed since.
	 */
	@Test
	@EnabledForJreRange(min = JRE.JAVA_17)
	public void squigglesBackgroundProducerMatchesGoldenImage() throws IOException {
		assertMatches(new SquigglesBackgroundProducer());
		return;
	}

	@Test
	public void transparentBackgroundProducerMatchesGoldenImage() throws IOException {
		assertMatches(new TransparentBackgroundProducer());
		return;
	}

	/**
	 * Asserts that {@code backgroundProducer}'s background, at the default size of 200 x 50, matches its golden image.
	 *
	 * @param backgroundProducer a {@link BackgroundProducer}
	 * @throws IOException if the golden image can't be read or written
	 */
	private static void assertMatches(BackgroundProducer backgroundProducer) throws IOException {
		GoldenImages.assertMatches("backgrounds/" + backgroundProducer.getClass().getSimpleName(),
				backgroundProducer.getBackground(200, 50));
		return;
	}
}
