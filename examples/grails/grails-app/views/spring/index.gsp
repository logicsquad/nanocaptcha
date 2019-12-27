<html>
    <head>
		<meta name="layout" content="main" />
    </head>
    <body>
        <h1 style="margin-left:20px;">SimpleCaptcha - Spring Generation Test</h1>
        <div style="margin-left:20px;margin-bottom:20px;width:90%">Generate a thousand CAPTCHA images all at once.</div>
        <div class="dialog" style="margin-left:20px;width:60%;">
            <g:each var="i" in="${(0..<1000)}">
                <%-- We have to add a bogus parameter here to keep the browser from caching the image. --%>
                ${i}<img src="${createLink(action:'simple',controller:'defaults',params:[nop:i])}"><br />
            </g:each>
        </div>
    </body>
</html>

