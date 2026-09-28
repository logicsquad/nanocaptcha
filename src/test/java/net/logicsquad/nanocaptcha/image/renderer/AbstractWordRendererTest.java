package net.logicsquad.nanocaptcha.image.renderer;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.content.ContentProducer;
import net.logicsquad.nanocaptcha.content.FiveLetterFirstNameContentProducer;
import net.logicsquad.nanocaptcha.content.LatinContentProducer;
import net.logicsquad.nanocaptcha.content.NumbersContentProducer;

/**
 * Unit tests on {@link AbstractWordRenderer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.2
 */
public class AbstractWordRendererTest {
	@Test
	public void defaultFontsCanDisplayEveryCharacterFromTheBuiltInProducers() {
		List<ContentProducer> producers = Arrays.asList(new LatinContentProducer(), new NumbersContentProducer(),
				new FiveLetterFirstNameContentProducer());
		for (ContentProducer producer : producers) {
			// Enough samples to see every character many times over
			for (int i = 0; i < 1000; i++) {
				for (char c : producer.getContent().toCharArray()) {
					for (Font font : AbstractWordRenderer.DEFAULT_FONTS) {
						assertTrue(font.canDisplay(c), AbstractWordRenderer.cannotDisplay(font, c));
					}
				}
			}
		}
		return;
	}
}
