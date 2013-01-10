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

import java.awt.Cursor;
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
import simple.crawler.mongo.CrawlingDBObject;
import simple.crawler.mongo.PageDBObject;
import simple.crawler.mongo.RawDBObject;
import simple.crawler.parser.HtmlParser;
import simple.crawler.parser.NodeCollectedVisitor;
import simple.crawler.parser.XPathUtil;
import simple.crawler.util.MD5;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 */
public class CongdongJavaStarter extends ForumCrawler {
   private final String baseUrl  = "http://congdongjava.com/forum/";

   private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

   private final LinkedBlockingQueue<CategoryDBObject> catQueue = new LinkedBlockingQueue<CategoryDBObject>();

   private final LinkedBlockingQueue<RawDBObject> rawQueue = new LinkedBlockingQueue<RawDBObject>();
   
   private final LinkedBlockingQueue<PageDBObject> pageQueue = new LinkedBlockingQueue<PageDBObject>();

   private final CrawlingDB db;

   CongdongJavaStarter(String dbName) throws Exception {
      this.db = new CrawlingDB(dbName);
      if (db.count(Collection.CATEGORY) == 0) {
         initCategory();
      }
      else
      {
         DBCursor cursor = db.find(Collection.CATEGORY);
         while(cursor.hasNext()) {
            CategoryDBObject cat = new CategoryDBObject(cursor.next());
            catQueue.add(cat);
         }
         
         cursor = db.find(Collection.PAGE);
         while(cursor.hasNext()) {
            PageDBObject page = new PageDBObject(cursor.next());
            pageQueue.add(page);
         }
         
         cursor = db.find(Collection.RAW);
         while(cursor.hasNext()) {
            DBObject obj = cursor.next();
            RawDBObject raw = new RawDBObject(new BasicDBObject("url", obj.get("url")).append("uuid", obj.get("uuid")));
            rawQueue.add(raw);
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
         catQueue.add(cat);
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
                  update(cat);
               } catch (Exception e) {
                  throw new RuntimeException(e);
               }
            }
         });
      }
      
      if(pageQueue.size() > 0) {
         final PageDBObject page = pageQueue.poll();
         executor.execute(new Runnable()
         {
            public void run()
            {
               try {
                  update(page);
               } catch(Exception e) {
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
                  extract(raw);
               } catch (Exception e) {
                  throw new RuntimeException(e);
               }
            }
         });
      }
   }
   
   String fetch(CrawlingDBObject obj) throws Exception {
      BasicDBObject query = new BasicDBObject("uuid", obj.getUUID());
      if (db.find(Collection.RAW, query).hasNext()) {
         return null;
      }

      String html = HttpClientUtil.fetch(HttpClientFactory.createNewDefaultHttpClient(), obj.getURL());
      RawDBObject raw = new RawDBObject(obj.getURL(), html);
      if (db.insert(raw, Collection.RAW)) {
         System.out.println("insert RAW: " + raw.getURL());
         rawQueue.add(raw);
      }
      else
      {
         String hash = MD5.digest(html).toString();
         if(!hash.equals(raw.getHash())) {
            if(db.update(raw, Collection.RAW))
            {
               System.out.println("Update RAW: " + raw.getURL());
               rawQueue.add(raw);
            }
         }
      }
      return html;
   }
   
   void update(CategoryDBObject cat) throws Exception {
      String html = fetch(cat);
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
   
   void update(PageDBObject page) throws Exception {
      String html = fetch(page);
      if(page.getTitle() == null) {
         HtmlParser parser = new HtmlParser();
         Document doc = parser.parseNonWellForm(html);
         Node node = (Node) XPathUtil.read(doc,
                                           "HTML/BODY[1]/DIV[@id='headerMover']/DIV[@id='content']/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/H1[1]",
                                           XPathConstants.NODE);
         if (node.getTextContent() != null) {
            page.put("title", node.getTextContent());
            if (db.update(page, Collection.PAGE)) {
               System.out.println("update PAGE: " + page.getURL());
            }
         }
      }
   }

   void extract(RawDBObject raw) throws Exception {
      HtmlParser parser = new HtmlParser();
      if(raw.getHtml() == null)
      {
         DBCursor cursor = db.find(Collection.RAW, new BasicDBObject("uuid", raw.getUUID()));
         raw = new RawDBObject(cursor.next());
      }
      Document doc = parser.parseNonWellForm(raw.getHtml());
      
      final Set<String> holder = new HashSet<String>();
      NodeCollectedVisitor visitor = new NodeCollectedVisitor("a", "href") 
      {
         private Pattern pattern = Pattern.compile("^forums/[a-zA-Z0-9-%]+\\.[0-9]+/(page-[0-9]+)?");
         
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
      
      holder.clear();
      visitor = new NodeCollectedVisitor("a", "href")
      {
         private Pattern pattern = Pattern.compile("^threads/[a-zA-Z0-9-%]+\\.[0-9]+/(page-[0-9]+)?");
         
         @Override
         public void collect(Node node)
         {
            String href = ((Element) node).getAttribute("href");
            Matcher matcher = pattern.matcher(href);
            if (matcher.matches())
               holder.add(baseUrl + href);
         }
      };
      visitor.traverse(doc);
      
      for(String url : holder) {
         PageDBObject page = new PageDBObject(url, null, new CategoryDBObject(raw.getURL(), null));
         if(db.insert(page, Collection.PAGE)) {
            System.out.println("Insert PAGE: " + url);
            pageQueue.add(page);
         }
      }
   }

   public static void main(String[] args) throws Exception {
      CongdongJavaStarter starter = new CongdongJavaStarter("congdongjava");
      while(true)
      {
         starter.run();
      }
      /*CrawlingDB db = new CrawlingDB("test");
      DBCursor cursor = db.find(Collection.CATEGORY);
      while(cursor.hasNext()) {
         CategoryDBObject cat = new CategoryDBObject(cursor.next());
         if(cat.getURL().indexOf("/forum/threads/") != -1)
         {
            db.remove(cat, Collection.CATEGORY);
         }
      }*/
   }
}
