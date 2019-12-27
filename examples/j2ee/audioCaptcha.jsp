<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML>
<html>
<head>
    <META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>SimpleCAPTCHA - Audio Example</title>
    <link href="sc.css" type="text/css" rel="stylesheet" />
    <script>
    /* Add bogus req param to url so that reloading will (hopefully) load a different .wav file. */
    function loadAudio() {
        document.getElementById("audioCaptcha").src = "audio.wav?bogus=" + new Date().getTime();
        document.getElementById("audioSupport").innerHTML = document.createElement('audio').canPlayType("audio/wav");
    }
    </script>
</head>
<body onload="loadAudio()">
<br>

<h3>SimpleCAPTCHA - Audio Example</h3>
<p>This page is used to demonstrate an audio CAPTCHA using the SimpleCaptcha framework. Below you should see audio controls. If you don't, click <a href="<c:url value="audio.wav" />">here</a> to browse directly to the servlet. Reloading the page will generate a new audio CAPTCHA.</p>

<audio controls autoplay id="audioCaptcha"></audio>

<p>Your browser support for the &lt;audio&gt; tag reports: "<span id="audioSupport"></span>"
<p><a href="<c:url value="simpleCaptcha.jsp" />">Simple CAPTCHA</a> | <a href="<c:url value="stickyCaptcha.jsp" />">Sticky CAPTCHA</a> | Audio CAPTCHA</p>
</body>
</html>

