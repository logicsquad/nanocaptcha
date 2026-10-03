/**
 * Image and audio CAPTCHAs, with no dependencies beyond the JDK. Start with
 * {@link net.logicsquad.nanocaptcha.image.ImageCaptcha} or {@link net.logicsquad.nanocaptcha.audio.AudioCaptcha}.
 *
 * @since 3.0
 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/66">#66</a>
 */
module net.logicsquad.nanocaptcha {
	// The API uses its types, such as BufferedImage and AudioInputStream, so modules that read NanoCaptcha read it too
	requires transitive java.desktop;

	exports net.logicsquad.nanocaptcha.audio;
	exports net.logicsquad.nanocaptcha.audio.noise;
	exports net.logicsquad.nanocaptcha.audio.producer;
	exports net.logicsquad.nanocaptcha.content;
	exports net.logicsquad.nanocaptcha.image;
	exports net.logicsquad.nanocaptcha.image.background;
	exports net.logicsquad.nanocaptcha.image.filter;
	exports net.logicsquad.nanocaptcha.image.noise;
	exports net.logicsquad.nanocaptcha.image.renderer;
}
