/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.client.protocol;

import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteInfo.LayerType;
import org.apache.http.conn.routing.RouteInfo.TunnelType;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.SM;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.impl.cookie.IgnoreSpecFactory;
import org.apache.http.impl.cookie.NetscapeDraftSpecFactory;
import org.apache.http.impl.cookie.RFC2109SpecFactory;
import org.apache.http.impl.cookie.RFC2965SpecFactory;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestRequestAddCookies {

    private HttpHost target;
    private CookieStore cookieStore;
    private Lookup<CookieSpecProvider> cookieSpecRegistry;

    @Before
    public void setUp() {
        this.target = new HttpHost("localhost.local", 80);
        this.cookieStore = new BasicCookieStore();
        BasicClientCookie2 cookie1 = new BasicClientCookie2("name1", "value1");
        cookie1.setVersion(1);
        cookie1.setDomain("localhost.local");
        cookie1.setPath("/");
        this.cookieStore.addCookie(cookie1);
        BasicClientCookie2 cookie2 = new BasicClientCookie2("name2", "value2");
        cookie2.setVersion(1);
        cookie2.setDomain("localhost.local");
        cookie2.setPath("/");
        this.cookieStore.addCookie(cookie2);

        this.cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
            .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
            .register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory())
            .register(CookieSpecs.NETSCAPE, new NetscapeDraftSpecFactory())
            .register(CookieSpecs.RFC_2109, new RFC2109SpecFactory())
            .register(CookieSpecs.RFC_2965, new RFC2965SpecFactory())
            .register(CookieSpecs.IGNORE_COOKIES, new IgnoreSpecFactory())
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRequestParameterCheck() throws Exception {
        HttpContext context = new BasicHttpContext();
        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(null, context);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testContextParameterCheck() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/");
        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, null);
    }

    @Test
    public void testAddCookies() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/");

        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(2, headers1.length);
        Assert.assertEquals("$Version=1; name1=\"value1\"", headers1[0].getValue());
        Assert.assertEquals("$Version=1; name2=\"value2\"", headers1[1].getValue());
        Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);

        CookieOrigin cookieOrigin = (CookieOrigin) context.getAttribute(
                ClientContext.COOKIE_ORIGIN);
        Assert.assertNotNull(cookieOrigin);
        Assert.assertEquals(this.target.getHostName(), cookieOrigin.getHost());
        Assert.assertEquals(this.target.getPort(), cookieOrigin.getPort());
        Assert.assertEquals("/", cookieOrigin.getPath());
        Assert.assertFalse(cookieOrigin.isSecure());
    }

    @Test
    public void testCookiesForConnectRequest() throws Exception {
        HttpRequest request = new BasicHttpRequest("CONNECT", "www.somedomain.com");

        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testNoCookieStore() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/");

        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, null);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testNoCookieSpecRegistry() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/");

        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, null);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testNoTargetHost() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/");

        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, null);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testNoHttpConnection() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/");

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ExecutionContext.HTTP_CONNECTION, null);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testAddCookiesUsingExplicitCookieSpec() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/");
        RequestConfig config = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.REQUEST_CONFIG, config);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        CookieSpec cookieSpec = (CookieSpec) context.getAttribute(
                ClientContext.COOKIE_SPEC);
        Assert.assertTrue(cookieSpec instanceof BrowserCompatSpec);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(1, headers1.length);
        Assert.assertEquals("name1=value1; name2=value2", headers1[0].getValue());
    }

    @Test
    public void testAuthScopeInvalidRequestURI() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "crap:");

        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);
    }

    @Test
    public void testAuthScopeRemotePortWhenDirect() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/stuff");

        this.target = new HttpHost("localhost.local");
        HttpRoute route = new HttpRoute(new HttpHost("localhost.local", 1234), null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        CookieOrigin cookieOrigin = (CookieOrigin) context.getAttribute(
                ClientContext.COOKIE_ORIGIN);
        Assert.assertNotNull(cookieOrigin);
        Assert.assertEquals(this.target.getHostName(), cookieOrigin.getHost());
        Assert.assertEquals(1234, cookieOrigin.getPort());
        Assert.assertEquals("/stuff", cookieOrigin.getPath());
        Assert.assertFalse(cookieOrigin.isSecure());
    }

    @Test
    public void testAuthDefaultHttpPortWhenProxy() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/stuff");

        this.target = new HttpHost("localhost.local");
        HttpRoute route = new HttpRoute(
                new HttpHost("localhost.local", 80), null, new HttpHost("localhost", 8888), false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        CookieOrigin cookieOrigin = (CookieOrigin) context.getAttribute(
                ClientContext.COOKIE_ORIGIN);
        Assert.assertNotNull(cookieOrigin);
        Assert.assertEquals(this.target.getHostName(), cookieOrigin.getHost());
        Assert.assertEquals(80, cookieOrigin.getPort());
        Assert.assertEquals("/stuff", cookieOrigin.getPath());
        Assert.assertFalse(cookieOrigin.isSecure());
    }

    @Test
    public void testAuthDefaultHttpsPortWhenProxy() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/stuff");

        this.target = new HttpHost("localhost", -1, "https");
        HttpRoute route = new HttpRoute(
                new HttpHost("localhost", 443, "https"), null,
                new HttpHost("localhost", 8888), true, TunnelType.TUNNELLED, LayerType.LAYERED);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        CookieOrigin cookieOrigin = (CookieOrigin) context.getAttribute(
                ClientContext.COOKIE_ORIGIN);
        Assert.assertNotNull(cookieOrigin);
        Assert.assertEquals(this.target.getHostName(), cookieOrigin.getHost());
        Assert.assertEquals(443, cookieOrigin.getPort());
        Assert.assertEquals("/stuff", cookieOrigin.getPath());
        Assert.assertTrue(cookieOrigin.isSecure());
    }

    @Test
    public void testExcludeExpiredCookies() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/");

        BasicClientCookie2 cookie3 = new BasicClientCookie2("name3", "value3");
        cookie3.setVersion(1);
        cookie3.setDomain("localhost.local");
        cookie3.setPath("/");
        cookie3.setExpiryDate(new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000)));

        this.cookieStore.addCookie(cookie3);

        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(2, headers1.length);
        Assert.assertEquals("$Version=1; name1=\"value1\"", headers1[0].getValue());
        Assert.assertEquals("$Version=1; name2=\"value2\"", headers1[1].getValue());
        Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testNoMatchingCookies() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/");

        this.cookieStore.clear();
        BasicClientCookie cookie3 = new BasicClientCookie("name3", "value3");
        cookie3.setDomain("www.somedomain.com");
        cookie3.setPath("/");
        this.cookieStore.addCookie(cookie3);

        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    // Helper method
    private BasicClientCookie makeCookie(String name, String value, String domain, String path) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(domain);
        cookie.setPath(path);
        return cookie;
    }

    @Test
    // Test for ordering adapted from test in Commons HC 3.1
    public void testCookieOrder() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/foobar/yada/yada");

        this.cookieStore.clear();

        cookieStore.addCookie(makeCookie("nomatch", "value", "localhost.local", "/noway"));
        cookieStore.addCookie(makeCookie("name2",   "value", "localhost.local", "/foobar/yada"));
        cookieStore.addCookie(makeCookie("name3",   "value", "localhost.local", "/foobar"));
        cookieStore.addCookie(makeCookie("name1",   "value", "localhost.local", "/foobar/yada/yada"));

        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(1, headers1.length);

        Assert.assertEquals("name1=value; name2=value; name3=value", headers1[0].getValue());
    }

    @Test
    public void testAddSpecVersionHeader() throws Exception {
        HttpRequest request = new BasicHttpRequest("GET", "/");

        this.cookieStore.clear();
        BasicClientCookie cookie1 = new BasicClientCookie("name1", "value1");
        cookie1.setVersion(0);
        cookie1.setDomain("localhost.local");
        cookie1.setPath("/");
        this.cookieStore.addCookie(cookie1);
        BasicClientCookie cookie2 = new BasicClientCookie("name2", "value2");
        cookie2.setVersion(0);
        cookie2.setDomain("localhost.local");
        cookie2.setPath("/");
        this.cookieStore.addCookie(cookie2);

        HttpRoute route = new HttpRoute(this.target, null, false);

        HttpContext context = new BasicHttpContext();
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(ClientContext.ROUTE, route);
        context.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        HttpRequestInterceptor interceptor = new RequestAddCookies();
        interceptor.process(request, context);

        Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(1, headers1.length);
        Assert.assertEquals("name1=value1; name2=value2", headers1[0].getValue());
        Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(1, headers2.length);
        Assert.assertEquals("$Version=1", headers2[0].getValue());
    }

}
