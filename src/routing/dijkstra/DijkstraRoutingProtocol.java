/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package routing.dijkstra;

import java.util.ArrayList;
import java.util.HashMap;
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
	private Timer helloTimer;
	private Timer LSPTimer;

	private ArrayList<IPAddress> neighborsList = new ArrayList<>();
	private Map<IPAddress, NeighborInfo> neighborInfoList = new HashMap<IPAddress, NeighborInfo>();
	private Map<IPAddress, LSPMessage> LSDB = new HashMap<IPAddress, LSPMessage>();
	private Map<IPAddress, NeighborInfo> table =  new HashMap<IPAddress, NeighborInfo>();
	
	
	private class  NeighborInfo
	{
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
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[idRouter=" + idRouter + ", metric=" + metric
					+ ", oif=" + oif + "]";
		}
		
		
		
	}
	
	/**
	 * 
	 * @param host
	 * @param name
	 */
	public DijkstraRoutingProtocol(Host host, String name) {
		super(host, name);
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
	private void dijkstra( Map<IPAddress, LSPMessage> LSDB,IPAddress source) {
		
		FibonacciHeap<LSPMessage> Q = new FibonacciHeap<LSPMessage>();
		
		Map<IPAddress, Node< LSPMessage>> D = new HashMap<IPAddress, Node< LSPMessage>>();
		
		
		for (Entry<IPAddress, LSPMessage> entry : LSDB.entrySet()) {
			
			IPAddress key = entry.getKey();
			LSPMessage value = entry.getValue();
			
			if(source.equals(key))
				D.put(key, Q.insert(value, 0.0));
			else
				D.put(key, Q.insert(value, Double.POSITIVE_INFINITY));
		}

		while (!Q.isEmpty())
		{
			Node<LSPMessage> u = Q.extractMin();
			// Update FIB
			this.table.put(u.object.getRouterID(), new NeighborInfo(u.object.getRouterID(), (int) u.value, u.object.oif));
			
			
			for (Entry<IPAddress, Integer> v : u.object.getLsp().entrySet()) {
				IPAddress key = v.getKey();
				int cuv = v.getValue();
				
				Node<LSPMessage> dv = D.get(key);
				//relax(u,v)
				if(dv != null){
				if(dv.value > u.value + cuv)
				{
					Q.decreaseKey(dv, u.value + cuv);
					//dv.object.oif = u.object.oif;
				}
				}
				
			}

		}
		System.out.println(this.router+ " - " +getRouterID());
		System.out.println(this.table);
		System.out.println("--------------------");
		
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
			iface.send(new Datagram(iface.getAddress(), IPAddress.BROADCAST,IP_PROTO_DIJKSTRA, 1, m), null);
		}
	}
	/**
	 * 
	 * @param src 
	 * @param datagram
	 */
	private void handleHello(IPInterfaceAdapter src, Datagram datagram) {
		HelloMessage hello = (HelloMessage) datagram.getPayload();
		
		if(!this.neighborsList.contains(hello.getRouterID()))
		{
			this.neighborsList.add(hello.getRouterID());
			this.neighborInfoList.put(hello.getRouterID(), new NeighborInfo(hello.getRouterID(), src.getMetric(), src));
			
		}
			
		/*if (hello.getNeighborsList().isEmpty()) {
			if(!this.neighborsList.contains(hello.getRouterID()))
				this.neighborsList.add(hello.getRouterID());
			
			
		} else {
			for (IPAddress ip : hello.getNeighborsList()) {
				if (ip.equals(getRouterID())) {
					if(!this.neighborsList.contains(ip)){
						this.neighborsList.add(hello.getRouterID());
						System.out.println(ip + " -- IS --> "+ getRouterID());
					}
					
				}else
				{
					System.out.println(ip+" -- NOT --> "+ getRouterID());
				}
			}
		}*/
/*
		System.out.println(this.router+"-"+ getRouterID());
		System.out.println(this.neighborInfoList);
		System.out.println("-------------------\n");*/
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
				iface.send(new Datagram(iface.getAddress(),
						IPAddress.BROADCAST, IP_PROTO_DIJKSTRA, 1, helloMsg),
						null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		// this.sendToNeighbors(new Message() {});
/*
		this.helloTimer = new Timer("tHello-" + this.router.name);

		TimerTask task = new TimerTask() {

			@Override
			public void run() {

				AbstractEvent evt = new AbstractEvent(HELLOInstervalTime) {

					@Override
					public void run() throws Exception {
						sendHello();

					}
				};

				router.getNetwork().scheduler.schedule(evt);
			}
		};
		this.helloTimer.schedule(task, 0, this.HELLOInstervalTime);
		*/
		AbstractTimer helloT = new AbstractTimer(this.router.getNetwork().scheduler,1,true) {
			
			@Override
			protected void run() throws Exception {
				sendHello();
				
			}
		};
		helloT.start();

	}

	/**
	 * 
	 */
	private void initLSPTimer() {

	/*	this.LSPTimer = new Timer("tLSP-" + this.router.name);

		TimerTask task = new TimerTask() {

			@Override
			public void run() {

				AbstractEvent evt = new AbstractEvent(LSPInstervalTime) {

					@Override
					public void run() throws Exception {
						LSDB.put(getRouterID(), creatLSP(getRouterID(), null));
						sendLSP(null, creatLSP(getRouterID(), null));

					}
				};

				router.getNetwork().scheduler.schedule(evt);
			}
		};
		this.LSPTimer.schedule(task, 500, this.LSPInstervalTime);*/
AbstractTimer helloT = new AbstractTimer(this.router.getNetwork().scheduler,10,true) {
			
			@Override
			protected void run() throws Exception {
				LSDB.put(getRouterID(), creatLSP(getRouterID(), (IPInterfaceAdapter) router.getInterfaceByName("lo")));
				sendLSP(null, creatLSP(getRouterID(), null));
				
			}
		};
		helloT.start();
	}
	/**
	 * 
	 * @param ipSrc
	 * @param oif
	 * @return
	 */
	private LSPMessage creatLSP(IPAddress ipSrc, IPInterfaceAdapter oif)
	{
		Map<IPAddress,Integer> lsp = new HashMap<>();
		
		for (Entry<IPAddress, NeighborInfo> entry : this.neighborInfoList.entrySet())  {
			IPAddress key = entry.getKey();
			NeighborInfo value = entry.getValue();
			
			lsp.put(key, value.metric);
		}
		LSPMessage lspMsg = new LSPMessage(ipSrc, oif, 0, lsp);
		return lspMsg;
	}
	/**
	 * 
	 * @param src 
	 * @param datagram
	 */
	private void handleLSP(IPInterfaceAdapter src, Datagram datagram) {
	
		//if((LSDB.get(datagram.src) == null) )
		//{
		//	LSDB.put(datagram.src,(LSPMessage) datagram.getPayload());
		//}
		//else if ((LSDB.get(datagram.src).getNumSequence() < (((LSPMessage)datagram.getPayload()).getNumSequence()))) {
			
			LSDB.put(datagram.src,(LSPMessage) datagram.getPayload());
		//} 
		/*System.out.println(this.router);
		System.out.println("--LSDB--");
		System.out.println(this.LSDB);
		System.out.println("-------------------\n");*/
		
		sendLSP(src, (LSPMessage) datagram.getPayload());
		
		this.dijkstra(LSDB, getRouterID());

	}
	/**
	 * 
	 * @return
	 */
	public IPAddress getRouterID() {
		IPAddress routerID= null;
		for (IPInterfaceAdapter iface: this.router.getIPLayer().getInterfaces()) {
			IPAddress addr= iface.getAddress();
			if (routerID == null)
				routerID= addr;
			else if (routerID.compareTo(addr) < 0)
				routerID= addr;
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
	}

	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram)
			throws Exception {
		//System.out.println(this.router + " : receive on " + src + " data :  "
			//	+ datagram.getPayload());

		if (datagram.getPayload() instanceof HelloMessage)
			this.handleHello(src,datagram);

		if (datagram.getPayload() instanceof LSPMessage)
			this.handleLSP(src,datagram);

	}

	@Override
	public void attrChanged(Interface iface, String attr) {
		System.out.println("attrChanged : on" + iface + "with " + attr);
	}

}
