import java.awt.image.BufferedImage

import nl.captcha.Captcha
import nl.captcha.backgrounds.*
import nl.captcha.servlet.CaptchaServletUtil

/**
* This controller shows how CAPTCHAs are built using SimpleCaptcha. Each action
* builds a CAPTCHA with the default providers, e.g. addText() with no argument. 
*
* Example call:
*
* http://localhost:8080/sc/defaults/simple
*
**/

class DefaultsController {
    private static final WIDTH = 200
    private static final HEIGHT = 50

    def simple = { 
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def border = { 
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .addBorder()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def gimp = { 
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def noisy = { 
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .addNoise()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def noisygimp = { 
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp()
            .addNoise()
            .addBorder()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def all = { 
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp()
            .addNoise()
            .addBackground()
            .addBorder()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }
}
