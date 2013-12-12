package routing.dijkstra;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Random;

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
	public static final String TOPO_FILE = "reso/data/topology.txt";
	public static int HELLOIntervalTime = 1;
	public static int LSPIntervalTime = 20;
	public static int LSPAGEIntervalTime = 40;

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
						HELLOIntervalTime, LSPIntervalTime));
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
}
