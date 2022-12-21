package net.logicsquad.nanocaptcha.audio.producer;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests on {@link RandomNumberVoiceProducer} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class RandomNumberVoiceProducerTest {
	@Before
	public void setup() {
		RandomNumberVoiceProducer.defaultLanguage = null;
		System.clearProperty(RandomNumberVoiceProducer.DEFAULT_LANGUAGE_KEY);
		return;
	}

	@Test(expected = NullPointerException.class)
	public void constructorThrowsOnNull() {
		new RandomNumberVoiceProducer(null);
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
}
