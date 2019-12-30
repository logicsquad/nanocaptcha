[![Build Status](https://travis-ci.org/logicsquad/nanocaptcha.svg?branch=master)](https://travis-ci.org/logicsquad/nanocaptcha)
[![License](https://img.shields.io/badge/License-BSD-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

NanoCaptcha
===========

What is this?
-------------
NanoCaptcha is a Java library for generating text and audio
CAPTCHAs. NanoCaptcha is intended to be:

* Self-contained: no network API hits to any external services.

* Minimally-dependent: using NanoCaptcha should not involve pulling in
  a plethora of JARs.

Getting started
---------------
You can build a minimal image CAPTCHA very easily:

    Captcha captcha = new Captcha.Builder(200, 50).addText().build();

This creates a 200 x 50 pixel image and adds five random characters
from the Latin alphabet.  The `getImage()` method returns the image as
a `BufferedImage` object. `isCorrect(String)` will verify the supplied
string against the text content of the image. If you need the text
content itself, call `getAnswer()`.  Image CAPTCHAs can be further
customised by:

* Using different `ContentProducer`s (e.g., `ChineseContentProducer`)
  and `WordRenderer`s (e.g., `ColoredEdgesWordRenderer`).
* Adding noise using a `NoiseProducer`.
* Adding various `ImageFilter`s.
* Adding a background or a border.

Building a minimal audio CAPTCHA is just as easy:

    	AudioCaptcha audio = new AudioCaptcha.Builder().addAnswer().build();

As with image CAPTCHAs, these can be further customised by:

* Adding background noise with a `NoiseProducer`.

Using NanoCaptcha
-----------------
At the moment you'll need to build NanoCaptcha from source:

    $ git clone https://github.com/logicsquad/nanocaptcha.git NanoCaptcha
    $ cd NanoCaptcha
    $ mvn package

Note that _the API may change prior to a 1.0 release,_ at which time
it will remain stable, consistent with [semantic
versioning](https://semver.org).

Contributing
------------
By all means, open issue tickets and pull requests if you have something
to contribute.

References
----------
NanoCaptcha is based on
[SimpleCaptcha](https://sourceforge.net/p/simplecaptcha/),
and uses
[JH Labs Java Image Filters](http://huxtable.com/ip/filters/).
