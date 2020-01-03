package net.logicsquad.nanocaptcha.audio.producer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private static final Random RAND = new Random();

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
		List<String> sampleNames;
		for (int i = 0; i < 10; i++) {
			sampleNames = new ArrayList<>();
			StringBuilder sb;
			for (String name : Arrays.asList(BUILT_IN_VOICES)) {
				sb = new StringBuilder(BUILT_IN_VOICES_PREFIX);
				sb.append(i).append("-").append(name).append(".wav");
				sampleNames.add(sb.toString());
			}
			BUILT_IN_VOICES_MAP.put(i, sampleNames);
		}
	}

	/**
	 * Map from each single digit to list of vocalizations to choose from for that digit
	 */
	private final Map<Integer, List<String>> voices;

	/**
	 * Constructor resulting in object providing built-in voices to vocalize digits.
	 */
	public RandomNumberVoiceProducer() {
		this(BUILT_IN_VOICES_MAP);
	}

	/**
	 * Creates a {@code RandomNumberVoiceProducer} for the given {@code voices}, a
	 * map of numbers to their corresponding filename options. Conceptually the map
	 * must look like the following:
	 *
	 * <pre>
	 * {1 => ["/my_sounds/1-quiet.wav", "/my_sounds/1-loud.wav"],
	 *  2 => ["/my_sounds/2-quiet.wav", "/my_sounds/2-loud.wav"]}
	 * </pre>
	 *
	 * @param voices map of digits to list of vocalizations of that digit
	 * @throws NullPointerException if {@code voices} is {@code null}
	 */
	public RandomNumberVoiceProducer(Map<Integer, List<String>> voices) {
		this.voices = Objects.requireNonNull(voices);
		return;
	}

	@Override
	public final Sample getVocalization(char number) {
		String stringNumber = Character.toString(number);
		try {
			int idx = Integer.parseInt(stringNumber);
			List<String> files = voices.get(idx);
			String filename = files.get(RAND.nextInt(files.size()));
			return new Sample(filename);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("RandomNumberVoiceProducer can only vocalize numbers.");
		}
	}
}
