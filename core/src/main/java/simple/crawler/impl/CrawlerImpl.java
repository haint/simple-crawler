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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import simple.crawler.ICrawler;
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
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class CrawlerImpl implements ICrawler
{
   private final Logger LOG = LoggerFactory.getLogger(this.getClass());
   
   /** . */
   private final AtomicInteger counter = new AtomicInteger();

   /** The category queue. */
   private final LinkedBlockingQueue<CategoryDBObject> catQueue = new LinkedBlockingQueue<CategoryDBObject>();

   /** The raw queue. */
   private final LinkedBlockingQueue<RawDBObject> rawQueue = new LinkedBlockingQueue<RawDBObject>();

   /** The page queue. */
   private final LinkedBlockingQueue<PageDBObject> pageQueue = new LinkedBlockingQueue<PageDBObject>();

   /** The crawler configuration of site. */
   private final CrawlerConfiguration config;

   /** The executor. */
   private final PausableThreadPoolExecutor executor;

   /** The crawling database. */
   private final CrawlingDB db;

   public CrawlerImpl(CrawlerConfiguration config, int corePoolSize, ServerAddress address, MongoClientOptions options)
   {
      this.executor = new PausableThreadPoolExecutor(corePoolSize);
      this.config = config;
      this.db = new CrawlingDB(config.getDbName(), address, options);
      init();
   }
   
   public void init()
   {
      long catSize = db.count(Collection.CATEGORY);
      if(catSize == 0)
      {
         CategoryDBObject cat = new CategoryDBObject(config.getBaseURL(), null);
         db.insert(cat, Collection.CATEGORY);
         catQueue.add(cat);
      }
      else
      {
         DBCursor cursor = db.find(Collection.RAW, new BasicDBObject("analyzed", false));
         while(cursor.hasNext())
         {
            rawQueue.add(new RawDBObject(cursor.next()));
         }
         cursor = db.find(Collection.PAGE);
         while(cursor.hasNext())
         {
            PageDBObject page = new PageDBObject(cursor.next());
            if(!db.find(Collection.RAW, new BasicDBObject("_id", page.getID())).hasNext())
            {
               pageQueue.add(page);
            }
         }
      }
   }
   
   public void schedule(long initialDelay, long delay, TimeUnit unit) throws Exception
   {
      executor.scheduleWithFixedDelay(new Runnable()
      {
         public void run()
         {
            DBCursor cursor = db.find(Collection.CATEGORY);
            while(cursor.hasNext())
            {
               CategoryDBObject cat = new CategoryDBObject(cursor.next());
               catQueue.add(cat);
            }
            
//            cursor = db.find(Collection.PAGE);
//            while(cursor.hasNext())
//            {
//               PageDBObject page = new PageDBObject(cursor.next());
//               pageQueue.add(page);
//            }
         }
      }, initialDelay, delay, unit);
   }

   public String fetch(CrawlingDBObject obj) throws Exception
   {
      BasicDBObject query = new BasicDBObject("_id", obj.getID());
      String html = HttpClientUtil.fetch(HttpClientFactory.createNewDefaultHttpClient(), obj.getURL());
      LOG.info("Fetch  " + obj.getURL());
      
      RawDBObject raw = new RawDBObject(obj.getURL(), html);
      boolean isUpdate = false;
      boolean isNew = true;
      DBCursor cursor = db.find(Collection.RAW, query);
      if (cursor.hasNext())
      {
         isNew = false;
         RawDBObject other = new RawDBObject(cursor.next());
         if(!raw.getHash().equals(other.getHash()))
         {
            isUpdate = true;
         }
      }

      
      if (isNew)
      {
         db.save(raw, Collection.RAW); 
         rawQueue.add(raw);
      }
      else if(isUpdate)
      {
         db.update(raw, Collection.RAW);
         rawQueue.add(raw);
      }
      else
      {
         LOG.info("Dont need update  " + obj.getURL());
         return null;
      }
      
      //
      return html;
   }

   public void update(CrawlingDBObject obj) throws Exception
   {
      String html = fetch(obj);
      if (obj instanceof CategoryDBObject)
      {
         CategoryDBObject cat = (CategoryDBObject)obj;
         if (cat.getTitle() == null && html != null)
         {
            HtmlParser parser = new HtmlParser();
            Document doc = parser.parseNonWellForm(html);
            Node node = (Node)XPathUtil.read(doc, config.getXpathCatTitle(), XPathConstants.NODE);
            if (node.getTextContent() != null)
            {
               cat.put("title", node.getTextContent());
               db.update(cat, Collection.CATEGORY);
            }
         }
      }
      else if (obj instanceof PageDBObject)
      {
         PageDBObject page = (PageDBObject)obj;
         if (page.getTitle() == null && html != null)
         {
            HtmlParser parser = new HtmlParser();
            Document doc = parser.parseNonWellForm(html);
            Node node = (Node)XPathUtil.read(doc, config.getXpathPageTitle(), XPathConstants.NODE);
            if (node.getTextContent() != null)
            {
               page.put("title", node.getTextContent());
               db.update(page, Collection.PAGE);
            }
         }
      }
   }

   public void analyze(RawDBObject raw) throws Exception
   {
      LOG.info("Analyze " + raw.getURL());
      HtmlParser parser = new HtmlParser();
      if (raw.getHtml() == null)
      {
         DBCursor cursor = db.find(Collection.RAW, new BasicDBObject("_id", raw.getID()));
         raw = new RawDBObject(cursor.next());
      }
      Document doc = parser.parseNonWellForm(raw.getHtml());

      final Set<String> holder = new HashSet<String>();
      NodeCollectedVisitor visitor = new NodeCollectedVisitor("a", "href")
      {
         @Override
         public void collect(Node node)
         {
            String href = ((Element)node).getAttribute("href");
            Matcher matcher = config.getCategoryLinkPattern().matcher(href);
            if (matcher.matches())
            {
               if (config.isAppendBaseURL())
               {
                  holder.add(config.getBaseURL() + href);
               }
               else
               {
                  holder.add(href);
               }
            }
         }
      };
      visitor.traverse(doc);

      for (String url : holder)
      {
         CategoryDBObject cat = new CategoryDBObject(url, null);
         if (db.insert(cat, Collection.CATEGORY))
         {
            LOG.info("Add new " + url);
            catQueue.add(cat);
         }
      }

      holder.clear();
      visitor = new NodeCollectedVisitor("a", "href")
      {
         @Override
         public void collect(Node node)
         {
            String href = ((Element)node).getAttribute("href");
            Matcher matcher = config.getPageLinkPattern().matcher(href);
            if (matcher.matches())
            {
               if (config.isAppendBaseURL())
               {
                  holder.add(config.getBaseURL() + href);
               }
               else
               {
                  holder.add(href);
               }
            }
         }
      };
      visitor.traverse(doc);

      for (String url : holder)
      {
         PageDBObject page = new PageDBObject(url, null, new CategoryDBObject(raw.getURL(), null));
         if (db.insert(page, Collection.PAGE))
         {
            LOG.info("Add new " + url);
            pageQueue.add(page);
         }
      }

      raw.append("analyzed", true);
      db.update(raw, Collection.RAW);
   }
   
   public void start() 
   {
      new Thread() {
         public void run()
         {
            while(true)
            {
               CrawlerImpl.this.run();
            }
         }
      }.start();
   }

   private void run()
   {
      if (catQueue.size() > 0)
      {
         final CategoryDBObject cat = catQueue.poll();
         executor.execute(new Runnable()
         {
            public void run()
            {
               try
               {
                  update(cat);
               }
               catch (Exception e)
               {
                  throw new RuntimeException(e);
               }
            }
         });
      }

      if (pageQueue.size() > 0)
      {
         final PageDBObject page = pageQueue.poll();
         executor.execute(new Runnable()
         {
            public void run()
            {
               try
               {
                  update(page);
               }
               catch (Exception e)
               {
                  throw new RuntimeException(e);
               }
            }
         });
      }

      if (rawQueue.size() > 0)
      {
         final RawDBObject raw = rawQueue.poll();
         executor.execute(new Runnable()
         {
            public void run()
            {
               try
               {
                  analyze(raw);
               }
               catch (Exception e)
               {
                  throw new RuntimeException(e);
               }
            }
         });
      }
   }

   public void terminate() throws InterruptedException
   {
      executor.shutdown();
      if (executor.awaitTermination(10, TimeUnit.SECONDS))
      {
         executor.shutdownNow();
      }
   }

   public void pause()
   {
      executor.pause();
   }

   public void resume()
   {
      executor.resume();
   }
}
