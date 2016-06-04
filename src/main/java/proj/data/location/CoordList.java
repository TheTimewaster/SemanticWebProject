package proj.data.location;


import java.util.ArrayList;
import java.util.List;


public class CoordList
{
	private static CoordList			_instance;

	private static List<StoreLocation>	_coordList;

	private static final double			_distanceDiff	= 0.0005;

	private CoordList()
	{
		_coordList = new ArrayList<StoreLocation>();
	}

	public static CoordList getInstance()
	{
		if ( _instance == null )
		{
			_instance = new CoordList();
		}

		return _instance;
	}

	/**
	 * This method evaluates distance between two different locations based on
	 * coordinates
	 * 
	 * @param lat
	 *            latitude
	 * @param lng
	 *            longitude
	 * @return
	 */
	public Coordinates calculateDistance(double lat, double lng)
	{
		for ( StoreLocation storeLocation : _coordList )
		{
			double distance = Math.sqrt(Math.pow(lat - storeLocation.getMainCoords().getLat(), 2.0)
			        + Math.pow(lng - storeLocation.getMainCoords().getLng(), 2.0));

			if ( distance < _distanceDiff )
			{
				// matches
				storeLocation.addAltCoord(new Coordinates(lat, lng));
				return storeLocation.getMainCoords();
			}
		}

		return null;
	}

	public void addNewStoreLocation(double lat, double lng)
	{
		_coordList.add(new StoreLocation(lat, lng));
	}
}
