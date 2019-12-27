import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import javax.imageio.*;

import nl.captcha.Captcha
import nl.captcha.backgrounds.*
import nl.captcha.gimpy.*
import nl.captcha.text.renderer.*
import nl.captcha.servlet.CaptchaServletUtil

/**
* Exercise the different gimpy renderers.
*
**/

class GimpyController {
    private static final WIDTH = 200
    private static final HEIGHT = 50

    def simple = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def fisheye = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp(new FishEyeGimpyRenderer(Color.RED, Color.BLUE))
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def ripple = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp(new RippleGimpyRenderer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def shear = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp(new ShearGimpyRenderer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def shadow = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp(new DropShadowGimpyRenderer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def shadow_big = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp(new DropShadowGimpyRenderer(3, 95))
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def block = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp(new BlockGimpyRenderer())
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    def blockbig = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp(new BlockGimpyRenderer(5))
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    /*
    def stretch = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText(new ColoredEdgesWordRenderer([Color.BLUE], null))
            .gimp(new StretchGimpyRenderer(1, 4.0))
            .build()

        def scaledImg = captcha.image.getScaledInstance(1, 3, Image.SCALE_SMOOTH)
        def g = captcha.image.createGraphics()
        g.drawImage(scaledImg)

        CaptchaServletUtil.writeImage(response, scaledImg)
    }
    */

    def multi = {
        def captcha = new Captcha.Builder(WIDTH, HEIGHT)
            .addText()
            .gimp(new BlockGimpyRenderer())
            .gimp(new DropShadowGimpyRenderer())
            .gimp(new RippleGimpyRenderer())
            .gimp(new FishEyeGimpyRenderer())
            .addBackground(new GradiatedBackgroundProducer())
            .addBorder()
            .build()
        CaptchaServletUtil.writeImage(response, captcha.image)
    }
}
