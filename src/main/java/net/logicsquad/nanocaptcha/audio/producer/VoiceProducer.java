package net.logicsquad.nanocaptcha.audio.producer;

import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * An object that can produce vocalized components of audio CAPTCHAs.
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
