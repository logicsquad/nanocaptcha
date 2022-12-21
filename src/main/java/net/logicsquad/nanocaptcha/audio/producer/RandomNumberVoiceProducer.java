package net.logicsquad.nanocaptcha.audio.producer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
	 * List of supported languages
	 */
	private static final List<Locale> SUPPORTED_LANGUAGES = Arrays.asList(Locale.ENGLISH, Locale.GERMAN);

	/**
	 * Property key for declaring a default language (which will be used in the
	 * no-args constructor) via 2-digit ISO 639 code
	 */
	static final String DEFAULT_LANGUAGE_KEY = "net.logicsquad.nanocaptcha.audio.producer.RandomNumberVoiceProducer.defaultLanguage";

	/**
	 * Default language of last resort if there's nothing set by property
	 */
	private static final Locale FALLBACK_LANGUAGE = Locale.ENGLISH;

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
	 * Default {@link Locale}
	 */
	static Locale defaultLanguage;

	/**
	 * Map from each single digit to list of vocalizations to choose from for that digit
	 */
	private final Map<Integer, List<String>> voices;

//	private final Locale locale;

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

//	public RandomNumberVoiceProducer(Locale locale) {
//		
//	}

//	private RandomNumberVoiceProducer
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

	/**
	 * Returns a default {@link Locale} to use when not explicitly declared by
	 * constructor.
	 * 
	 * @return default {@link Locale}
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/7">#7</a>
	 * @since 1.3
	 */
	static Locale defaultLanguage() {
		if (defaultLanguage == null) {
			String language = System.getProperty(DEFAULT_LANGUAGE_KEY);
			if (language == null || !SUPPORTED_LANGUAGES.stream().map(l -> l.getLanguage()).anyMatch(s -> s.equals(language))) {
				defaultLanguage = FALLBACK_LANGUAGE;
			} else {
				defaultLanguage = new Locale(language);
			}
		}
		return defaultLanguage;
	}
}
