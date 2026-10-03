package net.logicsquad.nanocaptcha.audio.producer;

import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * An object that can produce vocalized components of audio CAPTCHAs.
 * <p>
 * An {@link net.logicsquad.nanocaptcha.audio.AudioCaptcha.Factory AudioCaptcha.Factory} shares an implementation
 * between every CAPTCHA it makes, on whichever threads ask, so it has to be thread-safe. NanoCaptcha's own keep no
 * state that changes, and take any randomness from {@link java.util.concurrent.ThreadLocalRandom} on each call.
 * </p>
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public interface VoiceProducer {
	/**
	 * Generates a vocalization for a single character.
	 *
	 * @param letter character to vocalize
	 * @return a {@link Sample} containing the vocalization
	 */
	Sample getVocalization(char letter);
}
