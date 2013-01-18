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

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public final class CrawlerConfiguration implements Serializable
{
   /** . */
   private final String baseURL;

   /** . */
   private final String xpathCatTitle;

   /** . */
   private final String xpathPageTitle;

   /** . */
   private final Pattern categoryPattern;

   /** . */
   private final Pattern pagePattern;

   /** . */
   private final boolean appendBaseURL;

   public CrawlerConfiguration(String baseURL, String xpathCatTitle, String xpathPageTitle, String catPattern,
      String pagePattern, boolean appendBaseURL)
   {

      this.baseURL = baseURL;
      this.xpathCatTitle = xpathCatTitle;
      this.xpathPageTitle = xpathPageTitle;
      this.categoryPattern = Pattern.compile(catPattern);
      this.pagePattern = Pattern.compile(pagePattern);
      this.appendBaseURL = appendBaseURL;
   }

   public boolean isAppendBaseURL()
   {
      return appendBaseURL;
   }

   public Pattern getCategoryLinkPattern()
   {
      return categoryPattern;
   }

   public Pattern getPageLinkPattern()
   {
      return pagePattern;
   }

   public String getDbName()
   {
      if (baseURL.indexOf("www.") != -1)
      {
         return baseURL.substring(4, baseURL.lastIndexOf('.'));
      }

      return baseURL.substring(baseURL.indexOf("://") + 3, baseURL.lastIndexOf('.'));
   }

   public String getBaseURL()
   {
      return baseURL;
   }

   public String getXpathCatTitle()
   {
      return xpathCatTitle;
   }

   public String getXpathPageTitle()
   {
      return xpathPageTitle;
   }
}
