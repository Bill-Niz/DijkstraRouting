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

import java.util.Map;

import reso.common.Message;
import reso.ip.IPAddress;
import reso.ip.IPInterfaceAdapter;

/**
*
* @author Bill Nizeyimana
*/
public class LSPMessage implements Message{
	
	private final IPAddress routerID;
	private int numSequence;
	public byte numAdj = 0;
	public int age;
    private final Map<IPAddress,LSPData> lsp;
    public IPInterfaceAdapter oif;
    
	public static class LSPData
	{
		
		public IPAddress routerID;
		public int metric;
		
		/**
		 * Create an instance of LSPData
		 * 
		 * @param routerID
		 * @param metric
		 */
		public LSPData(IPAddress routerID, int metric) {
			super();
			
			this.routerID = routerID;
			this.metric = metric;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[" + routerID + " - cost:" + metric + "]";
		}
	}
	/**
	 * Create an instance of LSPMessage
	 * 
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
				+ numSequence + " ; age: " + age + " - lsp = " + lsp + "]";
	}
	
}
