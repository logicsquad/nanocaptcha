package net.logicsquad.nanocaptcha.audio.noise;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import net.logicsquad.nanocaptcha.audio.Mixer;
import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * <p>
 * Adds noise to a {@link Sample}. Noise is chosen at random from one of the
 * built-in noise files:
 * </p>
 *
 * <ul>
 * <li>{@code babble.wav}</li>
 * <li>{@code radio_static.wav}</li>
 * <li>{@code rain.wav}</li>
 * </ul>
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class RandomNoiseProducer implements NoiseProducer {
	/**
	 * Relative volume of background noise
	 */
	private static final double NOISE_VOLUME = 0.6;

    /**
     * Built-in noise samples
     */
	static final String[] BUILT_IN_NOISES = {
			"/sounds/noises/babble.wav",
			"/sounds/noises/radio_static.wav",
			"/sounds/noises/rain.wav", };

	/**
	 * Noises already read, by file name. A {@link Sample} doesn't change once it's created, so each file only needs
	 * reading once.
	 */
	private static final Map<String, Sample> SAMPLES = new ConcurrentHashMap<>();

	/**
	 * Noise files to use
	 */
    private final String[] noiseFiles;

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
		String noiseFile = noiseFiles[ThreadLocalRandom.current().nextInt(noiseFiles.length)];
		Sample noise = SAMPLES.computeIfAbsent(noiseFile, Sample::new);
		// Decrease the volume of the noise to make sure the voices can be heard
		return Mixer.mix(appended, 1.0, noise, NOISE_VOLUME);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(34);
		sb.append("[RandomNoiseProducer: noiseFiles=").append(Arrays.asList(noiseFiles).stream().collect(Collectors.joining(", "))).append("]");
		return sb.toString();
	}
}
