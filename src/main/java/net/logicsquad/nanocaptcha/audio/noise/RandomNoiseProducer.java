package net.logicsquad.nanocaptcha.audio.noise;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

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

    private static final Random RAND = new SecureRandom();
    private static final String[] DEFAULT_NOISES = {
            "/sounds/noises/radio_tuning.wav", 
            "/sounds/noises/restaurant.wav",
            "/sounds/noises/swimming.wav", };

    private final String _noiseFiles[];

    public RandomNoiseProducer() {
        this(DEFAULT_NOISES);
    }

    public RandomNoiseProducer(String[] noiseFiles) {
        _noiseFiles = noiseFiles;
    }

    /**
     * Append the given <code>samples</code> to each other, then add random
     * noise to the result.
     * 
     */
    @Override public Sample addNoise(List<Sample> samples) {
        Sample appended = Mixer.append(samples);
        String noiseFile = _noiseFiles[RAND.nextInt(_noiseFiles.length)];
        Sample noise = new Sample(noiseFile);

        // Decrease the volume of the noise to make sure the voices can be heard
        return Mixer.mix(appended, 1.0, noise, 0.6);
    }

    @Override public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[Noise files: ");
        sb.append(_noiseFiles);
        sb.append("]");

        return sb.toString();
    }
}
