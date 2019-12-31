package net.logicsquad.nanocaptcha.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.OffsetDateTime;

import net.logicsquad.nanocaptcha.content.ContentProducer;
import net.logicsquad.nanocaptcha.content.LatinContentProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.BackgroundProducer;
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
	 * @param builder a {@link Builder} object
	 */
	private ImageCaptcha(Builder builder) {
		image = builder.image;
		content = builder.content;
		created = OffsetDateTime.now();
		return;
	}

	/**
	 * <p>
	 * Builder for an {@link ImageCaptcha}. Elements are added to the image on the
	 * fly, so call the methods in an order that makes sense, e.g.:
	 * </p>
	 *
	 * <pre>
	 * ImageCaptcha image = addBackground().addContent().addNoise().addFilter().addBorder().build();
	 * </pre>
	 */
	public static class Builder {
		/**
		 * Text content
		 */
		private String content = "";

		/**
		 * Generated image
		 */
		private BufferedImage image;

		/**
		 * Background for generated image
		 */
		private BufferedImage background;

		/**
		 * Should we add a border?
		 */
		private boolean addBorder;

		/**
		 * Constructor taking a width and height (in pixels) for the generated image.
		 *
		 * @param width  image width
		 * @param height image height
		 */
		public Builder(int width, int height) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			return;
		}

		/**
		 * Adds a background using the default {@link BackgroundProducer} (a
		 * {@link TransparentBackgroundProducer}).
		 *
		 * @return this
		 */
		public Builder addBackground() {
			return addBackground(new TransparentBackgroundProducer());
		}

		/**
		 * Adds a background using the given {@link BackgroundProducer}. Note that
		 * adding more than one background does not have an additive effect: the last
		 * background added is the winner.
		 *
		 * @param backgroundProducer a {@link BackgroundProducer}
		 * @return this
		 */
		public Builder addBackground(BackgroundProducer backgroundProducer) {
			background = backgroundProducer.getBackground(image.getWidth(), image.getHeight());
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
		 * Adds content to the CAPTCHA using the given {@link ContentProducer}.
		 *
		 * @param contentProducer a {@link ContentProducer}
		 * @return this
		 */
		public Builder addContent(ContentProducer contentProducer) {
			return addContent(contentProducer, new DefaultWordRenderer());
		}

		/**
		 * Adds content to the CAPTCHA using the given {@link ContentProducer}, and
		 * render it to the image using the given {@link WordRenderer}.
		 *
		 * @param contentProducer a {@link ContentProducer}
		 * @param wordRenderer    a {@link WordRenderer}
		 * @return this
		 */
		public Builder addContent(ContentProducer contentProducer, WordRenderer wordRenderer) {
			content += contentProducer.getContent();
			wordRenderer.render(content, image);
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
			noiseProducer.makeNoise(image);
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
			filter.filter(image);
			return this;
		}

		/**
		 * Draws a single-pixel wide black border around the image.
		 *
		 * @return this
		 */
		public Builder addBorder() {
			addBorder = true;
			return this;
		}

		/**
		 * Builds the image CAPTCHA described by this object.
		 *
		 * @return {@link ImageCaptcha} as described by this {@code Builder}
		 */
		public ImageCaptcha build() {
			if (background == null) {
				background = new TransparentBackgroundProducer().getBackground(image.getWidth(), image.getHeight());
			}

			// Paint the main image over the background
			Graphics2D g = background.createGraphics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g.drawImage(image, null, null);

			if (addBorder) {
				int width = image.getWidth();
				int height = image.getHeight();
				g.setColor(Color.BLACK);
				g.drawLine(0, 0, 0, width);
				g.drawLine(0, 0, width, 0);
				g.drawLine(0, height - 1, width, height - 1);
				g.drawLine(width - 1, height - 1, width - 1, 0);
			}
			image = background;
			return new ImageCaptcha(this);
		}
	}

	/**
	 * Does CAPTCHA content match supplied {@code answer}?
	 *
	 * @param answer a candidate content match
	 * @return {@code true} if {@code answer} matches CAPTCHA content, otherwise
	 *         {@code false}
	 */
	public boolean isCorrect(String answer) {
		return answer.equals(content);
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
	 * Returns creation timestamp.
	 *
	 * @return creation timestamp
	 */
	public OffsetDateTime getCreated() {
		return created;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(35);
		sb.append("[ImageCaptcha: created=").append(created).append(" content='").append(content).append("']");
		return sb.toString();
	}
}
