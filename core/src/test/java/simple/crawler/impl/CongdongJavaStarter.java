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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;

import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 */
public class CongdongJavaStarter extends ForumCrawler {

   private final Logger LOG = LoggerFactory.getLogger(CongdongJavaStarter.class);

   private final String baseUrl  = "http://congdongjava.com/forum/";

   private final PausableThreadPoolExecutor executor;

   private final LinkedBlockingQueue<CategoryDBObject> catQueue = new LinkedBlockingQueue<CategoryDBObject>();

   private final LinkedBlockingQueue<RawDBObject> rawQueue = new LinkedBlockingQueue<RawDBObject>();

   private final LinkedBlockingQueue<PageDBObject> pageQueue = new LinkedBlockingQueue<PageDBObject>();

   private final CrawlingDB db;

   CongdongJavaStarter(String dbName, int numOfThreads) throws Exception {
      this.db = new CrawlingDB(dbName);
      this.executor = new PausableThreadPoolExecutor(numOfThreads);
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
            RawDBObject raw = new RawDBObject(new BasicDBObject("_id", obj.get("_id")).append("url", obj.get("url")));
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
   
   public void pause() {
      executor.pause();
   }
   
   public void resume() {
      executor.resume();
   }

   public void terminate() throws InterruptedException {
      executor.shutdownNow();
   }

   public void run() {
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
      BasicDBObject query = new BasicDBObject("_id", obj.getID());
      if (db.find(Collection.RAW, query).hasNext()) {
         return null;
      }

      String html = HttpClientUtil.fetch(HttpClientFactory.createNewDefaultHttpClient(), obj.getURL());
      LOG.info("fetch  "  + obj.getURL());
      RawDBObject raw = new RawDBObject(obj.getURL(), html);
      if (db.save(raw, Collection.RAW)) {
         rawQueue.add(raw);
      }
      return html;
   }

   void update(CategoryDBObject cat) throws Exception {
      String html = fetch(cat);
      if (cat.getTitle() == null && html != null) {
         HtmlParser parser = new HtmlParser();
         Document doc = parser.parseNonWellForm(html);
         Node node = (Node) XPathUtil.read(doc,
            "HTML/BODY[1]/DIV[@id='headerMover']/DIV[@id='content']/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/H1[1]",
            XPathConstants.NODE);
         if (node.getTextContent() != null) {
            cat.put("title", node.getTextContent());
            db.update(cat, Collection.CATEGORY);
         }
      }
   }

   void update(PageDBObject page) throws Exception {
      String html = fetch(page);
      if(page.getTitle() == null && html != null) {
         HtmlParser parser = new HtmlParser();
         Document doc = parser.parseNonWellForm(html);
         Node node = (Node) XPathUtil.read(doc,
            "HTML/BODY[1]/DIV[@id='headerMover']/DIV[@id='content']/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/H1[1]",
            XPathConstants.NODE);
         if (node.getTextContent() != null) {
            page.put("title", node.getTextContent());
            db.update(page, Collection.PAGE);
         }
      }
   }

   void extract(RawDBObject raw) throws Exception {
      HtmlParser parser = new HtmlParser();
      if(raw.getHtml() == null)
      {
         DBCursor cursor = db.find(Collection.RAW, new BasicDBObject("_id", raw.getID()));
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
            pageQueue.add(page);
         }
      }

      raw.append("analyzed", true);
      db.update(raw, Collection.RAW);
   }

   public static void main(String[] args) throws Exception {
      final CongdongJavaStarter starter = new CongdongJavaStarter("test", 2);
      Thread thread = new Thread("congdongjava") {
         public void run() {
            while(true) {
               starter.run();
               File file = new File("src/test/resources/pause");
               if(file.exists())
               {
                  starter.pause();
                  file.delete();
               }
               file = new File("src/test/resources/resume");
               if(file.exists())
               {
                  starter.resume();
                  file.delete();
               }
            }
         }
      };
      thread.start();
   }
}
