package net.logicsquad.nanocaptcha.image.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
	 * List of available {@link Font}s
	 */
	private final List<Font> fonts = new ArrayList<>();

	/**
	 * Constructor taking x- and y-axis offsets
	 * 
	 * @param xOffset       x-axis offset
	 * @param yOffset       y-axis offset
	 * @param colorSupplier {@link Color} supplier
	 * @since 1.4
	 */
	private DefaultWordRenderer(double xOffset, double yOffset, Supplier<Color> colorSupplier) {
		super(xOffset, yOffset, colorSupplier);
		this.fonts.addAll(DEFAULT_FONTS);
		return;
	}

	@Override
	public void render(final String word, BufferedImage image) {
		Graphics2D g = image.createGraphics();

		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
		g.setRenderingHints(hints);

		FontRenderContext frc = g.getFontRenderContext();
		int xBaseline = (int) Math.round(image.getWidth() * xOffset());
		int yBaseline = image.getHeight() - (int) Math.round(image.getHeight() * yOffset());

		char[] chars = new char[1];
		for (char c : word.toCharArray()) {
			chars[0] = c;

			g.setColor(colorSupplier().get());
			Font font = nextFont();
			g.setFont(font);
			GlyphVector gv = font.createGlyphVector(frc, chars);
			g.drawChars(chars, 0, chars.length, xBaseline, yBaseline);

			int width = (int) gv.getVisualBounds().getWidth();
			xBaseline = xBaseline + width;
		}
	}

	/**
	 * Returns a random {@link Font} from the list.
	 * 
	 * @return random {@link Font}
	 * @since 1.1
	 */
	private Font nextFont() {
		if (fonts.size() == 1) {
			return fonts.get(0);
		} else {
			return fonts.get(RAND.nextInt(fonts.size()));
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
			return new DefaultWordRenderer(xOffset, yOffset, colorSupplier);
		}
	}
}
