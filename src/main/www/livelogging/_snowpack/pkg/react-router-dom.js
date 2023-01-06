import { r as react } from './common/index-04edb6a1.js';
import { _ as _extends } from './common/extends-7477639a.js';
import './common/_commonjsHelpers-8c19dec8.js';

var r,B=r||(r={});B.Pop="POP";B.Push="PUSH";B.Replace="REPLACE";var C=function(b){return b};function E(b){b.preventDefault();b.returnValue="";}
function F(){var b=[];return {get length(){return b.length},push:function(h){b.push(h);return function(){b=b.filter(function(e){return e!==h});}},call:function(h){b.forEach(function(e){return e&&e(h)});}}}function H(){return Math.random().toString(36).substr(2,8)}function I(b){var h=b.pathname;h=void 0===h?"/":h;var e=b.search;e=void 0===e?"":e;b=b.hash;b=void 0===b?"":b;e&&"?"!==e&&(h+="?"===e.charAt(0)?e:"?"+e);b&&"#"!==b&&(h+="#"===b.charAt(0)?b:"#"+b);return h}
function J(b){var h={};if(b){var e=b.indexOf("#");0<=e&&(h.hash=b.substr(e),b=b.substr(0,e));e=b.indexOf("?");0<=e&&(h.search=b.substr(e),b=b.substr(0,e));b&&(h.pathname=b);}return h}
function createBrowserHistory(b){function h(){var c=p.location,a=m.state||{};return [a.idx,C({pathname:c.pathname,search:c.search,hash:c.hash,state:a.usr||null,key:a.key||"default"})]}function e(c){return "string"===typeof c?c:I(c)}function x(c,a){void 0===a&&(a=null);return C(_extends({pathname:q.pathname,hash:"",search:""},"string"===typeof c?J(c):c,{state:a,key:H()}))}function z(c){t=c;c=h();v=c[0];q=c[1];d.call({action:t,location:q});}function A(c,a){function f(){A(c,a);}var l=r.Push,k=x(c,
a);if(!g.length||(g.call({action:l,location:k,retry:f}),!1)){var n=[{usr:k.state,key:k.key,idx:v+1},e(k)];k=n[0];n=n[1];try{m.pushState(k,"",n);}catch(G){p.location.assign(n);}z(l);}}function y(c,a){function f(){y(c,a);}var l=r.Replace,k=x(c,a);g.length&&(g.call({action:l,location:k,retry:f}),1)||(k=[{usr:k.state,key:k.key,idx:v},e(k)],m.replaceState(k[0],"",k[1]),z(l));}function w(c){m.go(c);}void 0===b&&(b={});b=b.window;var p=void 0===b?document.defaultView:b,m=p.history,u=null;p.addEventListener("popstate",
function(){if(u)g.call(u),u=null;else {var c=r.Pop,a=h(),f=a[0];a=a[1];if(g.length)if(null!=f){var l=v-f;l&&(u={action:c,location:a,retry:function(){w(-1*l);}},w(l));}else;else z(c);}});var t=r.Pop;b=h();var v=b[0],q=b[1],d=F(),g=F();null==v&&(v=0,m.replaceState(_extends({},m.state,{idx:v}),""));return {get action(){return t},get location(){return q},createHref:e,push:A,replace:y,go:w,back:function(){w(-1);},forward:function(){w(1);},listen:function(c){return d.push(c)},block:function(c){var a=g.push(c);1===g.length&&p.addEventListener("beforeunload",E);return function(){a();g.length||p.removeEventListener("beforeunload",E);}}}}

/**
 * React Router v6.2.1
 *
 * Copyright (c) Remix Software Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE.md file in the root directory of this source tree.
 *
 * @license MIT
 */

function invariant(cond, message) {
  if (!cond) throw new Error(message);
}
// CONTEXT
///////////////////////////////////////////////////////////////////////////////

/**
 * A Navigator is a "location changer"; it's how you get to different locations.
 *
 * Every history instance conforms to the Navigator interface, but the
 * distinction is useful primarily when it comes to the low-level <Router> API
 * where both the location and a navigator must be provided separately in order
 * to avoid "tearing" that may occur in a suspense-enabled app if the action
 * and/or location were to be read directly from the history instance.
 */


const NavigationContext = /*#__PURE__*/react.createContext(null);

const LocationContext = /*#__PURE__*/react.createContext(null);

/**
 * Provides location context for the rest of the app.
 *
 * Note: You usually won't render a <Router> directly. Instead, you'll render a
 * router that is more specific to your environment such as a <BrowserRouter>
 * in web browsers or a <StaticRouter> for server rendering.
 *
 * @see https://reactrouter.com/docs/en/v6/api#router
 */
function Router(_ref3) {
  let {
    basename: basenameProp = "/",
    children = null,
    location: locationProp,
    navigationType = r.Pop,
    navigator,
    static: staticProp = false
  } = _ref3;
  !!useInRouterContext() ?  invariant(false) : void 0;
  let basename = normalizePathname(basenameProp);
  let navigationContext = react.useMemo(() => ({
    basename,
    navigator,
    static: staticProp
  }), [basename, navigator, staticProp]);

  if (typeof locationProp === "string") {
    locationProp = J(locationProp);
  }

  let {
    pathname = "/",
    search = "",
    hash = "",
    state = null,
    key = "default"
  } = locationProp;
  let location = react.useMemo(() => {
    let trailingPathname = stripBasename(pathname, basename);

    if (trailingPathname == null) {
      return null;
    }

    return {
      pathname: trailingPathname,
      search,
      hash,
      state,
      key
    };
  }, [basename, pathname, search, hash, state, key]);

  if (location == null) {
    return null;
  }

  return /*#__PURE__*/react.createElement(NavigationContext.Provider, {
    value: navigationContext
  }, /*#__PURE__*/react.createElement(LocationContext.Provider, {
    children: children,
    value: {
      location,
      navigationType
    }
  }));
}
/**
 * Returns true if this component is a descendant of a <Router>.
 *
 * @see https://reactrouter.com/docs/en/v6/api#useinroutercontext
 */

function useInRouterContext() {
  return react.useContext(LocationContext) != null;
}

function stripBasename(pathname, basename) {
  if (basename === "/") return pathname;

  if (!pathname.toLowerCase().startsWith(basename.toLowerCase())) {
    return null;
  }

  let nextChar = pathname.charAt(basename.length);

  if (nextChar && nextChar !== "/") {
    // pathname does not start with basename/
    return null;
  }

  return pathname.slice(basename.length) || "/";
}

const normalizePathname = pathname => pathname.replace(/\/+$/, "").replace(/^\/*/, "/");

/**
 * React Router DOM v6.2.1
 *
 * Copyright (c) Remix Software Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE.md file in the root directory of this source tree.
 *
 * @license MIT
 */
// COMPONENTS
////////////////////////////////////////////////////////////////////////////////

/**
 * A `<Router>` for use in web browsers. Provides the cleanest URLs.
 */
function BrowserRouter(_ref) {
  let {
    basename,
    children,
    window
  } = _ref;
  let historyRef = react.useRef();

  if (historyRef.current == null) {
    historyRef.current = createBrowserHistory({
      window
    });
  }

  let history = historyRef.current;
  let [state, setState] = react.useState({
    action: history.action,
    location: history.location
  });
  react.useLayoutEffect(() => history.listen(setState), [history]);
  return /*#__PURE__*/react.createElement(Router, {
    basename: basename,
    children: children,
    location: state.location,
    navigationType: state.action,
    navigator: history
  });
}

export { BrowserRouter };
