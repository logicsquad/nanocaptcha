import java.awt.Color

import nl.captcha.Captcha
import nl.captcha.backgrounds.*
import nl.captcha.servlet.CaptchaServletUtil

/** 
* Exercise the various background renderers.
**/

class BackgroundController {
    private static final WIDTH = 200
    private static final HEIGHT = 50

    def simple = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .addBackground()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def squiggles = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .addBackground(new SquigglesBackgroundProducer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def flatcolor = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .addBackground(new FlatColorBackgroundProducer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def blue = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .addBackground(new FlatColorBackgroundProducer(Color.BLUE))
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def gradiated = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .addBackground(new GradiatedBackgroundProducer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }
}
