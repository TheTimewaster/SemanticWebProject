package proj.web.resources.imp;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import proj.data.ResultMap;
import proj.data.StaticProperties;
import proj.web.parsing.ResourceParser;
import proj.web.resources.SingleWebResource;
import proj.web.resources.SingleWebResource.WebResourceType;
import proj.web.resources.imp.GoogleGeocodingResource;
import proj.web.workflow.WorkflowInterruptedException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * This parser parses the JSON from the Google Places API. Here we get
 * information about places. Then we try to map the address.
 * 
 * @author Tu
 *
 */
public class GooglePlacesResource extends SingleWebResource
{
	private static final String	  URL	     = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s+leipzig&key=AIzaSyAV201q9SNmr2WXEzT9HrSVG_YdMEjQn-M";

	private static final String[]	KEYWORDS	=
	                                         { "supermarkt", "bioladen", "discounter" };

	private Model	              _model;

	public GooglePlacesResource(Model model)
	{
		super();
		_model = model;
	}

	@Override
	public void startWorkflow() throws WorkflowInterruptedException
	{
		_data = new ResultMap();
		
		int i = 0;

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

					Resource storeModel = _model.createResource(StaticProperties.NAMESPACE_STORE + "=" + i++);
					storeModel.addProperty(_model.createProperty(StaticProperties.NAMESPACE_NAME), name);
					storeModel.addProperty(_model.createProperty(StaticProperties.NAMESPACE_ADRESS), adress);

					_model.getResource(StaticProperties.NAMESPACE_DISTRICT + "=" + district).addProperty(
					        _model.createProperty(StaticProperties.NAMESPACE_STORE), storeModel);

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
