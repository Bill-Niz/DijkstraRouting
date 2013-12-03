package routing.dijkstra;

public class Node<I> {
	
	public Node<I> parent;
	public Node<I> child;
	public Node<I> prev;
	public Node<I> next;
	public I object;
	public Boolean isMarked = false;
	public double value;
	public int degree = 0;
	
	
	public Node(I object, double value) {
		super();
		this.object = object;
		this.value = value;
		this.next = this;
		this.prev = this;
	}


	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Node [obj=" + object + ", val="
				+ value + "]";
	}


	public  void print(Node<I> n)
	{
		if(n==null)
			return;
		System.out.print(n);
		System.out.println("\t p:"+n.parent);
		System.out.println("\tprev:"+n.prev);
		System.out.println("\tnext:"+n.next);
		System.out.println("_");
	}


	


}
