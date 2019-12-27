<%@ page import="nl.captcha.Captcha" %>

<% 
// We're doing this in a JSP here, but in your own app you'll want to put
// this logic in your MVC framework of choice.

Captcha captcha = (Captcha) session.getAttribute(Captcha.NAME);
request.setCharacterEncoding("UTF-8"); // Do this so we can capture non-Latin chars
String answer = request.getParameter("answer");
if (captcha.isCorrect(answer)) { %>
    <b>Correct!</b>
<% } else { %>
    <b>Wrong!</b>
<% } %>
