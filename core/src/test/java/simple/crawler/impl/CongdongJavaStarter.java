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
package simple.crawler.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;

import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import simple.crawler.ForumCrawler;
import simple.crawler.http.HttpClientFactory;
import simple.crawler.http.HttpClientUtil;
import simple.crawler.mongo.CategoryDBObject;
import simple.crawler.mongo.CrawlingDB;
import simple.crawler.mongo.CrawlingDB.Collection;
import simple.crawler.mongo.RawDBObject;
import simple.crawler.parser.HtmlParser;
import simple.crawler.parser.NodeCollectedVisitor;
import simple.crawler.parser.XPathUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 */
public class CongdongJavaStarter extends ForumCrawler {
   private final String                                baseUrl  = "http://congdongjava.com/forum/";

   private ThreadPoolExecutor                          executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

   private final LinkedBlockingQueue<CategoryDBObject> catQueue = new LinkedBlockingQueue<CategoryDBObject>();

   private final LinkedBlockingQueue<RawDBObject>      rawQueue = new LinkedBlockingQueue<RawDBObject>();

   private final CrawlingDB                            db;

   CongdongJavaStarter() throws Exception {
      this.db = new CrawlingDB("test");
      if (db.count(Collection.CATEGORY) == 0) {
         initCategory();
      }
      if(db.count(Collection.CATEGORY) > db.count(Collection.RAW)) {
         DBCursor cursor = db.find(Collection.CATEGORY);
         while(cursor.hasNext()) {
            CategoryDBObject cat = new CategoryDBObject(cursor.next());
            if(db.find(Collection.RAW, new BasicDBObject("uuid", cat.getUUID())).hasNext())
               continue;
            catQueue.add(cat);
         }
      }
   }

   void initCategory() throws Exception {
      final DefaultHttpClient client = HttpClientFactory.getInstance();
      String html = HttpClientUtil.fetch(client, baseUrl);
      HtmlParser parser = new HtmlParser();
      Document doc = parser.parseNonWellForm(html);
      final Pattern pattern = Pattern.compile("forums/[a-zA-Z0-9-%]+\\.[0-9]+/(page-[0-9]+)?");
      final Set<String> holder = new HashSet<String>();
      NodeCollectedVisitor visitor = new NodeCollectedVisitor("a", "href") {
         @Override
         public void collect(Node node) {
            String href = ((Element) node).getAttribute("href");
            Matcher matcher = pattern.matcher(href);
            if (matcher.matches())
               holder.add(baseUrl + href);
         }
      };
      visitor.traverse(doc);
      for (final String url : holder) {
         CategoryDBObject cat = new CategoryDBObject(url, null);
         db.insert(cat, Collection.CATEGORY);
      }
   }

   void stop() throws InterruptedException {
      executor.shutdown();
      executor.awaitTermination(60, TimeUnit.SECONDS);
   }

   void run() throws Exception {
      if (catQueue.size() > 0) {
         final CategoryDBObject cat = catQueue.poll();
         executor.execute(new Runnable() {
            public void run() {
               try {
                  fetch(cat);
               } catch (Exception e) {
                  throw new RuntimeException(e);
               }
            }
         });
      }
      if (rawQueue.size() > 0) {
         final RawDBObject raw = rawQueue.poll();
         executor.execute(new Runnable() {
            public void run() {
               try {
                  fetch(raw);
               } catch (Exception e) {
                  throw new RuntimeException(e);
               }
            }
         });
      }
   }

   void fetch(CategoryDBObject cat) throws Exception {
      BasicDBObject query = new BasicDBObject("uuid", cat.getUUID());
      if (db.find(Collection.RAW, query).hasNext()) {
         return;
      }

      String html = HttpClientUtil.fetch(HttpClientFactory.createNewDefaultHttpClient(),
                                         cat.getURL());
      RawDBObject raw = new RawDBObject(cat.getURL(), html);
      if (db.insert(raw, Collection.RAW)) {
         System.out.println("insert RAW: " + raw.getURL());
         rawQueue.add(raw);
      }

      if (cat.getTitle() == null) {
         HtmlParser parser = new HtmlParser();
         Document doc = parser.parseNonWellForm(html);
         Node node = (Node) XPathUtil.read(doc,
                                           "HTML/BODY[1]/DIV[@id='headerMover']/DIV[@id='content']/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/H1[1]",
                                           XPathConstants.NODE);
         if (node.getTextContent() != null) {
            cat.put("title", node.getTextContent());
            if (db.update(cat, Collection.CATEGORY)) {
               System.out.println("update CAT: " + cat.getURL());
            }
         }
      }
   }

   void fetch(RawDBObject raw) throws Exception {
      HtmlParser parser = new HtmlParser();
      Document doc = parser.parseNonWellForm(raw.getHtml());
      final Pattern pattern = Pattern.compile("forums/[a-zA-Z0-9-%]+\\.[0-9]+/(page-[0-9]+)?");
      final Set<String> holder = new HashSet<String>();
      NodeCollectedVisitor visitor = new NodeCollectedVisitor("a", "href") {
         @Override
         public void collect(Node node) {
            String href = ((Element) node).getAttribute("href");
            Matcher matcher = pattern.matcher(href);
            if (matcher.matches())
               holder.add(baseUrl + href);
         }
      };
      visitor.traverse(doc);
      for (String url : holder) {
         CategoryDBObject cat = new CategoryDBObject(url, null);
         if (db.insert(cat, Collection.CATEGORY)) {
            System.out.println("insert CAT: " + url);
            catQueue.add(cat);
         }
      }
   }

   public static void main(String[] args) throws Exception {
      CongdongJavaStarter starter = new CongdongJavaStarter();
      while(true)
      {
         starter.run();
      }
   }
}
