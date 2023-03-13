package net.logicsquad.nanocaptcha.audio.producer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	public void constructorThrowsOnNull1() {
		assertThrows(NullPointerException.class, () -> new RandomNumberVoiceProducer((Map<Integer, List<String>>) null));
		return;
	}

	@Test
	public void constructorThrowsOnNull2() {
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
		// We don't support French yet
		RandomNumberVoiceProducer r3 = new RandomNumberVoiceProducer(Locale.FRENCH);
		assertEquals(r3.language, Locale.ENGLISH);
		return;	
	}
}
