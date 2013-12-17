/**
 The MIT License (MIT)

Copyright (c) 2013 Bill Nizeyimana

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package routing.dijkstra;

import java.util.ArrayList;

import reso.common.Message;
import reso.ip.IPAddress;

/**
 *
 * @author Bill Nizeyimana
 */
public class HelloMessage implements Message {
	
	private IPAddress routerID;
	private byte numNeighbor = 0;
	private ArrayList<HelloData> neighborsList = new ArrayList<>();
	
	
	public static class HelloData
	{
		private IPAddress routerID;
		private int cost;
		/**
		 * @param routerID
		 * @param cost
		 */
		public HelloData(IPAddress routerID, int cost) {
			super();
			this.routerID = routerID;
			this.cost = cost;
		}
		/**
		 * @return the routerID
		 */
		public IPAddress getRouterID() {
			return routerID;
		}
		/**
		 * @return the cost
		 */
		public int getCost() {
			return cost;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[routerID=" + routerID + ", cost=" + cost + "]";
		}
		
	}
	
	public IPAddress getRouterID() {
		return routerID;
	}
	/**
	 * 
	 * @param routerID
	 */
	public void setrouterID(IPAddress routerID) {
		this.routerID = routerID;
	}
	/**
	 * @return the numNeighbor
	 */
	public byte getNumNeighbor() {
		return (byte)this.neighborsList.size();
	}
	/**
	 * @return the neighborsList
	 */
	public ArrayList<HelloData> getNeighborsList() {
		return neighborsList;
	}
	/**
	 * @param neighborsList the neighborsList to set
	 */
	public void setNeighborsList(ArrayList<HelloData> neighborsList) {
		this.neighborsList = neighborsList;
	}
	/**
	 * 
	 * @param routerID
	 * @param neighborsList
	 */
	public HelloMessage(IPAddress routerID, ArrayList<HelloData> neighborsList) {
		super();
		this.routerID = routerID;
		this.neighborsList = neighborsList;
		this.numNeighbor = (byte)this.neighborsList.size();
	
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HelloMessage [routerID=" + routerID + ", numNeighbor="
				+ numNeighbor + ", neighborsList=" + neighborsList + "]";
	}
	
	
	
}
