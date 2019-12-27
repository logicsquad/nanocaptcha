import java.awt.AlphaComposite
import java.awt.Color
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp

import javax.imageio.ImageIO

import javax.media.jai.JAI
import javax.media.jai.RenderedOp

import com.sun.media.jai.codec.ImageCodec
import com.sun.media.jai.codec.ImageEncoder
import com.sun.media.jai.codec.TIFFEncodeParam

import nl.captcha.Captcha
import nl.captcha.backgrounds.*
import nl.captcha.servlet.CaptchaServletUtil

/** 
* Try to crack the various background renderers.
**/

class CrackController {
    static navigation = [
        order:2
    ]

    private static final WIDTH = 200
    private static final HEIGHT = 50

    def index = {
        render(view:"index")
    }

    def simple = {
        def capid = params.capid
        def attrName = Captcha.NAME + capid
        def captcha = session[attrName]

        if (captcha == null) {
            captcha = new Captcha.Builder(WIDTH, HEIGHT)
                .addText()
                .gimp(new nl.captcha.gimpy.FishEyeGimpyRenderer(Color.RED, Color.BLUE))
                .addBackground(new GradiatedBackgroundProducer())
                .addNoise()
                .build()

            session[attrName] = captcha
        }

        CaptchaServletUtil.writeImage(response, captcha.image)
    }

    private crack(String capId) {
        def attrName = Captcha.NAME + capId
        def captcha = session[attrName]
        if (captcha == null) {
            println("Getting captcha from session was null. This should not have happened.")
            return
        }

        def image = captcha.image
        BufferedImage dimg = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)

        dimg.createGraphics().with {
            setComposite(AlphaComposite.Src)
            drawImage(image, null, 0, 0)
            dispose()
        }

        (0..<dimg.height).each {i ->
            (0..<dimg.width).each {j ->
                if (dimg.getRGB(j, i) != Color.BLACK.RGB)
                {
                    dimg.setRGB(j, i, Color.WHITE.RGB)
                }
            }
        }

        session["crackedAnswer${capId}"] = doOcr(dimg)
    }

    def answer = {
        def ansId = params.ansId
        crack(ansId)
        def ans = session["crackedAnswer${ansId}"]
        if (!ans) {
            render "Not cracked"
            return
        }

        render ans
    }

    def again = {
        session.attributeNames.each { name ->
            session.removeAttribute(name)
        }

        redirect(action:index)
    }

    private doOcr = { image ->
        def tmpDir = System.properties['java.io.tmpdir']
        def tmpGif = "${tmpDir}tmp.gif"
        def tmpTif = "${tmpDir}tmp.tif"
        def tmpTesseract = "${tmpDir}tmp"
        ImageIO.write(image, 'gif', new File(tmpGif))
        convertToTiff(tmpGif, tmpTif)
        def tesseract = ['/opt/local/bin/tesseract', tmpTif, tmpTesseract].execute()
        tesseract.waitFor()
        return new File("${tmpTesseract}.txt").readLines()[0]
    }

    private void convertToTiff(String inputFile, String outputFile) {
        OutputStream ios
        try {
            ios = new BufferedOutputStream(new FileOutputStream(new File(outputFile)))
            ImageEncoder enc = ImageCodec.createImageEncoder("tiff", ios, new TIFFEncodeParam(compression: TIFFEncodeParam.COMPRESSION_NONE, littleEndian: false))
            RenderedOp src = JAI.create("fileload", inputFile)

            //Apply the color filter and return the result.
            ColorConvertOp filterObj = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null)
            BufferedImage dst = new BufferedImage(src.width, src.height, BufferedImage.TYPE_3BYTE_BGR)
            filterObj.filter(src.getAsBufferedImage(), dst)

            // save the output file
            enc.encode(dst)
        }
        catch (Exception e) {
            println e
        } finally {
            ios.close()
        }
    }
}
