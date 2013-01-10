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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
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
public class PostToVanillaForum
{

   public static void main(String[] args) throws Exception
   {
      for(int i = 0; i < 100; i++) {
         DefaultHttpClient httpclient = HttpClientFactory.createNewDefaultHttpClient();
         BasicClientCookie cookie1 = (BasicClientCookie)HttpClientUtil.addCookie(httpclient, "Vanilla", "1-1360399843%7C01023f917d56142964f2652ed3533a0c%7C1357807843%7C1%7C1360399843");
         cookie1.setDomain("localhost");
         cookie1.setPath("/");
         cookie1.setSecure(false);

         BasicClientCookie cookie2 = (BasicClientCookie)HttpClientUtil.addCookie(httpclient, "Vanilla-Volatile", "1-1357983322%7Cadbae68eabae41cbf9c75d0de4cb6b7f%7C1357810522%7C1%7C1357983322");
         cookie2.setDomain("localhost");
         cookie2.setPath("/");
         cookie2.setSecure(false);

         HttpPost post = new HttpPost("http://localhost/community/index.php?p=/post/discussion");
         HttpResponse response = httpclient.execute(post);
         String html = HttpClientUtil.getContentBodyAsString(response);
         HtmlParser parser = new HtmlParser();
         Document doc = parser.parseNonWellForm(html);
         Node node = (Node)XPathUtil.read(doc, "//*[@id=\"Form_TransientKey\"]", XPathConstants.NODE);
         String transientKey = ((Element)node).getAttribute("value");
         System.out.println(transientKey);
         
         List<NameValuePair> list = new ArrayList<NameValuePair>();
         list.add(new BasicNameValuePair("Discussion/TransientKey", transientKey));
         list.add(new BasicNameValuePair("Discussion/Name", "test" + i));
         list.add(new BasicNameValuePair("Discussion/Body", "test" + i));
         list.add(new BasicNameValuePair("Discussion/Post_Discussion", "Post Discussion"));
         post.setEntity(new UrlEncodedFormEntity(list));
         response = httpclient.execute(post);
         System.out.println(response);
      }
   }
}
