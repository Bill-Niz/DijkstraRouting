/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package routing.dijkstra;

import java.util.ArrayList;

import reso.common.Message;
import reso.ip.IPAddress;

/**
 *
 * @author billniz
 */
public class HelloMessage implements Message {
	
	private IPAddress routerID;
	private ArrayList<IPAddress> neighborsList = new ArrayList<>();
	
	
	
	public IPAddress getRouterID() {
		return routerID;
	}



	public void setrouterID(IPAddress routerID) {
		this.routerID = routerID;
	}



	/**
	 * @return the neighborsList
	 */
	public ArrayList<IPAddress> getNeighborsList() {
		return neighborsList;
	}



	/**
	 * @param neighborsList the neighborsList to set
	 */
	public void setNeighborsList(ArrayList<IPAddress> neighborsList) {
		this.neighborsList = neighborsList;
	}



	public HelloMessage(IPAddress routerID, ArrayList<IPAddress> neighborsList) {
		super();
		this.routerID = routerID;
		this.neighborsList = neighborsList;
	}



	@Override
	public String toString() {
		return "Hello [routerID=" + routerID + ", neighbors="
				+ neighborsList + "]";
	}
	
	
}
