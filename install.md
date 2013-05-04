---
layout: index
title: Wink.JS > Install
---

## Generation

1. ### copy [https://sourceforge.net/projects/winkjs/files/latest/download?source=files](winkjs-0.1.jar) into your WEB-INF/lib/ folder

2. ### web.xml configuration


	* #### Replacing RestServlet

		Replace **org.apache.wink.server.internal.servlet.RestServlet** by **org.nioto.winkjs.WinkJsRestServlet** in your web.xml file.

		_Optional_ : you can add a **jsapiurl** init param to specify an alternate path to get the Wink JS API Client ( default : /api-client.js in the ContextRoot of the webapp )

	#### OR

	* #### Add the WinkJsClientServlet
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
		#### Note :  In this case, the Wink RestServlet must be initialized before any call  to the WinkJsClientServlet ( using  <load-on-startup/> )


3. ### Retrieve your script file
Depending on the configuration choice you made, get a copy your script at :
		`http://[yourhost.com]/[ContextRoot]/[winkPath]/api-client.js` OR `http://[yourhost.com]/[ContextRoot]/winkjs/api.js`
