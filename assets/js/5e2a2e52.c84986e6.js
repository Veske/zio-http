"use strict";(self.webpackChunkzio_http_docs=self.webpackChunkzio_http_docs||[]).push([[887],{3905:(e,t,r)=>{r.d(t,{Zo:()=>s,kt:()=>f});var n=r(7294);function a(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function o(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function p(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?o(Object(r),!0).forEach((function(t){a(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):o(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function c(e,t){if(null==e)return{};var r,n,a=function(e,t){if(null==e)return{};var r,n,a={},o=Object.keys(e);for(n=0;n<o.length;n++)r=o[n],t.indexOf(r)>=0||(a[r]=e[r]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(n=0;n<o.length;n++)r=o[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(a[r]=e[r])}return a}var i=n.createContext({}),l=function(e){var t=n.useContext(i),r=t;return e&&(r="function"==typeof e?e(t):p(p({},t),e)),r},s=function(e){var t=l(e.components);return n.createElement(i.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},m=n.forwardRef((function(e,t){var r=e.components,a=e.mdxType,o=e.originalType,i=e.parentName,s=c(e,["components","mdxType","originalType","parentName"]),m=l(r),f=a,u=m["".concat(i,".").concat(f)]||m[f]||d[f]||o;return r?n.createElement(u,p(p({ref:t},s),{},{components:r})):n.createElement(u,p({ref:t},s))}));function f(e,t){var r=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=r.length,p=new Array(o);p[0]=m;var c={};for(var i in t)hasOwnProperty.call(t,i)&&(c[i]=t[i]);c.originalType=e,c.mdxType="string"==typeof e?e:a,p[1]=c;for(var l=2;l<o;l++)p[l]=r[l];return n.createElement.apply(null,p)}return n.createElement.apply(null,r)}m.displayName="MDXCreateElement"},724:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>i,contentTitle:()=>p,default:()=>d,frontMatter:()=>o,metadata:()=>c,toc:()=>l});var n=r(7462),a=(r(7294),r(3905));const o={},p="CSRF",c={unversionedId:"v1.x/examples/advanced-examples/middleware_csrf",id:"v1.x/examples/advanced-examples/middleware_csrf",title:"CSRF",description:"",source:"@site/docs/v1.x/examples/advanced-examples/middleware_csrf.md",sourceDirName:"v1.x/examples/advanced-examples",slug:"/v1.x/examples/advanced-examples/middleware_csrf",permalink:"/zio-http/docs/v1.x/examples/advanced-examples/middleware_csrf",draft:!1,tags:[],version:"current",frontMatter:{},sidebar:"tutorialSidebar",previous:{title:"CORS Handling",permalink:"/zio-http/docs/v1.x/examples/advanced-examples/middleware_cors"},next:{title:"Streaming File",permalink:"/zio-http/docs/v1.x/examples/advanced-examples/stream-file"}},i={},l=[],s={toc:l};function d(e){let{components:t,...r}=e;return(0,a.kt)("wrapper",(0,n.Z)({},s,r,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"csrf"},"CSRF"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'package example\n\nimport zio.http.Middleware.{csrfGenerate, csrfValidate}\nimport zio.http._\nimport zio.http.Server\nimport zio._\n\nobject CSRF extends App {\n  val privateApp = Http.collect[Request] { case Method.GET -> !! / "unsafeEndpoint" =>\n    Response.text("secure info")\n  } @@ csrfValidate() // Check for matching csrf header and cookie\n\n  val publicApp = Http.collect[Request] { case Method.GET -> !! / "safeEndpoint" =>\n    Response.text("hello")\n  } @@ csrfGenerate() // set x-csrf token cookie\n\n  val app                                                        = publicApp ++ privateApp\n  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =\n    Server.start(8090, app).exitCode\n}\n\n')))}d.isMDXComponent=!0}}]);