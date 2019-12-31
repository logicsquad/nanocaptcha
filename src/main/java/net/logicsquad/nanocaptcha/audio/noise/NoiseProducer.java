package net.logicsquad.nanocaptcha.audio.noise;

import java.util.List;

import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * An object that can add background noise to {@link Sample}s.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public interface NoiseProducer {
	/**
	 * Concatenates {@code samples}, adds background noise and returns the result.
	 *
	 * @param samples a list of {@link Sample}s
	 * @return concatenated {@link Sample}s with added noise
	 */
	Sample addNoise(List<Sample> samples);
}
