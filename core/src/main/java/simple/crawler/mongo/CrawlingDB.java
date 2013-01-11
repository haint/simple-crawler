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

import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 */
public class CrawlingDB {
   
   private final org.slf4j.Logger LOG = LoggerFactory.getLogger(CrawlingDB.class);
   
   private final String dbName;

   private final MongoClient client;

   public static enum Collection {
      CATEGORY,

      PAGE,

      RAW,
   }

   public CrawlingDB(String dbName) throws UnknownHostException {
      this.dbName = dbName;
      ServerAddress address = new ServerAddress("localhost");
      MongoClientOptions options = new MongoClientOptions.Builder().build();
      this.client = new MongoClient(address, options);
   }

   public CrawlingDB(String dbName, ServerAddress address) {
      this.dbName = dbName;
      MongoClientOptions options = new MongoClientOptions.Builder().build();
      this.client = new MongoClient(address, options);
   }

   public CrawlingDB(String dbName, ServerAddress address, MongoClientOptions options) {
      this.dbName = dbName;
      this.client = new MongoClient(address, options);
   }

   public String getDatabaseName() {
      return dbName;
   }

   public boolean insert(CrawlingDBObject obj, Collection collection) {
      DB db = client.getDB(dbName);
      DBCollection con = db.getCollection(collection.toString());
      try 
      {
         WriteResult result = con.insert(obj);
         if(result.getError() == null)
         {
            return true;
         }
         else
         {
            LOG.error(result.getError());
            return false;
         }
      }
      catch(MongoException.DuplicateKey e)
      {
         LOG.debug(e.getMessage());
         return false;
      }
   }
   
   public boolean save(CrawlingDBObject obj, Collection collection) {
      DB db = client.getDB(dbName);
      DBCollection con = db.getCollection(collection.toString());
      WriteResult result = con.save(obj, WriteConcern.JOURNALED);
      if(result.getError() == null)
      {
         return true;
      }
      else
      {
         LOG.error(result.getError());
         return false;
      }
   }

   public boolean update(CrawlingDBObject obj, Collection collection) {
      DB db = client.getDB(dbName);
      DBCollection con = db.getCollection(collection.toString());
      BasicDBObject query = new BasicDBObject("_id", obj.getID());
      WriteResult result = con.update(query, obj);
      if(result.getError() == null)
      {
         return true;
      }
      else
      {
         LOG.error(result.getError());
         return false;
      }
   }
   
   public void remove(CrawlingDBObject obj, Collection collection) {
      DB db = client.getDB(dbName);
      DBCollection con = db.getCollection(collection.toString());
      con.remove(obj, WriteConcern.SAFE);
   }

   public DBCursor find(Collection collection) {
      DB db = client.getDB(dbName);
      DBCollection con = db.getCollection(collection.toString());
      return con.find();
   }

   public DBCursor find(Collection collection, BasicDBObject query) {
      DB db = client.getDB(dbName);
      DBCollection con = db.getCollection(collection.toString());
      return con.find(query);
   }

   public long count(Collection collection) {
      DB db = client.getDB(dbName);
      DBCollection con = db.getCollection(collection.toString());
      return con.count();
   }
}
