package net.logicsquad.nanocaptcha.audio.noise;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.logicsquad.nanocaptcha.audio.Mixer;
import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * <p>
 * Adds noise to a {@link Sample} from one of the built-in noise files:
 * </p>
 *
 * <ul>
 * <li>{@code radio_tuning.wav}</li>
 * <li>{@code restaurant.wav}</li>
 * <li>{@code swimming.wav}</li>
 * </ul>
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class RandomNoiseProducer implements NoiseProducer {
	/**
	 * Random number generator
	 */
    private static final Random RAND = new SecureRandom();

    /**
     * Built-in noise samples
     */
	private static final String[] BUILT_IN_NOISES = {
			"/sounds/noises/radio_tuning.wav",
			"/sounds/noises/restaurant.wav",
			"/sounds/noises/swimming.wav", };

	/**
	 * Noise files to use
	 */
    private final String noiseFiles[];

	/**
	 * Constructor: object will use built-in noise files.
	 */
	public RandomNoiseProducer() {
		this(BUILT_IN_NOISES);
	}

	/**
	 * Constructor taking an array of noise filenames.
	 *
	 * @param noiseFiles noise filenames
	 */
    public RandomNoiseProducer(String[] noiseFiles) {
		this.noiseFiles = Arrays.copyOf(noiseFiles, noiseFiles.length);
		return;
    }

    /**
	 * Concatenates {@code samples}, then adds a random background noise sample
	 * (from this object's list of samples), returning the resulting {@link Sample}.
	 *
	 * @param samples a list of {@link Sample}s
	 * @return concatenated {@link Sample}s with added noise
	 */
	@Override
	public Sample addNoise(List<Sample> samples) {
		Sample appended = Mixer.concatenate(samples);
		String noiseFile = noiseFiles[RAND.nextInt(noiseFiles.length)];
		Sample noise = new Sample(noiseFile);
		// Decrease the volume of the noise to make sure the voices can be heard
		return Mixer.mix(appended, 1.0, noise, 0.6);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(34);
		sb.append("[RandomNoiseProducer: noiseFiles=")
				.append(Arrays.asList(noiseFiles).stream().collect(Collectors.joining(","))).append("]");
		return sb.toString();
	}
}
