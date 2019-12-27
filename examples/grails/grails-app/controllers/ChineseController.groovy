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

class ChineseController {
	private static final int WIDTH = 230
	private static final int HEIGHT = 50
	
	def simple = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ChineseTextProducer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def gimp = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ChineseTextProducer())
            .gimp()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def noisy = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ChineseTextProducer())
            .addNoise()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def multi = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addBackground(new GradiatedBackgroundProducer())
            .addText(new ChineseTextProducer())
            .gimp()
            .gimp(new DropShadowGimpyRenderer())
            .addNoise()
            .addNoise()
            .addNoise()
            .addBorder()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def block = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ChineseTextProducer())
            .gimp(new BlockGimpyRenderer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)

    }

    def shadow = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ChineseTextProducer())
            .gimp(new BlockGimpyRenderer())
            .gimp(new DropShadowGimpyRenderer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def outlined = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ChineseTextProducer(), new ColoredEdgesWordRenderer())
            .gimp(new DropShadowGimpyRenderer())
            .gimp()
            .addBackground(new GradiatedBackgroundProducer())
            .addBorder()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

	def big = {
        // Generate 7 Chinese characters
        def longChineseTxtProd = new ChineseTextProducer(7)
        // Set up our list of colors that the word renderer will randomly pick from
        def colors = [Color.BLACK, Color.BLUE, Color.RED]
        // Set up our list of fonts that the word renderer will randomly pick from
        def fonts = [new Font("Courier", Font.BOLD, 52)]
        // Build the word renderer
        def bigWordRenderer = new ColoredEdgesWordRenderer(colors, fonts, 0.0f)

        def captcha = new Captcha.Builder(400, 65)
            .addText(longChineseTxtProd, bigWordRenderer)
            .addNoise()
            .gimp()
            .gimp(new DropShadowGimpyRenderer())
            .addBackground(new GradiatedBackgroundProducer(Color.BLUE, Color.RED))
            .addBorder()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

}
