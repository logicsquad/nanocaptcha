package net.logicsquad.nanocaptcha.audio.producer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * A {@link VoiceProducer} that can generate a vocalization for a given number
 * in a randomly chosen voice.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class RandomNumberVoiceProducer implements VoiceProducer {
	/**
	 * Random number generator
	 */
	private static final Random RAND = new SecureRandom();

	/**
	 * Prefix for locating built-in voices
	 */
	private static final String BUILT_IN_VOICES_PREFIX = "/sounds/en/numbers/";

	/**
	 * Built-in voices
	 */
	private static final String[] BUILT_IN_VOICES = { "alex", "bruce", "fred", "ralph", "kathy", "vicki", "victoria" };

	/**
	 * Map from each single digit to built-in list of vocalizations for that digit
	 */
	private static final Map<Integer, List<String>> BUILT_IN_VOICES_MAP = new HashMap<>();

	static {
		// 10 digits
		for (int i = 0; i < 10; i++) {
			List<String> sampleNames = new ArrayList<>();
			for (String name : Arrays.asList(BUILT_IN_VOICES)) {
				StringBuilder sb = new StringBuilder(BUILT_IN_VOICES_PREFIX);
				sb.append(i);
				sb.append("-");
				sb.append(name);
				sb.append(".wav");
				sampleNames.add(sb.toString());
			}
			BUILT_IN_VOICES_MAP.put(i, sampleNames);
		}
	}

	/**
	 * Map from each single digit to list of vocalizations to choose from for that digit
	 */
	private final Map<Integer, List<String>> _voices;

	/**
	 * Constructor resulting in object providing built-in voices to vocalize digits.
	 */
	public RandomNumberVoiceProducer() {
		this(BUILT_IN_VOICES_MAP);
	}

	/**
	 * Creates a <code>RandomNumberVoiceProducer</code> for the given
	 * <code>voices</code>, a map of numbers to their corresponding filenames.
	 * Conceptually the map must look like the following:
	 * 
	 * <pre>
	 * {1 => ["/my_sounds/1-quiet.wav", "/my_sounds/1-loud.wav"],
	 *  2 => ["/my_sounds/2-quiet.wav", "/my_sounds/2-loud.wav"]}
	 * </pre>
	 * 
	 * @param voices
	 */
	public RandomNumberVoiceProducer(Map<Integer, List<String>> voices) {
		_voices = voices;
	}

//	public Map<Integer, String[]> getVoices() {
//		return Collections.unmodifiableMap(_voices);
//	}

	@Override
	public final Sample getVocalization(char num) {
		try {
			Integer.parseInt(num + "");
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Expected <num> to be a number, got '" + num + "' instead.", e);
		}
		int idx = Integer.parseInt(num + "");
		List<String> files = _voices.get(idx);
		String filename = files.get(RAND.nextInt(files.size()));
		return new Sample(filename);
	}
}
