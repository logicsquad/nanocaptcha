package net.logicsquad.nanocaptcha.audio;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.Test;

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
	private static final String WAV_FILENAME = "/hello.wav";

	@Test(expected = NullPointerException.class)
	public void stringConstructorThrowsOnNull() {
		new Sample((String) null);
		return;
	}

	@Test(expected = NullPointerException.class)
	public void inputStreamConstructorThrowsOnNull() {
		new Sample((InputStream) null);
		return;
	}

	@Test(expected = RuntimeException.class)
	public void constructorThrowsOnWrongFormat() {
		new Sample(MP3_FILENAME);
		return;
	}

	@Test(expected = IllegalArgumentException.class)
	public void stringConstructorThrowsOnWrongAudioParameters() {
		new Sample(WAV_FILENAME);
		return;
	}

	@Test(expected = IllegalArgumentException.class)
	public void inputStreamConstructorThrowsOnWrongAudioParameters() throws UnsupportedAudioFileException, IOException {
		AudioInputStream audioInputStream = AudioSystem
				.getAudioInputStream(SampleTest.class.getResourceAsStream(WAV_FILENAME));
		assertNotNull(audioInputStream);
		new Sample(audioInputStream);
		return;
	}
}
