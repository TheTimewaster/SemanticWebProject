package proj.data.location;


import java.util.ArrayList;
import java.util.List;


public class StoreLocation
{
	private Coordinates			_mainCoord;

	private List<Coordinates>	_altCoords;

	public StoreLocation(Double lat, Double lng)
	{
		_mainCoord = new Coordinates(lat, lng);

		_altCoords = new ArrayList<>();
	}

	public void addAltCoord(Double lat, Double lng)
	{
		addAltCoord(new Coordinates(lat, lng));
	}

	public void addAltCoord(Coordinates coord)
	{
		_altCoords.add(coord);
	}

	public List<Coordinates> getAltCoords()
	{
		return _altCoords;
	}

	public Coordinates getMainCoords()
	{
		return _mainCoord;
	}
}
