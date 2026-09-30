package net.logicsquad.nanocaptcha.image.renderer;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

	@Test
	public void cannotLoadFontBlamesATemporaryDirectoryItCantWriteTo(@TempDir Path directory) {
		// A missing directory, since a read-only one is still writable for root, as in the container tests
		Path missing = directory.resolve("missing");
		IllegalStateException e = AbstractWordRenderer.cannotLoadFont("/fonts/Example.ttf", new IOException("Problem reading font data."),
				missing);
		assertTrue(e.getMessage().contains("can't create one in '" + missing + "'"), e.getMessage());
		assertTrue(e.getMessage().contains("-Djava.io.tmpdir"), e.getMessage());
		assertEquals(1, e.getSuppressed().length);
		return;
	}

	@Test
	public void cannotLoadFontBlamesFontSupportOtherwise(@TempDir Path directory) throws IOException {
		IllegalStateException e = AbstractWordRenderer.cannotLoadFont("/fonts/Example.ttf", new IOException("Problem reading font data."),
				directory);
		assertTrue(e.getMessage().contains("fontconfig"), e.getMessage());
		// The check leaves nothing behind
		try (Stream<Path> files = Files.list(directory)) {
			assertEquals(0, files.count());
		}
		return;
	}
}
