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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import simple.crawler.HttpClientFactory;
import simple.crawler.HttpClientUtil;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ParsingTestCase extends Assert
{
	private static String html;
	
	@BeforeClass
	public static void init() throws Exception
	{
		FileInputStream fis = new FileInputStream(/*System.getProperty("test.resources")*/ "src/test/resources" + "/datum/index.html");
		BufferedInputStream bis = new BufferedInputStream(fis);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		for(int l = bis.read(buff); l != -1; l = bis.read(buff))
		{
			baos.write(buff, 0, buff.length);
		}
		html = new String(baos.toByteArray(), "UTF-8");
	}
	
	@Test
	public void testParsing() throws Exception
	{
		String html = 
					"<html>" +
					"<head>" +
					"<title>The Title</title>" +
					"</head>" +
					"<body>The Body</body>" +
					"</html>";
		
		HtmlParser parser = new HtmlParser();
		assertNotNull(parser.parseWellForm(html));
		
		html = HttpClientUtil.fetch(HttpClientFactory.getInstance(), "http://www.vn-zoom.com/f171/");
		try {
			parser.parseWellForm(html);
			fail();
		} catch(SAXParseException e) {
			
		}
	}
	
	@Test
	public void testGetTitle() throws Exception
	{
		HtmlParser parser = new HtmlParser();
		Document doc = parser.parseNonWellForm(html);
		assertEquals("Tin tức CNTT", HtmlDOMUtil.getTitle(doc));
	}
	
	@Test
	public void testGetBaseURL() throws Exception
	{
		HtmlParser parser = new HtmlParser();
		Document doc = parser.parseNonWellForm(html);
		assertEquals("http://www.vn-zoom.com/", HtmlDOMUtil.getBaseURL(doc));
	}
	
	@Test
	public void testCollectLink() throws Exception
	{
		HtmlParser parser = new HtmlParser();
		Document doc = parser.parseNonWellForm(html);
		final LinkedList<String> list = new LinkedList<String>();
		NodeCollectedVisitor visitor = new NodeCollectedVisitor("a", "href")
		{
			@Override
			public void collect(Node node)
			{
				String href = ((Element) node).getAttribute("href");
				if(href.startsWith("http://www.vn-zoom.com/f171/") && href.endsWith(".html"))
				{
					list.add(href);
				}
			}
		};
		visitor.traverse(doc);
		assertTrue(list.size() > 0);
		assertEquals("http://www.vn-zoom.com/f171/i2.html", list.get(0));
	}
}
