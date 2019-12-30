package net.logicsquad.nanocaptcha.audio;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.logicsquad.nanocaptcha.audio.noise.NoiseProducer;
import net.logicsquad.nanocaptcha.audio.noise.RandomNoiseProducer;
import net.logicsquad.nanocaptcha.audio.producer.RandomNumberVoiceProducer;
import net.logicsquad.nanocaptcha.audio.producer.VoiceProducer;
import net.logicsquad.nanocaptcha.content.ContentProducer;
import net.logicsquad.nanocaptcha.content.NumbersContentProducer;

/**
 * An audio CAPTCHA.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public final class AudioCaptcha {
	/**
	 * Generated audio
	 */
	private final Sample audio;

	/**
	 * Text content of audio
	 */
	private final String content;

	/**
	 * Creation timestamp
	 */
	private final OffsetDateTime created;

	/**
	 * Constructor
	 * 
	 * @param builder a {@link Builder} object
	 */
	private AudioCaptcha(Builder builder) {
		audio = builder.audio;
		content = builder.content;
		created = OffsetDateTime.now();
		return;
	}

	/**
	 * Build for an {@link AudioCaptcha}.
	 */
	public static class Builder {
		/**
		 * Random number generator
		 */
		private static final Random RAND = new SecureRandom();

		/**
		 * Text content
		 */
		private String content = "";

		/**
		 * Generated audio sample
		 */
		private Sample audio;

		/**
		 * {@link VoiceProducer}s
		 */
		private final List<VoiceProducer> voiceProducers;

		/**
		 * {@link NoiseProducer}s
		 */
		private final List<NoiseProducer> noiseProducers;

		/**
		 * Constructor
		 */
		public Builder() {
			voiceProducers = new ArrayList<VoiceProducer>();
			noiseProducers = new ArrayList<NoiseProducer>();
			return;
		}

		/**
		 * Adds content using the default {@link ContentProducer}
		 * ({@link NumbersContentProducer}).
		 * 
		 * @return this
		 */
		public Builder addContent() {
			return addContent(new NumbersContentProducer());
		}

		/**
		 * Adds content using {@code contentProducer}.
		 * 
		 * @param contentProducer a {@link ContentProducer}
		 * @return this
		 */
		public Builder addContent(ContentProducer contentProducer) {
			content += contentProducer.getContent();
			return this;
		}

		/**
		 * Adds the default {@link VoiceProducer} ({@link RandomNumberVoiceProducer}).
		 * 
		 * @return this
		 */
		public Builder addVoice() {
			voiceProducers.add(new RandomNumberVoiceProducer());
			return this;
		}

		/**
		 * Adds {@code voiceProducer}.
		 * 
		 * @param voiceProducer a {@link VoiceProducer}
		 * @return this
		 */
		public Builder addVoice(VoiceProducer voiceProducer) {
			voiceProducers.add(voiceProducer);
			return this;
		}

		/**
		 * Adds background noise using default {@link NoiseProducer}
		 * ({@link RandomNoiseProducer}).
		 * 
		 * @return this
		 */
		public Builder addNoise() {
			return addNoise(new RandomNoiseProducer());
		}

		/**
		 * Adds noise using {@code noiseProducer}.
		 * 
		 * @param noiseProducer a {@link NoiseProducer}
		 * @return this
		 */
		public Builder addNoise(NoiseProducer noiseProducer) {
			noiseProducers.add(noiseProducer);
			return this;
		}

		/**
		 * Builds the audio CAPTCHA described by this object.
		 * 
		 * @return {@link AudioCaptcha} as described by this {@code Builder}
		 */
		public AudioCaptcha build() {
			// Make sure we have at least one voiceProducer
			if (voiceProducers.isEmpty()) {
				addVoice();
			}

			// Convert answer to an array
			char[] ansAry = content.toCharArray();

			// Make a List of Samples for each character
			VoiceProducer vProd;
			List<Sample> samples = new ArrayList<Sample>();
			for (char c : ansAry) {
				// Create Sample for this character from one of the
				// VoiceProducers
				vProd = voiceProducers.get(RAND.nextInt(voiceProducers.size()));
				samples.add(vProd.getVocalization(c));
			}

			// 3. Add noise, if any, and return the result
			if (!noiseProducers.isEmpty()) {
				NoiseProducer nProd = noiseProducers.get(RAND.nextInt(noiseProducers.size()));
				audio = nProd.addNoise(samples);
				return new AudioCaptcha(this);
			}

			audio = Mixer.append(samples);
			return new AudioCaptcha(this);
		}
	}

	/**
	 * Does CAPTCHA content match supplied {@code answer}?
	 * 
	 * @param answer a candidate content match
	 * @return {@code true} if {@code answer} matches CAPTCHA content, otherwise
	 *         {@code false}
	 */
	public boolean isCorrect(String answer) {
		return answer.equals(content);
	}

	/**
	 * Returns content of this CAPTCHA.
	 * 
	 * @return content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Returns the audio for this {@code AudioCaptcha}.
	 *
	 * @return CAPTCHA audio
	 */
	public Sample getAudio() {
		return audio;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(35);
		sb.append("[AudioCaptcha: created=").append(created).append(" content='").append(content).append("']");
		return sb.toString();
	}

	/**
	 * Returns creation timestamp.
	 * 
	 * @return creation timestamp
	 */
	public OffsetDateTime getCreated() {
		return created;
	}
}
