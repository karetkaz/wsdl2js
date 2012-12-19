WSMath = {
	module: {
		url: 'http://localhost:8089/wsdl/WSMath',
		xmlns: 'http://modules.ws.server.com/',
		poststr: '<?xml version="1.0" encoding="utf-8"?>'
			+'<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">'
			+	'<soap:Body>{SOAP_BODY}</soap:Body>'
			+'</soap:Envelope>',
		xpath: {
			pow: '/S:Envelope/S:Body/ns2:powResponse/return',
			PI: '/S:Envelope/S:Body/ns2:PIResponse/return',
			sin: '/S:Envelope/S:Body/ns2:sinResponse/return',
		}
	}
};
WSMath.pow = function(x, y, callBack) {
	return WsdlUtils.WsdlInvoke(WSMath.module, 'pow', {x:x, y:y}, callBack);
};
WSMath.PI = function(callBack) {
	return WsdlUtils.WsdlInvoke(WSMath.module, 'PI', {}, callBack);
};
WSMath.sin = function(x, callBack) {
	return WsdlUtils.WsdlInvoke(WSMath.module, 'sin', {x:x}, callBack);
};
