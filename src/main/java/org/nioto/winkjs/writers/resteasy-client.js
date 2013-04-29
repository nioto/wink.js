// namespace
var Winkjs = {
};
Winkjs.__REST = function (){
  this.apiURL = null;
  this.loglevel = 0;
}

// constructor
Winkjs.__REST.Request = function (){
	Winkjs.__REST.log("Creating new Request");
	this.uri = null;
	this.method = "GET";
	this.username = null;
	this.password = null;
	this.acceptHeader = "*/*";
	this.contentTypeHeader = null;
	this.async = true;
	this.queryParameters = [];
	this.matrixParameters = [];
	this.formParameters = [];
	this.cookies = [];
	this.headers = [];
	this.entity = null;
}

Winkjs.__REST.Request.prototype = {
		execute : function(callback){
			var request = new XMLHttpRequest();
			var url = this.uri;
//			var Winkjs.__REST.Request = this;
			for(var i=0;i<this.matrixParameters.length;i++){
				url += ";" + Winkjs.__REST.Encoding.encodePathParamName(this.matrixParameters[i][0]);
				url += "=" + Winkjs.__REST.Encoding.encodePathParamValue(this.matrixParameters[i][1]);
			}
			for(var i=0;i<this.queryParameters.length;i++){
				if(i == 0)
					url += "?";
				else
					url += "&";
				url += Winkjs.__REST.Encoding.encodeQueryParamNameOrValue(this.queryParameters[i][0]);
				url += "=" + Winkjs.__REST.Encoding.encodeQueryParamNameOrValue(this.queryParameters[i][1]);
			}
			for(var i=0;i<this.cookies.length;i++){
				document.cookie = escape(this.cookies[i][0]) 
					+ "=" + escape(this.cookies[i][1]);
			}
			request.open(this.method, url, this.async, this.username, this.password);
			var acceptSet = false;
			var contentTypeSet = false;
			for(var i=0;i<this.headers.length;i++){
				if(this.headers[i][0].toLowerCase() == 'accept')
					acceptSet = this.headers[i][1];
				if(this.headers[i][0].toLowerCase() == 'content-type')
					contentTypeSet = this.headers[i][1];
				request.setRequestHeader(Winkjs.__REST.Encoding.encodeHeaderName(this.headers[i][0]),
						Winkjs.__REST.Encoding.encodeHeaderValue(this.headers[i][1]));
			}
			if(!acceptSet)
				request.setRequestHeader('Accept', this.acceptHeader);
			Winkjs.__REST.log("Got form params: "+this.formParameters.length);
			// see if we're sending an entity or a form
			if(this.entity && this.formParameters.length > 0)
				throw "Cannot have both an entity and form parameters";
			// form
			if(this.formParameters.length > 0){
				if(contentTypeSet && contentTypeSet != "application/x-www-form-urlencoded")
					throw "The ContentType that was set by header value ("+contentTypeSet+") is incompatible with form parameters";
				if(this.contentTypeHeader && this.contentTypeHeader != "application/x-www-form-urlencoded")
					throw "The ContentType that was set with setContentType ("+this.contentTypeHeader+") is incompatible with form parameters";
				contentTypeSet = "application/x-www-form-urlencoded";
				request.setRequestHeader('Content-Type', contentTypeSet);
			}else if(this.entity && !contentTypeSet && this.contentTypeHeader){
				// entity
				contentTypeSet = this.contentTypeHeader;
				request.setRequestHeader('Content-Type', this.contentTypeHeader);
			}
			// we use this flag to work around buggy browsers
			var gotReadyStateChangeEvent = false;
			if(callback){
				request.onreadystatechange = function() {
					gotReadyStateChangeEvent = true;
					Winkjs.__REST.log("Got readystatechange");
					Winkjs.__REST._complete(this, callback);
				};
			}
			var data = this.entity;
			if(this.entity){
				if(this.entity instanceof Element){
					if(!contentTypeSet || Winkjs.__REST._isXMLMIME(contentTypeSet))
						data = Winkjs.__REST.serialiseXML(this.entity);
				}else if(this.entity instanceof Document){
					if(!contentTypeSet || Winkjs.__REST._isXMLMIME(contentTypeSet))
						data = this.entity;
				}else if(this.entity instanceof Object){
					if(!contentTypeSet || Winkjs.__REST._isJSONMIME(contentTypeSet))
						data = JSON.stringify(this.entity);
				}
			}else if(this.formParameters.length > 0){
				data = '';
				for(var i=0;i<this.formParameters.length;i++){
					if(i > 0)
						data += "&";
					data += Winkjs.__REST.Encoding.encodeFormNameOrValue(this.formParameters[i][0]);
					data += "=" + Winkjs.__REST.Encoding.encodeFormNameOrValue(this.formParameters[i][1]);
				}
			}
			Winkjs.__REST.log("Content-Type set to "+contentTypeSet);
			Winkjs.__REST.log("Entity set to "+data);
			request.send(data);
			// now if the browser did not follow the specs and did not fire the events while synchronous,
			// handle it manually
			if(!this.async && !gotReadyStateChangeEvent && callback){
				Winkjs.__REST.log("Working around browser readystatechange bug");
				Winkjs.__REST._complete(request, callback);
			}
		},
		setAccepts : function(acceptHeader){
			Winkjs.__REST.log("setAccepts("+acceptHeader+")");
			this.acceptHeader = acceptHeader;
		},
		setCredentials : function(username, password){
			this.password = password;
			this.username = username;
		},
		setEntity : function(entity){
			Winkjs.__REST.log("setEntity("+entity+")");
			this.entity = entity;
		},
		setContentType : function(contentType){
			Winkjs.__REST.log("setContentType("+contentType+")");
			this.contentTypeHeader = contentType;
		},
		setURI : function(uri){
			Winkjs.__REST.log("setURI("+uri+")");
			this.uri = uri;
		},
		setMethod : function(method){
			Winkjs.__REST.log("setMethod("+method+")");
			this.method = method;
		},
		setAsync : function(async){
			Winkjs.__REST.log("setAsync("+async+")");
			this.async = async;
		},
		addCookie : function(name, value){
			Winkjs.__REST.log("addCookie("+name+"="+value+")");
			this.cookies.push([name, value]);
		},
		addQueryParameter : function(name, value){
			Winkjs.__REST.log("addQueryParameter("+name+"="+value+")");
			this.queryParameters.push([name, value]);
		},
		addMatrixParameter : function(name, value){
			Winkjs.__REST.log("addMatrixParameter("+name+"="+value+")");
			this.matrixParameters.push([name, value]);
		},
		addFormParameter : function(name, value){
			Winkjs.__REST.log("addFormParameter("+name+"="+value+")");
			this.formParameters.push([name, value]);
		},
		addHeader : function(name, value){
			Winkjs.__REST.log("addHeader("+name+"="+value+")");
			this.headers.push([name, value]);
		}
}

Winkjs.__REST.log = function(string){
	if(Winkjs.__REST.loglevel > 0)
		print(string);
}

Winkjs.__REST._complete = function(request, callback){
	Winkjs.__REST.log("Request ready state: "+request.readyState);
	if(request.readyState == 4) {
		var entity;
		Winkjs.__REST.log("Request status: "+request.status);
		Winkjs.__REST.log("Request response: "+request.responseText);
		if(request.status >= 200 && request.status < 300){
			var contentType = request.getResponseHeader("Content-Type");
			if(contentType != null){
				if(Winkjs.__REST._isXMLMIME(contentType))
					entity = request.responseXML;
				else if(Winkjs.__REST._isJSONMIME(contentType))
					entity = JSON.parse(request.responseText);
				else
					entity = request.responseText;
			}else
				entity = request.responseText;
		}
		Winkjs.__REST.log("Calling callback with: "+entity);
		callback(request.status, request, entity);
	}
}

Winkjs.__REST._isXMLMIME = function(contentType){
	return contentType == "text/xml"
			|| contentType == "application/xml"
			|| (contentType.indexOf("application/") == 0
				&& contentType.lastIndexOf("+xml") == (contentType.length - 4));
}

Winkjs.__REST._isJSONMIME = function(contentType){
	return contentType == "application/json"
			|| (contentType.indexOf("application/") == 0
				&& contentType.lastIndexOf("+json") == (contentType.length - 5));
}

/* Encoding */

Winkjs.__REST.Encoding = {};

Winkjs.__REST.Encoding.hash = function(a){
	var ret = {};
	for(var i=0;i<a.length;i++)
		ret[a[i]] = 1;
	return ret;
}

//
// rules

Winkjs.__REST.Encoding.Alpha = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                       'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

Winkjs.__REST.Encoding.Numeric = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];

Winkjs.__REST.Encoding.AlphaNum = [].concat(Winkjs.__REST.Encoding.Alpha, Winkjs.__REST.Encoding.Numeric); 

Winkjs.__REST.Encoding.AlphaNumHash = Winkjs.__REST.Encoding.hash(Winkjs.__REST.Encoding.AlphaNum);

/**
 * unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
 */
Winkjs.__REST.Encoding.Unreserved = [].concat(Winkjs.__REST.Encoding.AlphaNum, ['-', '.', '_', '~']);

/**
 * gen-delims = ":" / "/" / "?" / "#" / "[" / "]" / "@"
 */
Winkjs.__REST.Encoding.GenDelims = [':', '/', '?', '#', '[', ']', '@'];

/**
 * sub-delims = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
 */
Winkjs.__REST.Encoding.SubDelims = ['!','$','&','\'','(', ')', '*','+',',',';','='];

/**
 * reserved = gen-delims | sub-delims
 */
Winkjs.__REST.Encoding.Reserved = [].concat(Winkjs.__REST.Encoding.GenDelims, Winkjs.__REST.Encoding.SubDelims);

/**
 * pchar = unreserved | escaped | sub-delims | ":" | "@"
 * 
 * Note: we don't allow escaped here since we will escape it ourselves, so we don't want to allow them in the
 * unescaped sequences
 */
Winkjs.__REST.Encoding.PChar = [].concat(Winkjs.__REST.Encoding.Unreserved, Winkjs.__REST.Encoding.SubDelims, [':', '@']);

/**
 * path_segment = pchar <without> ";"
 */
Winkjs.__REST.Encoding.PathSegmentHash = Winkjs.__REST.Encoding.hash(Winkjs.__REST.Encoding.PChar);
delete Winkjs.__REST.Encoding.PathSegmentHash[";"];

/**
 * path_param_name = pchar <without> ";" | "="
 */
Winkjs.__REST.Encoding.PathParamHash = Winkjs.__REST.Encoding.hash(Winkjs.__REST.Encoding.PChar);
delete Winkjs.__REST.Encoding.PathParamHash[";"];
delete Winkjs.__REST.Encoding.PathParamHash["="];

/**
 * path_param_value = pchar <without> ";"
 */
Winkjs.__REST.Encoding.PathParamValueHash = Winkjs.__REST.Encoding.hash(Winkjs.__REST.Encoding.PChar);
delete Winkjs.__REST.Encoding.PathParamValueHash[";"];

/**
 * query = pchar / "/" / "?"
 */
Winkjs.__REST.Encoding.QueryHash = Winkjs.__REST.Encoding.hash([].concat(Winkjs.__REST.Encoding.PChar, ['/', '?']));
// deviate from the RFC to disallow separators such as "=", "@" and the famous "+" which is treated as a space
// when decoding
delete Winkjs.__REST.Encoding.QueryHash["="];
delete Winkjs.__REST.Encoding.QueryHash["&"];
delete Winkjs.__REST.Encoding.QueryHash["+"];

/**
 * fragment = pchar / "/" / "?"
 */
Winkjs.__REST.Encoding.FragmentHash = Winkjs.__REST.Encoding.hash([].concat(Winkjs.__REST.Encoding.PChar, ['/', '?']));

// HTTP

Winkjs.__REST.Encoding.HTTPSeparators = ["(" , ")" , "<" , ">" , "@"
                                , "," , ";" , ":" , "\\" , "\""
                                , "/" , "[" , "]" , "?" , "="
                                , "{" , "}" , ' ' , '\t'];

// This should also hold the CTLs but we never need them
Winkjs.__REST.Encoding.HTTPChar = [];
(function(){
	for(var i=32;i<127;i++)
		Winkjs.__REST.Encoding.HTTPChar.push(String.fromCharCode(i));
})()

// CHAR - separators
Winkjs.__REST.Encoding.HTTPToken = Winkjs.__REST.Encoding.hash(Winkjs.__REST.Encoding.HTTPChar);
(function(){
	for(var i=0;i<Winkjs.__REST.Encoding.HTTPSeparators.length;i++)
		delete Winkjs.__REST.Encoding.HTTPToken[Winkjs.__REST.Encoding.HTTPSeparators[i]];
})()

//
// functions

//see http://www.w3.org/TR/html4/interact/forms.html#h-17.13.4.1
//and http://www.apps.ietf.org/rfc/rfc1738.html#page-4
Winkjs.__REST.Encoding.encodeFormNameOrValue = function (val){
	return Winkjs.__REST.Encoding.encodeValue(val, Winkjs.__REST.Encoding.AlphaNumHash, true);
}

//see http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
Winkjs.__REST.Encoding.encodeHeaderName = function (val){
	// token+ from http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html#sec2
	
	// There is no way to encode a header name. it is either a valid token or invalid and the 
	// XMLHttpRequest will fail (http://www.w3.org/TR/XMLHttpRequest/#the-setrequestheader-method)
	// What we could do here is throw if the value is invalid
	return val;
}

//see http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
Winkjs.__REST.Encoding.encodeHeaderValue = function (val){
	// *TEXT or combinations of token, separators, and quoted-string from http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html#sec2
	// FIXME: implement me. Stef has given up, since it involves latin1, quoted strings, MIME encoding (http://www.ietf.org/rfc/rfc2047.txt)
	// which mentions a limit on encoded value of 75 chars, which should be split into several lines. This is mad.
	return val;
}

// see http://www.ietf.org/rfc/rfc3986.txt
Winkjs.__REST.Encoding.encodeQueryParamNameOrValue = function (val){
	return Winkjs.__REST.Encoding.encodeValue(val, Winkjs.__REST.Encoding.QueryHash);
}

//see http://www.ietf.org/rfc/rfc3986.txt
Winkjs.__REST.Encoding.encodePathSegment = function (val){
	return Winkjs.__REST.Encoding.encodeValue(val, Winkjs.__REST.Encoding.PathSegmentHash);
}

//see http://www.ietf.org/rfc/rfc3986.txt
Winkjs.__REST.Encoding.encodePathParamName = function (val){
	return Winkjs.__REST.Encoding.encodeValue(val, Winkjs.__REST.Encoding.PathParamHash);
}

//see http://www.ietf.org/rfc/rfc3986.txt
Winkjs.__REST.Encoding.encodePathParamValue = function (val){
	return Winkjs.__REST.Encoding.encodeValue(val, Winkjs.__REST.Encoding.PathParamValueHash);
}

Winkjs.__REST.Encoding.encodeValue = function (val, allowed, form){
	if(typeof val != "string"){
		Winkjs.__REST.log("val is not a string");
		return val;
	}
	if(val.length == 0){
		Winkjs.__REST.log("empty string");
		return val;
	}
	var ret = '';
	for(var i=0;i<val.length;i++){
		var first = val[i];
		if(allowed[first] == 1){
			Winkjs.__REST.log("char allowed: "+first);
			ret = ret.concat(first);
		}else if(form && (first == ' ' || first == '\n')){
			// special rules for application/x-www-form-urlencoded
			if(first == ' ')
				ret += '+';
			else
				ret += '%0D%0A';
		}else{
			// See http://www.faqs.org/rfcs/rfc2781.html 2.2
			
			// switch to codepoint
			first = val.charCodeAt(i);
			// utf-16 pair?
			if(first < 0xD800 || first > 0xDFFF){
				// just a single utf-16 char
				ret = ret.concat(Winkjs.__REST.Encoding.percentUTF8(first));
			}else{
				if(first > 0xDBFF || i+1 >= val.length)
					throw "Invalid UTF-16 value: " + val;
				var second = val.charCodeAt(++i);
				if(second < 0xDC00 || second > 0xDFFF)
					throw "Invalid UTF-16 value: " + val;
				// char = 10 lower bits of first shifted left + 10 lower bits of second 
				var c = ((first & 0x3FF) << 10) | (second & 0x3FF);
				// and add this
				c += 0x10000;
				// char is now 32 bit unicode
				ret = ret.concat(Winkjs.__REST.Encoding.percentUTF8(c));
			}
		}
	}
	return ret;
}

// see http://tools.ietf.org/html/rfc3629
Winkjs.__REST.Encoding.percentUTF8 = function(c){
	if(c < 0x80)
		return Winkjs.__REST.Encoding.percentByte(c);
	if(c < 0x800){
		var first = 0xC0 | ((c & 0x7C0) >> 6);
		var second = 0x80 | (c & 0x3F);
		return Winkjs.__REST.Encoding.percentByte(first, second);
	}
	if(c < 0x10000){
		var first = 0xE0 | ((c >> 12) & 0xF);
		var second = 0x80 | ((c >> 6) & 0x3F);
		var third = 0x80 | (c & 0x3F);
		return Winkjs.__REST.Encoding.percentByte(first, second, third);
	}
	if(c < 0x110000){
		var first = 0xF0 | ((c >> 18) & 0x7);
		var second = 0x80 | ((c >> 12) & 0x3F);
		var third = 0x80 | ((c >> 6) & 0x3F);
		var fourth = 0x80 | (c & 0x3F);
		return Winkjs.__REST.Encoding.percentByte(first, second, third, fourth);
	}
	throw "Invalid character for UTF-8: "+c;
}

Winkjs.__REST.Encoding.percentByte = function(){
	var ret = '';
	for(var i=0;i<arguments.length;i++){
		var b = arguments[i];
		if (b >= 0 && b <= 15)
			ret += "%0" + b.toString(16);
		else
			ret += "%" + b.toString(16);
	}
	return ret;
}

Winkjs.__REST.serialiseXML = function(node){
	if (typeof XMLSerializer != "undefined")
		return (new XMLSerializer()).serializeToString(node) ;
	else if (node.xml) return node.xml;
	else throw "XML.serialize is not supported or can't serialize " + node;
}
