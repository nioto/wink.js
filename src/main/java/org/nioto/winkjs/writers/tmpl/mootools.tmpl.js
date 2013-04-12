// BEGIN [functionname]
// [httpmethod] [uri]
[functionname] = function(_params){
	var params = _params ? _params : {};
	var options = {};
	options.method = '[httpmethod]' ;
  options.url = '[uri]';
  if(params.$username && params.$password ){
  	options.user = params.$username;
  	options.password = params.$password ;
  }
  ///// A Corriger
  if(params.$accepts)
  	request.setAccepts(params.$accepts);
[!empty(accepts)]
  else request.setAccepts('[accepts]');
[/empty]
  if(params.$contentType)
    request.setContentType(params.$contentType);
[!empty(contentType)]
  else
    request.setContentType('[contentType]');
[/empty]
//////
 if(params.$entity)
		options.data=params.$entity;

  if(params.$callback){
  	options.onSuccess = params.$callback;
  	new Request(options).send();
  } else{
  	var returnValue;
  	var callback = function(responseText, responseXML ){ returnValue = responseText;};
  	options.onSuccess = params.$callback;
  	options.async = false;
  	new Request(options).send();
  	return returnValue;
  }
}
// END [functionname]
