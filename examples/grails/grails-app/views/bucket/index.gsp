<html>
    <head>
        <title>SimpleCaptcha - Bucket o' Fun</title>
		<meta name="layout" content="main" />
    </head>
    <body>
        <h1 style="margin-left:20px;">SimpleCaptcha Bucket</h1>
        <div style="margin-left:20px;margin-bottom:20px;width:90%">This page is intended to show off the various renderers available to SimpleCaptcha. Reloading the page will yield new images. To see the code responsible for generating the CAPTCHA look at the source for the controller under <code>grails-app/controllers</code>.</div>
        <div class="dialog" style="margin-left:20px;width:80%;">
            <ul>
                <g:each var="c" in="${grailsApplication.controllerClasses}">
                    <g:if test="${!c.logicalPropertyName.contains('crack') && !c.logicalPropertyName.contains('bucket')}">
                        <li class="controller"><h1>${c.logicalPropertyName}Controller</h1><br/>
                        <g:each var="uri" in="${c.URIs}">
                            <g:if test="${uri.count('/') == 2 && !uri.endsWith('/') && !uri.endsWith('Bean') && !uri.endsWith('index')}">
                                <a href="${createLinkTo(file:uri)}"><img src="${createLinkTo(file:uri)}"> ${createLinkTo(file:uri)}</a><br />
                            </g:if>
                        </g:each>
                        </li>
                    </g:if>
                </g:each>
            </ul>
        </div>
    </body>
</html>
