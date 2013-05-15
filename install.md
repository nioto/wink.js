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
 <br /><br />
<em>Optional</em> : you can add a <string>jsapiurl</string> init param to specify an alternate path to get the Wink JS API Client ( default : /api-client.js in the ContextRoot of the webapp )
</li>

<h4>OR</h4>

<li><h4>Add the WinkJsClientServlet</h4>

<div class="highlight">
<pre>
<code class="xml language-xml" data-lang="xml"> 
&lt;servlet&gt;  
	&lt;description&gt;Simple Servlet to generate a JS client for the API&lt;/description&gt;
	&lt;servlet-name&gt;winjsServlet&lt;/servlet-name&gt;
	&lt;servlet-class&gt;org.nioto.winkjs.WinkJsClientServlet&lt;/servlet-class&gt;
  	&lt;!-- Mandatory, we need to know the path associated with Wink --&gt;
	&lt;init-param&gt;
		&lt;param-name&gt;winkpath&lt;/param-name&gt;
		&lt;param-value&gt;/wink&lt;/param-value&gt;
	&lt;/init-param&gt;
&lt;/servlet&gt;
&lt;servlet-mapping&gt;
	&lt;servlet-name&gt;winjsServlet&lt;/servlet-name&gt;
	&lt;url-pattern&gt;/winkjs/api.js&lt;/url-pattern&gt;
&lt;/servlet-mapping&gt; 
</code>
</pre>
</div>
<br/>


<h4>Note :</h4>
In this case, the Wink RestServlet must be initialized before any call  to the WinkJsClientServlet ( using  <code>&lt;load-on-startup/&gt;</code> )
</li>
</ul>
</li>

<li><h3>Retrieve your script file</h3>

Depending on the configuration choice you made, get a copy your script at :<br />
		`http://[yourhost.com]/[ContextRoot]/[winkPath]/api-client.js` OR `http://[yourhost.com]/[ContextRoot]/winkjs/api.js`
