<html>
    <head>
		<meta name="layout" content="main" />
        <title>SimpleCaptcha - Cracking Captchas</title>
        <g:javascript library="prototype" />
    </head>
    <body>
        <h1 style="margin-left:20px;">SimpleCaptcha - Cracking Captchas</h1>
        <div style="margin-left:20px;margin-bottom:20px;width:50%"><p>Try and crack some captchas using simple normalization techniques.</p>
        <p><a href="http://www.kellyrob99.com/blog/2010/03/14/breaking-weak-captcha-in-slightly-more-than-26-lines-of-groovy-code/">Reference.</a></p>
        </div>
        <div class="dialog" style="margin-left:20px;width:50%;">
            <table border="1">
                <thead>
                    <tr>
                        <td>Num</td>
                        <td>CAPTCHA</td>
                        <td>OCR Result</td>
                    </tr>
                </thead>
                <tbody>
                    <g:each var="i" in="${(0..<2)}">
                        <tr>
                            <td><h3>${i}</h3></td>
                            <td><img src="${createLink(action:'simple',controller:'crack',params:[capid:i])}"></td>
                            <td>
                                <div class="crackedAnswer" id="crackedAnswer${i}">
                                    <g:remoteLink controller="crack" action="answer" update="crackedAnswer${i}" params="'ansId=${i}'">Get Answer</g:remoteLink>
                                </div>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
            <a href="${createLink(action:'again',controller:'crack')}">Do it again (clears the session)</a>
        </div>
    </body>
</html>

