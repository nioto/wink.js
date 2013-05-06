---
layout: index
title: Wink.JS > How-to
---

## A look of the javascript code

For each Rest resources, there is a corresponding Javascript method.

```java
@Path("/")
public interface X{
 @GET
 public String Y();
 @PUT
 public void Z(String entity);
}
```

will generate 2 methods :

```js
Winkjs.X.Y(params){...}
Winkjs.X.Z(params){...}
```


### The `params` parameter

(from <http://docs.jboss.org/resteasy/2.0.0.GA/userguide/html/AJAX_Client.html>)
<table><caption>API parameter properties</caption><thead><tr>
						<th>Property name</th>
						<th>Default</th>
						<th>Description</th>
					</tr></thead><tbody><tr>
						<td>$entity</td>
						<td>
						</td><td>The entity to send as a PUT, POST request.</td>
					</tr><tr>
						<td>$contentType</td>
						<td>As determined by @Consumes.</td>
						<td>The MIME type of the body entity sent as the Content-Type header.</td>
					</tr><tr>
						<td>$accepts</td>
						<td>Determined by @Provides, defaults to \*/\*.</td>
						<td>The accepted MIME types sent as the Accept header.</td>
					</tr><tr>
						<td>$callback</td>
						<td>
						</td><td>
							Set to a function(httpCode, xmlHttpRequest, value) for an asynchronous call. If 
							not present, the call will be synchronous and return the value.
						</td>
					</tr><tr>
						<td>$apiURL</td>
						<td>Determined by container</td>
						<td>Set to the base URI of your JAX-RS endpoint, not including the last slash.</td>
					</tr><tr>
						<td>$username</td>
						<td>
						</td><td>If username and password are set, they will be used for credentials for the request.</td>
					</tr><tr>
						<td>$password</td>
						<td>
						</td><td>If username and password are set, they will be used for credentials for the request.</td>
					</tr></tbody></table>
					
<!-- _  -->

*Using the API*

```java
@Path("foo")
public class Foo{
 @Path("{id}")
 @GET
 public String get(@QueryParam("order") String order, @HeaderParam("X-Foo") String header,
                   @MatrixParam("colour") String colour, @CookieParam("Foo-Cookie") String cookie){
  …
 }
 @POST
 public void post(String text){
 }
}
```

We can use the previous JAX-RS API in JavaScript using the following code:

```js
var text = Winkjs.Foo.get({id: 45, order: 'desc', 'X-Foo': 'hello', colour: 'blue', 'Foo-Cookie': 123987235444});
Winkjs.Foo..put({$entity: text});
```
