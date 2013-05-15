---
layout: index
title: Wink.JS > Install
---

<h2>Generation</h2>

<ul>

<li><h3>Copy <a href="http://sourceforge.net/projects/winkjs/files/v0.2/">wink.js-0.2.jar</a> into your WEB-INF/lib/ folder</h3></li>

<li><h3>In your webapp configuration file : web.xml</h3>
<ul>
<li><h4>Replacing RestServlet</h4>
 Replace <strong>org.apache.wink.server.internal.servlet.RestServlet</strong> by <strong>org.nioto.winkjs.WinkJsRestServlet</strong> in your web.xml file.
 
<em>Optional</em> : you can add a <string>jsapiurl</string> init param to specify an alternate path to get the Wink JS API Client ( default : /api-client.js in the ContextRoot of the webapp )
</li>

<li><h4>OR</h4></li>

<li><h4>Add the WinkJsClientServlet</h4>

<div class="highlight">
<pre>
<code class="xml language-xml" data-lang="xml">
<servlet>  
	<description>Simple Servlet to generate a JS client for the API</description>
	<servlet-name>winjsServlet</servlet-name>
	<servlet-class>org.nioto.winkjs.WinkJsClientServlet</servlet-class>
  	<!-- Mandatory, we need to know the path associated with Wink -->
	<init-param>
		<param-name>winkpath</param-name>
		<param-value>/wink</param-value>
	</init-param>
</servlet>
<servlet-mapping>
	<servlet-name>winjsServlet</servlet-name>
	<url-pattern>/winkjs/api.js</url-pattern>
</servlet-mapping> 
</code></pre></div>
<h4>Note :</h4>
In this case, the Wink RestServlet must be initialized before any call  to the WinkJsClientServlet ( using  <code>&lt;load-on-startup/&gt;</code> )
</li>
</ul>
</li>

<li><h3>Retrieve your script file</h3>

Depending on the configuration choice you made, get a copy your script at :<br />
		`http://[yourhost.com]/[ContextRoot]/[winkPath]/api-client.js` OR `http://[yourhost.com]/[ContextRoot]/winkjs/api.js`
