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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import reso.common.AbstractApplication;
import reso.common.AbstractTimer;
import reso.common.Interface;
import reso.common.InterfaceAttrListener;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;
import reso.ip.IPLoopbackAdapter;
import reso.ip.IPRouter;
import routing.dijkstra.LSPMessage.LSPData;

/**
 * 
 * @author Bill Nizeyimana
 */
public class DijkstraRoutingProtocol extends AbstractApplication implements
		IPInterfaceListener, InterfaceAttrListener {

	public static final String PROTOCOL_NAME = "DIJKSTRA_ROUTING";
	public static final int IP_PROTO_DIJKSTRA = Datagram
			.allocateProtocolNumber(PROTOCOL_NAME);
	private int HELLOInstervalTime = 2;
	private int LSPInstervalTime = 20;
	private int AGEINGInstervalTime = 40;
	private IPRouter router;
	private AbstractTimer helloTimer;
	private AbstractTimer LSPTimer;
	private AbstractTimer AGEINGTimer;
	private int numSeq = 0;

	private ArrayList<IPAddress> neighborsList = new ArrayList<>();
	private Map<IPAddress, NeighborInfo> neighborInfoList = new HashMap<IPAddress, NeighborInfo>();
	private final Map<IPAddress, LSPMessage> LSDB = new HashMap<IPAddress, LSPMessage>();
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
	private synchronized void dijkstra(Map<IPAddress, LSPMessage> LSDB,
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
			// Update RIB
			updateRIB(u);

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
						if (revBest == null)
							revBest = new LinkedList<IPAddress>();
						revBest.add(u.object.getRouterID());
						reverseTable.put(key, revBest);
					}

				}

			}

		}
		updateFIB(reverseTable);
	}

	/**
	 * 
	 * @param u
	 */
	private void updateRIB(Node<LSPMessage> u) {
		if (!this.router.getIPLayer().hasAddress(u.object.getRouterID())) {
			NeighborInfo tData = this.table.get(u.object.getRouterID());
			if (tData == null) {
				this.table.put(u.object.getRouterID(), new NeighborInfo(
						u.object.getRouterID(), (int) u.value, u.object.oif));
			} else if (tData.metric > u.value) {
				this.table.put(u.object.getRouterID(), new NeighborInfo(
						u.object.getRouterID(), (int) u.value, u.object.oif));
				this.router.getIPLayer().removeRoute(u.object.getRouterID());
			}
		}

	}

	/**
	 * 
	 * @param reverseTable
	 */
	private void updateFIB(
			LinkedHashMap<IPAddress, LinkedList<IPAddress>> reverseTable) {
		for (Entry<IPAddress, NeighborInfo> entry : this.table.entrySet()) {
			try {

				IPAddress key = entry.getKey();
				NeighborInfo data = entry.getValue();
				LinkedList<IPAddress> revBest = reverseTable.get(key);

				if (!this.router.getIPLayer().hasAddress(data.idRouter)
						&& revBest != null) {

					IPAddress revIp = this
							.computeReversePath(reverseTable, key);

					NeighborInfo revD = this.router.getIPLayer().hasAddress(
							revIp) ? this.neighborInfoList.get(key)
							: this.neighborInfoList.get(revIp);
					IPInterfaceAdapter ita = null;
					if (revD != null)
						ita = revD.oif;
					this.router.getIPLayer().addRoute(
							new DijkstraRouteEntry(data.idRouter, ita,
									PROTOCOL_NAME, data.metric));
				}

			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}

	/***
	 * 
	 * @param reverseTable
	 * @param destination
	 * @return
	 */
	public IPAddress computeReversePath(
			LinkedHashMap<IPAddress, LinkedList<IPAddress>> reverseTable,
			IPAddress destination) {

		IPAddress bestIp = null;

		LinkedList<IPAddress> revBest = reverseTable.get(destination);
		IPAddress ip = revBest.get(revBest.size() - 1);
		bestIp = ip;

		while (true) {
			if (this.router.getIPLayer().hasAddress(ip))
				break;
			bestIp = ip;
			revBest = reverseTable.get(ip);
			ip = revBest.get(revBest.size() - 1);
		}

		return bestIp;
	}

	/**
	 * 
	 * @return
	 */
	private IPInterfaceAdapter getLoopBack() {
		for (IPInterfaceAdapter iface : this.router.getIPLayer()
				.getInterfaces()) {
			if (iface instanceof IPLoopbackAdapter) {
				return iface;
			}
		}
		return null;
	}

	/**
	 * This method process the incoming Hello datagrams
	 * 
	 * @param src
	 *            the IP interface where the datagrams are received
	 * 
	 * @param datagram
	 *            the received datagram
	 */
	private void handleHello(IPInterfaceAdapter src, Datagram datagram) {
		HelloMessage hello = (HelloMessage) datagram.getPayload();

		if (!this.neighborsList.contains(hello.getRouterID())) {
			this.neighborsList.add(hello.getRouterID());
			this.sendHello();
		} else {

			if (this.neighborInfoList.containsKey(hello.getRouterID())) {
				NeighborInfo nInfo = this.neighborInfoList.get(hello
						.getRouterID());
				if (nInfo.metric > src.getMetric())
					this.neighborInfoList.put(
							hello.getRouterID(),
							new NeighborInfo(hello.getRouterID(), src
									.getMetric(), src));
			}else
			{
				this.neighborInfoList.put(hello.getRouterID(), new NeighborInfo(
						hello.getRouterID(), src.getMetric(), src));
			}

		}

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
	 */
	private void sendLSP() {
		IPInterfaceAdapter ifaceLoop = getLoopBack();
		LSPMessage lsp = creatLSP(getRouterID(), ifaceLoop, numSeq);
		LSDB.put(getRouterID(), lsp);
		this.sendLSP(ifaceLoop, lsp);

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
	 * This method process the incoming LSP datagrams
	 * 
	 * @param src
	 *            the IP interface where the datagrams are received
	 * 
	 * @param datagram
	 *            the received datagram
	 */
	private void handleLSP(IPInterfaceAdapter src, Datagram datagram) {

		synchronized (this.LSDB) {
			LSPMessage lspMsg = (LSPMessage) datagram.getPayload();

			if ((LSDB.get(lspMsg.getRouterID()) == null)) {
				LSDB.put(lspMsg.getRouterID(), lspMsg);
				sendLSP(src, lspMsg);

			} else if (LSDB.get(lspMsg.getRouterID()).getNumSequence() < lspMsg
					.getNumSequence()
					&& !this.router.getIPLayer().hasAddress(
							lspMsg.getRouterID())) {

				LSDB.put(lspMsg.getRouterID(), lspMsg);
				sendLSP(src, lspMsg);
			}
		}

		this.dijkstra(LSDB, getRouterID());

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
				LSPInstervalTime, true) {

			@Override
			protected void run() throws Exception {

				sendLSP();
			}
		};
		this.LSPTimer.start();
	}

	/**
	 * 
	 */
	private void initAGEINGTimer() {

		this.AGEINGTimer = new AbstractTimer(
				this.router.getNetwork().scheduler, 2, true) {

			@Override
			protected void run() throws Exception {

				handleLSPAgeing();
			}
		};
		this.AGEINGTimer.start();
	}

	/**
	 * 
	 */
	protected void handleLSPAgeing() {

		synchronized (this.LSDB) {
			for (Entry<IPAddress, LSPMessage> entry : this.LSDB.entrySet()) {
				IPAddress key = entry.getKey();
				LSPMessage value = entry.getValue();

				if (!this.router.getIPLayer().hasAddress(key)) {
					if (value.age == 0)
						this.LSDB.remove(key);
					else
						value.age -= 1;
				}
			}
		}
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
		LSPMessage lspMsg = new LSPMessage(ipSrc, oif, numSequence, lsp,
				this.AGEINGInstervalTime);
		return lspMsg;
	}

	/**
	 * 
	 * @return
	 */
	private IPAddress getRouterID() {
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
		initAGEINGTimer();
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
		this.AGEINGTimer.stop();

	}

	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram)
			throws Exception {

		if (datagram.getPayload() instanceof HelloMessage) {
			System.out.println(getCurrrentTime() + ". " + this.router
					+ "- HELLO on " + src + ", src=" + datagram.src + " => "
					+ datagram.getPayload() + "\n");
			this.handleHello(src, datagram);
		}

		if (datagram.getPayload() instanceof LSPMessage) {
			System.out.println(getCurrrentTime() + ". " + this.router
					+ "- LSP on " + src + ", src=" + datagram.src + " => "
					+ datagram.getPayload() + "\n");
			this.handleLSP(src, datagram);
		}

	}

	@Override
	public void attrChanged(Interface iface, String attr) {
		System.out.println("attrChanged : on " + iface + " with " + attr);
		switch (attr) {
		case IPInterfaceAdapter.STATE: {
			this.numSeq++;
			if (!(Boolean) iface.getAttribute(attr))
				deleteNeighbor(iface);
			sendLSP();

			break;
		}
		case IPInterfaceAdapter.ATTR_METRIC:
			sendHello();
			break;
		default:
			break;
		}

	}

	/**
	 * 
	 * @return
	 */
	private String getCurrrentTime() {
		return ((double) (host.getNetwork().getScheduler().getCurrentTime()))
				+ " s";
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
