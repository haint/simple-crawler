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
package simple.crawler.parser;

import org.w3c.dom.Node;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class XPathNode
{
   private final String xpath;
   
   private final String text;
   
   private final Node node;
   
   public XPathNode(Node node)
   {
      this.node = node;
      this.xpath = XPathUtil.getXPath(node);
      this.text = normalize(node.getTextContent());
   }
   
   public String getXpath()
   {
      return xpath;
   }
   
   public String getText() 
   {
      return text;
   }
   
   public Node getNode()
   {
      return node;
   }
   
   public static String normalize(String text)
   {
      if (text == null)
         return null;
      text = text.trim();
      char[] buf = text.toCharArray();
      char preChar = 0;
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < buf.length; i++)
      {
         char c = buf[i];
         if (c == '\t')
            c = ' ';
         else if (c == '\n')
            c = ' ';
         else if (c == '\r')
            c = ' ';
         else if (c == 160)
            c = ' ';

         if (preChar == ' ' && c == ' ')
         {
         }
         else
         {
            b.append(c);
         }
         preChar = c;
      }
      return b.toString().trim();
   }
}
