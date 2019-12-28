package net.logicsquad.nanocaptcha.audio.noise;

import java.util.List;

import net.logicsquad.nanocaptcha.audio.Sample;

public interface NoiseProducer {
    public Sample addNoise(List<Sample> target);
}
