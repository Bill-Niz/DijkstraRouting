/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package routing.dijkstra;

import java.util.HashMap;
import java.util.Map;

import reso.common.Message;
import reso.ip.IPAddress;
import reso.ip.IPInterfaceAdapter;

/**
 *
 * @author billniz
 */
public class LSPMessage implements Message{
	
	private final IPAddress routerID;
	private int numSequence;
	public int age;
	public IPInterfaceAdapter oif;
    private final Map<IPAddress,LSPData> lsp;
    
	public static class LSPData
	{
		public byte numAdj;
		public IPAddress routerID;
		public int metric;
		public  IPInterfaceAdapter oif;
		
		/**
		 * @param routerID
		 * @param metric
		 * @param oif
		 */
		public LSPData(IPAddress routerID, int metric, IPInterfaceAdapter oif) {
			super();
			
			this.routerID = routerID;
			this.metric = metric;
			this.oif = oif;
			
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[" + routerID + ":" + metric
					+ ", oif=" + oif + "]";
		}
		
		
	}
	/**
	 * @param routerID
	 * @param numSequence
	 * @param lsp
	 */
	public LSPMessage(IPAddress routerID, IPInterfaceAdapter oif, int numSequence,
			Map<IPAddress, LSPData> lsp,int age) {
		super();
		this.oif = oif;
		this.routerID = routerID;
		this.numSequence = numSequence;
		this.lsp = lsp;
		this.age = age;
	}

	/**
	 * @return the routerID
	 */
	public IPAddress getRouterID() {
		return routerID;
	}

	/**
	 * @return the numSequence
	 */
	public int getNumSequence() {
		return numSequence;
	}

	/**
	 * @return the lsp
	 */
	public Map<IPAddress, LSPData> getLsp() {
		return lsp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + routerID + " : "
				+ numSequence + ";" + oif + ", lsp=" + lsp + "]";
	}
	
}
