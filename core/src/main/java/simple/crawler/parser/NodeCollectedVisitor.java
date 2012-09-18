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

import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public abstract class NodeCollectedVisitor extends NodeVisitor
{
	/** The tag name wish to collect */
	protected final String tagName;
	
	/** The attribute is used to filter */
	protected final String attribute;
	
	public NodeCollectedVisitor(String tagName, String attribute)
	{
		if(tagName == null) 
		{
			throw new NullPointerException();
		}
		this.tagName = tagName;
		this.attribute = attribute;
	}
	
   @Override
   public void preTraverse(Node node)
   {
   	if(tagName.equalsIgnoreCase(node.getNodeName()) && node instanceof Element) 
   	{
   		Element element = (Element) node;
   		String value = element.getAttribute(attribute);
   		if(value == null || value.isEmpty())
   		{
   			return;
   		}
   		
   		//
   		collect(node);
   	}
   }

   @Override
   public void postTraverse(Node node)
   {
   }
   
   public abstract void collect(Node node);
   
}
