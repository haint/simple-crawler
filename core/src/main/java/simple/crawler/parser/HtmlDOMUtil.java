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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class HtmlDOMUtil
{

	public static String getTitle(Document doc) 
	{
		Node titleNode = findFirstNodeByTagName(doc, "title");
		if(titleNode != null)
		{
			return titleNode.getTextContent();
		}
		return null;
	}
	
	public static String getBaseURL(Document doc) 
	{
		Node baseNode = findFirstNodeByTagName(doc, "base");
		if(baseNode != null)
		{
			Element element = (Element) baseNode;
			return element.getAttribute("href");
		}
		return null;
	}
	
	public static Node findFirstNodeByTagName(Node node, String tagName)
	{
		if(node == null)
		{
			return null;
		}
		if(tagName.equalsIgnoreCase(node.getNodeName()))
		{
			return node;
		}
		NodeList children = node.getChildNodes();
		for(int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			Node result = findFirstNodeByTagName(child, tagName); 
			if(result != null)
			{
				return result;
			}
		}
		return null;
	}
	
	public static void writeToStream(OutputStream os, Node node) throws Exception 
	{
		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
		DOMImplementationLS domImpl = (DOMImplementationLS)registry.getDOMImplementation("LS");
		LSSerializer serializer = domImpl.createLSSerializer();

		//
		LSOutput lso = domImpl.createLSOutput();
		lso.setByteStream(os);
		lso.setEncoding("UTF-8");
		serializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		serializer.write(node, lso);
	}
	
	public static String toXMLString(Node node) throws Exception 
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeToStream(baos, node);
		return new String(baos.toByteArray(), "UTF-8");
	}
}
