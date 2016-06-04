package proj.data.location;

public class Coordinates
{
	private double _lat, _lng;

	public Coordinates(Double lat, Double lng)
	{
		_lat = lat;
		_lng = lng;
	}

	public Double getLat()
	{
		return _lat;
	}

	public Double getLng()
	{
		return _lng;
	}
}
