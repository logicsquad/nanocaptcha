package net.logicsquad.nanocaptcha.audio;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link Sample} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class SampleTest {
	// An MP3 file can't be used at all
	private static final String MP3_FILENAME = "/hello.mp3";

	// This sample has the wrong encoding parameters for Sample
	private static final String WAV_BAD_FILENAME = "/hello.wav";

	// This sample is a copy of one of the known-good samples
	private static final String WAV_GOOD_FILENAME = "/0-alex.wav";

	// Known sample count
	private static final int WAV_GOOD_SAMPLES = 9847;

	@Test
	public void stringConstructorThrowsOnNull() {
		assertThrows(NullPointerException.class, () -> new Sample((String) null));
		return;
	}

	@Test
	public void inputStreamConstructorThrowsOnNull() {
		assertThrows(NullPointerException.class, () -> new Sample((InputStream) null));
		return;
	}

	@Test
	public void constructorThrowsOnWrongFormat() {
		assertThrows(RuntimeException.class, () -> new Sample(MP3_FILENAME));
		return;
	}

	@Test
	public void stringConstructorThrowsOnWrongAudioParameters() {
		assertThrows(IllegalArgumentException.class, () -> new Sample(WAV_BAD_FILENAME));
		return;
	}

	@Test
	public void inputStreamConstructorThrowsOnWrongAudioParameters() throws UnsupportedAudioFileException, IOException {
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(SampleTest.class.getResourceAsStream(WAV_BAD_FILENAME));
		assertNotNull(audioInputStream);
		assertThrows(IllegalArgumentException.class, () -> new Sample(audioInputStream));
		return;
	}

	@Test
	public void canCreateSampleFromSuitableInput() {
		Sample sample = new Sample(WAV_GOOD_FILENAME);
		assertNotNull(sample);
		assertEquals(WAV_GOOD_SAMPLES, sample.getSampleCount());
		return;
	}
}
