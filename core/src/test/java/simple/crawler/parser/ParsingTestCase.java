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

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import simple.crawler.HttpClientFactory;
import simple.crawler.HttpClientUtil;

import junit.framework.Assert;

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
		html = HttpClientUtil.fetch(HttpClientFactory.getInstance(), "http://www.vn-zoom.com/f171/");
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
}
