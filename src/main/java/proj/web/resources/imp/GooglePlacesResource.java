package proj.web.resources.imp;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import proj.data.IdGenerator;
import proj.data.ResultMap;
import proj.data.StaticProperties;
import proj.web.parsing.ResourceParser;
import proj.web.resources.SingleWebResource;
import proj.web.workflow.WorkflowInterruptedException;


/**
 * This parser parses the JSON from the Google Places API. Here we get
 * information about places. Then we try to map the address.
 * 
 * @author Tu
 *
 */
public class GooglePlacesResource extends SingleWebResource
{
	private static final String		URL			= "https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s+leipzig&key=AIzaSyAV201q9SNmr2WXEzT9HrSVG_YdMEjQn-M";

	private static final String[]	KEYWORDS	=
	{ "supermarkt", "bioladen", "discounter" };

	public GooglePlacesResource(Model model)
	{
		super(model);
	}

	@Override
	public void startWorkflow() throws WorkflowInterruptedException
	{
		int i = 0;

		for ( String keyword : KEYWORDS )
		{
			ByteArrayOutputStream out = null;
			try
			{
				out = new ByteArrayOutputStream();
				executeRequest(String.format(URL, keyword), out);

				String response = new String(out.toByteArray(), "UTF-8");

				JsonObject gPlacesObj = (JsonObject) ResourceParser.parseResource(response, WebResourceType.JSON_OBJ);

				JsonArray array = gPlacesObj.getAsJsonArray("results");

				for ( Object placeEntry : array )
				{
					Map<String, String> placeEntryMap = buildResult(placeEntry);

					if ( placeEntryMap != null )
					{
						String id = IdGenerator.generateMd5Id(placeEntryMap.get("lat"), placeEntryMap.get("lng"));

						Resource storeModel = _model.createResource(StaticProperties.NAMESPACE_STORE + "=" + id);
						storeModel.addProperty(_model.createProperty(StaticProperties.NAMESPACE_NAME),
						        placeEntryMap.get("name"));
						storeModel.addProperty(_model.createProperty(StaticProperties.NAMESPACE_ADRESS),
						        placeEntryMap.get("adress"));
						storeModel.addProperty(_model.createProperty(StaticProperties.GEO_NAMESPACE_URI, "lat"),
						        placeEntryMap.get("lat"));
						storeModel.addProperty(_model.createProperty(StaticProperties.GEO_NAMESPACE_URI, "long"),
						        placeEntryMap.get("lng"));

						_model.getResource(StaticProperties.NAMESPACE_DISTRICT + "=" + placeEntryMap.get("district"))
						        .addProperty(_model.createProperty(StaticProperties.NAMESPACE_STORE), storeModel);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
			finally
			{
				if ( out != null )
				{
					try
					{
						out.close();
					}
					catch (IOException e)
					{
						throw new WorkflowInterruptedException(e);
					}
				}
			}
		}
	}

	private List<Object> searchFullAdress(double lat, double lng)
	{
		SingleWebResource districtSearchResource = new GoogleGeocodingResource(lat, lng);

		try
		{
			districtSearchResource.startWorkflow();
		}
		catch (WorkflowInterruptedException e)
		{
			return null;
		}

		return districtSearchResource.getData().get(ResultMap.FULL_ADDRESS_KEY);
	}

	private Map<String, String> buildResult(Object placeEntry)
	{
		JsonObject placeObject = (JsonObject) placeEntry;
		java.util.Map<String, String> valueList = null;

		if ( isTypeGroceriesSupermarket(placeObject.getAsJsonArray("types")) )
		{
			valueList = new HashMap<>();

			String name = placeObject.get("name").getAsString();

			String lat = placeObject.getAsJsonObject("geometry").getAsJsonObject("location").get("lat").getAsString();
			String lng = placeObject.getAsJsonObject("geometry").getAsJsonObject("location").get("lng").getAsString();

			valueList.put("name", name);
			valueList.put("lat", lat);
			valueList.put("lng", lng);

			List<Object> fullAdress = searchFullAdress(Double.valueOf(lat), Double.valueOf(lng));

			String district = fullAdress.get(0).toString();

			if ( fullAdress.size() > 0 )
			{
				valueList.put("adress", fullAdress.get(1).toString());
			}

			if ( district != null )
			{
				valueList.put("district", district);
			}
		}

		return valueList;
	}

	private boolean isTypeGroceriesSupermarket(JsonArray typesArray)
	{
		for ( Object typeObj : typesArray )
		{
			JsonPrimitive value = (JsonPrimitive) typeObj;

			if ( "grocery_or_supermarket".equals(value.getAsString()) )
			{
				return true;
			}
			else
			{
				continue;
			}
		}

		return false;
	}
}
