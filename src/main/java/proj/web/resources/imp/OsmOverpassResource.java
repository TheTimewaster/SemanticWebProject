package proj.web.resources.imp;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import proj.data.IdGenerator;
import proj.data.ResultMap;
import proj.data.StaticProperties;
import proj.web.resources.SingleWebResource;
import proj.web.workflow.WorkflowInterruptedException;


public class OsmOverpassResource extends SingleWebResource
{
	private final static String		URL			= "http://overpass-api.de/api/interpreter";

	private final static String		POST_BODY	= "data=area[name=Leipzig];node(area)%s;out;";

	private final static String[]	KEYWORDS	=
	{ "[shop=supermarket][organic=yes]", "[shop=supermarket]", "[shop=supermarket][organic=only]",
	        "[shop=convenience][organic=yes]", "[shop=convenience][organic=only]" };

	public OsmOverpassResource(Model model)
	{
		super(model);
	}

	@Override
	public void startWorkflow() throws WorkflowInterruptedException
	{
		for ( String keyword : KEYWORDS )
		{
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				executePostRequest(URL, out, String.format(POST_BODY, keyword));

				List<Map<String, String>> resultList = readXmlFile(out.toByteArray());

				String id = IdGenerator.generateMd5Id("51.3083254", "12.3714901");

				Resource expect = _model.getResource(StaticProperties.NAMESPACE_STORE + "=" + id);
				expect.hasProperty(_model.createProperty(StaticProperties.NAMESPACE_NAME));
				expect.addProperty(_model.createProperty(StaticProperties.NAMESPACE_URI + "test"), "foo");

				break;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new WorkflowInterruptedException(e);
			}

		}

	}

	private List<Map<String, String>> readXmlFile(byte[] bytesOfString)
	        throws ParserConfigurationException, SAXException, IOException, WorkflowInterruptedException
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbuilder = dbFactory.newDocumentBuilder();

		InputStream is = new ByteArrayInputStream(bytesOfString);
		Document doc = dbuilder.parse(is);

		NodeList nodes = doc.getElementsByTagName("node");

		List<Map<String, String>> resultList = new ArrayList<>();

		for ( int i = 0; i < nodes.getLength(); i++ )
		{
			Map<String, String> storeInfoMap = new HashMap<>();

			Node locationNode = nodes.item(i);

			NodeList locationProperties = locationNode.getChildNodes();

			String lat = locationNode.getAttributes().getNamedItem("lat").getNodeValue();
			String lng = locationNode.getAttributes().getNamedItem("lon").getNodeValue();

			storeInfoMap.put("lat", lat);
			storeInfoMap.put("lng", lng);

			GoogleGeocodingResource res = new GoogleGeocodingResource(Double.valueOf(lat), Double.valueOf(lng));
			res.startWorkflow();
			ResultMap resultMap = res.getData();

			if ( resultMap.get(ResultMap.FULL_ADDRESS_KEY) != null )
			{
				String fullAdress = resultMap.get(ResultMap.FULL_ADDRESS_KEY).get(0).toString();
				storeInfoMap.put("adress", fullAdress);
				String district = resultMap.get(ResultMap.FULL_ADDRESS_KEY).get(1).toString();
				storeInfoMap.put("district", district);
			}

			for ( int j = 0; j < locationProperties.getLength(); j++ )
			{
				Node propertyNode = locationProperties.item(j);
				if ( propertyNode.getAttributes() != null
				        && "name".equals(propertyNode.getAttributes().getNamedItem("k").getNodeValue()) )
				{
					String storeName = propertyNode.getAttributes().getNamedItem("v").getNodeValue();
					storeInfoMap.put("name", storeName);
				}
			}

			resultList.add(storeInfoMap);
		}

		return resultList;
	}

}
