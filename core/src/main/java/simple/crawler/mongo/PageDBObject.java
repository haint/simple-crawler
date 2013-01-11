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
import com.mongodb.DBObject;

import simple.crawler.util.HttpURL;
import simple.crawler.util.UriID;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class PageDBObject extends CrawlingDBObject
{
   public PageDBObject(String url, String title, CategoryDBObject category)
   {
      super(null);
      UriID uri = new UriID(new HttpURL(url));
      put("_id", uri.toString());
      put("url", url);
      put("title", title);
      put("category", category);
   }
   
   public PageDBObject(DBObject obj)
   {
      super(obj);
      put("_id", obj.get("_id"));
      put("url", obj.get("url"));
      put("title", obj.get("tite"));
      put("category", obj.get("category"));
   }
   
   public CategoryDBObject getCategory()
   {
      return new CategoryDBObject((DBObject)get("category"));
   }
   
   public String getTitle()
   {
      return getString("title");
   }
   
   @Override
   public String getID()
   {
      return getString("_id");
   }

   @Override
   public String getURL()
   {
      return getString("url");
   }
}
