package net.logicsquad.nanocaptcha.image;

import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

/**
 * Compares images from tests with golden images stored under {@code src/test/resources/golden}. When a change to how
 * something looks is intended, regenerate them on Java 17 or later with {@code mvn test -Dgolden.update=true}, which
 * rewrites only the images that have changed, and look at those before committing them.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.3
 */
public final class GoldenImages {
	/**
	 * Seed for the {@link java.util.Random} given to anything random
	 */
	public static final long SEED = 1;

	/**
	 * Whether to write golden images that don't match, rather than fail
	 */
	private static final boolean UPDATE = Boolean.getBoolean("golden.update");

	private GoldenImages() {
		throw new AssertionError();
	}

	/**
	 * Returns a fresh copy of the input for noise producers and filters: the logo in {@code input.png}, on a
	 * transparent background, in the type of image an {@link ImageCaptcha.Builder} draws on.
	 *
	 * @return input image
	 * @throws IOException if {@code input.png} can't be read
	 */
	public static BufferedImage input() throws IOException {
		BufferedImage png = read("/input.png");
		if (png == null) {
			throw new IOException("Can't find input.png");
		}
		int width = png.getWidth();
		int height = png.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, width, height, png.getRGB(0, 0, width, height, null, 0, width), 0, width);
		return image;
	}

	/**
	 * Asserts that {@code actual} matches the golden image called {@code name}. With {@code -Dgolden.update=true},
	 * makes {@code actual} the golden image instead, unless it already matches.
	 *
	 * @param name   name of golden image: a path under {@code golden}, without {@code .png}
	 * @param actual image to compare
	 * @throws IOException if the golden image can't be read or written
	 */
	public static void assertMatches(String name, BufferedImage actual) throws IOException {
		BufferedImage expected = read("/golden/" + name + ".png");
		String difference = expected == null ? "there's no golden image" : difference(expected, actual);
		if (difference == null) {
			return;
		}
		if (UPDATE) {
			Path file = Paths.get(System.getProperty("basedir", ""), "src", "test", "resources", "golden", name + ".png");
			Files.createDirectories(file.getParent());
			ImageIO.write(actual, "png", file.toFile());
			return;
		}
		fail(name + ": " + difference + ". If the change is intended, regenerate the golden images with "
				+ "-Dgolden.update=true.");
		return;
	}

	/**
	 * Describes how {@code actual} differs from {@code expected}, treating all fully transparent pixels as the same.
	 *
	 * @param expected expected image
	 * @param actual   actual image
	 * @return description, or {@code null} if the images match
	 */
	private static String difference(BufferedImage expected, BufferedImage actual) {
		int width = expected.getWidth();
		int height = expected.getHeight();
		if (actual.getWidth() != width || actual.getHeight() != height) {
			return "the image is " + actual.getWidth() + " x " + actual.getHeight() + ", not " + width + " x " + height;
		}
		int differing = 0;
		int maximum = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int e = expected.getRGB(x, y);
				int a = actual.getRGB(x, y);
				if (e != a && ((e >>> 24) != 0 || (a >>> 24) != 0)) {
					differing++;
					for (int shift = 0; shift < 32; shift += 8) {
						maximum = Math.max(maximum, Math.abs(((e >>> shift) & 0xff) - ((a >>> shift) & 0xff)));
					}
				}
			}
		}
		return differing == 0 ? null
				: differing + " of " + width * height + " pixels differ from the golden image, by up to " + maximum;
	}

	/**
	 * Reads an image from a test resource.
	 *
	 * @param name resource name
	 * @return image, or {@code null} if there's no such resource
	 * @throws IOException if the image can't be read
	 */
	private static BufferedImage read(String name) throws IOException {
		try (InputStream in = GoldenImages.class.getResourceAsStream(name)) {
			return in == null ? null : ImageIO.read(in);
		}
	}
}
