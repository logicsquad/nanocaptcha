import java.awt.AlphaComposite
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.Font
import java.awt.Color
import java.awt.GradientPaint
import java.awt.geom.Rectangle2D

import javax.imageio.ImageIO

import nl.captcha.text.producer.*
import nl.captcha.text.renderer.*
import nl.captcha.backgrounds.*
import nl.captcha.gimpy.*
import nl.captcha.Captcha
import nl.captcha.servlet.CaptchaServletUtil

class ArabicController {
	private static final int WIDTH = 230
	private static final int HEIGHT = 50
	
	def simple = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ArabicTextProducer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

	def simple_long = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ArabicTextProducer(10))
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def noisy = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ArabicTextProducer())
            .addNoise()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def multi = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addBackground(new GradiatedBackgroundProducer())
            .addText(new ArabicTextProducer())
            .gimp()
            .gimp(new DropShadowGimpyRenderer())
            .addNoise()
            .addNoise()
            .addNoise()
            .addBorder()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }
}
