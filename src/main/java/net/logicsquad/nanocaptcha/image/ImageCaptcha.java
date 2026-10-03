package net.logicsquad.nanocaptcha.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import net.logicsquad.nanocaptcha.content.ContentProducer;
import net.logicsquad.nanocaptcha.content.LatinContentProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.BackgroundProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.FlatColorBackgroundProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.TransparentBackgroundProducer;
import net.logicsquad.nanocaptcha.image.filter.ImageFilter;
import net.logicsquad.nanocaptcha.image.filter.RippleImageFilter;
import net.logicsquad.nanocaptcha.image.noise.CurvedLineNoiseProducer;
import net.logicsquad.nanocaptcha.image.noise.NoiseProducer;
import net.logicsquad.nanocaptcha.image.renderer.DefaultWordRenderer;
import net.logicsquad.nanocaptcha.image.renderer.WordRenderer;

/**
 * An image CAPTCHA.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public final class ImageCaptcha {
	/**
	 * Key for {@code defaultX} property
	 */
	private static final String DEFAULT_X_KEY = "net.logicsquad.nanocaptcha.image.ImageCaptcha.defaultX";

	/**
	 * Key for {@code defaultY} property
	 */
	private static final String DEFAULT_Y_KEY = "net.logicsquad.nanocaptcha.image.ImageCaptcha.defaultY";

	/**
	 * Default x-value if {@code defaultX} not set
	 */
	private static final int DEFAULT_X = 200;

	/**
	 * Default y-value if {@code defaultY} not set
	 */
	private static final int DEFAULT_Y = 50;

	/**
	 * Colour of the default background
	 */
	private static final Color DEFAULT_BACKGROUND = Color.LIGHT_GRAY;

	/**
	 * Generated image
	 */
	private final BufferedImage image;

	/**
	 * Text content of image
	 */
	private final String content;

	/**
	 * Creation timestamp
	 */
	private final OffsetDateTime created;

	/**
	 * Constructor
	 *
	 * @param content text content
	 * @param image   generated image
	 */
	private ImageCaptcha(String content, BufferedImage image) {
		this.content = content;
		this.image = image;
		created = OffsetDateTime.now();
		return;
	}

	/**
	 * <p>
	 * Returns a new {@code ImageCaptcha} with some very basic settings:
	 * </p>
	 *
	 * <ul>
	 * <li>x- and y-dimensions 200 x 50, unless overridden by properties;</li>
	 * <li>{@link LatinContentProducer} with length 5;</li>
	 * <li>{@link DefaultWordRenderer} with <em>its</em> defaults; and</li>
	 * <li>a light grey background, the {@link Factory.Builder}'s default.</li>
	 * </ul>
	 *
	 * <p>
	 * To override the x- and y-dimensions for your project, you can set these properties:
	 * </p>
	 *
	 * <ul>
	 * <li>{@code net.logicsquad.nanocaptcha.image.ImageCaptcha.defaultX}</li>
	 * <li>{@code net.logicsquad.nanocaptcha.image.ImageCaptcha.defaultY}</li>
	 * </ul>
	 *
	 * @return new {@code ImageCaptcha}
	 * @since 2.0
	 */
	public static ImageCaptcha create() {
		int width = Integer.getInteger(DEFAULT_X_KEY, DEFAULT_X);
		int height = Integer.getInteger(DEFAULT_Y_KEY, DEFAULT_Y);
		return new Factory.Builder(width, height).addContent().build().create();
	}

	/**
	 * <p>
	 * Makes {@link ImageCaptcha}s, each with new content and randomness. A {@code Factory} can't be changed, and it's safe
	 * to share between threads, so a web application can build one when it starts, and ask it for a CAPTCHA for each
	 * request:
	 * </p>
	 *
	 * <pre>
	 * ImageCaptcha.Factory captchas = new ImageCaptcha.Factory.Builder(200, 50).addContent().addNoise().build();
	 * ImageCaptcha captcha = captchas.create();
	 * </pre>
	 *
	 * <p>
	 * Every CAPTCHA a {@code Factory} makes uses the same producers, renderers and filters, so any of your own need to be
	 * thread-safe, as NanoCaptcha's own are.
	 * </p>
	 *
	 * @since 3.0
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/64">#64</a>
	 */
	public static final class Factory {
		/**
		 * Image width
		 */
		private final int width;

		/**
		 * Image height
		 */
		private final int height;

		/**
		 * Producer of the content, or {@code null} for none
		 */
		private final ContentProducer contentProducer;

		/**
		 * Steps that draw on the image, in order, each given the image and its content
		 */
		private final List<BiConsumer<BufferedImage, String>> steps;

		/**
		 * Producer of the background
		 */
		private final BackgroundProducer backgroundProducer;

		/**
		 * Should we add a border?
		 */
		private final boolean addBorder;

		/**
		 * Constructor
		 *
		 * @param builder a {@link Builder}, whose configuration this copies
		 */
		private Factory(Builder builder) {
			width = builder.width;
			height = builder.height;
			contentProducer = builder.contentProducer;
			steps = List.copyOf(builder.steps);
			backgroundProducer = builder.backgroundProducer == null ? new FlatColorBackgroundProducer(DEFAULT_BACKGROUND)
					: builder.backgroundProducer;
			addBorder = builder.addBorder;
			return;
		}

		/**
		 * Returns a new {@link ImageCaptcha}, with new content and randomness.
		 *
		 * @return new {@link ImageCaptcha}
		 */
		public ImageCaptcha create() {
			String content = contentProducer == null ? "" : contentProducer.getContent();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (BiConsumer<BufferedImage, String> step : steps) {
				step.accept(image, content);
			}
			// Paint the main image over the background
			BufferedImage background = backgroundProducer.getBackground(width, height);
			Graphics2D g = background.createGraphics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g.drawImage(image, null, null);
			g.dispose();
			if (addBorder) {
				g = background.createGraphics();
				g.setColor(Color.BLACK);
				g.drawRect(0, 0, width - 1, height - 1);
				g.dispose();
			}
			return new ImageCaptcha(content, background);
		}

		/**
		 * <p>
		 * Builder for a {@link Factory}. The {@link Factory} draws each CAPTCHA's content, noise and filters in the order
		 * they were added, over its background, with any border on top. So add them in an order that makes sense, e.g.:
		 * </p>
		 *
		 * <pre>
		 * ImageCaptcha.Factory captchas = new ImageCaptcha.Factory.Builder(200, 50)
		 *     .addBackground().addContent().addNoise().addFilter().addBorder().build();
		 * </pre>
		 *
		 * <p>
		 * A {@code Builder} isn't thread-safe, but the {@link Factory} it builds is. Changing a {@code Builder} after
		 * {@link #build()} doesn't change the {@link Factory} it built.
		 * </p>
		 *
		 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/64">#64</a>
		 */
		public static final class Builder implements net.logicsquad.nanocaptcha.Builder<Factory> {
			/**
			 * Image width
			 */
			private final int width;

			/**
			 * Image height
			 */
			private final int height;

			/**
			 * Producer of the content, or {@code null} for none
			 */
			private ContentProducer contentProducer;

			/**
			 * Steps that draw on the image, in the order they were added, each given the image and its content
			 */
			private final List<BiConsumer<BufferedImage, String>> steps = new ArrayList<>();

			/**
			 * Producer of the background, or {@code null} for the default
			 */
			private BackgroundProducer backgroundProducer;

			/**
			 * Should we add a border?
			 */
			private boolean addBorder;

			/**
			 * Constructor taking a width and height (in pixels) for the generated image.
			 *
			 * @param width  image width
			 * @param height image height
			 * @throws IllegalArgumentException if {@code width} or {@code height} isn't positive
			 */
			public Builder(int width, int height) {
				if (width <= 0 || height <= 0) {
					throw new IllegalArgumentException("The width (" + width + ") and height (" + height + ") must be positive.");
				}
				this.width = width;
				this.height = height;
				return;
			}

			/**
			 * Adds the default background, a flat light grey ({@link Color#LIGHT_GRAY}), which the image also gets if no
			 * background is added.
			 *
			 * @return this
			 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/68">#68</a>
			 */
			public Builder addBackground() {
				return addBackground(new FlatColorBackgroundProducer(DEFAULT_BACKGROUND));
			}

			/**
			 * Adds a background using the given {@link BackgroundProducer}. Note that
			 * adding more than one background does not have an additive effect: the last
			 * background added is the winner. For a transparent image, add a
			 * {@link TransparentBackgroundProducer}.
			 *
			 * @param backgroundProducer a {@link BackgroundProducer}
			 * @return this
			 */
			public Builder addBackground(BackgroundProducer backgroundProducer) {
				this.backgroundProducer = Objects.requireNonNull(backgroundProducer);
				return this;
			}

			/**
			 * Adds content to the CAPTCHA using the default {@link ContentProducer}.
			 *
			 * @return this
			 */
			public Builder addContent() {
				return addContent(new LatinContentProducer());
			}

			/**
			 * Adds content (of length {@code length}) to the CAPTCHA using the default {@link ContentProducer}.
			 *
			 * @param length number of content units to add
			 * @return this
			 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/9">#9</a>
			 * @since 1.4
			 */
			public Builder addContent(int length) {
				return addContent(new LatinContentProducer(length));
			}

			/**
			 * Adds content to the CAPTCHA using the given {@link ContentProducer}.
			 *
			 * @param contentProducer a {@link ContentProducer}
			 * @return this
			 */
			public Builder addContent(ContentProducer contentProducer) {
				return addContent(contentProducer, new DefaultWordRenderer.Builder().build());
			}

			/**
			 * Adds content to the CAPTCHA using the given {@link ContentProducer}, and
			 * render it to the image using the given {@link WordRenderer}.
			 *
			 * @param contentProducer a {@link ContentProducer}
			 * @param wordRenderer    a {@link WordRenderer}
			 * @return this
			 * @throws IllegalStateException if this {@code Builder} already has content
			 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/48">#48</a>
			 */
			public Builder addContent(ContentProducer contentProducer, WordRenderer wordRenderer) {
				Objects.requireNonNull(contentProducer);
				Objects.requireNonNull(wordRenderer);
				if (this.contentProducer != null) {
					throw new IllegalStateException(
							"This Builder already has content, and an image CAPTCHA has only one answer.");
				}
				this.contentProducer = contentProducer;
				steps.add((image, content) -> wordRenderer.render(content, image));
				return this;
			}

			/**
			 * Adds noise using the default {@link NoiseProducer} (a
			 * {@link CurvedLineNoiseProducer}).
			 *
			 * @return this
			 */
			public Builder addNoise() {
				return addNoise(new CurvedLineNoiseProducer());
			}

			/**
			 * Adds noise using the given {@link NoiseProducer}.
			 *
			 * @param noiseProducer a {@link NoiseProducer}
			 * @return this
			 */
			public Builder addNoise(NoiseProducer noiseProducer) {
				Objects.requireNonNull(noiseProducer);
				steps.add((image, content) -> noiseProducer.makeNoise(image));
				return this;
			}

			/**
			 * Filters the image using the default {@link ImageFilter} (a
			 * {@link RippleImageFilter}).
			 *
			 * @return this
			 */
			public Builder addFilter() {
				return addFilter(new RippleImageFilter());
			}

			/**
			 * Filters the image using the given {@link ImageFilter}.
			 *
			 * @param filter an {@link ImageFilter}
			 * @return this
			 */
			public Builder addFilter(ImageFilter filter) {
				Objects.requireNonNull(filter);
				steps.add((image, content) -> filter.filter(image));
				return this;
			}

			/**
			 * Draws a single-pixel wide black border around the image.
			 *
			 * @return this
			 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/47">#47</a>
			 */
			public Builder addBorder() {
				addBorder = true;
				return this;
			}

			/**
			 * Builds a {@link Factory} that makes CAPTCHAs as this {@code Builder} is configured now.
			 *
			 * @return new {@link Factory}
			 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/64">#64</a>
			 */
			@Override
			public Factory build() {
				return new Factory(this);
			}
		}
	}

	/**
	 * Does CAPTCHA content match supplied {@code answer}? Case is ignored, so that an answer a mobile keyboard has
	 * capitalised still matches, and so is whitespace at either end, which autofill can add. For an exact comparison,
	 * use {@link #isCorrect(String, boolean)}. If {@code answer} is {@code null}, this method returns {@code false}.
	 *
	 * @param answer a candidate content match
	 * @return {@code true} if {@code answer} matches CAPTCHA content, otherwise
	 *         {@code false}
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/69">#69</a>
	 */
	public boolean isCorrect(String answer) {
		return isCorrect(answer, true);
	}

	/**
	 * Does CAPTCHA content match supplied {@code answer}? With {@code normalise}, case and whitespace at either end of
	 * {@code answer} are ignored, as {@link #isCorrect(String)} ignores them. Without it, {@code answer} has to match
	 * exactly. If {@code answer} is {@code null}, this method returns {@code false}.
	 *
	 * @param answer    a candidate content match
	 * @param normalise whether to ignore case, and whitespace at either end of {@code answer}
	 * @return {@code true} if {@code answer} matches CAPTCHA content, otherwise
	 *         {@code false}
	 * @since 3.0
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/69">#69</a>
	 */
	public boolean isCorrect(String answer, boolean normalise) {
		if (answer == null) {
			return false;
		}
		return normalise ? content.equalsIgnoreCase(answer.strip()) : content.equals(answer);
	}

	/**
	 * Returns content of this CAPTCHA.
	 *
	 * @return content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Returns the image for this {@code ImageCaptcha}.
	 *
	 * @return CAPTCHA image
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * Writes the image for this {@code ImageCaptcha} to {@code out} as a PNG file, transparency included, leaving
	 * {@code out} open.
	 *
	 * @param out an {@link OutputStream}
	 * @throws IOException if unable to write to {@code out}
	 * @since 2.2
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/45">#45</a>
	 */
	public void writePng(OutputStream out) throws IOException {
		// Given a plain OutputStream, ImageIO would buffer through a temporary file. Closing this doesn't close out.
		try (ImageOutputStream ios = new MemoryCacheImageOutputStream(Objects.requireNonNull(out))) {
			if (!ImageIO.write(image, "png", ios)) {
				throw new IllegalStateException("ImageIO has no PNG writer for this image");
			}
		}
	}

	/**
	 * Returns the image for this {@code ImageCaptcha} as the contents of a PNG file, transparency included.
	 *
	 * @return PNG file contents
	 * @since 2.2
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/45">#45</a>
	 */
	public byte[] toPng() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			writePng(out);
		} catch (IOException e) {
			// Everything here is in memory, so this shouldn't happen
			throw new UncheckedIOException(e);
		}
		return out.toByteArray();
	}

	/**
	 * Returns the image for this {@code ImageCaptcha} as a {@code data:} URI holding a PNG, ready to use as the
	 * {@code src} of an HTML {@code <img>} element.
	 *
	 * @return {@code data:} URI
	 * @since 2.2
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/45">#45</a>
	 */
	public String toDataUri() {
		return "data:image/png;base64," + Base64.getEncoder().encodeToString(toPng());
	}

	/**
	 * Returns creation timestamp.
	 *
	 * @return creation timestamp
	 */
	public OffsetDateTime getCreated() {
		return created;
	}

	/**
	 * Returns a description of this {@code ImageCaptcha} for debugging, with the length of its answer, but not the answer
	 * itself, which would then end up wherever the description does. For the answer, use {@link #getContent()}.
	 *
	 * @return description
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/70">#70</a>
	 */
	@Override
	public String toString() {
		int length = content.codePointCount(0, content.length());
		StringBuilder sb = new StringBuilder(64);
		sb.append("[ImageCaptcha: created=").append(created).append(" content=").append(length)
				.append(length == 1 ? " character]" : " characters]");
		return sb.toString();
	}
}
