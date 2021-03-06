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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;

import reso.common.Network;
import reso.common.Node;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPLayer;
import reso.ip.IPRouter;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.FIBDumper;
import reso.utilities.NetworkBuilder;
import reso.utilities.NetworkGrapher;

public class Demo {
	public static final String TOPO_FILE = "routing/dijkstra/topology.txt";
	public static int HELLOIntervalTime = 1;
	public static int LSPIntervalTime = 20;
	public static int LSPAGEIntervalTime = 30;
	public static int DijkstraIntervalTime = 40;
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String filename = Demo.class.getClassLoader().getResource(TOPO_FILE)
				.getFile();
		AbstractScheduler scheduler = new Scheduler();

		try {
			Network network = NetworkBuilder.loadTopology(filename, scheduler);

			// Add routing protocol application to each router
			for (Node n : network.getNodes()) {
				if (!(n instanceof IPRouter))
					continue;
				IPRouter router = (IPRouter) n;
				router.addApplication(new DijkstraRoutingProtocol(router,
						HELLOIntervalTime, LSPIntervalTime,LSPAGEIntervalTime,DijkstraIntervalTime));
				router.start();
			}
			 
			scheduler.runUntil(100);
			
			// Display forwarding table for each node
			FIBDumper.dumpForAllRouters(network);

			for (Node n : network.getNodes()) {
				 
				IPAddress ndst = getRouterID(((IPHost) n).getIPLayer());
				File f = new File("topology-routing-" + ndst + ".dot");
				System.out.println("Writing file " + f);
				Writer w = new BufferedWriter(new FileWriter(f));
				NetworkGrapher.toGraphviz2(network, ndst, new PrintWriter(w));
				w.close();
			}
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}

	}
	/**
	 * 
	 * @param ip
	 * @return
	 */
	private static IPAddress getRouterID(IPLayer ip) {
		IPAddress routerID = null;
		for (IPInterfaceAdapter iface : ip.getInterfaces()) {
			IPAddress addr = iface.getAddress();
			if (routerID == null)
				routerID = addr;
			else if (routerID.compareTo(addr) < 0)
				routerID = addr;
		}
		return routerID;
	}
}
