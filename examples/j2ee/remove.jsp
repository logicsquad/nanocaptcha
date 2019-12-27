<%@ page import="nl.captcha.Captcha" %>
<%@ page import="nl.captcha.audio.AudioCaptcha" %>

<meta http-equiv="refresh" content="1;url=stickyCaptcha.jsp" >

<% session.removeAttribute(Captcha.NAME); %>
<% session.removeAttribute(AudioCaptcha.NAME); %>
Removed.
