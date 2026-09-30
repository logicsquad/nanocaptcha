package net.logicsquad.nanocaptcha.audio.producer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.logicsquad.nanocaptcha.audio.Sample;

/**
 * Unit tests on {@link RandomNumberVoiceProducer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class RandomNumberVoiceProducerTest {
	@BeforeEach
	public void setup() {
		RandomNumberVoiceProducer.defaultLanguage = null;
		System.clearProperty(RandomNumberVoiceProducer.DEFAULT_LANGUAGE_KEY);
		return;
	}

	@Test
	public void constructorThrowsOnNull() {
		assertThrows(NullPointerException.class, () -> new RandomNumberVoiceProducer((Locale) null));
		return;
	}

	@Test
	public void defaultLocaleReturnsEnglishIfPropertyNotSet() {
		assertEquals(Locale.ENGLISH, RandomNumberVoiceProducer.defaultLanguage());
		return;
	}

	@Test
	public void defaultLocaleReturnsRequestedLocaleForLanguageIfPropertySetAndSupported() {
		System.setProperty(RandomNumberVoiceProducer.DEFAULT_LANGUAGE_KEY, "de");
		assertEquals(Locale.GERMAN, RandomNumberVoiceProducer.defaultLanguage());
		return;
	}

	@Test
	public void defaultLocaleReturnsEnglishIfRequestedLanguageIsUnsupported() {
		System.setProperty(RandomNumberVoiceProducer.DEFAULT_LANGUAGE_KEY, "xx");
		assertEquals(Locale.ENGLISH, RandomNumberVoiceProducer.defaultLanguage());
		return;
	}

	@Test
	public void localeConstructorReturnsObjectWithExpectedLanguage() {
		RandomNumberVoiceProducer r1 = new RandomNumberVoiceProducer(Locale.ENGLISH);
		assertEquals(r1.language, Locale.ENGLISH);
		RandomNumberVoiceProducer r2 = new RandomNumberVoiceProducer(Locale.GERMAN);
		assertEquals(r2.language, Locale.GERMAN);
		RandomNumberVoiceProducer r3 = new RandomNumberVoiceProducer(Locale.FRENCH);
		assertEquals(r3.language, Locale.FRENCH);
		// We don't support Italian yet
		RandomNumberVoiceProducer r4 = new RandomNumberVoiceProducer(Locale.ITALIAN);
		assertEquals(r4.language, Locale.ENGLISH);
		return;
	}

	@Test
	public void localeConstructorMatchesRegionalLocalesOnLanguage() {
		assertEquals(Locale.GERMAN, new RandomNumberVoiceProducer(Locale.GERMANY).language);
		assertEquals(Locale.GERMAN, new RandomNumberVoiceProducer(Locale.forLanguageTag("de-AT")).language);
		assertEquals(Locale.FRENCH, new RandomNumberVoiceProducer(Locale.FRANCE).language);
		assertEquals(Locale.FRENCH, new RandomNumberVoiceProducer(Locale.CANADA_FRENCH).language);
		assertEquals(Locale.ENGLISH, new RandomNumberVoiceProducer(Locale.US).language);
		// Italian still isn't supported, in any region
		assertEquals(Locale.ENGLISH, new RandomNumberVoiceProducer(Locale.ITALY).language);
		return;
	}

	@Test
	public void regionalLocaleProducesItsLanguagesVoices() {
		assertNotNull(new RandomNumberVoiceProducer(Locale.GERMANY).getVocalization('7'));
		assertNotNull(new RandomNumberVoiceProducer(Locale.CANADA_FRENCH).getVocalization('7'));
		return;
	}

	@Test
	public void readsEachVocalizationOnlyOnce() {
		RandomNumberVoiceProducer producer = new RandomNumberVoiceProducer(Locale.ENGLISH);
		Set<Sample> distinct = Collections.newSetFromMap(new IdentityHashMap<>());
		for (int i = 0; i < 100; i++) {
			distinct.add(producer.getVocalization('5'));
		}
		// One Sample per voice, however many times the digit is asked for
		assertEquals(RandomNumberVoiceProducer.VOICES.get(Locale.ENGLISH).size(), distinct.size());
		return;
	}

	@Test
	public void everyVoiceHasAUsableSampleForEveryDigit() {
		for (Map.Entry<Locale, List<String>> entry : RandomNumberVoiceProducer.VOICES.entrySet()) {
			for (String voice : entry.getValue()) {
				for (int i = 0; i < 10; i++) {
					String filename = String.format("/sounds/%s/numbers/%d_%s.wav", entry.getKey().getLanguage(), i, voice);
					assertDoesNotThrow(() -> new Sample(RandomNumberVoiceProducer.class.getResource(filename)), filename);
				}
			}
		}
		return;
	}
}
