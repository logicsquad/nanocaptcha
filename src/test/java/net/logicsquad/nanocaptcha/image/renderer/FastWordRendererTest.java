package net.logicsquad.nanocaptcha.image.renderer;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link FastWordRenderer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.2
 */
@SuppressWarnings("deprecation")
public class FastWordRendererTest {
	@Test
	public void renderKeepsWorkingOnceThePointersWrap() {
		FastWordRenderer renderer = new FastWordRenderer.Builder().build();
		BufferedImage image = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
		// Five glyphs take both pointers from here past Integer.MAX_VALUE, where they go negative.
		FastWordRenderer.idxPointer.set(Integer.MAX_VALUE - 3);
		FastWordRenderer.fudgePointer.set(Integer.MAX_VALUE - 3);
		assertDoesNotThrow(() -> renderer.render("abcde", image));
		return;
	}
}
