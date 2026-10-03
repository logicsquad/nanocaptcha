import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import net.logicsquad.nanocaptcha.content.LatinContentProducer;
import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import net.logicsquad.nanocaptcha.image.background.GradiatedBackgroundProducer;
import net.logicsquad.nanocaptcha.image.noise.CurvedLineNoiseProducer;
import net.logicsquad.nanocaptcha.image.renderer.DefaultWordRenderer;

/**
 * Makes the sample CAPTCHAs the README shows, in {@code docs/samples}. Run it from the project root, after building:
 *
 * <pre>
 * mvn compile
 * java -cp target/classes scripts/GenerateSamples.java
 * </pre>
 *
 * <p>
 * Each run makes new CAPTCHAs, so run it when the way they look changes, and look at them before committing them.
 * </p>
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/97">#97</a>
 */
public class GenerateSamples {
	public static void main(String[] args) throws IOException {
		System.setProperty("java.awt.headless", "true");
		Path samples = Path.of("docs", "samples");
		Files.createDirectories(samples);
		write(samples.resolve("create.png"), ImageCaptcha.create());
		write(samples.resolve("noisy.png"),
				new ImageCaptcha.Factory.Builder(200, 50).addContent().addNoise().addFilter().addBorder().build().create());
		// The README's custom example
		ImageCaptcha.Factory captchas = new ImageCaptcha.Factory.Builder(400, 100)
				.addContent(new LatinContentProducer(7),
						new DefaultWordRenderer.Builder()
								.randomColor(Color.BLACK, Color.BLUE, Color.CYAN, Color.RED)
								.build())
				.addBackground(new GradiatedBackgroundProducer())
				.addNoise(new CurvedLineNoiseProducer())
				.build();
		write(samples.resolve("custom.png"), captchas.create());
		return;
	}

	/**
	 * Writes {@code captcha} to {@code path} as a PNG file.
	 *
	 * @param path    where to write it
	 * @param captcha an {@link ImageCaptcha}
	 * @throws IOException if the file can't be written
	 */
	private static void write(Path path, ImageCaptcha captcha) throws IOException {
		try (OutputStream out = Files.newOutputStream(path)) {
			captcha.writePng(out);
		}
		System.out.println("Wrote " + path);
		return;
	}
}
