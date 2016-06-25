package proj.web.resources.imp;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import proj.data.IdGenerator;
import proj.data.ResultMap;
import proj.data.StaticProperties;
import proj.data.location.CoordList;
import proj.data.location.Coordinates;
import proj.web.resources.SingleWebResource;
import proj.web.workflow.WorkflowInterruptedException;


public class OsmOverpassResource extends SingleWebResource
{
	private final static String		URL			= "http://overpass-api.de/api/interpreter";

	private final static String		POST_BODY	= "data=area[name=Leipzig];node(area)%s;out;";

	private final static String[]	KEYWORDS	=
	{ "[shop=supermarket][organic=only]", "[shop=supermarket]", "[shop=supermarket][organic=yes]",
	        "[shop=convenience][organic=yes]", "[shop=convenience][organic=only]" };

	private static final Logger		LOGGER		= LoggerFactory.getLogger(OsmOverpassResource.class);

	private Set<String>				_osmOverpassIds;

	public OsmOverpassResource(Model model)
	{
		super(model);
		_osmOverpassIds = new HashSet<>();
	}

	@Override
	public void startWorkflow() throws WorkflowInterruptedException
	{
		for ( String keyword : KEYWORDS )
		{
			int counter = 0;

			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				executePostRequest(URL, out, String.format(POST_BODY, keyword));

				LOGGER.info("Request executed: " + keyword);

				List<Map<String, String>> resultList = readXmlFile(out.toByteArray());

				for ( Map<String, String> result : resultList )
				{
					String latString = result.get("lat");
					String lngString = result.get("lng");
					Coordinates coordPair = CoordList.getInstance().calculateDistance(Double.valueOf(latString),
					        Double.valueOf(lngString));
					if ( coordPair != null )
					{
						String id = IdGenerator.generateMd5Id(coordPair.getLat().toString(),
						        coordPair.getLng().toString());
						Resource storeResource = _model.createResource(StaticProperties.NAMESPACE_STORE + "-" + id);
						storeResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_STORETYPE), keyword);
					}
					else
					{
						String district = result.get("district");
						String id = IdGenerator.generateMd5Id(latString, lngString);

						Resource storeResource = _model.createResource(StaticProperties.NAMESPACE_STORE + "-" + id);

						if ( result.get("name") != null )
						{
							storeResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_NAME),
							        result.get("name"));
						}
						else if(result.get("operator") != null)
						{
							storeResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_NAME),
							        result.get("operator"));
						}
						else
						{
							continue;
						}
						storeResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_ADRESS),
						        result.get("adress"));
						storeResource.addProperty(_model.createProperty(StaticProperties.GEO_NAMESPACE_URI, "lat"),
						        result.get("lat"));
						storeResource.addProperty(_model.createProperty(StaticProperties.GEO_NAMESPACE_URI, "long"),
						        result.get("lng"));
						storeResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_STORETYPE), keyword);

						_model.getResource(StaticProperties.NAMESPACE_DISTRICT + "-" + district)
						        .addProperty(_model.createProperty(StaticProperties.NAMESPACE_STORE), storeResource);
						counter++;
					}
				}
				LOGGER.info("Processing finished: " + counter + " new results found!");
				// remove when continue development
				// break;
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
			String id = locationNode.getAttributes().getNamedItem("id").getNodeValue();
			
			if ( !_osmOverpassIds.contains(id) )
			{
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
				}

				if ( "51.2729842".equals(storeInfoMap.get("lat")) )
				{
					System.out.println("stop");
				}

				for ( int j = 0; j < locationProperties.getLength(); j++ )
				{
					Node propertyNode = locationProperties.item(j);
					if ( propertyNode.getAttributes() != null )
					{
						if ( "name".equals(propertyNode.getAttributes().getNamedItem("k").getNodeValue()) )
						{
							String storeName = propertyNode.getAttributes().getNamedItem("v").getNodeValue();
							storeInfoMap.put("name", storeName);
						}

						// fallback to operator when name is not available
						if ( "operator".equals(propertyNode.getAttributes().getNamedItem("k").getNodeType()) )
						{
							String storeName = propertyNode.getAttributes().getNamedItem("v").getNodeValue();
							storeInfoMap.put("operator", storeName);
						}
					}
				}

				resultList.add(storeInfoMap);
				_osmOverpassIds.add(id);
			}

		}

		return resultList;
	}

}
