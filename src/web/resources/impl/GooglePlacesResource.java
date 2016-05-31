package web.resources.impl;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import data.ResultMap;
import web.parsing.ResourceParser;
import web.resources.SingleWebResource;
import web.workflow.WorkflowInterruptedException;


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

	// private JsonObject _gPlacesObj;

	@Override
	public void startWorkflow() throws WorkflowInterruptedException
	{
		_data = new ResultMap();

		for ( String keyword : KEYWORDS )
		{
			ResultMap tmpData;
			ByteArrayOutputStream out = null;
			try
			{
				tmpData = new ResultMap();
				out = new ByteArrayOutputStream();
				executeRequest(String.format(URL, keyword), out);

				String response = new String(out.toByteArray(), "UTF-8");

				JsonObject gPlacesObj = (JsonObject) ResourceParser.parseResource(response, WebResourceType.JSON_OBJ);

				JsonArray array = gPlacesObj.getAsJsonArray("results");

				int i = 0;

				for ( Object placeEntry : array )
				{
					JsonObject placeObject = (JsonObject) placeEntry;
					String name = placeObject.get("name").getAsString();
					String adress = placeObject.get("formatted_address").getAsString();

					String lat = placeObject.getAsJsonObject("geometry").getAsJsonObject("location").get("lat")
					        .getAsString();
					String lng = placeObject.getAsJsonObject("geometry").getAsJsonObject("location").get("lng")
					        .getAsString();

					List<Object> valueList = new ArrayList<Object>();
					valueList.add(name);
					valueList.add(adress);
					valueList.add(lat);
					valueList.add(lng);

					String district = searchForDistrict(Double.valueOf(lat), Double.valueOf(lng));

					if ( district != null )
					{
						valueList.add(district);
					}

					tmpData.put(Integer.toString(i), valueList);
					i++;

					System.out.println(valueList);
				}
			}
			catch (Exception e)
			{
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

			if ( tmpData != null )
			{
				_data.addAll(tmpData);
			}

		}
	}

	private String searchForDistrict(double lat, double lng)
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

		return districtSearchResource.getData().get("district").get(0).toString();
	}

}
