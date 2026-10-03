package net.logicsquad.nanocaptcha.audio.producer;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

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
	 * Vocalizations already read, by file name. A {@link Sample} doesn't change once it's created, so each file only needs
	 * reading once.
	 */
	private static final Map<String, Sample> SAMPLES = new ConcurrentHashMap<>();

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
	private static final String PATH_PREFIX_TEMPLATE = "/net/logicsquad/nanocaptcha/sounds/%s/numbers/";

	/**
	 * Built-in voices, by language. Each voice has a vocalization of every digit, such as
	 * {@code /net/logicsquad/nanocaptcha/sounds/de/numbers/7_b.wav} for a 7 in German voice {@code b}, made by
	 * {@code scripts/generate-audio.py}. Adding a language takes only its files and an entry here.
	 *
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/67">#67</a>
	 */
	static final Map<Locale, List<String>> VOICES = Map.of(
			Locale.ENGLISH, List.of("a", "b", "c"),
			Locale.GERMAN, List.of("a", "b"),
			Locale.FRENCH, List.of("a"));

	/**
	 * Default {@link Locale}
	 */
	static volatile Locale defaultLanguage;

	/**
	 * Vocalizations to choose from for each digit, by digit. They're worked out in the constructor, rather than when
	 * they're first needed, so that another thread sharing this object can't see them half done.
	 *
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/64">#64</a>
	 */
	private final Map<Integer, List<String>> vocalizations;

	/**
	 * Language to use for vocalizations
	 */
	final Locale language;

	/**
	 * Constructor resulting in object providing built-in voices to vocalize digits in the default language: English,
	 * unless the {@code net.logicsquad.nanocaptcha.audio.producer.RandomNumberVoiceProducer.defaultLanguage} system
	 * property names another supported language. The JVM's default {@link Locale} isn't used.
	 */
	public RandomNumberVoiceProducer() {
		this(defaultLanguage());
	}

	/**
	 * Constructor taking a language {@link Locale}. Only the language counts, so a regional {@link Locale} such as
	 * {@link Locale#GERMANY} or {@code fr-CA} gets that language's voices. If {@code language} is not a supported
	 * language, the default language will be used.
	 *
	 * @param language a {@link Locale} representing a language
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/7">#7</a>
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/39">#39</a>
	 * @since 1.4
	 */
	public RandomNumberVoiceProducer(Locale language) {
		Objects.requireNonNull(language);
		this.language = VOICES.keySet().stream().filter(l -> l.getLanguage().equals(language.getLanguage())).findFirst()
				.orElseGet(RandomNumberVoiceProducer::defaultLanguage);
		vocalizations = vocalizations(this.language);
		return;
	}

	@Override
	public final Sample getVocalization(char number) {
		String stringNumber = Character.toString(number);
		try {
			int idx = Integer.parseInt(stringNumber);
			List<String> files = vocalizations.get(idx);
			String filename = files.get(ThreadLocalRandom.current().nextInt(files.size()));
			return SAMPLES.computeIfAbsent(filename, RandomNumberVoiceProducer::readBuiltIn);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("RandomNumberVoiceProducer can only vocalize numbers.", e);
		}
	}

	/**
	 * Reads the built-in vocalization {@code filename} through this class, which can always see NanoCaptcha's own
	 * resources.
	 *
	 * @param filename resource name
	 * @return vocalization
	 * @throws IllegalStateException if the vocalization is missing
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/50">#50</a>
	 */
	private static Sample readBuiltIn(String filename) {
		URL url = RandomNumberVoiceProducer.class.getResource(filename);
		if (url == null) {
			throw new IllegalStateException("NanoCaptcha's vocalization '" + filename + "' is missing from the classpath.");
		}
		return new Sample(url);
	}

	/**
	 * Returns a default {@link Locale} to use when not explicitly declared by constructor.
	 *
	 * @return default {@link Locale}
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/7">#7</a>
	 * @since 1.4
	 */
	static Locale defaultLanguage() {
		if (defaultLanguage == null) {
			synchronized (RandomNumberVoiceProducer.class) {
				if (defaultLanguage == null) {
					String language = System.getProperty(DEFAULT_LANGUAGE_KEY);
					if (language == null || !VOICES.keySet().stream().map(l -> l.getLanguage()).anyMatch(s -> s.equals(language))) {
						defaultLanguage = FALLBACK_LANGUAGE;
					} else {
						defaultLanguage = new Locale(language);
					}
				}
			}
		}
		return defaultLanguage;
	}

	/**
	 * Returns the vocalizations to choose from for each digit in {@code language}.
	 *
	 * @param language a language in {@link #VOICES}
	 * @return vocalizations, by digit
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/7">#7</a>
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/64">#64</a>
	 */
	private static Map<Integer, List<String>> vocalizations(Locale language) {
		String pathPrefix = String.format(PATH_PREFIX_TEMPLATE, language.getLanguage());
		Map<Integer, List<String>> vocalizations = new HashMap<>();
		for (int i = 0; i < 10; i++) {
			List<String> sampleNames = new ArrayList<>();
			for (String name : VOICES.get(language)) {
				sampleNames.add(pathPrefix + i + "_" + name + ".wav");
			}
			vocalizations.put(i, sampleNames);
		}
		return vocalizations;
	}
}
