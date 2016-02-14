var escapeJSONString = (function() {
	var b = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g, a = {
		"\b" : "\\b",
		"\t" : "\\t",
		"\n" : "\\n",
		"\f" : "\\f",
		"\r" : "\\r",
		'"' : '\\"',
		"\\" : "\\\\"
	};
	return function(c) {
		b.lastIndex = 0;
		return b.test(c) ? '"'
				+ c.replace(b, function(d) {
					var e = a[d];
					return typeof e === "string" ? e : "\\u"
							+ ("0000" + d.charCodeAt(0).toString(16)).slice(-4)
				}) + '"' : '"' + c + '"'
	}
})();
function toJSON(f) {
	var a = "$_$jabsorbed$813492";
	var h;
	var e = [];
	function c() {
		var i;
		while (h) {
			i = h[a].prev;
			delete h[a];
			h = i
		}
	}
	var g = {};
	var b;
	function d(k, j, l) {
		var u = [], n, m, t, r, q;
		if (k === null || k === undefined) {
			return "null"
		} else {
			if (typeof k === "string") {
				return escapeJSONString(k)
			} else {
				if (typeof k === "number") {
					return k.toString()
				} else {
					if (typeof k === "boolean") {
						return k.toString()
					} else {
						if (k[a]) {
							n = [ l ];
							t = j;
							while (t) {
								if (m) {
									m.unshift(t[a].ref)
								}
								if (t === k) {
									r = t;
									m = [ r[a].ref ]
								}
								n.unshift(t[a].ref);
								t = t[a].parent
							}
							if (r) {
								if (JSONRpcClient.fixupCircRefs) {
									n.shift();
									m.shift();
									e.push([ n, m ]);
									return g
								} else {
									c();
									throw new Error(
											"circular reference detected!")
								}
							} else {
								if (JSONRpcClient.fixupDuplicates) {
									m = [ k[a].ref ];
									t = k[a].parent;
									while (t) {
										m.unshift(t[a].ref);
										t = t[a].parent
									}
									n.shift();
									m.shift();
									e.push([ n, m ]);
									return g
								}
							}
						} else {
							k[a] = {
								parent : j,
								prev : h,
								ref : l
							};
							h = k
						}
						if (k.constructor === Date) {
							if (k.javaClass) {
								return '{javaClass: "' + k.javaClass
										+ '", time: ' + k.valueOf() + "}"
							} else {
								return '{javaClass: "java.util.Date", time: '
										+ k.valueOf() + "}"
							}
						} else {
							if (k.constructor === Array) {
								for (q = 0; q < k.length; q++) {
									b = d(k[q], k, q);
									u.push(b === g ? null : b)
								}
								return "[" + u.join(", ") + "]"
							} else {
								for ( var s in k) {
									if (s === a) {
									} else {
										if (k[s] === null || k[s] === undefined) {
											u.push('"' + s + '": null')
										} else {
											if (typeof k[s] == "function") {
											} else {
												b = d(k[s], k, s);
												if (b !== g) {
													u.push(escapeJSONString(s)
															+ ": " + b)
												}
											}
										}
									}
								}
								return "{" + u.join(", ") + "}"
							}
						}
					}
				}
			}
		}
	}
	b = d(f, null, "root");
	c();
	if (e.length) {
		return {
			json : b,
			fixups : e
		}
	} else {
		return {
			json : b
		}
	}
}
function JSONRpcClient() {
	var b = 0, h, a, d, c, e, g = (typeof arguments[0]), f = true;
	if (g === "function") {
		this.readyCB = arguments[0];
		b++
	} else {
		if (arguments[0] && g === "object" && arguments[0].length) {
			this._addMethods(arguments[0]);
			b++;
			f = false
		}
	}
	this.serverURL = arguments[b];
	this.user = arguments[b + 1];
	this.pass = arguments[b + 2];
	this.objectID = 0;
	if (f) {
		this._addMethods([ "system.listMethods" ]);
		h = JSONRpcClient._makeRequest(this, "system.listMethods", []);
		if (this.readyCB) {
			c = this;
			h.cb = function(i, j) {
				if (!j) {
					c._addMethods(i)
				}
				c.readyCB(i, j)
			}
		}
		if (!this.readyCB) {
			d = JSONRpcClient._sendRequest(this, h);
			this._addMethods(d)
		} else {
			JSONRpcClient.async_requests.push(h);
			JSONRpcClient.kick_async()
		}
	}
}
JSONRpcClient.prototype.createCallableProxy = function(g, f) {
	var e, d, c, a, b;
	e = new JSONRPCCallableProxy(g, f);
	for (a in JSONRpcClient.knownClasses[f]) {
		e[a] = JSONRpcClient.bind(JSONRpcClient.knownClasses[f][a], e)
	}
	return e
};
function JSONRPCCallableProxy() {
	this.objectID = arguments[0];
	this.javaClass = arguments[1];
	this.JSONRPCType = "CallableReference"
}
JSONRpcClient.knownClasses = {};
JSONRpcClient.Exception = function(b) {
	var a;
	for ( var c in b) {
		if (b.hasOwnProperty(c)) {
			this[c] = b[c]
		}
	}
	if (this.trace) {
		a = this.trace.match(/^([^:]*)/);
		if (a) {
			this.name = a[0]
		}
	}
	if (!this.name) {
		this.name = "JSONRpcClientException"
	}
};
JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION = 490;
JSONRpcClient.Exception.CODE_ERR_CLIENT = 550;
JSONRpcClient.Exception.CODE_ERR_PARSE = 590;
JSONRpcClient.Exception.CODE_ERR_NOMETHOD = 591;
JSONRpcClient.Exception.CODE_ERR_UNMARSHALL = 592;
JSONRpcClient.Exception.CODE_ERR_MARSHALL = 593;
JSONRpcClient.Exception.prototype = new Error();
JSONRpcClient.Exception.prototype.toString = function(a, c) {
	var b = "";
	if (this.name) {
		b += this.name
	}
	if (this.message) {
		b += ": " + this.message
	}
	if (b.length == 0) {
		b = "no exception information given"
	}
	return b
};
JSONRpcClient.default_ex_handler = function(c) {
	var b, d = "";
	for (b in c) {
		d += b + "\t" + c[b] + "\n"
	}
	alert(d)
};
JSONRpcClient.toplevel_ex_handler = JSONRpcClient.default_ex_handler;
JSONRpcClient.profile_async = false;
JSONRpcClient.max_req_active = 1;
JSONRpcClient.requestId = 1;
JSONRpcClient.fixupCircRefs = true;
JSONRpcClient.fixupDuplicates = true;
JSONRpcClient.transformDates = false;
JSONRpcClient.transformDateWithoutHint = false;
JSONRpcClient.javaDateClasses = {
	"java.util.Date" : true,
	"java.sql.Date" : true,
	"java.sql.Time" : true,
	"java.sql.Timestamp" : true
};
JSONRpcClient.bind = function(b, a) {
	return function() {
		return b.apply(a, arguments)
	}
};
JSONRpcClient._createMethod = function(a, c) {
	var b = function() {
		var d = [], g;
		for (var e = 0; e < arguments.length; e++) {
			d.push(arguments[e])
		}
		if (typeof d[0] == "function") {
			g = d.shift()
		}
		var f = JSONRpcClient._makeRequest(this, c, d, this.objectID, g);
		if (!g) {
			return JSONRpcClient._sendRequest(a, f)
		} else {
			JSONRpcClient.async_requests.push(f);
			JSONRpcClient.kick_async();
			return f.requestId
		}
	};
	return b
};
JSONRpcClient.prototype.createObject = function() {
	var c = [], f = null, b, a, e;
	for (var d = 0; d < arguments.length; d++) {
		c.push(arguments[d])
	}
	if (typeof c[0] == "function") {
		f = c.shift()
	}
	b = c[0] + ".$constructor";
	a = c[1];
	e = JSONRpcClient._makeRequest(this, b, a, 0, f);
	if (f === null) {
		return JSONRpcClient._sendRequest(this, e)
	} else {
		JSONRpcClient.async_requests.push(e);
		JSONRpcClient.kick_async();
		return e.requestId
	}
};
JSONRpcClient.CALLABLE_REFERENCE_METHOD_PREFIX = ".ref";
JSONRpcClient.prototype._addMethods = function(m, g) {
	var c, j, l, d, a, f = [], e, b, o, k;
	for (var h = 0; h < m.length; h++) {
		j = this;
		l = m[h].split(".");
		o = m[h].indexOf("[");
		k = m[h].indexOf("]");
		if ((m[h].substring(0,
				JSONRpcClient.CALLABLE_REFERENCE_METHOD_PREFIX.length) == JSONRpcClient.CALLABLE_REFERENCE_METHOD_PREFIX)
				&& (o != -1) && (k != -1) && (o < k)) {
			e = m[h].substring(o + 1, k)
		} else {
			for (d = 0; d < l.length - 1; d++) {
				c = l[d];
				if (j[c]) {
					j = j[c]
				} else {
					j[c] = {};
					j = j[c]
				}
			}
		}
		c = l[l.length - 1];
		if (e) {
			a = JSONRpcClient._createMethod(this, c);
			if (!JSONRpcClient.knownClasses[e]) {
				JSONRpcClient.knownClasses[e] = {}
			}
			JSONRpcClient.knownClasses[e][c] = a
		} else {
			a = JSONRpcClient._createMethod(this, m[h]);
			if ((!j[c]) && (!g)) {
				j[c] = JSONRpcClient.bind(a, this)
			}
			f.push(a)
		}
		e = null
	}
	return f
};
JSONRpcClient._getCharsetFromHeaders = function(a) {
	var f, d, b;
	try {
		f = a.getResponseHeader("Content-type");
		d = f.split(/\s*;\s*/);
		for (b = 0; b < d.length; b++) {
			if (d[b].substring(0, 8) == "charset=") {
				return d[b].substring(8, d[b].length)
			}
		}
	} catch (c) {
	}
	return "UTF-8"
};
JSONRpcClient.async_requests = [];
JSONRpcClient.async_inflight = {};
JSONRpcClient.async_responses = [];
JSONRpcClient.async_timeout = null;
JSONRpcClient.num_req_active = 0;
JSONRpcClient._async_handler = function() {
	var a, b;
	JSONRpcClient.async_timeout = null;
	while (JSONRpcClient.async_responses.length > 0) {
		a = JSONRpcClient.async_responses.shift();
		if (a.canceled) {
			continue
		}
		if (a.profile) {
			a.profile.dispatch = new Date()
		}
		try {
			a.cb(a.result, a.ex, a.profile)
		} catch (c) {
			JSONRpcClient.toplevel_ex_handler(c)
		}
	}
	while (JSONRpcClient.async_requests.length > 0
			&& JSONRpcClient.num_req_active < JSONRpcClient.max_req_active) {
		b = JSONRpcClient.async_requests.shift();
		if (b.canceled) {
			continue
		}
		JSONRpcClient._sendRequest(b.client, b)
	}
};
JSONRpcClient.kick_async = function() {
	if (!JSONRpcClient.async_timeout) {
		JSONRpcClient.async_timeout = setTimeout(JSONRpcClient._async_handler,
				0)
	}
};
JSONRpcClient.cancelRequest = function(b) {
	if (JSONRpcClient.async_inflight[b]) {
		JSONRpcClient.async_inflight[b].canceled = true;
		return true
	}
	var a;
	for (a in JSONRpcClient.async_requests) {
		if (JSONRpcClient.async_requests[a].requestId == b) {
			JSONRpcClient.async_requests[a].canceled = true;
			return true
		}
	}
	for (a in JSONRpcClient.async_responses) {
		if (JSONRpcClient.async_responses[a].requestId == b) {
			JSONRpcClient.async_responses[a].canceled = true;
			return true
		}
	}
	return false
};
JSONRpcClient._makeRequest = function(b, c, e, h, a) {
	var f = {};
	f.client = b;
	f.requestId = JSONRpcClient.requestId++;
	var g = "{id:" + f.requestId + ",method:";
	if ((h) && (h > 0)) {
		g += '".obj[' + h + "]." + c + '"'
	} else {
		g += '"' + c + '"'
	}
	if (a) {
		f.cb = a
	}
	if (JSONRpcClient.profile_async) {
		f.profile = {
			submit : new Date()
		}
	}
	var d = toJSON(e);
	g += ",params:" + d.json;
	if (d.fixups) {
		g += ",fixups:" + toJSON(d.fixups).json
	}
	f.data = g + "}";
	return f
};
JSONRpcClient._sendRequest = function(a, c) {
	var b;
	if (c.profile) {
		c.profile.start = new Date()
	}
	b = JSONRpcClient.poolGetHTTPRequest();
	JSONRpcClient.num_req_active++;
	b.open("POST", a.serverURL, !!c.cb, a.user, a.pass);
	try {
		b.setRequestHeader("Content-type", "text/plain")
	} catch (d) {
	}
	if (c.cb) {
		b.onreadystatechange = function() {
			var f;
			if (b.readyState == 4) {
				b.onreadystatechange = function() {
				};
				f = {
					cb : c.cb,
					result : null,
					ex : null
				};
				if (c.profile) {
					f.profile = c.profile;
					f.profile.end = new Date()
				} else {
					f.profile = false
				}
				try {
					f.result = a._handleResponse(b)
				} catch (g) {
					f.ex = g
				}
				if (!JSONRpcClient.async_inflight[c.requestId].canceled) {
					JSONRpcClient.async_responses.push(f)
				}
				delete JSONRpcClient.async_inflight[c.requestId];
				JSONRpcClient.kick_async()
			}
		}
	} else {
		b.onreadystatechange = function() {
		}
	}
	JSONRpcClient.async_inflight[c.requestId] = c;
	try {
		b.send(c.data)
	} catch (d) {
		JSONRpcClient.poolReturnHTTPRequest(b);
		JSONRpcClient.num_req_active--;
		throw new JSONRpcClient.Exception({
			code : JSONRpcClient.Exception.CODE_ERR_CLIENT,
			message : "Connection failed"
		})
	}
	if (!c.cb) {
		delete JSONRpcClient.async_inflight[c.requestId];
		return a._handleResponse(b)
	}
	return null
};
JSONRpcClient.prototype._handleResponse = function(b) {
	if (!this.charset) {
		this.charset = JSONRpcClient._getCharsetFromHeaders(b)
	}
	var a, f, c;
	try {
		a = b.status;
		f = b.statusText;
		c = b.responseText
	} catch (d) {
		JSONRpcClient.poolReturnHTTPRequest(b);
		JSONRpcClient.num_req_active--;
		JSONRpcClient.kick_async();
		throw new JSONRpcClient.Exception({
			code : JSONRpcClient.Exception.CODE_ERR_CLIENT,
			message : "Connection failed"
		})
	}
	JSONRpcClient.poolReturnHTTPRequest(b);
	JSONRpcClient.num_req_active--;
	if (a != 200) {
		throw new JSONRpcClient.Exception({
			code : a,
			message : f
		})
	}
	return this.unmarshallResponse(c)
};
JSONRpcClient.prototype.unmarshallResponse = function(data) {
	function applyFixups(obj, fixups) {
		function findOriginal(ob, original) {
			for (var i = 0, j = original.length; i < j; i++) {
				ob = ob[original[i]]
			}
			return ob
		}
		function applyFixup(ob, fixups, value) {
			var j = fixups.length - 1;
			for (var i = 0; i < j; i++) {
				ob = ob[fixups[i]]
			}
			ob[fixups[j]] = value
		}
		for (var i = 0, j = fixups.length; i < j; i++) {
			applyFixup(obj, fixups[i][0], findOriginal(obj, fixups[i][1]))
		}
	}
	function transformDate(obj) {
		function hasOnlyProperty(obj, prop) {
			var i, count = 0;
			if (obj.hasOwnProperty(prop)) {
				for (i in obj) {
					if (obj.hasOwnProperty(i)) {
						count++;
						if (count > 1) {
							return
						}
					}
				}
				return true
			}
		}
		var i, d;
		if (obj && typeof obj === "object") {
			if ((obj.javaClass && JSONRpcClient.javaDateClasses[obj.javaClass])) {
				d = new Date(obj.time);
				if (obj.javaClass !== "java.util.Date") {
					d.javaClass = obj.javaClass
				}
				return d
			} else {
				if (JSONRpcClient.transformDateWithoutHint
						&& hasOnlyProperty(obj, "time")) {
					return new Date(obj.time)
				} else {
					for (i in obj) {
						if (obj.hasOwnProperty(i)) {
							obj[i] = transformDate(obj[i])
						}
					}
					return obj
				}
			}
		} else {
			return obj
		}
	}
	var obj;
	try {
		eval("obj = " + data)
	} catch (e) {
		throw new JSONRpcClient.Exception({
			code : 550,
			message : "error parsing result"
		})
	}
	if (obj.error) {
		throw new JSONRpcClient.Exception(obj.error)
	}
	var r = obj.result;
	var i, tmp;
	if (r) {
		if (r.objectID && r.JSONRPCType == "CallableReference") {
			return this.createCallableProxy(r.objectID, r.javaClass)
		} else {
			r = JSONRpcClient.extractCallableReferences(this,
					JSONRpcClient.transformDates ? transformDate(r) : r);
			if (obj.fixups) {
				applyFixups(r, obj.fixups)
			}
		}
	}
	return r
};
JSONRpcClient.extractCallableReferences = function(b, a) {
	var d, c, e;
	for (d in a) {
		if (typeof (a[d]) == "object") {
			c = JSONRpcClient.makeCallableReference(b, a[d]);
			if (c) {
				a[d] = c
			} else {
				c = JSONRpcClient.extractCallableReferences(b, a[d]);
				a[d] = c
			}
		}
		if (typeof (d) == "object") {
			c = JSONRpcClient.makeCallableReference(b, d);
			if (c) {
				e = a[d];
				delete a[d];
				a[c] = e
			} else {
				c = JSONRpcClient.extractCallableReferences(b, d);
				e = a[d];
				delete a[d];
				a[c] = e
			}
		}
	}
	return a
};
JSONRpcClient.makeCallableReference = function(a, b) {
	if (b && b.objectID && b.javaClass && b.JSONRPCType == "CallableReference") {
		return a.createCallableProxy(b.objectID, b.javaClass)
	}
	return null
};
JSONRpcClient.http_spare = [];
JSONRpcClient.http_max_spare = 8;
JSONRpcClient.poolGetHTTPRequest = function() {
	var a = JSONRpcClient.http_spare.pop();
	if (a) {
		return a
	}
	return JSONRpcClient.getHTTPRequest()
};
JSONRpcClient.poolReturnHTTPRequest = function(a) {
	if (JSONRpcClient.http_spare.length >= JSONRpcClient.http_max_spare) {
		delete a
	} else {
		JSONRpcClient.http_spare.push(a)
	}
};
JSONRpcClient.msxmlNames = [ "MSXML2.XMLHTTP.6.0", "MSXML2.XMLHTTP.3.0",
		"MSXML2.XMLHTTP", "MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.4.0",
		"Microsoft.XMLHTTP" ];
JSONRpcClient.getHTTPRequest = function() {
	try {
		JSONRpcClient.httpObjectName = "XMLHttpRequest";
		return new XMLHttpRequest()
	} catch (b) {
	}
	for (var a = 0; a < JSONRpcClient.msxmlNames.length; a++) {
		try {
			JSONRpcClient.httpObjectName = JSONRpcClient.msxmlNames[a];
			return new ActiveXObject(JSONRpcClient.msxmlNames[a])
		} catch (b) {
		}
	}
	JSONRpcClient.httpObjectName = null;
	throw new JSONRpcClient.Exception({
		code : 0,
		message : "Can't create XMLHttpRequest object"
	})
};