![](https://github.com/logicsquad/nanocaptcha/workflows/build/badge.svg)
[![License](https://img.shields.io/badge/License-BSD-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

NanoCaptcha
===========

What is this?
-------------
NanoCaptcha is a Java library for generating image and audio
CAPTCHAs. NanoCaptcha is intended to be:

* Self-contained: no network API hits to any external services.

* Dependency-free: using NanoCaptcha doesn't pull in any other JARs.

It's worth being clear about what a CAPTCHA like this can do. Modern
OCR and speech recognition can read short text and digit CAPTCHAs
reliably, so NanoCaptcha is a speed bump for untargeted form spam,
not a barrier to a determined attacker. Its strengths are that it's
self-hosted, sends nothing to third parties, doesn't need JavaScript,
and has an audio alternative. For stronger self-hosted protection,
combine it with a honeypot field, timing checks, or a proof-of-work
scheme such as [ALTCHA](https://github.com/altcha-org/altcha-lib-java).

Getting started
---------------
You can build a minimal image CAPTCHA very easily:

    ImageCaptcha imageCaptcha = ImageCaptcha.create();

This creates a 200 x 50 pixel image with a light grey background,
and adds five random characters from the Latin alphabet. The
`getImage()` method returns the image as a `BufferedImage` object.
`isCorrect(String)` will verify the supplied string against the text
content of the image. If you need the text content itself, call
`getContent()`.  Image CAPTCHAs can be further customised by:

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

A `Builder` draws as it goes, so each one makes a single CAPTCHA: use
a new `Builder` for each.

The built-in fonts can display everything NanoCaptcha's own content
producers generate. For your own content in other scripts, supply a
font that can display it, such as one installed on the server:

    new DefaultWordRenderer.Builder()
        .font(new Font("Noto Sans CJK SC", Font.BOLD, 40))
        .build()

If a renderer's font can't display a character, it throws an
`IllegalArgumentException` rather than drawing an empty box.

The built-in fonts are sized to the image: 40 pt in the default height
of 50 pixels, and in proportion otherwise. A font you supply keeps its
size. Either way, text too wide for the image shrinks to fit.

To send an image CAPTCHA to a browser, `writePng()` writes it to an
`OutputStream` as a PNG file, for example in a servlet:

    response.setContentType("image/png");
    imageCaptcha.writePng(response.getOutputStream());

`toPng()` returns the same PNG file as a `byte[]`, and `toDataUri()`
returns it as a `data:` URI, which can go straight into the `src` of
an `<img>` tag, so there's no separate request for the image.

An image with a `TransparentBackgroundProducer` is transparent where
nothing is drawn, and JPEG can't store transparency, so
`ImageIO.write(imageCaptcha.getImage(), "jpg", out)` returns `false`
and writes nothing. Use PNG, or keep an opaque background, such as the
default light grey.

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

To send the clip to a browser instead, `getAudio().writeWav()` writes
it to an `OutputStream` as a WAV file, and `getAudio().toWav()`
returns the WAV file as a `byte[]`.

Using NanoCaptcha
-----------------
You can use NanoCaptcha in your projects by including it as a Maven dependency:

    <dependency>
      <groupId>net.logicsquad</groupId>
      <artifactId>nanocaptcha</artifactId>
      <version>2.3</version>
    </dependency>

Using NanoCaptcha in a web application
--------------------------------------
Most of the protection a CAPTCHA gives comes from how it's used:

* Keep only the answer, from `getContent()`, on the server and tied to
  the visitor's session, along with when it was created, from
  `getCreated()`. Never send the answer to the browser, in a hidden
  field, a cookie or anywhere else. There's no need to keep the CAPTCHA
  itself.

* Allow one attempt per CAPTCHA, right or wrong, and then make a new
  one. A five-digit answer has 100,000 possibilities, and five
  characters from `LatinContentProducer` about 6.4 million, so
  unlimited guesses would get through eventually.

* Expire CAPTCHAs after a few minutes.

* Rate-limit how often each client can get a new CAPTCHA and submit an
  answer.

* Mobile keyboards often capitalise the first letter, and autofill can
  add a space. `isCorrect()` ignores case and whitespace at either end
  of the answer, and `isCorrect(answer, false)` compares exactly. If
  you keep only the answer, as below, compare it the same way, and add
  `autocapitalize="none"` to the input field anyway.

* Send images as PNG and audio as WAV, as described above, and offer
  an audio CAPTCHA as an alternative to the image.

For example, in a servlet:

    // Showing the form
    ImageCaptcha captcha = ImageCaptcha.create();
    session.setAttribute("captchaAnswer", captcha.getContent());
    session.setAttribute("captchaCreated", captcha.getCreated());
    // ... and put captcha.toDataUri() in the form's <img> tag

    // Checking the form: one attempt, within five minutes
    String answer = (String) session.getAttribute("captchaAnswer");
    OffsetDateTime created = (OffsetDateTime) session.getAttribute("captchaCreated");
    session.removeAttribute("captchaAnswer");
    session.removeAttribute("captchaCreated");
    String given = request.getParameter("captcha");
    boolean passed = answer != null && given != null
        && created.isAfter(OffsetDateTime.now().minusMinutes(5))
        && answer.equalsIgnoreCase(given.strip());

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

Java also copies each font to a temporary file while loading it, so
the first image CAPTCHA needs a writable temporary directory. With a
read-only root filesystem, as with `docker run --read-only` or
Kubernetes's `readOnlyRootFilesystem: true`, mount a writable
directory at `/tmp`, such as a `tmpfs` or an `emptyDir` volume, or
point `-Djava.io.tmpdir` at one. Otherwise NanoCaptcha fails with an
error saying it can't create a temporary file.

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
