package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Renders the content onto the image.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @author <a href="mailto:botyrbojey@gmail.com">bivashy</a>
 * @since 1.0
 */
public final class DefaultWordRenderer extends AbstractWordRenderer {
	/**
	 * Constructor taking its settings from a {@link Builder}
	 *
	 * @param builder a {@link Builder}
	 * @since 2.3
	 */
	private DefaultWordRenderer(Builder builder) {
		super(builder);
		return;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if a font can't display a character in {@code word}
	 */
	@Override
	public void render(final String word, BufferedImage image) {
		render(word, image, ThreadLocalRandom.current());
	}

	/**
	 * Renders {@code word} onto {@code image}, using {@code random}, so that tests can seed it.
	 *
	 * @param word   word to render
	 * @param image  image to render onto
	 * @param random a {@link Random}
	 * @throws IllegalArgumentException if a font can't display a character in {@code word}
	 */
	void render(String word, BufferedImage image, Random random) {
		Graphics2D g = image.createGraphics();
		try {
			RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
			g.setRenderingHints(hints);

			// Choose every glyph's font first, so that a random baseline can allow for the rows each one inks
			FontRenderContext frc = g.getFontRenderContext();
			char[] chars = word.toCharArray();
			Font[] fonts = new Font[chars.length];
			GlyphVector[] glyphs = new GlyphVector[chars.length];
			int ascent = 0;
			int descent = 0;
			for (int i = 0; i < chars.length; i++) {
				fonts[i] = fontSupplier().get();
				if (!fonts[i].canDisplay(chars[i])) {
					throw new IllegalArgumentException(
							cannotDisplay(fonts[i], chars[i]) + " Supply a font that can with DefaultWordRenderer.Builder.font().");
				}
				glyphs[i] = fonts[i].createGlyphVector(frc, new char[] { chars[i] });
				Rectangle bounds = glyphs[i].getPixelBounds(frc, 0, 0);
				ascent = Math.max(ascent, -bounds.y);
				descent = Math.max(descent, bounds.y + bounds.height);
			}
			int xBaseline = (int) Math.round(image.getWidth() * xOffset());
			int yBaseline = randomYOffset() ? randomBaseline(image.getHeight(), ascent, descent, random)
					: image.getHeight() - (int) Math.round(image.getHeight() * yOffset());

			for (int i = 0; i < chars.length; i++) {
				g.setColor(colorSupplier().get());
				g.setFont(fonts[i]);
				g.drawChars(chars, i, 1, xBaseline, yBaseline);
				xBaseline = xBaseline + (int) glyphs[i].getVisualBounds().getWidth();
			}
		} finally {
			g.dispose();
		}
	}

	/**
	 * Builder for {@code DefaultWordRenderer}.
	 * 
	 * @since 1.4
	 */
	public static class Builder extends AbstractWordRenderer.Builder {
		@Override
		public DefaultWordRenderer build() {
			return new DefaultWordRenderer(this);
		}
	}
}
