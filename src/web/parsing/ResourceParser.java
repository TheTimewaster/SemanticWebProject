package web.parsing;


import org.jsoup.nodes.Element;

import web.resources.SingleWebResource.WebResourceType;


public class ResourceParser
{
	public static Object parseResource(String rawResource, WebResourceType type)
	{
		switch (type)
		{
		case HTML_DOC:
			HtmlParser htmlParser = new HtmlParser();
			Element _document = htmlParser.parseHtmlString(rawResource);
			return _document;
		case JSON_OBJ:
			JSONParser jsonParser = new JSONParser();
			Object _object = jsonParser.parseRawJson(rawResource);
			return _object;

		default:
			return null;

		}
	}
}
