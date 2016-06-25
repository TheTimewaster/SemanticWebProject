package proj.web.resources.imp;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proj.data.IdGenerator;
import proj.data.ResultMap;
import proj.data.StaticProperties;
import proj.data.location.CoordList;
import proj.web.parsing.ResourceParser;
import proj.web.resources.SingleWebResource;
import proj.web.workflow.WorkflowInterruptedException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


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

	private Set<String>				_googleMapsIds;

	private static final String[]	KEYWORDS	=
	{ "supermarkt", "bioladen", "discounter" };

	private static final Logger		LOGGER		= LoggerFactory.getLogger(GooglePlacesResource.class);

	public GooglePlacesResource(Model model)
	{
		super(model);
		_googleMapsIds = new HashSet<>();
	}

	@Override
	public void startWorkflow() throws WorkflowInterruptedException
	{
		JsonObject gPlacesObj = null;
		String response = null;
		String requestUrl = null;

		for ( String keyword : KEYWORDS )
		{
			int counter = 0;

			String nextPageToken = "";
			ByteArrayOutputStream out = null;
			try
			{
				while (true)
				{
					out = new ByteArrayOutputStream();

					requestUrl = (nextPageToken.isEmpty()) ? String.format(URL, keyword)
					        : String.format(URL + "&pagetoken=" + nextPageToken, keyword);

					executeRequest(requestUrl, out);

					LOGGER.info("Request executed: " + keyword + "\t" + requestUrl);
					response = new String(out.toByteArray(), "UTF-8");

					gPlacesObj = (JsonObject) ResourceParser.parseResource(response, WebResourceType.JSON_OBJ);
					JsonArray resultsArray = gPlacesObj.getAsJsonArray("results");
					writeResultsToModel(resultsArray, keyword);

					if ( gPlacesObj.has("next_page_token") )
					{
						nextPageToken = gPlacesObj.getAsJsonPrimitive("next_page_token").getAsString();
						LOGGER.info("Next page found: " + keyword + "\t" + nextPageToken);
					}
					else
					{
						LOGGER.info("Last page found: " + keyword + "\t");
						break;
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.error("An error occured for: " + keyword + "\t" + requestUrl + "\t" + response, e);
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

	private void writeResultsToModel(JsonArray resultsArray, String keyword)
	{
		int counter = 0;
		for ( Object placeEntry : resultsArray )
		{

			Map<String, String> placeEntryMap = buildResult(placeEntry);

			if ( placeEntryMap != null )
			{
				String id = IdGenerator.generateMd5Id(placeEntryMap.get("lat"), placeEntryMap.get("lng"));
				Resource storeModel = _model.createResource(StaticProperties.NAMESPACE_STORE + "-" + id);

				// new entry
				if ( placeEntryMap.get("adress") != null && placeEntryMap.get("district") != null )
				{
					storeModel.addProperty(_model.createProperty(StaticProperties.NAMESPACE_NAME),
					        placeEntryMap.get("name"));
					storeModel.addProperty(_model.createProperty(StaticProperties.NAMESPACE_ADRESS),
					        placeEntryMap.get("adress"));
					storeModel.addProperty(_model.createProperty(StaticProperties.GEO_NAMESPACE_URI, "lat"),
					        placeEntryMap.get("lat"));
					storeModel.addProperty(_model.createProperty(StaticProperties.GEO_NAMESPACE_URI, "long"),
					        placeEntryMap.get("lng"));
					storeModel.addProperty(_model.createProperty(StaticProperties.NAMESPACE_STORETYPE), keyword);

					_model.getResource(StaticProperties.NAMESPACE_DISTRICT + "-" + placeEntryMap.get("district"))
					        .addProperty(_model.createProperty(StaticProperties.NAMESPACE_STORE), storeModel);

					CoordList.getInstance().addNewStoreLocation(Double.valueOf(placeEntryMap.get("lat")),
					        Double.valueOf(placeEntryMap.get("lng")));
					counter++;
				}
				else
				{
					// entry exists, just add keyword for further validation
					storeModel.addProperty(_model.createProperty(StaticProperties.NAMESPACE_STORETYPE), keyword);
				}
			}
		}

		LOGGER.info("Processing finished: " + counter + " new results found!");
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
		Map<String, String> valueList = null;

		if ( isTypeGroceriesSupermarket(placeObject.getAsJsonArray("types")) )
		{
			valueList = new HashMap<>();

			String name = placeObject.get("name").getAsString();
			String gMapsId = placeObject.get("id").getAsString();

			String lat = placeObject.getAsJsonObject("geometry").getAsJsonObject("location").get("lat").getAsString();
			String lng = placeObject.getAsJsonObject("geometry").getAsJsonObject("location").get("lng").getAsString();

			valueList.put("name", name);
			valueList.put("lat", lat);
			valueList.put("lng", lng);

			// avoid address search when was found in previous search
			if ( !_googleMapsIds.contains(gMapsId) )
			{
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

				_googleMapsIds.add(gMapsId);
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
