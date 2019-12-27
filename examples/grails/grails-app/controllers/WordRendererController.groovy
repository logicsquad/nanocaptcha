
import java.awt.Color
import java.awt.Font

import nl.captcha.Captcha
import nl.captcha.backgrounds.*
import nl.captcha.gimpy.*
import nl.captcha.text.producer.*
import nl.captcha.text.renderer.*
import nl.captcha.servlet.CaptchaServletUtil

class WordRendererController {
    private static final WIDTH = 150
    private static final HEIGHT = 50

    def simple = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def diff_fonts = {
        def colors = [Color.BLACK, Color.BLUE]
        def fonts = [new Font("Geneva", Font.ITALIC, 48), new Font("Courier", Font.BOLD, 48), new Font("Arial", Font.BOLD, 48)]
        def wordRenderer = new ColoredEdgesWordRenderer(colors, fonts, 0)
        def captcha = new Captcha.Builder(200, 70)
            .addText(wordRenderer)
            .gimp()
            .gimp(new DropShadowGimpyRenderer())
            .addBackground(new GradiatedBackgroundProducer())
            .addBorder()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def outlined = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ColoredEdgesWordRenderer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

}
