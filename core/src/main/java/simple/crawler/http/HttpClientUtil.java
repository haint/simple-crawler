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
package simple.crawler.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class HttpClientUtil
{
	
	public static String fetch(HttpClient httpclient, String uri) throws Exception
	{
		if(httpclient == null) 
		{
			throw new NullPointerException();
		}
		if(uri == null)
		{
			throw new NullPointerException();
		}
		HttpGet get = new HttpGet(uri);
		return getContentBodyAsString(httpclient.execute(get));
	}
	
	public static Cookie addCookie(HttpClient httpclient, String name, String value) 
	{
		if(httpclient == null)
		{
			throw new NullPointerException();
		}
		BasicClientCookie cookie = new BasicClientCookie(name, value);
		((AbstractHttpClient) httpclient).getCookieStore().addCookie(cookie);
		return cookie;
	}
	
	public static void setProxy(HttpClient httpclient, String proxy, int port)
	{
		HttpHost host = new HttpHost(proxy, port, "http");
		((AbstractHttpClient) httpclient).getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, host);
	}
	
	public static String getContentBodyAsString(HttpResponse res) throws IOException 
	{
		InputStream is = res.getEntity().getContent();
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		for(int l = bis.read(buff); l != -1; l = bis.read(buff))
		{
			baos.write(buff, 0, buff.length);
			buff = new byte[1024];
		}
		return new String(baos.toByteArray(), "UTF-8");
	}

	public static String getContentType(HttpResponse res) 
	{
		Header header = res.getFirstHeader("Content-Type");
		if(header == null) 
		{
			return "unknown/unknown";
		}
		String value = header.getValue();
		return value.substring(0, value.lastIndexOf(';'));
	}
}
