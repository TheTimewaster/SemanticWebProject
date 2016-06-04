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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import proj.data.CoordList;
import proj.data.IdGenerator;
import proj.data.ResultMap;
import proj.data.StaticProperties;
import proj.web.resources.SingleWebResource;
import proj.web.workflow.WorkflowInterruptedException;


public class OsmOverpassResource extends SingleWebResource
{
	private final static String	  URL	     = "http://overpass-api.de/api/interpreter";

	private final static String	  POST_BODY	 = "data=area[name=Leipzig];node(area)%s;out;";

	private final static String[]	KEYWORDS	=
	                                         { "[shop=supermarket][organic=only]", "[shop=supermarket]",
	        "[shop=supermarket][organic=yes]", "[shop=convenience][organic=yes]", "[shop=convenience][organic=only]" };

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

				for ( Map<String, String> result : resultList )
				{
					String latString = result.get("lat");
					String lngString = result.get("lng");
					Double[] coordPair = CoordList.getInstance().calculateDistance(Double.valueOf(latString),
					        Double.valueOf(lngString));
					if ( coordPair != null )
					{
						String id = IdGenerator.generateMd5Id(coordPair[0].toString(), coordPair[1].toString());
						Resource storeResource = _model.createResource(StaticProperties.NAMESPACE_STORE + "=" + id);
						storeResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_STORETYPE), keyword);
					}
					else
					{
						String district = result.get("district");
						String id = IdGenerator.generateMd5Id(latString, lngString);

						Resource storeResource = _model.createResource(StaticProperties.NAMESPACE_STORE + "=" + id);
						storeResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_NAME),
						        result.get("name"));
						storeResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_ADRESS),
						        result.get("adress"));
						storeResource.addProperty(_model.createProperty(StaticProperties.GEO_NAMESPACE_URI, "lat"),
						        result.get("lat"));
						storeResource.addProperty(_model.createProperty(StaticProperties.GEO_NAMESPACE_URI, "long"),
						        result.get("lng"));
						storeResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_STORETYPE), keyword);

						_model.getResource(StaticProperties.NAMESPACE_DISTRICT + "=" + district).addProperty(
						        _model.createProperty(StaticProperties.NAMESPACE_STORE), storeResource);
					}
				}

				// remove when continue development
				break;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new WorkflowInterruptedException(e);
			}

		}

	}

	private List<Map<String, String>> readXmlFile(byte[] bytesOfString) throws ParserConfigurationException,
	        SAXException, IOException, WorkflowInterruptedException
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
				String fullAdress = resultMap.get(ResultMap.FULL_ADDRESS_KEY).get(1).toString();
				storeInfoMap.put("adress", fullAdress);
				String district = resultMap.get(ResultMap.FULL_ADDRESS_KEY).get(0).toString();
				storeInfoMap.put("district", district);

				if ( district.contains(",") )
				{
					System.out.println("here");
				}
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
