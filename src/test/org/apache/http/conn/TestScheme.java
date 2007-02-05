/*
 * $HeadURL$
 * $Revision$
 * $Date$
 * 
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

package org.apache.http.conn;

import java.util.Iterator;

import org.apache.http.conn.ssl.SSLSocketFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for {@link Scheme} and {@link SchemeSet}.
 *
 * @author <a href="mailto:rolandw at apache.org">Roland Weber</a>
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 */
public class TestScheme extends TestCase {

    public TestScheme(String testName) {
        super(testName);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestScheme.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        return new TestSuite(TestScheme.class);
    }

    public void testConstructor() {
        Scheme http = new Scheme
            ("http", PlainSocketFactory.getSocketFactory(), 80);
        assertEquals("http", http.getName()); 
        assertEquals(80, http.getDefaultPort()); 
        assertSame(PlainSocketFactory.getSocketFactory(),
                   http.getSocketFactory()); 
        assertFalse(http.isLayered()); 
        Scheme https = new Scheme
            ("https", SSLSocketFactory.getSocketFactory(), 443);
        assertEquals("https", https.getName()); 
        assertEquals(443, https.getDefaultPort()); 
        assertSame(SSLSocketFactory.getSocketFactory(),
                   https.getSocketFactory()); 
        assertTrue(https.isLayered());

        Scheme hTtP = new Scheme
            ("hTtP", PlainSocketFactory.getSocketFactory(), 80);
        assertEquals("http", hTtP.getName());
        // the rest is no different from above

        try {
            new Scheme(null, PlainSocketFactory.getSocketFactory(), 80);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            new Scheme("http", null, 80);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            new Scheme("http", PlainSocketFactory.getSocketFactory(), -1);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            new Scheme("http", PlainSocketFactory.getSocketFactory(), 70000);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testRegisterUnregister() {
        SchemeSet schmset = new SchemeSet();

        Scheme http = new Scheme
            ("http", PlainSocketFactory.getSocketFactory(), 80);
        Scheme https = new Scheme
            ("https", SSLSocketFactory.getSocketFactory(), 443);
        Scheme myhttp = new Scheme
            ("http", PlainSocketFactory.getSocketFactory(), 80);

    	assertNull(schmset.register(myhttp));
    	assertNull(schmset.register(https));
    	assertSame(myhttp, schmset.register(http));
    	assertSame(http, schmset.getScheme("http"));
    	assertSame(https, schmset.getScheme("https"));

    	schmset.unregister("http");
    	schmset.unregister("https");

        assertNull(schmset.get("http")); // get() does not throw exception
    	try {
            schmset.getScheme("http"); // getScheme() does throw exception
            fail("IllegalStateException should have been thrown");
    	} catch (IllegalStateException ex) {
            // expected
    	}
    }


    public void testIterator() {
        SchemeSet schmset = new SchemeSet();

        Iterator iter = schmset.getSchemeNames();
        assertNotNull(iter);
        assertFalse(iter.hasNext());

        Scheme http = new Scheme
            ("http", PlainSocketFactory.getSocketFactory(), 80);
        Scheme https = new Scheme
            ("https", SSLSocketFactory.getSocketFactory(), 443);

    	schmset.register(http);
    	schmset.register(https);

        iter = schmset.getSchemeNames();
        assertNotNull(iter);
        assertTrue(iter.hasNext());

        boolean flaghttp  = false;
        boolean flaghttps = false;
        String name = (String) iter.next();
        assertTrue(iter.hasNext());

        if ("http".equals(name))
            flaghttp = true;
        else if ("https".equals(name))
            flaghttps = true;
        else
            fail("unexpected name in iterator: " + name);

        assertNotNull(schmset.get(name));
        iter.remove();
        assertTrue(iter.hasNext());
        assertNull(schmset.get(name));

        name = (String) iter.next();
        assertFalse(iter.hasNext());

        if ("http".equals(name)) {
            if (flaghttp) fail("name 'http' found twice");
        } else if ("https".equals(name)) {
            if (flaghttps) fail("name 'https' found twice");
        } else {
            fail("unexpected name in iterator: " + name);
        }

        assertNotNull(schmset.get(name));
    }

    public void testIllegalRegisterUnregister() {
        SchemeSet schmset = new SchemeSet();
        try {
        	schmset.register(null);
        	fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex) {
        	// expected
        }
        try {
        	schmset.unregister(null);
        	fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex) {
        	// expected
        }
        try {
        	schmset.get(null);
        	fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex) {
        	// expected
        }
        try {
        	schmset.getScheme(null);
        	fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex) {
        	// expected
        }
    }
    
    public void testResolvePort() {
        Scheme http = new Scheme
            ("http", PlainSocketFactory.getSocketFactory(), 80);

        assertEquals(8080, http.resolvePort(8080));
        assertEquals(80, http.resolvePort(-1));
    }
    
    public void testHashCode() {
        Scheme http = new Scheme
            ("http", PlainSocketFactory.getSocketFactory(), 80);
        Scheme myhttp = new Scheme
            ("http", PlainSocketFactory.getSocketFactory(), 80);
        Scheme https = new Scheme
            ("http", SSLSocketFactory.getSocketFactory(), 443);

        assertTrue(http.hashCode() != https.hashCode());
        assertTrue(http.hashCode() == myhttp.hashCode());
    }
    
    public void testEquals() {
        Scheme http = new Scheme
            ("http", PlainSocketFactory.getSocketFactory(), 80);
        Scheme myhttp = new Scheme
            ("http", PlainSocketFactory.getSocketFactory(), 80);
        Scheme https = new Scheme
            ("http", SSLSocketFactory.getSocketFactory(), 443);

        assertFalse(http.equals(https));
        assertFalse(http.equals(null));
        assertFalse(http.equals("http"));
        assertTrue(http.equals(http));
        assertTrue(http.equals(myhttp));
        assertFalse(http.equals(https));
    }

    public void testToString() {
        Scheme http = new Scheme
            ("http", PlainSocketFactory.getSocketFactory(), 80);
        assertEquals("http:80", http.toString());
    }
    
}