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
	 * Prefix for locating voices
	 */
	private static final String PATH_PREFIX_TEMPLATE = "/sounds/%s/numbers/";

	/**
	 * English voices
	 */
	private static final List<String> VOICES_EN = Arrays.asList("a", "b", "c", "d", "e", "f", "g");

	/**
	 * German voices
	 */
	private static final List<String> VOICES_DE = Arrays.asList("a", "b");

	/**
	 * Map from language to list of voice names
	 */
	private static final Map<Locale, List<String>> VOICES = new HashMap<>();

	static {
		VOICES.put(Locale.ENGLISH, VOICES_EN);
		VOICES.put(Locale.GERMAN, VOICES_DE);
	}

	/**
	 * Default {@link Locale}
	 */
	static Locale defaultLanguage;

	/**
	 * Map from each single digit to list of vocalizations to choose from for that
	 * digit
	 */
	private Map<Integer, List<String>> vocalizations;

	/**
	 * Language to use for vocalizations
	 */
	final Locale language;

	/**
	 * Prefix to path for vocalizations
	 */
	private String pathPrefix;

	/**
	 * Constructor resulting in object providing built-in voices to vocalize digits.
	 */
	public RandomNumberVoiceProducer() {
		this(defaultLanguage());
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
	 * @deprecated Use {@link #RandomNumberVoiceProducer(Locale)} with a supported
	 *             language instead
	 */
	@Deprecated
	public RandomNumberVoiceProducer(Map<Integer, List<String>> voices) {
		this.language = defaultLanguage();
		this.vocalizations = Objects.requireNonNull(voices);
		return;
	}

	/**
	 * Constructor taking a language {@link Locale}. If {@code language} is not a
	 * supported language, the default language will be used.
	 * 
	 * @param language a {@link Locale} representing a language
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/7">#7</a>
	 * @since 1.3
	 */
	public RandomNumberVoiceProducer(Locale language) {
		Objects.requireNonNull(language);
		this.language = SUPPORTED_LANGUAGES.contains(language) ? language : defaultLanguage();
		return;
	}

	@Override
	public final Sample getVocalization(char number) {
		String stringNumber = Character.toString(number);
		try {
			int idx = Integer.parseInt(stringNumber);
			List<String> files = vocalizations().get(idx);
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

	/**
	 * Returns a localized path prefix to find the vocalizations.
	 * 
	 * @return path prefix
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/7">#7</a>
	 * @since 1.3
	 */
	private String pathPrefix() {
		if (pathPrefix == null) {
			pathPrefix = String.format(PATH_PREFIX_TEMPLATE, language.getLanguage());
		}
		return pathPrefix;
	}

	/**
	 * Returns the map from numbers to vocalization samples.
	 * 
	 * @return map of vocalizations
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/7">#7</a>
	 * @since 1.3
	 */
	private Map<Integer, List<String>> vocalizations() {
		if (vocalizations == null) {
			vocalizations = new HashMap<>();
			List<String> sampleNames;
			for (int i = 0; i < 10; i++) {
				sampleNames = new ArrayList<>();
				StringBuilder sb;
				for (String name : VOICES.get(language)) {
					sb = new StringBuilder(pathPrefix());
					sb.append(i).append("_").append(name).append(".wav");
					sampleNames.add(sb.toString());
				}
				vocalizations.put(i, sampleNames);
			}
		}
		return vocalizations;
	}
}
