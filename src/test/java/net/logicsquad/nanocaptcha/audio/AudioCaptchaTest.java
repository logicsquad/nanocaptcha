package net.logicsquad.nanocaptcha.audio;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link AudioCaptcha} class.
 *
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 2.2
 */
public class AudioCaptchaTest {
	@Test
	public void audioCanBeWrittenTwice() throws IOException {
		assertWritesTwice(AudioCaptcha.create());
		assertWritesTwice(new AudioCaptcha.Builder().addContent().addVoice().addNoise().build());
		return;
	}

	@Test
	public void isCorrectAcceptsOnlyTheContent() {
		AudioCaptcha captcha = AudioCaptcha.create();
		assertTrue(captcha.isCorrect(captcha.getContent()));
		assertFalse(captcha.isCorrect(captcha.getContent() + "0"));
		assertFalse(captcha.isCorrect(null));
		return;
	}

	/**
	 * Writes {@code captcha}'s audio twice, and checks that both copies hold the same audio (#40).
	 *
	 * @param captcha an {@link AudioCaptcha}
	 * @throws IOException if unable to write the audio
	 */
	private static void assertWritesTwice(AudioCaptcha captcha) throws IOException {
		ByteArrayOutputStream first = new ByteArrayOutputStream();
		ByteArrayOutputStream second = new ByteArrayOutputStream();
		AudioSystem.write(captcha.getAudio().getAudioInputStream(), AudioFileFormat.Type.WAVE, first);
		AudioSystem.write(captcha.getAudio().getAudioInputStream(), AudioFileFormat.Type.WAVE, second);
		assertTrue(first.size() > 44, "only " + first.size() + " bytes");
		assertArrayEquals(first.toByteArray(), second.toByteArray());
		return;
	}
}
