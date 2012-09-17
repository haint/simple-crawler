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

import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class XPathUtil
{
	public Object read(Node node, String expression, QName returnType) throws XPathExpressionException
	{
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExp = xpath.compile(expression);
		return xPathExp.evaluate(node, returnType);
	}
	
	public static final String getXPath(Node node) 
	{
		if(node == null) 
		{
			return null;
		}
		
		//
		Node parent = null;
		Stack<Node> hierarchy = new Stack<Node>();
		StringBuilder sb = new StringBuilder();
		
		//
		hierarchy.push(node);
		parent = node.getParentNode();
		while(parent != null && parent.getNodeType() != Node.DOCUMENT_NODE)
		{
			hierarchy.push(parent);
			parent = parent.getParentNode();
		}
		
		if(hierarchy.isEmpty()) 
		{
			return null;
		}
		
		Object obj = null;
		while((obj = hierarchy.pop()) != null)
		{
			Node n = (Node) obj;
			boolean handled = false;
			
			//Only handle elements
			if(n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element e = (Element) n;
				if(sb.length() == 0)
				{
					sb.append(e.getNodeName());
				}
				else
				{
					sb.append("/");
					sb.append(e.getNodeName());
					if(e.hasAttributes())
					{
						if(e.hasAttribute("id"))
						{
							sb.append("[@id='").append(e.getAttribute("id")).append("']");
							handled = true;
						}
						else if(e.hasAttribute("name"))
						{
							sb.append("[@name='").append(e.getAttribute("name")).append("']");
							handled = true;
						}
					}
					
					//
					if(!handled)
					{
						int pre_siblings = 1;
						Node preSiblingNode = n.getPreviousSibling();
						while(preSiblingNode != null)
						{
							if(preSiblingNode.getNodeType() == n.getNodeType() 
										&& preSiblingNode.getNodeName().equalsIgnoreCase(n.getNodeName()))
							{
								pre_siblings++;
							}
							preSiblingNode = preSiblingNode.getPreviousSibling();
						}
						sb.append("[").append(pre_siblings).append("]");
					}
				}
			}
		}
		
		return sb.toString();
	}
}
