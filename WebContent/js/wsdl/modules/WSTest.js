WSTest = {
	module: {
		url: 'http://127.0.0.1:8089/wsdl/WSTest',
		xmlns: 'http://modules.ws.server.com/',
		poststr: '<?xml version="1.0" encoding="utf-8"?>'
			+'<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">'
			+	'<soap:Body>{SOAP_BODY}</soap:Body>'
			+'</soap:Envelope>',
		xpath: {
			strcat: '/S:Envelope/S:Body/ns2:strcatResponse/return',
			getArray2: '/S:Envelope/S:Body/ns2:getArray2Response/return',
			getServerTime: '/S:Envelope/S:Body/ns2:getServerTimeResponse/return',
			session: '/S:Envelope/S:Body/ns2:sessionResponse/return',
			login: '/S:Envelope/S:Body/ns2:loginResponse/return',
			getArray: '/S:Envelope/S:Body/ns2:getArrayResponse/return',
		}
	}
};
WSTest.strcat = function(a, b, callBack) {
	return WsdlUtils.WsdlInvoke(WSTest.module, 'strcat', {a:a, b:b}, callBack);
};
WSTest.getArray2 = function(a, b, callBack) {
	return WsdlUtils.WsdlInvoke(WSTest.module, 'getArray2', {a:a, b:b}, callBack);
};
WSTest.getServerTime = function(callBack) {
	return WsdlUtils.WsdlInvoke(WSTest.module, 'getServerTime', {}, callBack);
};
WSTest.session = function(callBack) {
	return WsdlUtils.WsdlInvoke(WSTest.module, 'session', {}, callBack);
};
WSTest.login = function(username, password, callBack) {
	return WsdlUtils.WsdlInvoke(WSTest.module, 'login', {username:username, password:password}, callBack);
};
WSTest.getArray = function(a, b, callBack) {
	return WsdlUtils.WsdlInvoke(WSTest.module, 'getArray', {a:a, b:b}, callBack);
};
