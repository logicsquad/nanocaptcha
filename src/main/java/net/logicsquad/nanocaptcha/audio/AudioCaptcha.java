package net.logicsquad.nanocaptcha.audio;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

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
	 * Quietest volume for a digit, as a multiplier
	 */
	private static final double MIN_VOLUME = 0.7;

	/**
	 * Longest gap after a digit (in samples): a quarter of a second
	 */
	private static final int MAX_GAP = (int) (Sample.SC_AUDIO_FORMAT.getSampleRate() / 4);

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
	 * @param content text content
	 * @param audio   generated audio
	 */
	private AudioCaptcha(String content, Sample audio) {
		this.content = content;
		this.audio = audio;
		created = OffsetDateTime.now();
		return;
	}

	/**
	 * <p>
	 * Returns a new {@code AudioCaptcha} with some very basic settings:
	 * </p>
	 *
	 * <ul>
	 * <li>{@link NumbersContentProducer} with length 5; and</li>
	 * <li>{@link RandomNumberVoiceProducer} in its default language.</li>
	 * </ul>
	 *
	 * <p>
	 * That is, the audio clip will contain five numbers read out in English, unless the
	 * {@code net.logicsquad.nanocaptcha.audio.producer.RandomNumberVoiceProducer.defaultLanguage} system property names
	 * another supported language. The JVM's default {@link java.util.Locale Locale} isn't used.
	 * </p>
	 *
	 * @return new {@code AudioCaptcha}
	 * @since 2.0
	 */
	public static AudioCaptcha create() {
		return new Factory.Builder().addContent().build().create();
	}

	/**
	 * <p>
	 * Makes {@link AudioCaptcha}s, each with new content and randomness. A {@code Factory} can't be changed, and it's safe
	 * to share between threads, so a web application can build one when it starts, and ask it for a CAPTCHA for each
	 * request:
	 * </p>
	 *
	 * <pre>
	 * AudioCaptcha.Factory captchas = new AudioCaptcha.Factory.Builder().addContent().addNoise().build();
	 * AudioCaptcha captcha = captchas.create();
	 * </pre>
	 *
	 * <p>
	 * Every CAPTCHA a {@code Factory} makes uses the same producers, so any of your own need to be thread-safe, as
	 * NanoCaptcha's own are.
	 * </p>
	 *
	 * @since 3.0
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/64">#64</a>
	 */
	public static final class Factory {
		/**
		 * {@link ContentProducer}s, whose content is read out in order
		 */
		private final List<ContentProducer> contentProducers;

		/**
		 * {@link VoiceProducer}s, at least one
		 */
		private final List<VoiceProducer> voiceProducers;

		/**
		 * {@link NoiseProducer}s
		 */
		private final List<NoiseProducer> noiseProducers;

		/**
		 * Constructor
		 *
		 * @param builder a {@link Builder}, whose configuration this copies
		 */
		private Factory(Builder builder) {
			contentProducers = List.copyOf(builder.contentProducers);
			// Make sure we have at least one voiceProducer
			voiceProducers = builder.voiceProducers.isEmpty() ? List.of(new RandomNumberVoiceProducer())
					: List.copyOf(builder.voiceProducers);
			noiseProducers = List.copyOf(builder.noiseProducers);
			return;
		}

		/**
		 * Returns a new {@link AudioCaptcha}, with new content and randomness.
		 *
		 * @return new {@link AudioCaptcha}
		 */
		public AudioCaptcha create() {
			StringBuilder sb = new StringBuilder();
			for (ContentProducer contentProducer : contentProducers) {
				sb.append(contentProducer.getContent());
			}
			String content = sb.toString();

			// Convert answer to an array
			char[] ansAry = content.toCharArray();

			// Make a List of Samples for each character, each at its own volume and
			// followed by its own gap, so that the clips can't be matched one by one
			ThreadLocalRandom random = ThreadLocalRandom.current();
			VoiceProducer vProd;
			List<Sample> samples = new ArrayList<>();
			for (int i = 0; i < ansAry.length; i++) {
				// Create Sample for this character from one of the
				// VoiceProducers
				vProd = voiceProducers.get(random.nextInt(voiceProducers.size()));
				double volume = MIN_VOLUME + (1 - MIN_VOLUME) * random.nextDouble();
				int gap = i == ansAry.length - 1 ? 0 : random.nextInt(MAX_GAP + 1);
				samples.add(AudioMixer.adjust(vProd.getVocalization(ansAry[i]), volume, gap));
			}

			// Add noise, if any, and return the result
			if (!noiseProducers.isEmpty()) {
				NoiseProducer nProd = noiseProducers.get(random.nextInt(noiseProducers.size()));
				return new AudioCaptcha(content, nProd.addNoise(samples));
			}
			return new AudioCaptcha(content, AudioMixer.concatenate(samples));
		}

		/**
		 * Builder for a {@link Factory}. A {@code Builder} isn't thread-safe, but the {@link Factory} it builds is. Changing a
		 * {@code Builder} after {@link #build()} doesn't change the {@link Factory} it built.
		 *
		 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/64">#64</a>
		 */
		public static final class Builder implements net.logicsquad.nanocaptcha.Builder<Factory> {
			/**
			 * {@link ContentProducer}s, whose content is read out in the order they were added
			 */
			private final List<ContentProducer> contentProducers;

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
				contentProducers = new ArrayList<>();
				voiceProducers = new ArrayList<>();
				noiseProducers = new ArrayList<>();
				return;
			}

			/**
			 * Adds content using the default {@link ContentProducer} ({@link NumbersContentProducer}).
			 *
			 * @return this
			 */
			public Builder addContent() {
				return addContent(new NumbersContentProducer());
			}

			/**
			 * Adds content (of length {@code length}) using the default {@link ContentProducer} ({@link NumbersContentProducer}).
			 *
			 * @param length number of content units to add
			 * @return this
			 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/9">#9</a>
			 * @since 1.4
			 */
			public Builder addContent(int length) {
				return addContent(new NumbersContentProducer(length));
			}

			/**
			 * Adds content using {@code contentProducer}.
			 *
			 * @param contentProducer a {@link ContentProducer}
			 * @return this
			 */
			public Builder addContent(ContentProducer contentProducer) {
				contentProducers.add(Objects.requireNonNull(contentProducer));
				return this;
			}

			/**
			 * Adds the default {@link VoiceProducer} ({@link RandomNumberVoiceProducer}).
			 *
			 * @return this
			 */
			public Builder addVoice() {
				return addVoice(new RandomNumberVoiceProducer());
			}

			/**
			 * Adds {@code voiceProducer}.
			 *
			 * @param voiceProducer a {@link VoiceProducer}
			 * @return this
			 */
			public Builder addVoice(VoiceProducer voiceProducer) {
				voiceProducers.add(Objects.requireNonNull(voiceProducer));
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
				noiseProducers.add(Objects.requireNonNull(noiseProducer));
				return this;
			}

			/**
			 * Builds a {@link Factory} that makes CAPTCHAs as this {@code Builder} is configured now.
			 *
			 * @return new {@link Factory}
			 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/64">#64</a>
			 */
			@Override
			public Factory build() {
				return new Factory(this);
			}
		}
	}

	/**
	 * Does CAPTCHA content match supplied {@code answer}? Case is ignored, so that an answer a mobile keyboard has
	 * capitalised still matches, and so is whitespace at either end, which autofill can add. For an exact comparison,
	 * use {@link #isCorrect(String, boolean)}. If {@code answer} is {@code null}, this method returns {@code false}.
	 *
	 * @param answer a candidate content match
	 * @return {@code true} if {@code answer} matches CAPTCHA content, otherwise
	 *         {@code false}
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/46">#46</a>
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/69">#69</a>
	 */
	public boolean isCorrect(String answer) {
		return isCorrect(answer, true);
	}

	/**
	 * Does CAPTCHA content match supplied {@code answer}? With {@code normalise}, case and whitespace at either end of
	 * {@code answer} are ignored, as {@link #isCorrect(String)} ignores them. Without it, {@code answer} has to match
	 * exactly. If {@code answer} is {@code null}, this method returns {@code false}.
	 *
	 * @param answer    a candidate content match
	 * @param normalise whether to ignore case, and whitespace at either end of {@code answer}
	 * @return {@code true} if {@code answer} matches CAPTCHA content, otherwise
	 *         {@code false}
	 * @since 3.0
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/69">#69</a>
	 */
	public boolean isCorrect(String answer, boolean normalise) {
		if (answer == null) {
			return false;
		}
		return normalise ? content.equalsIgnoreCase(answer.strip()) : content.equals(answer);
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

	/**
	 * Returns a description of this {@code AudioCaptcha} for debugging, with the length of its answer, but not the answer
	 * itself, which would then end up wherever the description does. For the answer, use {@link #getContent()}.
	 *
	 * @return description
	 * @see <a href="https://github.com/logicsquad/nanocaptcha/issues/70">#70</a>
	 */
	@Override
	public String toString() {
		int length = content.codePointCount(0, content.length());
		StringBuilder sb = new StringBuilder(64);
		sb.append("[AudioCaptcha: created=").append(created).append(" content=").append(length)
				.append(length == 1 ? " character]" : " characters]");
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
