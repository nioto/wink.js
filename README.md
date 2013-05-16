Wink JS Api Client
==================
 
 Based on the [RestEasy-JSAPI](http://docs.jboss.org/resteasy/2.0.0.GA/userguide/html/AJAX_Client.html), a Java client for your Wink Rest resources
 

The code use **Wink v1.3.0**, but works with **v1.2.1-incubating**

How to use
----------

Put the _winkjs-0.1.jar_ into your WEB-INF/lib next to "wink-server-1.3.0.jar"

### Using WinkJsRestServlet extending the Wink RestServlet

Replace *org.apache.wink.server.internal.servlet.RestServlet* by *org.nioto.winkjs.WinkJsRestServlet* in your web.xml 

Optional: you can add a *jsapiurl* init param to specify an alternate Url to get the JS API Client ( default : */api-client.js* in the ContextRoot of the webapp )

### Using org.nioto.winkjs.WinkJsClientServlet without changing your Wink RestServlet configuration

Add in your web.xml the configuration for the servlet :

    <servlet>  
      <description>Simple Servlet to generate a JS client for the API</description>
      <servlet-name>_ah_warmup0</servlet-name>
      <servlet-class>org.nioto.winkjs.WinkJsClientServlet</servlet-class>
      <!-- Mandatory, we need to know the path associated with Wink -->
      <init-param>
        <param-name>winkpath</param-name>
        <param-value>/wink</param-value>
      </init-param>
    </servlet>
    <servlet-mapping>
      <servlet-name>_ah_warmup0</servlet-name>
      <url-pattern>/winkjs/api.js</url-pattern>
    </servlet-mapping> 
  
## [View website for more informations](http://nioto.github.io/wink.js/)
