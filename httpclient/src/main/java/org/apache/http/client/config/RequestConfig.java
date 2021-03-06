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

package org.apache.http.client.config;

import java.net.InetAddress;
import java.util.Collection;

import org.apache.http.HttpHost;

public class RequestConfig implements Cloneable {

    public static final RequestConfig DEFAULT = new Builder().build();

    private final boolean expectContinueEnabled;
    private final HttpHost defaultProxy;
    private final InetAddress localAddress;
    private final boolean staleConnectionCheckEnabled;
    private final String cookieSpec;
    private final boolean redirectsEnabled;
    private final boolean relativeRedirectsAllowed;
    private final boolean circularRedirectsAllowed;
    private final int maxRedirects;
    private final boolean authenticationEnabled;
    private final Collection<String> targetPreferredAuthSchemes;
    private final Collection<String> proxyPreferredAuthSchemes;
    private final int connectionRequestTimeout;
    private final int connectTimeout;
    private final int socketTimeout;

    RequestConfig(
            final boolean expectContinueEnabled,
            final HttpHost defaultProxy,
            final InetAddress localAddress,
            final boolean staleConnectionCheckEnabled,
            final String cookieSpec,
            final boolean redirectsEnabled,
            final boolean relativeRedirectsAllowed,
            final boolean circularRedirectsAllowed,
            final int maxRedirects,
            final boolean authenticationEnabled,
            final Collection<String> targetPreferredAuthSchemes,
            final Collection<String> proxyPreferredAuthSchemes,
            final int connectionRequestTimeout,
            final int connectTimeout,
            final int socketTimeout) {
        super();
        this.expectContinueEnabled = expectContinueEnabled;
        this.defaultProxy = defaultProxy;
        this.localAddress = localAddress;
        this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
        this.cookieSpec = cookieSpec;
        this.redirectsEnabled = redirectsEnabled;
        this.relativeRedirectsAllowed = relativeRedirectsAllowed;
        this.circularRedirectsAllowed = circularRedirectsAllowed;
        this.maxRedirects = maxRedirects;
        this.authenticationEnabled = authenticationEnabled;
        this.targetPreferredAuthSchemes = targetPreferredAuthSchemes;
        this.proxyPreferredAuthSchemes = proxyPreferredAuthSchemes;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
    }

    public boolean isExpectContinueEnabled() {
        return expectContinueEnabled;
    }

    public HttpHost getDefaultProxy() {
        return defaultProxy;
    }

    public InetAddress getLocalAddress() {
        return localAddress;
    }

    public boolean isStaleConnectionCheckEnabled() {
        return staleConnectionCheckEnabled;
    }

    public String getCookieSpec() {
        return cookieSpec;
    }

    public boolean isRedirectsEnabled() {
        return redirectsEnabled;
    }

    public boolean isRelativeRedirectsAllowed() {
        return relativeRedirectsAllowed;
    }

    public boolean isCircularRedirectsAllowed() {
        return circularRedirectsAllowed;
    }

    public int getMaxRedirects() {
        return maxRedirects;
    }

    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }

    public Collection<String> getTargetPreferredAuthSchemes() {
        return targetPreferredAuthSchemes;
    }

    public Collection<String> getProxyPreferredAuthSchemes() {
        return proxyPreferredAuthSchemes;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    @Override
    protected RequestConfig clone() throws CloneNotSupportedException {
        return (RequestConfig) super.clone();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(", expectContinueEnabled=").append(expectContinueEnabled);
        builder.append(", defaultProxy=").append(defaultProxy);
        builder.append(", localAddress=").append(localAddress);
        builder.append(", staleConnectionCheckEnabled=").append(staleConnectionCheckEnabled);
        builder.append(", cookieSpec=").append(cookieSpec);
        builder.append(", redirectsEnabled=").append(redirectsEnabled);
        builder.append(", relativeRedirectsAllowed=").append(relativeRedirectsAllowed);
        builder.append(", maxRedirects=").append(maxRedirects);
        builder.append(", circularRedirectsAllowed=").append(circularRedirectsAllowed);
        builder.append(", authenticationEnabled=").append(authenticationEnabled);
        builder.append(", targetPreferredAuthSchemes=").append(targetPreferredAuthSchemes);
        builder.append(", proxyPreferredAuthSchemes=").append(proxyPreferredAuthSchemes);
        builder.append(", connectionRequestTimeout=").append(connectionRequestTimeout);
        builder.append(", connectTimeout=").append(connectTimeout);
        builder.append(", socketTimeout=").append(socketTimeout);
        builder.append("]");
        return builder.toString();
    }

    public static RequestConfig.Builder custom() {
        return new Builder();
    }

    public static RequestConfig.Builder copy(final RequestConfig config) {
        return new Builder()
            .setExpectContinueEnabled(config.isExpectContinueEnabled())
            .setDefaultProxy(config.getDefaultProxy())
            .setLocalAddress(config.getLocalAddress())
            .setStaleConnectionCheckEnabled(config.isStaleConnectionCheckEnabled())
            .setCookieSpec(config.getCookieSpec())
            .setRedirectsEnabled(config.isRedirectsEnabled())
            .setRelativeRedirectsAllowed(config.isRelativeRedirectsAllowed())
            .setCircularRedirectsAllowed(config.isCircularRedirectsAllowed())
            .setMaxRedirects(config.getMaxRedirects())
            .setAuthenticationEnabled(config.isAuthenticationEnabled())
            .setTargetPreferredAuthSchemes(config.getProxyPreferredAuthSchemes())
            .setProxyPreferredAuthSchemes(config.getProxyPreferredAuthSchemes())
            .setConnectionRequestTimeout(config.getConnectionRequestTimeout())
            .setConnectTimeout(config.getConnectTimeout())
            .setSocketTimeout(config.getSocketTimeout());
    }

    public static class Builder {

        private boolean expectContinueEnabled;
        private HttpHost defaultProxy;
        private InetAddress localAddress;
        private boolean staleConnectionCheckEnabled;
        private String cookieSpec;
        private boolean redirectsEnabled;
        private boolean relativeRedirectsAllowed;
        private boolean circularRedirectsAllowed;
        private int maxRedirects;
        private boolean authenticationEnabled;
        private Collection<String> targetPreferredAuthSchemes;
        private Collection<String> proxyPreferredAuthSchemes;
        private int connectionRequestTimeout;
        private int connectTimeout;
        private int socketTimeout;

        Builder() {
            super();
            this.staleConnectionCheckEnabled = true;
            this.redirectsEnabled = true;
            this.maxRedirects = 50;
            this.relativeRedirectsAllowed = true;
            this.authenticationEnabled = true;
            this.connectionRequestTimeout = -1;
            this.connectTimeout = -1;
            this.socketTimeout = -1;
        }

        public Builder setExpectContinueEnabled(boolean expectContinueEnabled) {
            this.expectContinueEnabled = expectContinueEnabled;
            return this;
        }

        public Builder setDefaultProxy(final HttpHost defaultProxy) {
            this.defaultProxy = defaultProxy;
            return this;
        }

        public Builder setLocalAddress(final InetAddress localAddress) {
            this.localAddress = localAddress;
            return this;
        }

        public Builder setStaleConnectionCheckEnabled(boolean staleConnectionCheckEnabled) {
            this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
            return this;
        }

        public Builder setCookieSpec(final String cookieSpec) {
            this.cookieSpec = cookieSpec;
            return this;
        }

        public Builder setRedirectsEnabled(boolean redirectsEnabled) {
            this.redirectsEnabled = redirectsEnabled;
            return this;
        }

        public Builder setRelativeRedirectsAllowed(boolean relativeRedirectsAllowed) {
            this.relativeRedirectsAllowed = relativeRedirectsAllowed;
            return this;
        }

        public Builder setCircularRedirectsAllowed(boolean circularRedirectsAllowed) {
            this.circularRedirectsAllowed = circularRedirectsAllowed;
            return this;
        }

        public Builder setMaxRedirects(final int maxRedirects) {
            this.maxRedirects = maxRedirects;
            return this;
        }

        public Builder setAuthenticationEnabled(boolean authenticationEnabled) {
            this.authenticationEnabled = authenticationEnabled;
            return this;
        }

        public Builder setTargetPreferredAuthSchemes(final Collection<String> targetPreferredAuthSchemes) {
            this.targetPreferredAuthSchemes = targetPreferredAuthSchemes;
            return this;
        }

        public Builder setProxyPreferredAuthSchemes(final Collection<String> proxyPreferredAuthSchemes) {
            this.proxyPreferredAuthSchemes = proxyPreferredAuthSchemes;
            return this;
        }

        public Builder setConnectionRequestTimeout(final int connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        public Builder setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setSocketTimeout(final int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public RequestConfig build() {
            return new RequestConfig(
                    expectContinueEnabled,
                    defaultProxy,
                    localAddress,
                    staleConnectionCheckEnabled,
                    cookieSpec,
                    redirectsEnabled,
                    relativeRedirectsAllowed,
                    circularRedirectsAllowed,
                    maxRedirects,
                    authenticationEnabled,
                    targetPreferredAuthSchemes,
                    proxyPreferredAuthSchemes,
                    connectionRequestTimeout,
                    connectTimeout,
                    socketTimeout);
        }

    }

}
