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

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class HttpClientFactory
{
	/** . */
	public static int MAX_HTTP_CONNECTION = 3000;
	
	/** . */
	private static DefaultHttpClient httpclient;
	
	public static DefaultHttpClient getInstance() 
	{
		if(httpclient == null) 
		{
			httpclient = createNewDefaultHttpClient();
		}
		
		return httpclient;
	}
	
	public static DefaultHttpClient createNewDefaultHttpClient()
	{
		//
		HttpParams params = new BasicHttpParams();

		//Determines the connection timeout
		HttpConnectionParams.setConnectionTimeout(params, 1 * 60 * 1000);
		
		//Determines the socket timeout
		HttpConnectionParams.setSoTimeout(params, 1* 60 * 1000);
		
		//Determines whether stale connection check is to be used
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		
		//The Nagle's algorithm tries to conserve bandwidth by minimizing the number of segments that are sent. 
		//When application wish to decrease network latency and increase performance, they can disable Nagle's algorithm (that is enable TCP_NODELAY)
		//Data will be sent earlier, at the cost of an increase in bandwidth consumption
		HttpConnectionParams.setTcpNoDelay(params, true);
		
		//
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (Windows; U; Windows NT 6.1; ru; rv:1.9.2.4) Gecko/20100513 Firefox/3.6.4");
		
		//Create and initialize scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		PoolingClientConnectionManager pm = new PoolingClientConnectionManager(schemeRegistry);
		
		//
		DefaultHttpClient httpclient = new DefaultHttpClient(pm, params);
		//ConnManagerParams.setMaxTotalConnections(params, MAX_HTTP_CONNECTION);
		//ConnManagerParams.setMaxConnectionsPerRoute(params, defaultConnPerRoute);
		//ConnManagerParams.setTimeout(params, 1 * 60 * 1000);
		httpclient.getParams().setParameter("http.conn-manager.max-total", MAX_HTTP_CONNECTION);
		ConnPerRoute defaultConnPerRoute = new ConnPerRoute()
		{
			public int getMaxForRoute(HttpRoute route)
			{
				return 4;
			}
		};
		httpclient.getParams().setParameter("http.conn-manager.max-per-route", defaultConnPerRoute);
		httpclient.getParams().setParameter("http.conn-manager.timeout", 1 * 60 * 1000L);
		httpclient.getParams().setParameter("http.protocol.allow-circular-redirects", true);
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
		
		//
		HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() 
		{
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context)
			{
				if(executionCount > 2) 
				{
					return false;
				}
				if (exception instanceof NoHttpResponseException)
				{
					return true;
				}
				if(exception instanceof SSLHandshakeException)
				{
					return false;
				}
				HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
				if (!(request instanceof HttpEntityEnclosingRequest)) 
				{
					return true;
				}
				return false;
			}
		};
		httpclient.setHttpRequestRetryHandler(retryHandler);
		
		HttpRequestInterceptor requestInterceptor = new HttpRequestInterceptor()
		{
			public void process(HttpRequest request, HttpContext context) throws HttpException, IOException
			{
				if(!request.containsHeader("Accept-Encoding")) 
				{
					request.addHeader("Accept-Encoding", "gzip, deflate");
				}
			}
		};
		
		HttpResponseInterceptor responseInterceptor = new HttpResponseInterceptor()
		{
			public void process(HttpResponse response, HttpContext context) throws HttpException, IOException
			{
				HttpEntity entity = response.getEntity();
				Header header = entity.getContentEncoding();
				if(header != null)
				{
					HeaderElement[] codecs = header.getElements();
					for(int i = 0; i < codecs.length; i++) 
					{
						String codecName = codecs[i].getName();
						if("gzip".equalsIgnoreCase(codecName))
						{
							response.setEntity(new GzipDecompressingEntity(entity));
							return;
						}
						else if("deflate".equalsIgnoreCase(codecName))
						{
							response.setEntity(new DeflateDecompressingEntity(entity));
							return;
						}
					}
				}
			}
		};
		
		httpclient.addRequestInterceptor(requestInterceptor);
		httpclient.addResponseInterceptor(responseInterceptor);
		httpclient.setRedirectStrategy(new DefaultRedirectStrategy());
		
		return httpclient;
	}
}
