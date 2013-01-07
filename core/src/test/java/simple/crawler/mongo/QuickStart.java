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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class QuickStart
{
   public static void main(String[] args) throws Exception
   {
      MongoClient client = new MongoClient("localhost");
      client.dropDatabase("test");
      DB db = client.getDB("test");
      DBCollection coll = db.getCollection("testCollection");
      client.setWriteConcern(WriteConcern.JOURNALED);
      BasicDBObject doc = new BasicDBObject("name", "MongoDB").append("type", "database").append("count", 1)
               .append("info", new BasicDBObject("x", 100).append("y", 200));
      coll.insert(doc);
      DBObject myDoc = coll.findOne();
      System.out.println(myDoc);
      System.out.println(coll.count());
      
      DBCursor cursor = coll.find();
      try {
         while(cursor.hasNext())
         {
            System.out.println(cursor.next());
         }
      } finally {
         cursor.close();
      }
      
      for(int i = 0; i < 10; i++)
      {
         coll.insert(new BasicDBObject("i", i));
      }
      
      BasicDBObject query = new BasicDBObject("i", 9);
      cursor = coll.find(query);
      try {
         while(cursor.hasNext())
         {
            System.out.println(cursor.next());
         }
      } finally {
         cursor.close();
      }
      client.dropDatabase("test");
   }
}
