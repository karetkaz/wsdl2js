var WsdlUtils = {

	/** WsdlInvoke
	 * if options is a function it will be treated as onSuccess callback.
	 * if onSuccess callback is not setted the function blocks execution
	 * and returns the value, else it returns nonblocking undefined,
	 * then whne the result finishes loading the callback will be executed.
	**/
	WsdlInvoke: function(module, func, args, options) {
		function toXml(pl) {
			/*var xml = '';
			if (typeof(pl) == 'object') {
				var isArray	= false;
				if (pl)
					isArray = pl.length !== undefined;
				for (var p in pl) {
					if (typeof(pl[p]) != 'function') {
						var inner = toXml(pl[p], p);
						if (isArray) {
							if (id && p != pl.length - 1) {
								xml +=  inner + '</' + id + '>'  + '<' + id + '>';
							}
							else {
								xml += inner;
							}
						}
						else {
							if (pl[p] != null)
								xml += '<' + p + '>' + inner + '</' + p + '>';
							else
								xml += '<' + p + '/>';
						}
					}
				}
			}
			else if (typeof(pl) != 'function') {
				xml = pl.toString().replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
			}
			return xml;
			*/
			return WsdlUtils.toXmlString(pl, '', '');
		}

		var poststr = '';
		for(var key in args) {
			var obj = args[key];
			if (obj.constructor === Array) {
				for (var i = 0; i < obj.length; ++i) {
					poststr += '<' + key + ' xmlns="">' + toXml(obj[i]) + '</' + key + '>';
				}
			}
			else {
				poststr += '<' + key + ' xmlns="">' + toXml(obj) + '</' + key + '>';
			}
		}
		poststr = '<' + func + ' xmlns="' + module.xmlns + '">' + poststr + '</' + func + '>';

		var http_request = undefined;
		if (window.XMLHttpRequest) {
			http_request = new XMLHttpRequest();
		}
		else if (window.ActiveXObject) {
			http_request = new ActiveXObject('Microsoft.XMLHTTP');
		}

		if (typeof(options) === 'function') {
			options = {onSuccess: options};
		}
		else if (options === undefined) {
			options = {};
		}

		if (options.mapResult !== null) {
			var toType = options.mapResult;

			if (toType === undefined) {
				toType = Object;
			}

			else if (toType === Boolean || toType.constructor === Boolean) {}
			else if (toType === Number || toType.constructor === Number) {}
			else if (toType === String || toType.constructor === String) {}
			else if (toType === Object || toType.constructor === Object) {}
			else if (toType === Array || toType.constructor === Array) {}
			else {
				toType = null;
			}
			if (toType != null) {
				options.mapResult = function(xml) {
					// TODO: WsdlUtils.nsDoc.nsp.ns2 = nsp.module.xmlns;
					return WsdlUtils.getObjectByXPath(xml, module.xpath[func], toType);
				}
			}
		}

		var requestresponse = function() {
			if (http_request.readyState == 4) {
				// on success return the response as an object, or invoke the onSuccess callback
				if (http_request.status >= 200 && http_request.status <= 201) {
					var result = http_request.responseXML
					if (options.mapResult) {
						result = options.mapResult(result);
					}
					if (options.onSuccess) {
						options.onSuccess(result);
						return undefined;
					}
					return result;
				}
				//~ if server gives error throw the response as an exception, and invoke the onError callback
				else {

					var error = WsdlUtils.getObjectByXPath(http_request.responseXML, '/S:Envelope/S:Body/S:Fault');
					var exception = {message: error.faultstring};

					if (error.hasOwnProperty('detail')) {
						var detail = error.detail;
						for (var key in detail) {
							exception[key.replace(/^ns2:/, '')] = detail[key];
						}
					}

					if (options.onError) {
						options.onError(exception);
						//~ return undefined;
					}
					throw exception;
				}
			}
		};

		http_request.onreadystatechange = options.onSuccess ? requestresponse : null;
		http_request.open('POST', module.url, options.onSuccess !== undefined);
		http_request.setRequestHeader('Content-Type', 'text/xml');
		http_request.send(module.poststr.replace('{SOAP_BODY}', poststr));

		if (options.onSuccess) {
			return undefined;
		}

		return requestresponse();
	},

	xmlFromString: function(str) {
		return new DOMParser().parseFromString(str, 'text/xml');
	},
	xmlToString: function(xml) {
		return new XMLSerializer().serializeToString(xml);
	},

	toJsString: function(obj, prefix) {
		if (obj === null || obj === undefined) {
			return obj === null ? 'null' : 'undefined';
		}
		if (obj.constructor === Function) {
			//~ return 'function' + obj.toString().match(/\(.*\)/) + ' { ... }';
			return null;
		}
		if (obj.constructor === Object) {
			var result = '';
			var prefix2 = '';
			if (prefix === null || prefix === undefined) {
				prefix = '';
			}
			else if (prefix !== '') {
				prefix2 = prefix + '\t';
			}
			for (var key in obj) {
				var inner = WsdlUtils.toJsString(obj[key], prefix2);
				if (inner !== null) {
					if (result !== '') {
						result += ', ';
					}
					result += prefix2 + key + ': ' + inner;
				}
			}
			if (prefix !== '' && result.indexOf(prefix) == 0) {
				result += prefix;
			}
			return '{' + result + '}';
		}
		if (obj.constructor === Array) {
			var result = '';
			if (prefix === null || prefix === undefined) {
				prefix = '';
			}
			for (var idx = 0; idx < obj.length; idx += 1) {
				var inner = WsdlUtils.toJsString(obj[idx], prefix);
				if (inner !== null) {
					if (result !== '') {
						result += ', ';
					}
					result += inner;
				}
			}
			if (prefix !== '' && result.indexOf(prefix) == 0) {
				result += prefix;
			}
			return '[' + result + ']';
		}

		if (obj.constructor === String) {
			return '"' + obj.toString().replace(/\"/g, '\\"') + '"';
		}

		// Boolean, Number, Date, Math, RegExp, Global, ... ???
		return obj.toString();
	},
	toXmlString: function(obj, prefix, key) {
		if (obj === null || obj === undefined) {
			//~ return obj === null ? 'null' : 'undefined';
			return null;
		}
		if (obj.constructor === Function) {
			//~ return 'function' + obj.toString().match(/\(.*\)/);
			return null;
		}
		if (obj.constructor === Object) {
			var result = '';
			var prefix2 = '';
			if (prefix === null || prefix === undefined) {
				prefix = '';
			}
			else if (prefix !== '') {
				prefix2 = prefix + '\t';
			}
			for (var key in obj) {
				var inner = WsdlUtils.toXmlString(obj[key], prefix2, key);
				if (inner !== null) {
					result += prefix2 + '<' + key + '>' + inner + '</' + key + '>';
				}
			}
			if (prefix !== '' && result.indexOf(prefix) >= 0) {
				result += prefix;
			}
			return result;
		}
		if (obj.constructor === Array) {
			var result = '';
			if (prefix === null || prefix === undefined) {
				prefix = '';
			}
			for (var idx = 0; idx < obj.length; idx += 1) {
				var inner = WsdlUtils.toXmlString(obj[idx], prefix, key);
				if (inner !== null) {
					if (result !== '') {
						result += '</' + key + '>' + prefix + '<' + key + '>';
					}
					result += inner;
				}
			}
			return result;
		}

		if (obj.constructor === String) {
			return obj.toString().replace(/</g, '&lt;').replace(/>/g, '&gt;');
		}

		// Boolean, Number, Date, Math, RegExp, Global, ... ???
		return obj.toString();
	},

	nsDoc: {
		nsp: {
			ns2: 'http://modules.ws.server.com/'
		},
		doc: null,
		AddNamespace: function(prefix, uri) {
			if (WsdlUtils.nsDoc.doc == null) {
				WsdlUtils.nsDoc.doc = document.implementation.createDocument("", "namespaces", null);
			}
			var ns = WsdlUtils.nsDoc.doc.createElement('ns');
			ns.setAttribute('prefix', prefix);
			ns.setAttribute('uri', uri);
			WsdlUtils.nsDoc.doc.documentElement.appendChild(ns);
		},
		resolver: function(prefix) {
			if (WsdlUtils.nsDoc.doc == null) {
				WsdlUtils.nsDoc.AddNamespace('S', 'http://schemas.xmlsoap.org/soap/envelope/');
				for (var nsp in WsdlUtils.nsDoc.nsp)
					WsdlUtils.nsDoc.AddNamespace(nsp, WsdlUtils.nsDoc.nsp[nsp]);
			}
			return WsdlUtils.nsDoc.doc.evaluate('/namespaces/ns[@prefix="'+prefix+'"][1]/@uri', WsdlUtils.nsDoc.doc, null, XPathResult.ANY_TYPE, null).iterateNext().textContent;
		}
	},

	/// returns the subdocument(s) of the document.
	getNodesByXPath: function(document, xpath) {
		if (document && xpath) {
			var evaluator = new XPathEvaluator();
			var resultSet = evaluator.evaluate(xpath, document, WsdlUtils.nsDoc.resolver, XPathResult.ANY_TYPE, null);
			if (resultSet.resultType == XPathResult.UNORDERED_NODE_ITERATOR_TYPE) {
					var r, result = [];
					while (r = resultSet.iterateNext()) {
						result[result.length] = r;
					}
					return result;
			}
		}
		return [];
	},

	getObjectByXPath: function(document, xpath, mapVal) {
		function isArray(obj) {
			if (obj === null || obj === undefined) {
				return false;
			}
			return obj.constructor === Array;
		}

		function xmlToObject(xml, attrs, mapVal) {
			var key = xml.nodeType;
			var obj = {};

			if (key === 3) {
				if (mapVal === Boolean) {
					switch (xml.nodeValue.toLowerCase()) {
						/*default:
							return undefined;
						case "1":
						case "true":
							return true;
						*/

						case '':
						case '0':
						case 'false':
							return false;

						default:
							return true;
					}
				}

				if (typeof(mapVal) === 'function') {
					return mapVal.apply(this, [xml.nodeValue])
				}

				return xml.nodeValue;
			}

			/*if (key == 1 && attrs && xml.attributes.length) {
				var attributes = {};
				for (var i = 0; i < xml.attributes.length; i++) {
					attributes[xml.attributes[i].nodeName] = xml.attributes[i].nodeValue;
				}
				obj["@attributes"] = attributes;
			}*/

			var mapArray = {};
			var getArray = false;

			if (isArray(mapVal)) {
				mapVal = mapVal[0];
				getArray = true;
			}

			// primitive: <tag>value</tag>
			if (xml.childNodes.length === 1 && xml.firstChild.nodeType === 3) {
				obj = xmlToObject(xml.firstChild, attrs, mapVal);
			}
			else for (var n = 0; n < xml.childNodes.length; n++) {
				var child = xml.childNodes[n];
				key = child.nodeName;
				var mapField = mapVal && mapVal[key];

				if (obj.hasOwnProperty(key)) {
					if (!mapArray.hasOwnProperty(key)) {
						if (isArray(mapField)) {
							mapArray[key] = mapField[0];
						}
						else {
							mapArray[key] = mapField;
							obj[key] = [obj[key]];
						}
					}
					obj[key].push(xmlToObject(child, attrs, mapArray[key]));
				}
				else {
					obj[key] = xmlToObject(child, attrs, mapField);
				}
			}
			return getArray ? [obj] : obj;
		}

		if (document && xpath) {
			var retnode = null;
			// hack for arrays
			var lastMemeber = xpath.lastIndexOf('/');
			if (lastMemeber >= 0) {
				retnode = xpath.substr(lastMemeber + 1);
				xpath = xpath.substr(0, lastMemeber);
			}

			var result = {};
			var evaluator = new XPathEvaluator();

			if (mapVal === null || mapVal === undefined) {
				mapVal = Object;
			}
			else if (mapVal === Boolean || mapVal.constructor == Boolean) {
				var resultSet = evaluator.evaluate(xpath, document, WsdlUtils.nsDoc.resolver, XPathResult.BOOLEAN_TYPE, null);
				if (resultSet.resultType == XPathResult.BOOLEAN_TYPE) {
					return resultSet.booleanValue;
				}
				return mapVal === Boolean ? undefined : mapVal;
			}
			else if (mapVal === Number || mapVal.constructor == Number) {
				var resultSet = evaluator.evaluate(xpath, document, WsdlUtils.nsDoc.resolver, XPathResult.NUMBER_TYPE, null);
				if (resultSet.resultType == XPathResult.NUMBER_TYPE) {
					return resultSet.numberValue;
				}
				return mapVal === Number ? undefined : mapVal;
			}
			else if (mapVal === String || mapVal.constructor == String) {
				var resultSet = evaluator.evaluate(xpath, document, WsdlUtils.nsDoc.resolver, XPathResult.STRING_TYPE, null);
				if (resultSet.resultType == XPathResult.STRING_TYPE) {
					return resultSet.stringValue;
				}
				return mapVal === String ? undefined : mapVal;
			}

			var resultSet = evaluator.evaluate(xpath, document, WsdlUtils.nsDoc.resolver, XPathResult.ANY_TYPE, null);
			if (resultSet.resultType == XPathResult.UNORDERED_NODE_ITERATOR_TYPE) {
				var it = resultSet.iterateNext();
				if (resultSet.iterateNext()) {
					throw "invalid webservice result.";
				}
				if (it) {
					if (retnode === null) {
						return xmlToObject(it, false, mapVal);
					}
					else {
						var mapping = {};
						mapping[retnode] = mapVal;
						return xmlToObject(it, false, mapping)[retnode];
					}
				}
				return undefined;
			}
		}
		return undefined;
	},

	/// returns a given node as a boolen
	getBooleanByXPath: function(document, xpath, defVal) {
		return WsdlUtils.getObjectByXPath(document, xpath, Boolean) || defVal;
	},
	getNumberByXPath: function(document, xpath, defVal) {
		return WsdlUtils.getObjectByXPath(document, xpath, Number) || defVal;
	},
	getStringByXPath: function(document, xpath, defVal) {
		return WsdlUtils.getObjectByXPath(document, xpath, String) || defVal;
	},

	/// dynamically include a javascript file
	includeJavascript: function(src, callBack) {
		if (document.createElement && document.getElementsByTagName) {
			var head_tag = document.getElementsByTagName('head')[0];
			var script_tag = document.createElement('script');
			script_tag.setAttribute('type', 'text/javascript');
			if (callBack) {
				script_tag.setAttribute('src', src);
				//~ head_tag.appendChild(script_tag);
				script_tag.onload = callBack;
			}
			else {
				var http_request = undefined;

				if (window.XMLHttpRequest) {
					http_request = new XMLHttpRequest();
				}
				else if (window.ActiveXObject) {
					http_request = new ActiveXObject("Microsoft.XMLHTTP");
				}
				http_request.open("GET", src, false);
				http_request.send();
				var script = http_request.responseText;
				script_tag.innerHTML = script;
			}
			head_tag.appendChild(script_tag);
		}
	}
};
