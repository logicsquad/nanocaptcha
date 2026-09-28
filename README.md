![](https://github.com/logicsquad/nanocaptcha/workflows/build/badge.svg)
[![License](https://img.shields.io/badge/License-BSD-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

NanoCaptcha
===========

What is this?
-------------
NanoCaptcha is a Java library for generating image and audio
CAPTCHAs. NanoCaptcha is intended to be:

* Self-contained: no network API hits to any external services.

* Minimally-dependent: using NanoCaptcha should not involve pulling in
  a plethora of JARs, and ideally none at all.

Getting started
---------------
You can build a minimal image CAPTCHA very easily:

    ImageCaptcha imageCaptcha = ImageCaptcha.create();

This creates a 200 x 50 pixel image and adds five random characters
from the Latin alphabet.  The `getImage()` method returns the image as
a `BufferedImage` object. `isCorrect(String)` will verify the supplied
string against the text content of the image. If you need the text
content itself, call `getContent()`.  Image CAPTCHAs can be further
customised by:

* Using different `ContentProducer`s (e.g., `NumbersContentProducer`).
* Supplying your own `Color`s and `Font`s.
* Adding noise using a `NoiseProducer`.
* Adding various `ImageFilter`s.
* Adding a background or a border.

To create a custom CAPTCHA, you can use an `ImageCaptcha.Builder`,
e.g.:

    ImageCaptcha imageCaptcha = new ImageCaptcha.Builder(400, 100)
        .addContent(new LatinContentProducer(7),
            new DefaultWordRenderer.Builder()
                .randomColor(Color.BLACK, Color.BLUE, Color.CYAN, Color.RED)
                .build())
        .addBackground(new GradiatedBackgroundProducer())
        .addNoise(new CurvedLineNoiseProducer())
        .build();

The built-in fonts can display everything NanoCaptcha's own content
producers generate, except for `ChineseContentProducer` and
`ArabicContentProducer`, which are deprecated and will be removed in
3.0. For those, or for your own content in other scripts, supply a
font that can display it, such as one installed on the server:

    new DefaultWordRenderer.Builder()
        .font(new Font("Noto Sans CJK SC", Font.BOLD, 40))
        .build()

`FastWordRenderer` only uses the built-in fonts. If a renderer's font
can't display a character, it throws an `IllegalArgumentException`
rather than drawing an empty box.

Building a minimal audio CAPTCHA is just as easy:

    AudioCaptcha audioCaptcha = AudioCaptcha.create();

This creates a CAPTCHA with an audio clip containing five numbers read
out in English. To customise your CAPTCHA, you can use
`AudioCaptcha.Builder`.

There is support for different languages. (Currently English, German
and French are supported.) You can set the system property
`net.logicsquad.nanocaptcha.audio.producer.RandomNumberVoiceProducer.defaultLanguage`
to a 2-digit code for a supported language, e.g., `de`, and
`AudioCaptcha.create()` will return German digit vocalizations. The
JVM's default `Locale` isn't used. Alternatively, you can supply a
`RandomNumberVoiceProducer` explicitly, for example in the language of
each visitor to a web application:

    AudioCaptcha audioCaptcha = new AudioCaptcha.Builder()
        .addContent()
        .addVoice(new RandomNumberVoiceProducer(request.getLocale()))
        .build();

Only the language counts, so `de-AT` gets German and `fr-CA` gets
French, and an unsupported language gets the default. You can even mix
languages by calling `addVoice()` with more than one
`RandomNumberVoiceProducer`.

As with image CAPTCHAs, these can be further customised by:

* Adding background noise with a `NoiseProducer`.

Playing the audio is probably application-dependent, but the following
snippet will play the clip locally:

    Clip clip = AudioSystem.getClip();
    clip.open(audioCaptcha.getAudio().getAudioInputStream());
    clip.start();
    Thread.sleep(10000);

(The call to `Thread.sleep()` is simply to keep the JVM alive long
enough to play the clip.)

Using NanoCaptcha
-----------------
You can use NanoCaptcha in your projects by including it as a Maven dependency:

    <dependency>
      <groupId>net.logicsquad</groupId>
      <artifactId>nanocaptcha</artifactId>
      <version>2.1</version>
    </dependency>

Running in containers
---------------------
NanoCaptcha draws image CAPTCHAs with its own fonts, but the JDK can
only use fonts when fontconfig and at least one font are installed.
Some container images leave them out, including Alpine-based JDK
images and slim images with a JDK copied in. There, NanoCaptcha fails
the first time it draws an image CAPTCHA, with an error beginning
"NanoCaptcha can't load its font". To fix it, add the packages to your
image:

    # Debian and Ubuntu
    RUN apt-get update && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core

    # Alpine
    RUN apk add --no-cache fontconfig ttf-dejavu

or start from a JDK image that already includes them, such as
`eclipse-temurin`. Alpine's packages don't help a JDK built for glibc,
such as the one in `bellsoft/liberica-openjdk-alpine`, so use its
`-musl` variant instead. Audio CAPTCHAs don't need fonts.

Contributing
------------
By all means, open issue tickets and pull requests if you have something
to contribute.

References
----------
NanoCaptcha is based on
[SimpleCaptcha](https://sourceforge.net/p/simplecaptcha/),
and incorporates code from
[JH Labs Java Image Filters](http://huxtable.com/ip/filters/).
