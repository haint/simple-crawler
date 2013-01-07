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
package simple.crawler.mongo;

import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import simple.crawler.mongo.CrawlingDB.Collection;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class MongoDBTestCase extends Assert
{
   @After
   public void tearDown() throws UnknownHostException
   {
      MongoClient client = new MongoClient("localhost");
      client.dropDatabase("test");
   }
   
   @Test
   public void testCategory() throws Exception 
   {
      CrawlingDB db = new CrawlingDB("test");
      
      CategoryDBObject c1 = new CategoryDBObject("http://localhost/cat1", "Category one");
      assertTrue(db.insert(c1, Collection.CATEGORY));
      CategoryDBObject c2 = new CategoryDBObject("http://localhost/cat1", "Category one");
      assertFalse(db.insert(c2, Collection.CATEGORY));
      
      c2 = new CategoryDBObject("http://localhost/cat1", "Category one update");
      db.update(c2, Collection.CATEGORY);
      
      DBCursor cursor = db.find(Collection.CATEGORY, new BasicDBObject("uri", c2.getUUID()));
      CategoryDBObject c3 = new CategoryDBObject(cursor.next());
      assertEquals("Category one update", c3.getTitle());
      assertFalse(cursor.hasNext());
   }
   
   @Test
   public void testPage() throws Exception
   {
      CrawlingDB db = new CrawlingDB("test");
      
      CategoryDBObject c1 = new CategoryDBObject("http://localhost/cat1", "Category one");
      assertTrue(db.insert(c1, Collection.CATEGORY));
      PageDBObject p1 = new PageDBObject("http://localhost/page1", "Page one", c1);
      assertTrue(db.insert(p1, Collection.PAGE));
      
      p1 = new PageDBObject("http://localhost/page1", "Page one", new CategoryDBObject("http://localhost/cat2", "Category Two"));
      db.update(p1, Collection.PAGE);

      DBCursor cursor = db.find(Collection.PAGE);
      PageDBObject p2 = new PageDBObject(cursor.next());
      assertEquals("Category Two", p2.getCategory().getTitle());
      assertFalse(cursor.hasNext());
   }
}
