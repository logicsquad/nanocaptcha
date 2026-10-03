package net.logicsquad.nanocaptcha.audio.noise;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.sound.sampled.AudioInputStream;

import net.logicsquad.nanocaptcha.audio.Mixer;
import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * <p>
 * Adds noise to a {@link Sample}. Noise is chosen at random from the noises given to
 * {@link #RandomNoiseProducer(List)}, or by default from the built-in noise files:
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
			"/net/logicsquad/nanocaptcha/sounds/noises/babble.wav",
			"/net/logicsquad/nanocaptcha/sounds/noises/radio_static.wav",
			"/net/logicsquad/nanocaptcha/sounds/noises/rain.wav", };

	/**
	 * Noises already read, by file name. A {@link Sample} doesn't change once it's created, so each file only needs
	 * reading once.
	 */
	private static final Map<String, Sample> SAMPLES = new ConcurrentHashMap<>();

	/**
	 * Noises to choose from
	 */
	private final List<Sample> noises;

	/**
	 * Constructor: object will use built-in noise files.
	 */
	public RandomNoiseProducer() {
		this(builtInNoises());
	}

	/**
	 * Constructor taking an array of noise resource names, which are read with {@link Sample#Sample(String)}. So only
	 * resources that NanoCaptcha's own class loader and module can see are found, and a name without a leading
	 * {@code /} is relative to the {@code net.logicsquad.nanocaptcha.audio} package.
	 *
	 * @param noiseFiles names of noise resources
	 * @throws IllegalArgumentException if a resource can't be found, or its audio format is unsupported, or
	 *                                  {@code noiseFiles} is empty
	 * @deprecated Use {@link #RandomNoiseProducer(List)}, with {@link Sample}s made by {@link Sample#Sample(URL)} from
	 *             your own class's {@link Class#getResource(String)}, which finds your resources wherever they are. This
	 *             constructor will be removed in 3.0.
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/50">#50</a>
	 */
	@Deprecated
	public RandomNoiseProducer(String[] noiseFiles) {
		this(read(noiseFiles));
	}

	/**
	 * Constructor taking the noises to choose from, such as ones read with {@link Sample#Sample(URL)}.
	 *
	 * @param noises noises
	 * @throws NullPointerException     if {@code noises} or any of its elements is {@code null}
	 * @throws IllegalArgumentException if {@code noises} is empty
	 * @since 2.2
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/50">#50</a>
	 */
	public RandomNoiseProducer(List<Sample> noises) {
		if (noises.isEmpty()) {
			throw new IllegalArgumentException("RandomNoiseProducer needs at least one noise.");
		}
		List<Sample> copy = new ArrayList<>(noises.size());
		for (Sample noise : noises) {
			copy.add(Objects.requireNonNull(noise));
		}
		this.noises = Collections.unmodifiableList(copy);
		return;
	}

	/**
	 * Returns the built-in noises, reading each one the first time it's needed.
	 *
	 * @return built-in noises
	 */
	private static List<Sample> builtInNoises() {
		List<Sample> noises = new ArrayList<>(BUILT_IN_NOISES.length);
		for (String name : BUILT_IN_NOISES) {
			noises.add(SAMPLES.computeIfAbsent(name, RandomNoiseProducer::readBuiltIn));
		}
		return noises;
	}

	/**
	 * Reads the built-in noise {@code name} through this class, which can always see NanoCaptcha's own resources.
	 *
	 * @param name resource name
	 * @return noise
	 * @throws IllegalStateException if the noise is missing
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/50">#50</a>
	 */
	private static Sample readBuiltIn(String name) {
		URL url = RandomNoiseProducer.class.getResource(name);
		if (url == null) {
			throw new IllegalStateException("NanoCaptcha's noise '" + name + "' is missing from the classpath.");
		}
		return new Sample(url);
	}

	/**
	 * Returns the noises named in {@code noiseFiles}, reading each one with {@link Sample#Sample(String)} the first time
	 * it's needed.
	 *
	 * @param noiseFiles names of noise resources
	 * @return noises
	 */
	@SuppressWarnings("deprecation")
	private static List<Sample> read(String[] noiseFiles) {
		List<Sample> noises = new ArrayList<>(noiseFiles.length);
		for (String name : noiseFiles) {
			noises.add(SAMPLES.computeIfAbsent(name, Sample::new));
		}
		return noises;
	}

	/**
	 * Concatenates {@code samples}, then adds a random background noise sample
	 * (from this object's list of samples), from a random point in it, returning
	 * the resulting {@link Sample}.
	 *
	 * @param samples a list of {@link Sample}s
	 * @return concatenated {@link Sample}s with added noise
	 */
	@Override
	public Sample addNoise(List<Sample> samples) {
		Sample appended = Mixer.concatenate(samples);
		ThreadLocalRandom random = ThreadLocalRandom.current();
		Sample noise = noises.get(random.nextInt(noises.size()));
		// Start the noise anywhere, so that it can't be lined up and subtracted again
		noise = from(noise, appended.getAudioInputStream().getFrameLength(), random);
		// Decrease the volume of the noise to make sure the voices can be heard
		return Mixer.mix(appended, 1.0, noise, NOISE_VOLUME);
	}

	/**
	 * Returns {@code noise} from a random point, leaving at least {@code length}
	 * samples of it, so that it only has to repeat if it's shorter than that
	 * anyway.
	 *
	 * @param noise  a noise {@link Sample}
	 * @param length number of samples of noise needed
	 * @param random a {@link Random}
	 * @return noise from a random point
	 */
	static Sample from(Sample noise, long length, Random random) {
		AudioInputStream stream = noise.getAudioInputStream();
		long spare = stream.getFrameLength() - length;
		if (spare <= 0) {
			return noise;
		}
		long skip = (long) (random.nextDouble() * (spare + 1)) * stream.getFormat().getFrameSize();
		try {
			while (skip > 0) {
				long skipped = stream.skip(skip);
				if (skipped <= 0) {
					break;
				}
				skip -= skipped;
			}
		} catch (IOException e) {
			// The stream reads from memory, so this shouldn't happen
			throw new UncheckedIOException(e);
		}
		return new Sample(stream);
	}

	@Override
	public String toString() {
		return "[RandomNoiseProducer: noises=" + noises.size() + "]";
	}
}
