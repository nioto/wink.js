---
layout: index
title: Wink.JS > Install
---

<h2>Generation</h2>

<ul>

<li><h3>Copy [https://sourceforge.net/projects/winkjs/files/latest/download?source=files](winkjs-0.1.jar) into your WEB-INF/lib/ folder</h3></li>

<li><h3>web.xml configuration</h3>
<br />

<ul>
<li><h4>Replacing RestServlet</h4>
<br />
 Replace **org.apache.wink.server.internal.servlet.RestServlet** by **org.nioto.winkjs.WinkJsRestServlet** in your web.xml file.
<br />
 _Optional_ : you can add a **jsapiurl** init param to specify an alternate path to get the Wink JS API Client ( default : /api-client.js in the ContextRoot of the webapp )
</li>

<li><h4>OR</h4></li>

<li><h4>Add the WinkJsClientServlet</h4>
<br />

```xml
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
```
<br />

<h4>Note :</h4>
<br />
In this case, the Wink RestServlet must be initialized before any call  to the WinkJsClientServlet ( using  <load-on-startup/> )
</li>
</ul>
</li>

<li><h3>Retrieve your script file</h3>
<br />
<br />

Depending on the configuration choice you made, get a copy your script at :<br />
		`http://[yourhost.com]/[ContextRoot]/[winkPath]/api-client.js` OR `http://[yourhost.com]/[ContextRoot]/winkjs/api.js`
