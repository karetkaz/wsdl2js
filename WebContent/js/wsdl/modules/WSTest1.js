WSTest1 = {
	module: {
		url: 'http://localhost:8089/wsdl/WSTest1',
		xmlns: 'http://modules.ws.server.com/',
		poststr: '<?xml version="1.0" encoding="utf-8"?>'
			+'<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">'
			+	'<soap:Body>{SOAP_BODY}</soap:Body>'
			+'</soap:Envelope>',
		xpath: {
			strcat: '/S:Envelope/S:Body/ns2:strcatResponse/return',
			getArray2: '/S:Envelope/S:Body/ns2:getArray2Response/return',
			getServerTime: '/S:Envelope/S:Body/ns2:getServerTimeResponse/return',
			getArray: '/S:Envelope/S:Body/ns2:getArrayResponse/return',
		}
	}
};
WSTest1.strcat = function(a, b, callBack) {
	return WsdlUtils.WsdlInvoke(WSTest1.module, 'strcat', {a:a, b:b}, callBack);
};
WSTest1.getArray2 = function(a, b, callBack) {
	return WsdlUtils.WsdlInvoke(WSTest1.module, 'getArray2', {a:a, b:b}, callBack);
};
WSTest1.getServerTime = function(callBack) {
	return WsdlUtils.WsdlInvoke(WSTest1.module, 'getServerTime', {}, callBack);
};
WSTest1.getArray = function(a, b, callBack) {
	return WsdlUtils.WsdlInvoke(WSTest1.module, 'getArray', {a:a, b:b}, callBack);
};
