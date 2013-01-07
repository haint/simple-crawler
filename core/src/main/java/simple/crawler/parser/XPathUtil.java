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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public final class XPathUtil
{
	public static Object read(Node node, String expression, QName returnType) throws XPathExpressionException
	{
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExp = xpath.compile(expression);
		return xPathExp.evaluate(node, returnType);
	}
	
	public static void collectXpathContainText(Node node, String textContent, List<String> holder)
	{
	   if(textContent.equals(node.getTextContent()))
	   {
	      String xpath = getXPath(node);
	      if(!holder.contains(xpath))
	      {
	         holder.add(xpath);
	      }
	   }
	   
	   if(node.hasChildNodes())
	   {
	      Node child = node.getFirstChild();
	      while(child != null)
	      {
	         collectXpathContainText(child, textContent, holder);
	         child = child.getNextSibling();
	      }
	   }
	}
	
	public static String getXPath(Node node)
   {
      if (null == node)
         return null;

      // declarations
      Node parent = null;
      Stack<Node> hierarchy = new Stack<Node>();
      StringBuffer buffer = new StringBuffer();

      // push element on stack
      hierarchy.push(node);

      parent = node.getParentNode();
      while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE)
      {
         // push on stack
         hierarchy.push(parent);

         // get parent of parent
         parent = parent.getParentNode();
      }

      // construct xpath
      Object obj = null;
      while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop()))
      {
         Node n = (Node)obj;
         boolean handled = false;

         // only consider elements
         if (n.getNodeType() == Node.ELEMENT_NODE)
         {
            Element e = (Element)n;

            // is this the root element?
            if (buffer.length() == 0)
            {
               // root element - simply append element name
               buffer.append(n.getNodeName());
            }
            else
            {
               // child element - append slash and element name
               buffer.append("/");
               buffer.append(n.getNodeName());

               if (n.hasAttributes())
               {
                  // see if the element has a name or id attribute
                  if (e.hasAttribute("id"))
                  {
                     // id attribute found - use that
                     buffer.append("[@id='" + e.getAttribute("id") + "']");
                     handled = true;
                  }
                  else if (e.hasAttribute("name"))
                  {
                     // name attribute found - use that
                     buffer.append("[@name='" + e.getAttribute("name") + "']");
                     handled = true;
                  }
               }

               if (!handled)
               {
                  // no known attribute we could use - get sibling index
                  int prev_siblings = 1;
                  Node prev_sibling = n.getPreviousSibling();
                  while (null != prev_sibling)
                  {
                     if (prev_sibling.getNodeType() == n.getNodeType())
                     {
                        if (prev_sibling.getNodeName().equalsIgnoreCase(n.getNodeName()))
                        {
                           prev_siblings++;
                        }
                     }
                     prev_sibling = prev_sibling.getPreviousSibling();
                  }
                  buffer.append("[" + prev_siblings + "]");
               }
            }
         }
      }

      // return buffer
      return buffer.toString();
   }
   
   public static List<XPathNode> getXPathContentNodes(Node root) 
   {
      List<XPathNode> holder = new ArrayList<XPathNode>();
      findCandidateXPath(holder, root);
      return holder;
   }
	
	static void dumpXpath(Node node, PrintStream printer)
	{
	   NodeList list = node.getChildNodes();
	   for(int i = 0; i < list.getLength(); i++)
	   {
	      Node item = list.item(i);
	      printer.println("Xpath: " + getXPath(item));
	      printer.println("Text Content: " + item.getTextContent());
	      if(item.hasChildNodes())
	      {
	         dumpXpath(item, printer);
	      }
	   }
	}
	
   private static String[] CONTENT_NODE =
   {"div", "p", "h", "h1", "h2", "h3", "h4", "h5", "h6", "span", "b", "strong", "em", "td", "li", "ol", "ul", "u", "i",
      "a", "font", "pre", "blockquote"};

   private static boolean isContentNode(String nodeName)
   {
      for (String sel : CONTENT_NODE)
      {
         if (nodeName.equalsIgnoreCase(sel))
            return true;
      }
      return false;
   }
   
   private static void findCandidateXPath(List<XPathNode> candidateXpath, Node node)
   {
      ArrayList<Node> stack = new ArrayList<Node>(10000);
      while (node != null)
      {
         if (node.getNodeName().equals("#text"))
         {
            if (node.getParentNode() != null)
            {
               if (isContentNode(node.getParentNode().getNodeName()))
               {
                  if (node.getTextContent().trim().length() > 0)
                  {
                     XPathNode xpathNode = new XPathNode(node);
                     candidateXpath.add(xpathNode);
                  }
               }
            }
         }
         else if (node.getNodeName().equals("img"))
         {
            candidateXpath.add(new XPathNode(node));
         }
         else
         {
            Node child = node.getLastChild();
            while (child != null)
            {
               if (child.getNodeType() == Node.ELEMENT_NODE || child.getNodeType() == Node.TEXT_NODE)
               {
                  stack.add(child);
               }
               child = child.getPreviousSibling();
            }
         }
         int size = stack.size();
         if (size == 0)
            node = null;
         else
            node = stack.remove(size - 1);
      }
   }
}
