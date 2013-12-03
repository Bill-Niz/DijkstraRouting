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
	public  IPInterfaceAdapter oif;
	private int numSequence;
    private final Map<IPAddress,Integer> lsp;
    
	

	/**
	 * @return the oif
	 */
	public IPInterfaceAdapter getOif() {
		return oif;
	}

	/**
	 * @param routerID
	 * @param oif
	 * @param numSequence
	 * @param lsp
	 */
	public LSPMessage(IPAddress routerID, IPInterfaceAdapter oif, int numSequence,
			Map<IPAddress, Integer> lsp) {
		super();
		this.routerID = routerID;
		this.oif = oif;
		this.numSequence = numSequence;
		this.lsp = lsp;
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
	public Map<IPAddress, Integer> getLsp() {
		return lsp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LSPMessage [routerID=" + routerID + ", oif=" + oif + ", numSequence="
				+ numSequence + ", lsp=" + lsp + "]";
	}
    
    
}
