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

import simple.crawler.util.HttpURL;
import simple.crawler.util.UriID;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class CategoryDBObject extends CrawlingDBObject
{
   public CategoryDBObject(String url, String title)
   {
      super(null);
      HttpURL httpUrl = new HttpURL(url);
      UriID uri = new UriID(httpUrl);
      put("uuid", uri.toString());
      put("url", url);
      put("title", title);
      put("hostname", httpUrl.getHost());
   }
   
   public CategoryDBObject(DBObject obj)
   {
      super(obj);
      put("uuid", obj.get("uuid"));
      put("url", obj.get("url"));
      put("title", obj.get("title"));
      put("hostname", obj.get("hostname"));
   }
   
   public String getHostname()
   {
      return getString("hostname");
   }
   
   public String getTitle()
   {
      return getString("title");
   }

   @Override
   public String getUUID()
   {
      return getString("uuid");
   }

   @Override
   public String getURL()
   {
      return getString("url");
   }
}
