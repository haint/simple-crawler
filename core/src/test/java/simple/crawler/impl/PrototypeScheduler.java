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

import java.util.concurrent.TimeUnit;

import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;



/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class PrototypeScheduler
{
   public static void main(String[] args) throws Exception
   {
      CrawlerConfiguration config = new CrawlerConfiguration(
         "http://congdongjava.com/forum/", 
         "HTML/BODY[1]/DIV[@id='headerMover']/DIV[@id='content']/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/H1[1]", 
         "HTML/BODY[1]/DIV[@id='headerMover']/DIV[@id='content']/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/H1[1]", 
         "^forums/[a-zA-Z0-9-%]+\\.[0-9]+/(page-[0-9]+)?", 
         "^threads/[a-zA-Z0-9-%]+\\.[0-9]+/(page-[0-9]+)?", 
         true);
      CrawlerImpl crawler = new CrawlerImpl(config, 10, new ServerAddress("localhost"), new MongoClientOptions.Builder().build());
      crawler.schedule(1, 60 * 60, TimeUnit.SECONDS);
      crawler.start();
   }
}
