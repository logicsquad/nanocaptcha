<html>
    <head>
        <title><g:layoutTitle default="SimpleCaptcha" /></title>
        <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
        <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:layoutHead />
        <g:javascript library="application" />
        <nav:resources />
    </head>
    <body>
        <div id="menu"><nav:render /></div>
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
        </div>	
        <g:layoutBody />		
    </body>	
</html>
