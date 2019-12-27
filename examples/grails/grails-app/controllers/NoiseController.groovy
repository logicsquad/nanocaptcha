import java.awt.Color

import nl.captcha.Captcha
import nl.captcha.noise.*
import nl.captcha.backgrounds.*
import nl.captcha.servlet.CaptchaServletUtil

class NoiseController {
	private static final WIDTH = 200
    private static final HEIGHT = 50

    def simple = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addNoise()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def curved = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addNoise(new CurvedLineNoiseProducer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def curved_bigandblue = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addNoise(new CurvedLineNoiseProducer(Color.BLUE, 8))
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def straightline = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addNoise(new StraightLineNoiseProducer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def multilines = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addNoise(new StraightLineNoiseProducer(Color.RED, 6))
            .addNoise(new StraightLineNoiseProducer(Color.BLUE, 4))
            .addNoise(new StraightLineNoiseProducer(Color.WHITE, 2))
            .addBackground()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }
}
