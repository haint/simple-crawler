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

import javax.xml.xpath.XPathConstants;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class DOMTestCase extends Assert
{

   @Test
   public void testModify() throws Exception {
      String html = 
               "<html>" +
               "  <head>" +
               "     <title>hello</title>" +
               "  </head>" +
               "  <body>" +
               "     <div>" +
               "        <p>Hello world</p>" +
               "     </div>" +
               "  </body>" +
               "</html>";
      HtmlParser parser = new HtmlParser();
      Document doc = parser.parseNonWellForm(html);
      Node node = (Node)XPathUtil.read(doc, "HTML/BODY/DIV[1]/P", XPathConstants.NODE);
      Node parent = node.getParentNode();
      Node newNode = doc.createElement("pre");
      newNode.setTextContent(node.getTextContent());
      parent.replaceChild(newNode, node);
      node = (Node)XPathUtil.read(doc, "HTML/BODY/DIV[1]/PRE", XPathConstants.NODE);
      assertNotNull(node);
      assertEquals("Hello world", node.getTextContent());
   }
}
