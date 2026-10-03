package net.logicsquad.nanocaptcha.audio.noise;

import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * An object that can add background noise to a {@link Sample}.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public interface NoiseProducer {
	/**
	 * Adds background noise to {@code clip}, and returns the result.
	 *
	 * @param clip the spoken digits, one after another
	 * @return {@code clip} with noise added
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/86">#86</a>
	 */
	Sample addNoise(Sample clip);
}
