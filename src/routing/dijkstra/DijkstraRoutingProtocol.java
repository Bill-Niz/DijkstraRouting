/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package routing.dijkstra;

import java.net.InterfaceAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import reso.common.AbstractApplication;
import reso.common.AbstractTimer;
import reso.common.Host;
import reso.common.Interface;
import reso.common.InterfaceAttrListener;
import reso.common.Message;
import reso.examples.dv_routing.DVMessage;
import reso.examples.dv_routing.DVRoutingEntry;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;
import reso.ip.IPLayer;
import reso.ip.IPLoopbackAdapter;
import reso.ip.IPRouteEntry;
import reso.ip.IPRouter;
import reso.scheduler.AbstractEvent;
import reso.utilities.FIBDumper;
import routing.dijkstra.LSPMessage.LSPData;
import routing.dijkstra.Node;

/**
 * 
 * @author billniz
 */
public class DijkstraRoutingProtocol extends AbstractApplication implements
		IPInterfaceListener, InterfaceAttrListener {

	public static final String PROTOCOL_NAME = "DIJKSTRA_ROUTING";
	public static final int IP_PROTO_DIJKSTRA = Datagram
			.allocateProtocolNumber(PROTOCOL_NAME);
	private int HELLOInstervalTime;
	private int LSPInstervalTime;
	private IPRouter router;
	private AbstractTimer helloTimer;
	private AbstractTimer LSPTimer;
	private int numSeq = 0;

	private ArrayList<IPAddress> neighborsList = new ArrayList<>();
	private Map<IPAddress, NeighborInfo> neighborInfoList = new HashMap<IPAddress, NeighborInfo>();
	private Map<IPAddress, LSPMessage> LSDB = new HashMap<IPAddress, LSPMessage>();
	private Map<IPAddress, NeighborInfo> table = new HashMap<IPAddress, NeighborInfo>();

	private class NeighborInfo {
		public IPAddress idRouter;
		public int metric;
		public IPInterfaceAdapter oif;

		/**
		 * @param idRouter
		 * @param metric
		 * @param oif
		 */
		public NeighborInfo(IPAddress idRouter, int metric,
				IPInterfaceAdapter oif) {
			super();
			this.idRouter = idRouter;
			this.metric = metric;
			this.oif = oif;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[" + idRouter + " : " + metric + " => " + oif + "]";
		}

	}
	/**
	 * 
	 * @param router
	 * @param hELLOInstervalTime
	 * @param lSPInstervalTime
	 */
	public DijkstraRoutingProtocol(IPRouter router, int hELLOInstervalTime,
			int lSPInstervalTime) {
		super(router, PROTOCOL_NAME);
		this.router = router;
		HELLOInstervalTime = hELLOInstervalTime;
		LSPInstervalTime = lSPInstervalTime;
	}

	/**
	 * 
	 * 
	 * @param LSDB
	 * @param source
	 */
	private   void dijkstra(Map<IPAddress, LSPMessage> LSDB,
			IPAddress source) {

		FibonacciHeap<LSPMessage> Q = new FibonacciHeap<LSPMessage>();

		Map<IPAddress, Node<LSPMessage>> D = new HashMap<IPAddress, Node<LSPMessage>>();
		
		LinkedHashMap<IPAddress, LinkedList<IPAddress>> reverseTable = new LinkedHashMap<IPAddress, LinkedList<IPAddress>>();
		
		for (Entry<IPAddress, LSPMessage> entry : LSDB.entrySet()) {

			IPAddress key = entry.getKey();
			LSPMessage value = entry.getValue();

			if (source.equals(key))
				D.put(key, Q.insert(value, 0.0));
			else
				D.put(key, Q.insert(value, Double.POSITIVE_INFINITY));
		}

		while (!Q.isEmpty()) {
			Node<LSPMessage> u = Q.extractMin();
			// Update FIB
			if(!this.router.getIPLayer().hasAddress(u.object.getRouterID()))
			{
				NeighborInfo tData = this.table.get(u.object.getRouterID());
				if( tData == null){
				this.table.put(u.object.getRouterID(),
						new NeighborInfo(u.object.getRouterID(), (int) u.value,
								u.object.oif));
				}else if (tData.metric > u.value) {
					this.table.put(u.object.getRouterID(),
							new NeighborInfo(u.object.getRouterID(), (int) u.value,
									u.object.oif));
					this.router.getIPLayer().removeRoute(u.object.getRouterID());
				}
			}
			
			D.remove(u.object.getRouterID());

			for (Entry<IPAddress, LSPData> v : u.object.getLsp().entrySet()) {

				IPAddress key = v.getKey();
				LSPData data = v.getValue();

				Node<LSPMessage> dv = D.get(key);
				// relax(u,v)
				if (dv != null) {
					if (dv.value > u.value + data.metric) {
						Q.decreaseKey(dv, u.value + data.metric);
						
						LinkedList<IPAddress> revBest = reverseTable.get(key);
						if(revBest == null)
							revBest = new LinkedList<IPAddress>();
						revBest.add(u.object.getRouterID());
						reverseTable.put(key, revBest);
					}

				}

			}

		}

		for (Entry<IPAddress, NeighborInfo> entry : this.table.entrySet()) {
			try {

				IPAddress key = entry.getKey();
				NeighborInfo data = entry.getValue();
				LinkedList<IPAddress> revBest = reverseTable.get(key);
				
				if ( !data.idRouter.equals(source) && revBest !=null) {
					
					IPAddress revIp = this.computeReversePath(reverseTable, key);//revBest.get(revBest.size()-1);
					
					NeighborInfo revD = (revIp.equals(source)) ? this.neighborInfoList.get(key) : this.neighborInfoList.get(revIp);
					IPInterfaceAdapter ita = null;
					if(revD != null)
					 ita = revD.oif;
					this.router.getIPLayer().addRoute(
							new DijkstraRouteEntry(data.idRouter, ita,
									PROTOCOL_NAME, data.metric));
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		System.out.println(this.router + " - " + getRouterID());
		System.out.println(this.neighborInfoList);*/

	}
	/***
	 * 
	 * @param reverseTable
	 * @param destination
	 * @return
	 */
	public IPAddress computeReversePath(LinkedHashMap<IPAddress, LinkedList<IPAddress>> reverseTable , IPAddress destination)
	{
		
		IPAddress bestIp = null;
		
		LinkedList<IPAddress> revBest = reverseTable.get(destination);
		IPAddress ip = revBest.get(revBest.size()-1);
		bestIp = ip;
		
		while(true)
		{
			if(this.router.getIPLayer().hasAddress(ip))
				break;
			bestIp = ip;
			revBest = reverseTable.get(ip);
			ip = revBest.get(revBest.size()-1);	
			
		}
		
		return bestIp;
	}

	/**
	 * 
	 * @param m
	 * @throws Exception
	 */
	private void sendToNeighbors(Message m) throws Exception {
		for (IPInterfaceAdapter iface : this.router.getIPLayer()
				.getInterfaces()) {
			if (iface instanceof IPLoopbackAdapter) {
				continue;
			}
			iface.send(new Datagram(iface.getAddress(), IPAddress.BROADCAST,
					IP_PROTO_DIJKSTRA, 1, m), null);
		}
	}

	private IPInterfaceAdapter getLoopBack()
	{
		for (IPInterfaceAdapter iface : this.router.getIPLayer()
				.getInterfaces()) {
			if (iface instanceof IPLoopbackAdapter) {
				return iface;
			}
		}
		return null;
	}
	/**
	 * 
	 * @param src
	 * @param datagram
	 */
	private void handleHello(IPInterfaceAdapter src, Datagram datagram) {
		HelloMessage hello = (HelloMessage) datagram.getPayload();

		// if (!this.neighborsList.contains(hello.getRouterID())) {
		if (!this.neighborsList.contains(hello.getRouterID()))
			this.neighborsList.add(hello.getRouterID());
		this.neighborInfoList.put(hello.getRouterID(),
				new NeighborInfo(hello.getRouterID(), src.getMetric(), src));
		System.out.println("-------------------\n");
		System.out.printf("--------- TTL : %d ----------\n",datagram.getTTL());
		System.out.println(this.router + "-" + getRouterID());
		System.out.println(this.neighborInfoList);
		System.out.println("-------------------\n");
		

		// }

	}

	/**
	 * 
	 */
	private void sendHello() {
		try {
			for (IPInterfaceAdapter iface : this.router.getIPLayer()
					.getInterfaces()) {
				if (iface instanceof IPLoopbackAdapter) {
					continue;
				}
				HelloMessage helloMsg = new HelloMessage(getRouterID(),
						this.neighborsList);
				iface.send(new Datagram(iface.getAddress(),IPAddress.BROADCAST, IP_PROTO_DIJKSTRA, 1, helloMsg),null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void sendLSP() {
		IPInterfaceAdapter ifaceLoop = getLoopBack();
		LSPMessage firstLsp = creatLSP(getRouterID(), ifaceLoop, numSeq);
		LSDB.put(getRouterID(), firstLsp);
		this.sendLSP(ifaceLoop, firstLsp);

	}

	/**
	 * 
	 * @param src
	 * @param lsp
	 */
	private void sendLSP(IPInterfaceAdapter src, LSPMessage lsp) {
		try {
			for (IPInterfaceAdapter iface : this.router.getIPLayer()
					.getInterfaces()) {
				if (iface instanceof IPLoopbackAdapter || iface.equals(src)) {
					continue;
				}
				lsp.oif = iface;
				iface.send(new Datagram(iface.getAddress(),
						IPAddress.BROADCAST, IP_PROTO_DIJKSTRA, 1, lsp), null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void initHelloTimer() throws Exception {
		
		this.helloTimer = new AbstractTimer(this.router.getNetwork().scheduler,
				HELLOInstervalTime, true) {

			@Override
			protected void run() throws Exception {
				sendHello();

			}
		};
		this.helloTimer.start();

	}

	/**
	 * 
	 */
	private void initLSPTimer() {

		this.LSPTimer = new AbstractTimer(this.router.getNetwork().scheduler,
				LSPInstervalTime, false) {

			@Override
			protected void run() throws Exception {

				sendLSP();
			}
		};
		this.LSPTimer.start();
	}

	/**
	 * 
	 * @param ipSrc
	 * @param oif
	 * @return
	 */
	private LSPMessage creatLSP(IPAddress ipSrc, IPInterfaceAdapter oif,
			int numSequence) {
		Map<IPAddress, LSPData> lsp = new HashMap<>();

		for (Entry<IPAddress, NeighborInfo> entry : this.neighborInfoList
				.entrySet()) {
			IPAddress key = entry.getKey();
			NeighborInfo value = entry.getValue();

			lsp.put(key, new LSPMessage.LSPData(key, value.metric, value.oif));
		}
		LSPMessage lspMsg = new LSPMessage(ipSrc, oif, numSequence, lsp);
		return lspMsg;
	}

	/**
	 * 
	 * @param src
	 * @param datagram
	 */
	private void handleLSP(IPInterfaceAdapter src, Datagram datagram) {

		LSPMessage lspMsg = (LSPMessage) datagram.getPayload();
		
		if ((LSDB.get(lspMsg.getRouterID()) == null)) {
			LSDB.put(lspMsg.getRouterID(), lspMsg);
			sendLSP(src, lspMsg);
			
		} else if (LSDB.get(lspMsg.getRouterID()).getNumSequence() < lspMsg
				.getNumSequence() && ! this.router.getIPLayer().hasAddress(lspMsg.getRouterID())) {

			LSDB.put(lspMsg.getRouterID(), lspMsg);
			sendLSP(src, lspMsg);
		}
	
		this.dijkstra(LSDB, getRouterID());

	}

	/**
	 * 
	 * @return
	 */
	public IPAddress getRouterID() {
		IPAddress routerID = null;
		for (IPInterfaceAdapter iface : this.router.getIPLayer()
				.getInterfaces()) {
			IPAddress addr = iface.getAddress();
			if (routerID == null)
				routerID = addr;
			else if (routerID.compareTo(addr) < 0)
				routerID = addr;
		}
		return routerID;
	}

	@Override
	public void start() throws Exception {
		this.router.getIPLayer().addListener(IP_PROTO_DIJKSTRA, this);
		for (IPInterfaceAdapter iface : this.router.getIPLayer()
				.getInterfaces()) {
			iface.addAttrListener(this);
		}

		initHelloTimer();
		initLSPTimer();
	}

	@Override
	public void stop() {
		this.router.getIPLayer().removeListener(IP_PROTO_DIJKSTRA, this);
		for (IPInterfaceAdapter iface : this.router.getIPLayer()
				.getInterfaces()) {
			iface.removeAttrListener(this);
		}
		this.helloTimer.stop();
		this.LSPTimer.stop();
	}

	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram)
			throws Exception {
		 

		if (datagram.getPayload() instanceof HelloMessage)
		{
			System.out.println(this.router + " : receive HELLO on " + src + " data :  "
					 + datagram.getPayload());
			this.handleHello(src, datagram);
		}

		if (datagram.getPayload() instanceof LSPMessage)
		{
			System.out.println(this.router + " : receive LSP on " + src + " data :  "
					 + datagram.getPayload());
			this.handleLSP(src, datagram);
		}

	}

	@Override
	public void attrChanged(Interface iface, String attr) {
		System.out.println("attrChanged : on" + iface + " with " + attr);
		if (IPInterfaceAdapter.STATE.equals(attr)) {
			this.numSeq++;
			deleteNeighbor(iface);
			sendLSP();

		}
		if (IPInterfaceAdapter.ATTR_METRIC.equals(attr))
			sendHello();

	}

	/**
	 * 
	 * @param iface
	 */
	private void deleteNeighbor(Interface iface) {
		for (Entry<IPAddress, NeighborInfo> entry : this.neighborInfoList
				.entrySet()) {
			IPAddress key = entry.getKey();
			NeighborInfo value = entry.getValue();

			if (value.oif.equals(iface)) {
				this.neighborsList.remove(key);
				this.neighborInfoList.remove(key);
				break;
			}
		}
	}

}
