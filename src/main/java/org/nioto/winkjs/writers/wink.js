// namespace
var Wink = {
  apiURL : null
};
Wink.options = function() {
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
};
Wink.options.prototype = {
  setAccepts : function(acceptHeader) {
    this.acceptHeader = acceptHeader;
  },
  setCredentials : function(username, password) {
    this.password = password;
    this.username = username;
  },
  setEntity : function(entity) {
    this.entity = entity;
  },
  setContentType : function(contentType) {
    this.contentTypeHeader = contentType;
  },
  setURI : function(uri) {
    this.uri = uri;
  },
  setMethod : function(method) {
    this.method = method;
  },
  setAsync : function(async) {
    this.async = async;
  },
  addCookie : function(name, value) {
    this.cookies.push([ name, value ]);
  },
  addQueryParameter : function(name, value) {
    this.queryParameters.push([ name, value ]);
  },
  addMatrixParameter : function(name, value) {
    this.matrixParameters.push([ name, value ]);
  },
  addFormParameter : function(name, value) {
    this.formParameters.push([ name, value ]);
  },
  addHeader : function(name, value) {
    this.headers.push([ name, value ]);
  },
  
  
}
