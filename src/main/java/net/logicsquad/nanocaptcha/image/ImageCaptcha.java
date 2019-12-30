package net.logicsquad.nanocaptcha.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.OffsetDateTime;

import javax.imageio.ImageIO;

import net.logicsquad.nanocaptcha.content.AbstractContentProducer;
import net.logicsquad.nanocaptcha.content.LatinContentProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.BackgroundProducer;
import net.logicsquad.nanocaptcha.image.backgrounds.TransparentBackgroundProducer;
import net.logicsquad.nanocaptcha.image.filter.ImageFilter;
import net.logicsquad.nanocaptcha.image.filter.RippleImageFilter;
import net.logicsquad.nanocaptcha.image.noise.CurvedLineNoiseProducer;
import net.logicsquad.nanocaptcha.image.noise.NoiseProducer;
import net.logicsquad.nanocaptcha.image.renderer.DefaultWordRenderer;
import net.logicsquad.nanocaptcha.image.renderer.WordRenderer;
import net.logicsquad.nanocaptcha.content.ContentProducer;

/**
 * A builder for generating a CAPTCHA image/answer pair.
 * 
 * <p>
 * Example for generating a new CAPTCHA:
 * </p>
 * <pre>Captcha captcha = new Captcha.Builder(200, 50)
 * 	.addText()
 * 	.addBackground()
 * 	.build();</pre>
 * <p>Note that the <code>build()</code> must always be called last. Other methods are optional,
 * and can sometimes be repeated. For example:</p>
 * <pre>Captcha captcha = new Captcha.Builder(200, 50)
 * 	.addText()
 * 	.addNoise()
 * 	.addNoise()
 * 	.addNoise()
 * 	.addBackground()
 * 	.build();</pre>
 * <p>Adding multiple backgrounds has no affect; the last background added will simply be the
 * one that is eventually rendered.</p>
 * <p>To validate that <code>answerStr</code> is a correct answer to the CAPTCHA:</p>
 * 
 * <code>captcha.isCorrect(answerStr);</code>
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public final class ImageCaptcha implements Serializable {

    private static final long serialVersionUID = 617511236L;
    public static final String NAME = "simpleCaptcha";
    private Builder _builder;

    private ImageCaptcha(Builder builder) {
        _builder = builder;
    }

    public static class Builder implements Serializable {
        private static final long serialVersionUID = 12L;
        /**
         * @serial
         */
        private String _answer = "";
        /**
         * @serial
         */
        private BufferedImage _img;
        /**
         * @serial
         */
        private BufferedImage _bg;
        /**
         * @serial
         */
        private OffsetDateTime created;

        private boolean _addBorder = false;

        public Builder(int width, int height) {
            _img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        /**
         * Add a background using the default {@link BackgroundProducer} (a {@link TransparentBackgroundProducer}).
         */
        public Builder addBackground() {
            return addBackground(new TransparentBackgroundProducer());
        }

        /**
         * Add a background using the given {@link BackgroundProducer}.
         * 
         * @param bgProd
         */
        public Builder addBackground(BackgroundProducer bgProd) {
        	_bg = bgProd.getBackground(_img.getWidth(), _img.getHeight());
            
            return this;
        }

        /**
         * Generate the answer to the CAPTCHA using the {@link AbstractContentProducer}.
         */
        public Builder addText() {
            return addText(new LatinContentProducer());
        }

        /**
         * Generate the answer to the CAPTCHA using the given
         * {@link ContentProducer}.
         * 
         * @param txtProd
         */
        public Builder addText(ContentProducer txtProd) {
            return addText(txtProd, new DefaultWordRenderer());
        }

        /**
         * Generate the answer to the CAPTCHA using the default
         * {@link ContentProducer}, and render it to the image using the given
         * {@link WordRenderer}.
         *
         * @param wRenderer
         */
        public Builder addText(WordRenderer wRenderer) {
        	return addText(new LatinContentProducer(), wRenderer);
        }

        /**
         * Generate the answer to the CAPTCHA using the given
         * {@link ContentProducer}, and render it to the image using the given
         * {@link WordRenderer}.
         *
         * @param txtProd
         * @param wRenderer
         */
        public Builder addText(ContentProducer txtProd, WordRenderer wRenderer) {
        	_answer += txtProd.getContent();
        	wRenderer.render(_answer, _img);
        	
        	return this;
        }

        /**
         * Add noise using the default {@link NoiseProducer} (a {@link CurvedLineNoiseProducer}).
         */
        public Builder addNoise() {
            return this.addNoise(new CurvedLineNoiseProducer());
        }

        /**
         * Add noise using the given NoiseProducer.
         * 
         * @param nProd
         */
        public Builder addNoise(NoiseProducer nProd) {
            nProd.makeNoise(_img);
            return this;
        }

        /**
         * Filter the image using the default {@link ImageFilter} (a {@link RippleImageFilter}).
         */
        public Builder filter() {
            return filter(new RippleImageFilter());
        }

        /**
         * Filter the image using the given {@link ImageFilter}.
         * 
         * @param filter
         */
        public Builder filter(ImageFilter filter) {
            filter.filter(_img);
            return this;
        }

        /**
         * Draw a single-pixel wide black border around the image.
         */
        public Builder addBorder() {
        	_addBorder = true;

            return this;
        }

        /**
         * Build the CAPTCHA. This method should always be called, and should always
         * be called last.
         * 
         * @return The constructed CAPTCHA.
         */
        public ImageCaptcha build() {
        	if (_bg == null) {
        		_bg = new TransparentBackgroundProducer().getBackground(_img.getWidth(), _img.getHeight());
        	}

        	// Paint the main image over the background
        	Graphics2D g = _bg.createGraphics();
        	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        	g.drawImage(_img, null, null);
        	
        	if (_addBorder) {
        		int width = _img.getWidth();
        		int height = _img.getHeight();
        		
	            g.setColor(Color.BLACK);
	            g.drawLine(0, 0, 0, width);
	            g.drawLine(0, 0, width, 0);
	            g.drawLine(0, height - 1, width, height - 1);
	            g.drawLine(width - 1, height - 1, width - 1, 0);
        	}

        	_img = _bg;

            created = OffsetDateTime.now();

            return new ImageCaptcha(this);
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[Answer: ");
            sb.append(_answer);
            sb.append("][Timestamp: ");
            sb.append(created);
            sb.append("][Image: ");
            sb.append(_img);
            sb.append("]");

            return sb.toString();
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(_answer);
            out.writeObject(created);
            ImageIO.write(_img, "png", ImageIO.createImageOutputStream(out));
        }

        private void readObject(ObjectInputStream in) throws IOException,
                ClassNotFoundException {
            _answer = (String) in.readObject();
            created = (OffsetDateTime) in.readObject();
            _img = ImageIO.read(ImageIO.createImageInputStream(in));
        }
    }

    public boolean isCorrect(String answer) {
        return answer.equals(_builder._answer);
    }
    
    public String getContent() {
    	return _builder._answer;
    }

    /**
     * Get the CAPTCHA image, a PNG.
     *
     * @return A PNG CAPTCHA image.
     */
    public BufferedImage getImage() {
        return _builder._img;
    }

    public OffsetDateTime getCreated() {
        return _builder.created;
    }

    @Override
    public String toString() {
        return _builder.toString();
    }
}
