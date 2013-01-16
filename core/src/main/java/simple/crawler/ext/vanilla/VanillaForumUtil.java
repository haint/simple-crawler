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
package simple.crawler.ext.vanilla;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import simple.crawler.http.HttpClientFactory;
import simple.crawler.http.HttpClientUtil;
import simple.crawler.parser.HtmlParser;
import simple.crawler.parser.XPathUtil;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class VanillaForumUtil
{
   public static HttpContext login(String loginURL, String username, String password) throws Exception {
      DefaultHttpClient httpclient = HttpClientFactory.getInstance();
      CookieStore cookieStore = new BasicCookieStore();
      HttpContext httpContext = new BasicHttpContext();
      httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
      HttpResponse res = httpclient.execute(new HttpGet(loginURL), httpContext);
      
      String html = HttpClientUtil.getContentBodyAsString(res); 
      HtmlParser parser = new HtmlParser();
      Document doc = parser.parseNonWellForm(html);
      
      //
      Node loginTransientKey = (Node)XPathUtil.read(doc, "//*[@id=\"Form_TransientKey\"]", XPathConstants.NODE);
      Node hpt = (Node)XPathUtil.read(doc, "//*[@id=\"Form_hpt\"]", XPathConstants.NODE);
      Node target = (Node)XPathUtil.read(doc, "//*[@id=\"Form_Target\"]", XPathConstants.NODE);
      Node clientHour = (Node)XPathUtil.read(doc, "//*[@id=\"Form_ClientHour\"]", XPathConstants.NODE);
      
      //
      List<NameValuePair> list = new ArrayList<NameValuePair>();
      list.add(new BasicNameValuePair("Form/TransientKey", ((Element)loginTransientKey).getAttribute("value")));
      list.add(new BasicNameValuePair("Form/hpt", ((Element)hpt).getAttribute("value")));
      list.add(new BasicNameValuePair("Form/Target", ((Element)target).getAttribute("value")));
      list.add(new BasicNameValuePair("Form/ClientHour", ((Element)clientHour).getAttribute("value")));
      list.add(new BasicNameValuePair("Form/Email", "admin"));
      list.add(new BasicNameValuePair("Form/Password", "admin"));
      list.add(new BasicNameValuePair("Form/Sign_In", "Sign In"));
      list.add(new BasicNameValuePair("Form/RememberMe", "1"));
      list.add(new BasicNameValuePair("Checkboxes[]", "RememberMe"));
      
      HttpPost post = new HttpPost(loginURL);
      post.setEntity(new UrlEncodedFormEntity(list));
      res = httpclient.execute(post, httpContext);
      return httpContext;
   }
   
   public static int postDiscussion(HttpContext httpContext, String postURL, String title, String body) throws Exception {
      DefaultHttpClient httpclient = HttpClientFactory.getInstance();
      HttpResponse res = httpclient.execute(new HttpGet(postURL), httpContext);
      String html = HttpClientUtil.getContentBodyAsString(res);
      HtmlParser parser = new HtmlParser();
      Document doc = parser.parseNonWellForm(html);
      Node node = (Node)XPathUtil.read(doc, "//*[@id=\"Form_TransientKey\"]", XPathConstants.NODE);
      String postTransientKey = ((Element)node).getAttribute("value");
      
      HttpPost post = new HttpPost(postURL);
      List<NameValuePair> list = new ArrayList<NameValuePair>();
      list.add(new BasicNameValuePair("Discussion/TransientKey", postTransientKey));
      list.add(new BasicNameValuePair("Discussion/Name", title));
      list.add(new BasicNameValuePair("Discussion/Body", body));
      list.add(new BasicNameValuePair("Discussion/Post_Discussion", "Post Discussion"));
      post.setEntity(new UrlEncodedFormEntity(list));
      res = httpclient.execute(post, httpContext);
      return res.getStatusLine().getStatusCode();
   }
}
