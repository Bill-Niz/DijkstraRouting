package routing.dijkstra;

import reso.ip.IPAddress;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPRouteEntry;

public class DijkstraRouteEntry extends IPRouteEntry {

	private int metric;
	
	public DijkstraRouteEntry(IPAddress dst, IPInterfaceAdapter oif, String type , int metric) {
		super(dst, oif, type);
		this.metric = metric;
	}
	
	public String toString() {
		String s= super.toString();
		s+= ", metric=" + metric;
		return s; 
	}

}
