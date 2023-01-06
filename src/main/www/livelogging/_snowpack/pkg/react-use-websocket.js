import { c as createCommonjsModule, a as commonjsGlobal, g as getDefaultExportFromCjs } from './common/_commonjsHelpers-8c19dec8.js';
import { r as react } from './common/index-04edb6a1.js';

var constants = createCommonjsModule(function (module, exports) {
Object.defineProperty(exports, "__esModule", { value: true });
exports.isEventSourceSupported = exports.isReactNative = exports.ReadyState = exports.UNPARSABLE_JSON_OBJECT = exports.DEFAULT_RECONNECT_INTERVAL_MS = exports.DEFAULT_RECONNECT_LIMIT = exports.SOCKET_IO_PING_CODE = exports.SOCKET_IO_PATH = exports.SOCKET_IO_PING_INTERVAL = exports.DEFAULT_EVENT_SOURCE_OPTIONS = exports.EMPTY_EVENT_HANDLERS = exports.DEFAULT_OPTIONS = void 0;
var MILLISECONDS = 1;
var SECONDS = 1000 * MILLISECONDS;
exports.DEFAULT_OPTIONS = {};
exports.EMPTY_EVENT_HANDLERS = {};
exports.DEFAULT_EVENT_SOURCE_OPTIONS = {
    withCredentials: false,
    events: exports.EMPTY_EVENT_HANDLERS,
};
exports.SOCKET_IO_PING_INTERVAL = 25 * SECONDS;
exports.SOCKET_IO_PATH = '/socket.io/?EIO=3&transport=websocket';
exports.SOCKET_IO_PING_CODE = '2';
exports.DEFAULT_RECONNECT_LIMIT = 20;
exports.DEFAULT_RECONNECT_INTERVAL_MS = 5000;
exports.UNPARSABLE_JSON_OBJECT = {};
(function (ReadyState) {
    ReadyState[ReadyState["UNINSTANTIATED"] = -1] = "UNINSTANTIATED";
    ReadyState[ReadyState["CONNECTING"] = 0] = "CONNECTING";
    ReadyState[ReadyState["OPEN"] = 1] = "OPEN";
    ReadyState[ReadyState["CLOSING"] = 2] = "CLOSING";
    ReadyState[ReadyState["CLOSED"] = 3] = "CLOSED";
})(exports.ReadyState || (exports.ReadyState = {}));
var eventSourceSupported = function () {
    try {
        return 'EventSource' in globalThis;
    }
    catch (e) {
        return false;
    }
};
exports.isReactNative = typeof navigator !== 'undefined' && navigator.product === 'ReactNative';
exports.isEventSourceSupported = !exports.isReactNative && eventSourceSupported();

});

var globals = createCommonjsModule(function (module, exports) {
Object.defineProperty(exports, "__esModule", { value: true });
exports.sharedWebSockets = void 0;
exports.sharedWebSockets = {};

});

var socketIo = createCommonjsModule(function (module, exports) {
Object.defineProperty(exports, "__esModule", { value: true });
exports.setUpSocketIOPing = exports.appendQueryParams = exports.parseSocketIOUrl = void 0;

exports.parseSocketIOUrl = function (url) {
    if (url) {
        var isSecure = /^https|wss/.test(url);
        var strippedProtocol = url.replace(/^(https?|wss?)(:\/\/)?/, '');
        var removedFinalBackSlack = strippedProtocol.replace(/\/$/, '');
        var protocol = isSecure ? 'wss' : 'ws';
        return protocol + "://" + removedFinalBackSlack + constants.SOCKET_IO_PATH;
    }
    else if (url === '') {
        var isSecure = /^https/.test(window.location.protocol);
        var protocol = isSecure ? 'wss' : 'ws';
        var port = window.location.port ? ":" + window.location.port : '';
        return protocol + "://" + window.location.hostname + port + constants.SOCKET_IO_PATH;
    }
    return url;
};
exports.appendQueryParams = function (url, params) {
    if (params === void 0) { params = {}; }
    var hasParamsRegex = /\?([\w]+=[\w]+)/;
    var alreadyHasParams = hasParamsRegex.test(url);
    var stringified = "" + Object.entries(params).reduce(function (next, _a) {
        var key = _a[0], value = _a[1];
        return next + (key + "=" + value + "&");
    }, '').slice(0, -1);
    return "" + url + (alreadyHasParams ? '&' : '?') + stringified;
};
exports.setUpSocketIOPing = function (sendMessage, interval) {
    if (interval === void 0) { interval = constants.SOCKET_IO_PING_INTERVAL; }
    var ping = function () { return sendMessage(constants.SOCKET_IO_PING_CODE); };
    return setInterval(ping, interval);
};

});

var util = createCommonjsModule(function (module, exports) {
Object.defineProperty(exports, "__esModule", { value: true });
exports.assertIsWebSocket = void 0;
function assertIsWebSocket(webSocketInstance) {
    if (webSocketInstance instanceof WebSocket === false)
        throw new Error('');
}
exports.assertIsWebSocket = assertIsWebSocket;

});

var attachListener = createCommonjsModule(function (module, exports) {
var __assign = (commonjsGlobal && commonjsGlobal.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.attachListeners = void 0;



var bindMessageHandler = function (webSocketInstance, optionsRef, setLastMessage) {
    webSocketInstance.onmessage = function (message) {
        optionsRef.current.onMessage && optionsRef.current.onMessage(message);
        if (typeof optionsRef.current.filter === 'function' && optionsRef.current.filter(message) !== true) {
            return;
        }
        setLastMessage(message);
    };
};
var bindOpenHandler = function (webSocketInstance, optionsRef, setReadyState, reconnectCount) {
    webSocketInstance.onopen = function (event) {
        optionsRef.current.onOpen && optionsRef.current.onOpen(event);
        reconnectCount.current = 0;
        setReadyState(constants.ReadyState.OPEN);
    };
};
var bindCloseHandler = function (webSocketInstance, optionsRef, setReadyState, reconnect, reconnectCount) {
    if (constants.isEventSourceSupported && webSocketInstance instanceof EventSource) {
        return function () { };
    }
    util.assertIsWebSocket(webSocketInstance);
    var reconnectTimeout;
    webSocketInstance.onclose = function (event) {
        var _a, _b;
        optionsRef.current.onClose && optionsRef.current.onClose(event);
        setReadyState(constants.ReadyState.CLOSED);
        if (optionsRef.current.shouldReconnect && optionsRef.current.shouldReconnect(event)) {
            var reconnectAttempts = (_a = optionsRef.current.reconnectAttempts) !== null && _a !== void 0 ? _a : constants.DEFAULT_RECONNECT_LIMIT;
            if (reconnectCount.current < reconnectAttempts) {
                reconnectTimeout = window.setTimeout(function () {
                    reconnectCount.current++;
                    reconnect();
                }, (_b = optionsRef.current.reconnectInterval) !== null && _b !== void 0 ? _b : constants.DEFAULT_RECONNECT_INTERVAL_MS);
            }
            else {
                optionsRef.current.onReconnectStop && optionsRef.current.onReconnectStop(reconnectAttempts);
                console.warn("Max reconnect attempts of " + reconnectAttempts + " exceeded");
            }
        }
    };
    return function () { return reconnectTimeout && window.clearTimeout(reconnectTimeout); };
};
var bindErrorHandler = function (webSocketInstance, optionsRef, setReadyState, reconnect, reconnectCount) {
    var reconnectTimeout;
    webSocketInstance.onerror = function (error) {
        var _a, _b;
        optionsRef.current.onError && optionsRef.current.onError(error);
        if (constants.isEventSourceSupported && webSocketInstance instanceof EventSource) {
            optionsRef.current.onClose && optionsRef.current.onClose(__assign(__assign({}, error), { code: 1006, reason: "An error occurred with the EventSource: " + error, wasClean: false }));
            setReadyState(constants.ReadyState.CLOSED);
            webSocketInstance.close();
        }
        if (optionsRef.current.retryOnError) {
            if (reconnectCount.current < ((_a = optionsRef.current.reconnectAttempts) !== null && _a !== void 0 ? _a : constants.DEFAULT_RECONNECT_LIMIT)) {
                reconnectTimeout = window.setTimeout(function () {
                    reconnectCount.current++;
                    reconnect();
                }, (_b = optionsRef.current.reconnectInterval) !== null && _b !== void 0 ? _b : constants.DEFAULT_RECONNECT_INTERVAL_MS);
            }
            else {
                optionsRef.current.onReconnectStop && optionsRef.current.onReconnectStop(optionsRef.current.reconnectAttempts);
                console.warn("Max reconnect attempts of " + optionsRef.current.reconnectAttempts + " exceeded");
            }
        }
    };
    return function () { return reconnectTimeout && window.clearTimeout(reconnectTimeout); };
};
exports.attachListeners = function (webSocketInstance, setters, optionsRef, reconnect, reconnectCount, sendMessage) {
    var setLastMessage = setters.setLastMessage, setReadyState = setters.setReadyState;
    var interval;
    var cancelReconnectOnClose;
    var cancelReconnectOnError;
    if (optionsRef.current.fromSocketIO) {
        interval = socketIo.setUpSocketIOPing(sendMessage);
    }
    bindMessageHandler(webSocketInstance, optionsRef, setLastMessage);
    bindOpenHandler(webSocketInstance, optionsRef, setReadyState, reconnectCount);
    cancelReconnectOnClose = bindCloseHandler(webSocketInstance, optionsRef, setReadyState, reconnect, reconnectCount);
    cancelReconnectOnError = bindErrorHandler(webSocketInstance, optionsRef, setReadyState, reconnect, reconnectCount);
    return function () {
        setReadyState(constants.ReadyState.CLOSING);
        cancelReconnectOnClose();
        cancelReconnectOnError();
        webSocketInstance.close();
        if (interval)
            clearInterval(interval);
    };
};

});

var manageSubscribers = createCommonjsModule(function (module, exports) {
Object.defineProperty(exports, "__esModule", { value: true });
exports.removeSubscriber = exports.addSubscriber = exports.hasSubscribers = exports.getSubscribers = void 0;
var subscribers = {};
var EMPTY_LIST = [];
exports.getSubscribers = function (url) {
    if (exports.hasSubscribers(url)) {
        return Array.from(subscribers[url]);
    }
    return EMPTY_LIST;
};
exports.hasSubscribers = function (url) {
    var _a;
    return ((_a = subscribers[url]) === null || _a === void 0 ? void 0 : _a.size) > 0;
};
exports.addSubscriber = function (url, subscriber) {
    subscribers[url] = subscribers[url] || new Set();
    subscribers[url].add(subscriber);
};
exports.removeSubscriber = function (url, subscriber) {
    subscribers[url].delete(subscriber);
};

});

var attachSharedListeners = createCommonjsModule(function (module, exports) {
var __assign = (commonjsGlobal && commonjsGlobal.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.attachSharedListeners = void 0;




var bindMessageHandler = function (webSocketInstance, url) {
    webSocketInstance.onmessage = function (message) {
        manageSubscribers.getSubscribers(url).forEach(function (subscriber) {
            if (subscriber.optionsRef.current.onMessage) {
                subscriber.optionsRef.current.onMessage(message);
            }
            if (typeof subscriber.optionsRef.current.filter === 'function' &&
                subscriber.optionsRef.current.filter(message) !== true) {
                return;
            }
            subscriber.setLastMessage(message);
        });
    };
};
var bindOpenHandler = function (webSocketInstance, url) {
    webSocketInstance.onopen = function (event) {
        manageSubscribers.getSubscribers(url).forEach(function (subscriber) {
            subscriber.reconnectCount.current = 0;
            if (subscriber.optionsRef.current.onOpen) {
                subscriber.optionsRef.current.onOpen(event);
            }
            subscriber.setReadyState(constants.ReadyState.OPEN);
        });
    };
};
var bindCloseHandler = function (webSocketInstance, url) {
    if (webSocketInstance instanceof WebSocket) {
        webSocketInstance.onclose = function (event) {
            manageSubscribers.getSubscribers(url).forEach(function (subscriber) {
                if (subscriber.optionsRef.current.onClose) {
                    subscriber.optionsRef.current.onClose(event);
                }
                subscriber.setReadyState(constants.ReadyState.CLOSED);
            });
            delete globals.sharedWebSockets[url];
            manageSubscribers.getSubscribers(url).forEach(function (subscriber) {
                var _a, _b;
                if (subscriber.optionsRef.current.shouldReconnect &&
                    subscriber.optionsRef.current.shouldReconnect(event)) {
                    var reconnectAttempts = (_a = subscriber.optionsRef.current.reconnectAttempts) !== null && _a !== void 0 ? _a : constants.DEFAULT_RECONNECT_LIMIT;
                    if (subscriber.reconnectCount.current < reconnectAttempts) {
                        setTimeout(function () {
                            subscriber.reconnectCount.current++;
                            subscriber.reconnect.current();
                        }, (_b = subscriber.optionsRef.current.reconnectInterval) !== null && _b !== void 0 ? _b : constants.DEFAULT_RECONNECT_INTERVAL_MS);
                    }
                    else {
                        subscriber.optionsRef.current.onReconnectStop && subscriber.optionsRef.current.onReconnectStop(subscriber.optionsRef.current.reconnectAttempts);
                        console.warn("Max reconnect attempts of " + reconnectAttempts + " exceeded");
                    }
                }
            });
        };
    }
};
var bindErrorHandler = function (webSocketInstance, url) {
    webSocketInstance.onerror = function (error) {
        manageSubscribers.getSubscribers(url).forEach(function (subscriber) {
            if (subscriber.optionsRef.current.onError) {
                subscriber.optionsRef.current.onError(error);
            }
            if (constants.isEventSourceSupported && webSocketInstance instanceof EventSource) {
                subscriber.optionsRef.current.onClose && subscriber.optionsRef.current.onClose(__assign(__assign({}, error), { code: 1006, reason: "An error occurred with the EventSource: " + error, wasClean: false }));
                subscriber.setReadyState(constants.ReadyState.CLOSED);
            }
        });
        if (constants.isEventSourceSupported && webSocketInstance instanceof EventSource) {
            webSocketInstance.close();
        }
    };
};
exports.attachSharedListeners = function (webSocketInstance, url, optionsRef, sendMessage) {
    var interval;
    if (optionsRef.current.fromSocketIO) {
        interval = socketIo.setUpSocketIOPing(sendMessage);
    }
    bindMessageHandler(webSocketInstance, url);
    bindCloseHandler(webSocketInstance, url);
    bindOpenHandler(webSocketInstance, url);
    bindErrorHandler(webSocketInstance, url);
    return function () {
        if (interval)
            clearInterval(interval);
    };
};

});

var createOrJoin = createCommonjsModule(function (module, exports) {
Object.defineProperty(exports, "__esModule", { value: true });
exports.createOrJoinSocket = void 0;





//TODO ensure that all onClose callbacks are called
var cleanSubscribers = function (url, subscriber, optionsRef, setReadyState, clearSocketIoPingInterval) {
    return function () {
        manageSubscribers.removeSubscriber(url, subscriber);
        if (!manageSubscribers.hasSubscribers(url)) {
            try {
                var socketLike = globals.sharedWebSockets[url];
                if (socketLike instanceof WebSocket) {
                    socketLike.onclose = function (event) {
                        if (optionsRef.current.onClose) {
                            optionsRef.current.onClose(event);
                        }
                        setReadyState(constants.ReadyState.CLOSED);
                    };
                }
                socketLike.close();
            }
            catch (e) {
            }
            if (clearSocketIoPingInterval)
                clearSocketIoPingInterval();
            delete globals.sharedWebSockets[url];
        }
    };
};
exports.createOrJoinSocket = function (webSocketRef, url, setReadyState, optionsRef, setLastMessage, startRef, reconnectCount, sendMessage) {
    if (!constants.isEventSourceSupported && optionsRef.current.eventSourceOptions) {
        if (constants.isReactNative) {
            throw new Error('EventSource is not supported in ReactNative');
        }
        else {
            throw new Error('EventSource is not supported');
        }
    }
    if (optionsRef.current.share) {
        var clearSocketIoPingInterval = null;
        if (globals.sharedWebSockets[url] === undefined) {
            globals.sharedWebSockets[url] = optionsRef.current.eventSourceOptions ?
                new EventSource(url, optionsRef.current.eventSourceOptions) :
                new WebSocket(url, optionsRef.current.protocols);
            webSocketRef.current = globals.sharedWebSockets[url];
            setReadyState(constants.ReadyState.CONNECTING);
            clearSocketIoPingInterval = attachSharedListeners.attachSharedListeners(globals.sharedWebSockets[url], url, optionsRef, sendMessage);
        }
        else {
            webSocketRef.current = globals.sharedWebSockets[url];
            setReadyState(globals.sharedWebSockets[url].readyState);
        }
        var subscriber = {
            setLastMessage: setLastMessage,
            setReadyState: setReadyState,
            optionsRef: optionsRef,
            reconnectCount: reconnectCount,
            reconnect: startRef,
        };
        manageSubscribers.addSubscriber(url, subscriber);
        return cleanSubscribers(url, subscriber, optionsRef, setReadyState, clearSocketIoPingInterval);
    }
    else {
        webSocketRef.current = optionsRef.current.eventSourceOptions ?
            new EventSource(url, optionsRef.current.eventSourceOptions) :
            new WebSocket(url, optionsRef.current.protocols);
        setReadyState(constants.ReadyState.CONNECTING);
        if (!webSocketRef.current) {
            throw new Error('WebSocket failed to be created');
        }
        return attachListener.attachListeners(webSocketRef.current, {
            setLastMessage: setLastMessage,
            setReadyState: setReadyState
        }, optionsRef, startRef.current, reconnectCount, sendMessage);
    }
};

});

var getUrl = createCommonjsModule(function (module, exports) {
var __awaiter = (commonjsGlobal && commonjsGlobal.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (commonjsGlobal && commonjsGlobal.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.getUrl = void 0;

exports.getUrl = function (url, optionsRef) { return __awaiter(void 0, void 0, void 0, function () {
    var convertedUrl, parsedUrl, parsedWithQueryParams;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (!(typeof url === 'function')) return [3 /*break*/, 2];
                return [4 /*yield*/, url()];
            case 1:
                convertedUrl = _a.sent();
                return [3 /*break*/, 3];
            case 2:
                convertedUrl = url;
                _a.label = 3;
            case 3:
                parsedUrl = optionsRef.current.fromSocketIO ?
                    socketIo.parseSocketIOUrl(convertedUrl) :
                    convertedUrl;
                parsedWithQueryParams = optionsRef.current.queryParams ?
                    socketIo.appendQueryParams(parsedUrl, optionsRef.current.queryParams) :
                    parsedUrl;
                return [2 /*return*/, parsedWithQueryParams];
        }
    });
}); };

});

var proxy = createCommonjsModule(function (module, exports) {
Object.defineProperty(exports, "__esModule", { value: true });
exports.websocketWrapper = void 0;
exports.websocketWrapper = function (webSocket, start) {
    return new Proxy(webSocket, {
        get: function (obj, key) {
            var val = obj[key];
            if (key === 'reconnect')
                return start;
            if (typeof val === 'function') {
                console.error('Calling methods directly on the websocket is not supported at this moment. You must use the methods returned by useWebSocket.');
                //Prevent error thrown by invoking a non-function
                return function () { };
            }
            else {
                return val;
            }
        },
        set: function (obj, key, val) {
            if (/^on/.test(key)) {
                console.warn('The websocket\'s event handlers should be defined through the options object passed into useWebSocket.');
                return false;
            }
            else {
                obj[key] = val;
                return true;
            }
        },
    });
};
exports.default = exports.websocketWrapper;

});

var useWebsocket = createCommonjsModule(function (module, exports) {
var __assign = (commonjsGlobal && commonjsGlobal.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __awaiter = (commonjsGlobal && commonjsGlobal.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (commonjsGlobal && commonjsGlobal.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
var __importDefault = (commonjsGlobal && commonjsGlobal.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.useWebSocket = void 0;




var proxy_1 = __importDefault(proxy);

exports.useWebSocket = function (url, options, connect) {
    if (options === void 0) { options = constants.DEFAULT_OPTIONS; }
    if (connect === void 0) { connect = true; }
    var _a = react.useState(null), lastMessage = _a[0], setLastMessage = _a[1];
    var _b = react.useState({}), readyState = _b[0], setReadyState = _b[1];
    var lastJsonMessage = react.useMemo(function () {
        if (lastMessage) {
            try {
                return JSON.parse(lastMessage.data);
            }
            catch (e) {
                return constants.UNPARSABLE_JSON_OBJECT;
            }
        }
        return null;
    }, [lastMessage]);
    var convertedUrl = react.useRef(null);
    var webSocketRef = react.useRef(null);
    var startRef = react.useRef(function () { return void 0; });
    var reconnectCount = react.useRef(0);
    var messageQueue = react.useRef([]);
    var webSocketProxy = react.useRef(null);
    var optionsCache = react.useRef(options);
    optionsCache.current = options;
    var readyStateFromUrl = convertedUrl.current && readyState[convertedUrl.current] !== undefined ?
        readyState[convertedUrl.current] :
        url !== null && connect === true ?
            constants.ReadyState.CONNECTING :
            constants.ReadyState.UNINSTANTIATED;
    var stringifiedQueryParams = options.queryParams ? JSON.stringify(options.queryParams) : null;
    var sendMessage = react.useCallback(function (message, keep) {
        var _a;
        if (keep === void 0) { keep = true; }
        if (constants.isEventSourceSupported && webSocketRef.current instanceof EventSource) {
            console.warn('Unable to send a message from an eventSource');
            return;
        }
        if (((_a = webSocketRef.current) === null || _a === void 0 ? void 0 : _a.readyState) === constants.ReadyState.OPEN) {
            util.assertIsWebSocket(webSocketRef.current);
            webSocketRef.current.send(message);
        }
        else if (keep) {
            messageQueue.current.push(message);
        }
    }, []);
    var sendJsonMessage = react.useCallback(function (message, keep) {
        if (keep === void 0) { keep = true; }
        sendMessage(JSON.stringify(message), keep);
    }, [sendMessage]);
    var getWebSocket = react.useCallback(function () {
        if (optionsCache.current.share !== true || (constants.isEventSourceSupported && webSocketRef.current instanceof EventSource)) {
            return webSocketRef.current;
        }
        if (webSocketProxy.current === null && webSocketRef.current) {
            util.assertIsWebSocket(webSocketRef.current);
            webSocketProxy.current = proxy_1.default(webSocketRef.current, startRef);
        }
        return webSocketProxy.current;
    }, []);
    react.useEffect(function () {
        if (url !== null && connect === true) {
            var removeListeners_1;
            var expectClose_1 = false;
            var start_1 = function () { return __awaiter(void 0, void 0, void 0, function () {
                var _a, protectedSetLastMessage, protectedSetReadyState;
                return __generator(this, function (_b) {
                    switch (_b.label) {
                        case 0:
                            _a = convertedUrl;
                            return [4 /*yield*/, getUrl.getUrl(url, optionsCache)];
                        case 1:
                            _a.current = _b.sent();
                            protectedSetLastMessage = function (message) {
                                if (!expectClose_1) {
                                    setLastMessage(message);
                                }
                            };
                            protectedSetReadyState = function (state) {
                                if (!expectClose_1) {
                                    setReadyState(function (prev) {
                                        var _a;
                                        return (__assign(__assign({}, prev), (convertedUrl.current && (_a = {}, _a[convertedUrl.current] = state, _a))));
                                    });
                                }
                            };
                            removeListeners_1 = createOrJoin.createOrJoinSocket(webSocketRef, convertedUrl.current, protectedSetReadyState, optionsCache, protectedSetLastMessage, startRef, reconnectCount, sendMessage);
                            return [2 /*return*/];
                    }
                });
            }); };
            startRef.current = function () {
                if (!expectClose_1) {
                    if (webSocketProxy.current)
                        webSocketProxy.current = null;
                    removeListeners_1 === null || removeListeners_1 === void 0 ? void 0 : removeListeners_1();
                    start_1();
                }
            };
            start_1();
            return function () {
                expectClose_1 = true;
                if (webSocketProxy.current)
                    webSocketProxy.current = null;
                removeListeners_1 === null || removeListeners_1 === void 0 ? void 0 : removeListeners_1();
                setLastMessage(null);
            };
        }
        else if (url === null || connect === false) {
            reconnectCount.current = 0; // reset reconnection attempts
            setReadyState(function (prev) {
                var _a;
                return (__assign(__assign({}, prev), (convertedUrl.current && (_a = {}, _a[convertedUrl.current] = constants.ReadyState.CLOSED, _a))));
            });
        }
    }, [url, connect, stringifiedQueryParams, sendMessage]);
    react.useEffect(function () {
        if (readyStateFromUrl === constants.ReadyState.OPEN) {
            messageQueue.current.splice(0).forEach(function (message) {
                sendMessage(message);
            });
        }
    }, [readyStateFromUrl]);
    return {
        sendMessage: sendMessage,
        sendJsonMessage: sendJsonMessage,
        lastMessage: lastMessage,
        lastJsonMessage: lastJsonMessage,
        readyState: readyStateFromUrl,
        getWebSocket: getWebSocket,
    };
};

});

var useSocketIo = createCommonjsModule(function (module, exports) {
var __assign = (commonjsGlobal && commonjsGlobal.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.useSocketIO = void 0;



var emptyEvent = {
    type: 'empty',
    payload: null,
};
var getSocketData = function (event) {
    if (!event || !event.data) {
        return emptyEvent;
    }
    var match = event.data.match(/\[.*]/);
    if (!match) {
        return emptyEvent;
    }
    var data = JSON.parse(match);
    if (!Array.isArray(data) || !data[1]) {
        return emptyEvent;
    }
    return {
        type: data[0],
        payload: data[1],
    };
};
exports.useSocketIO = function (url, options, connect) {
    if (options === void 0) { options = constants.DEFAULT_OPTIONS; }
    if (connect === void 0) { connect = true; }
    var optionsWithSocketIO = react.useMemo(function () { return (__assign(__assign({}, options), { fromSocketIO: true })); }, []);
    var _a = useWebsocket.useWebSocket(url, optionsWithSocketIO, connect), sendMessage = _a.sendMessage, sendJsonMessage = _a.sendJsonMessage, lastMessage = _a.lastMessage, readyState = _a.readyState, getWebSocket = _a.getWebSocket;
    var socketIOLastMessage = react.useMemo(function () {
        return getSocketData(lastMessage);
    }, [lastMessage]);
    return {
        sendMessage: sendMessage,
        sendJsonMessage: sendJsonMessage,
        lastMessage: socketIOLastMessage,
        lastJsonMessage: socketIOLastMessage,
        readyState: readyState,
        getWebSocket: getWebSocket,
    };
};

});

var useEventSource = createCommonjsModule(function (module, exports) {
var __assign = (commonjsGlobal && commonjsGlobal.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __rest = (commonjsGlobal && commonjsGlobal.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.useEventSource = void 0;



exports.useEventSource = function (url, _a, connect) {
    if (_a === void 0) { _a = constants.DEFAULT_EVENT_SOURCE_OPTIONS; }
    if (connect === void 0) { connect = true; }
    var withCredentials = _a.withCredentials, events = _a.events, options = __rest(_a, ["withCredentials", "events"]);
    var optionsWithEventSource = __assign(__assign({}, options), { eventSourceOptions: {
            withCredentials: withCredentials,
        } });
    var eventsRef = react.useRef(constants.EMPTY_EVENT_HANDLERS);
    if (events) {
        eventsRef.current = events;
    }
    var _b = useWebsocket.useWebSocket(url, optionsWithEventSource, connect), lastMessage = _b.lastMessage, readyState = _b.readyState, getWebSocket = _b.getWebSocket;
    react.useEffect(function () {
        if (lastMessage === null || lastMessage === void 0 ? void 0 : lastMessage.type) {
            Object.entries(eventsRef.current).forEach(function (_a) {
                var type = _a[0], handler = _a[1];
                if (type === lastMessage.type) {
                    handler(lastMessage);
                }
            });
        }
    }, [lastMessage]);
    return {
        lastEvent: lastMessage,
        readyState: readyState,
        getEventSource: getWebSocket,
    };
};

});

var dist = createCommonjsModule(function (module, exports) {
Object.defineProperty(exports, "__esModule", { value: true });

Object.defineProperty(exports, "default", { enumerable: true, get: function () { return useWebsocket.useWebSocket; } });

Object.defineProperty(exports, "useSocketIO", { enumerable: true, get: function () { return useSocketIo.useSocketIO; } });

Object.defineProperty(exports, "ReadyState", { enumerable: true, get: function () { return constants.ReadyState; } });

Object.defineProperty(exports, "useEventSource", { enumerable: true, get: function () { return useEventSource.useEventSource; } });

});

var __pika_web_default_export_for_treeshaking__ = /*@__PURE__*/getDefaultExportFromCjs(dist);

var ReadyState = dist.ReadyState;
export default __pika_web_default_export_for_treeshaking__;
export { ReadyState };
