package proj.data;


import java.util.ArrayList;
import java.util.List;


public class CoordList
{
	private static CoordList	  _instance;

	private static List<Double[]>	_coordList;

	private static final double	  _distanceDiff	= 0.0005;

	private CoordList()
	{
		_coordList = new ArrayList<Double[]>();
	}

	public static CoordList getInstance()
	{
		if ( _instance == null )
		{
			_instance = new CoordList();
		}

		return _instance;
	}

	public Double[] calculateDistance(double lat, double lon)
	{
		for ( Double[] coordPair : _coordList )
		{
			double distance = Math.sqrt(Math.pow(lat - coordPair[0], 2.0) + Math.pow(lon - coordPair[1], 2.0));

			if ( distance < _distanceDiff )
			{
				return coordPair;
			}
		}

		return null;
	}

	public void addCoord(double lat, double lon)
	{
		_coordList.add(new Double[]
		{ lat, lon });
	}
}
