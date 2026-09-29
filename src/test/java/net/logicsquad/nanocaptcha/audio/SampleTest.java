package net.logicsquad.nanocaptcha.audio;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.sound.sampled.AudioFileFormat;
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
	private static final String WAV_GOOD_FILENAME = "/0_a.wav";

	// Known sample count
	private static final int WAV_GOOD_SAMPLES = 15221;

	@Test
	@SuppressWarnings("deprecation")
	public void stringConstructorThrowsOnNull() {
		assertThrows(NullPointerException.class, () -> new Sample((String) null));
		return;
	}

	@Test
	@SuppressWarnings("deprecation")
	public void stringConstructorStillReadsResources() {
		assertEquals(WAV_GOOD_SAMPLES, new Sample(WAV_GOOD_FILENAME).getSampleCount());
		return;
	}

	@Test
	@SuppressWarnings("deprecation")
	public void stringConstructorNamesAMissingResource() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new Sample("/no/such/sample.wav"));
		assertTrue(e.getMessage().contains("'/no/such/sample.wav'"), e.getMessage());
		return;
	}

	@Test
	public void urlConstructorThrowsOnNull() {
		NullPointerException e = assertThrows(NullPointerException.class, () -> new Sample((URL) null));
		assertTrue(e.getMessage().contains("getResource()"), e.getMessage());
		return;
	}

	@Test
	public void inputStreamConstructorThrowsOnNull() {
		assertThrows(NullPointerException.class, () -> new Sample((InputStream) null));
		return;
	}

	@Test
	public void constructorThrowsOnWrongFormat() {
		assertThrows(RuntimeException.class, () -> new Sample(resource(MP3_FILENAME)));
		return;
	}

	@Test
	public void urlConstructorThrowsOnWrongAudioParameters() {
		assertThrows(IllegalArgumentException.class, () -> new Sample(resource(WAV_BAD_FILENAME)));
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
		Sample sample = new Sample(resource(WAV_GOOD_FILENAME));
		assertNotNull(sample);
		assertEquals(WAV_GOOD_SAMPLES, sample.getSampleCount());
		return;
	}

	@Test
	public void audioCanBeReadMoreThanOnce() throws IOException {
		Sample sample = new Sample(resource(WAV_GOOD_FILENAME));
		byte[] first = readAll(sample.getAudioInputStream());
		byte[] second = readAll(sample.getAudioInputStream());
		assertEquals(WAV_GOOD_SAMPLES * 2, first.length);
		assertArrayEquals(first, second);
		return;
	}

	@Test
	public void decodesKnownSamples() {
		// Low bytes of 0x80 and above used to decode 256 steps too low (#41).
		short[] values = { 0, 1, -1, 127, 128, 255, 256, -128, -129, -256, 1000, -1000, 32767, -32768 };
		double[] decoded = new Sample(new ByteArrayInputStream(wav(values))).getInterleavedSamples();
		assertEquals(values.length, decoded.length);
		for (int i = 0; i < values.length; i++) {
			assertEquals(values[i] / 32768.0, decoded[i], 0.0, "sample " + i + " (" + values[i] + ")");
		}
		return;
	}

	@Test
	public void readsAStreamThatArrivesInSmallPieces() {
		short[] values = new short[4000];
		for (int i = 0; i < values.length; i++) {
			values[i] = (short) (i * 7);
		}
		// A stream that never reports anything available, and gives up at most 7 bytes per read
		InputStream trickle = new FilterInputStream(new ByteArrayInputStream(wav(values))) {
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return super.read(b, off, Math.min(len, 7));
			}

			@Override
			public int available() {
				return 0;
			}
		};
		double[] decoded = new Sample(trickle).getInterleavedSamples();
		assertEquals(values.length, decoded.length);
		assertEquals(values[values.length - 1] / 32768.0, decoded[decoded.length - 1], 0.0);
		return;
	}

	@Test
	public void toWavReturnsAWavFileWithEverySample() {
		Sample sample = new Sample(resource(WAV_GOOD_FILENAME));
		byte[] wav = sample.toWav();
		assertEquals("RIFF", new String(wav, 0, 4, StandardCharsets.US_ASCII));
		assertEquals("WAVE", new String(wav, 8, 4, StandardCharsets.US_ASCII));
		assertEquals(44 + WAV_GOOD_SAMPLES * 2, wav.length);
		assertArrayEquals(sample.getInterleavedSamples(), new Sample(new ByteArrayInputStream(wav)).getInterleavedSamples());
		return;
	}

	@Test
	public void writeWavWritesTheSameBytesEveryTime() throws IOException {
		Sample sample = new Sample(resource(WAV_GOOD_FILENAME));
		ByteArrayOutputStream first = new ByteArrayOutputStream();
		ByteArrayOutputStream second = new ByteArrayOutputStream();
		sample.writeWav(first);
		sample.writeWav(second);
		assertArrayEquals(sample.toWav(), first.toByteArray());
		assertArrayEquals(first.toByteArray(), second.toByteArray());
		return;
	}

	/**
	 * Returns the {@link URL} of the test resource {@code name}.
	 *
	 * @param name resource name
	 * @return {@link URL}
	 */
	private static URL resource(String name) {
		return SampleTest.class.getResource(name);
	}

	/**
	 * Returns a WAV file in {@link Sample#SC_AUDIO_FORMAT} containing {@code values}.
	 *
	 * @param values 16-bit samples
	 * @return WAV file contents
	 */
	private static byte[] wav(short[] values) {
		byte[] data = new byte[values.length * 2];
		for (int i = 0; i < values.length; i++) {
			data[2 * i] = (byte) values[i];
			data[2 * i + 1] = (byte) (values[i] >> 8);
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(data), Sample.SC_AUDIO_FORMAT, values.length),
					AudioFileFormat.Type.WAVE, out);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return out.toByteArray();
	}

	/**
	 * Returns every byte remaining in {@code in}.
	 *
	 * @param in an {@link InputStream}
	 * @return bytes read
	 * @throws IOException if unable to read {@code in}
	 */
	private static byte[] readAll(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int count;
		while ((count = in.read(buffer)) != -1) {
			out.write(buffer, 0, count);
		}
		return out.toByteArray();
	}
}
