package net.logicsquad.nanocaptcha.audio.producer;

import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * Generates a vocalization for a single character.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 *
 */
public interface VoiceProducer {
    public Sample getVocalization(char letter);
}
