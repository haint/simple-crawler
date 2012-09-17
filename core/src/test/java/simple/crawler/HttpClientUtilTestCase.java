/*
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package simple.crawler;

import java.util.List;

import junit.framework.Assert;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class HttpClientUtilTestCase extends Assert
{
	
	@Test
	public void testAddCookie() throws Exception 
	{
		DefaultHttpClient httpclient = HttpClientFactory.getInstance();
		BasicClientCookie cookie = (BasicClientCookie)HttpClientUtil.addCookie(httpclient, "jsession", "12345");
		cookie.setDomain("org.simple");
		cookie.setPath("/");
		cookie.setSecure(false);
		
		List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		assertEquals(1, cookies.size());
		assertEquals("jsession", cookies.get(0).getName());
		assertEquals("12345", cookies.get(0).getValue());
		assertEquals("org.simple", cookies.get(0).getDomain());
		assertEquals("/", cookies.get(0).getPath());
		assertFalse(cookies.get(0).isSecure());
	}
	
	@Test
	public void dump() throws Exception
	{
		String html = HttpClientUtil.fetch(HttpClientFactory.getInstance(), "http://www.vn-zoom.com/f171/");
		assertNotNull(html);
	}
}
