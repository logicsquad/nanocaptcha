package nl.captcha.audio.noise;

import java.util.List;

import nl.captcha.audio.Sample;

public interface NoiseProducer {
    public Sample addNoise(List<Sample> target);
}
