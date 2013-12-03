/**
 * 
 */
package routing.dijkstra.testing;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import routing.dijkstra.FibonacciHeap;

/**
 * @author billniz
 * 
 */
public class FibonacciHeapTest {
	FibonacciHeap<String> fibHeap ;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		 fibHeap = new FibonacciHeap<String>();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link routing.dijkstra.FibonacciHeap#insert(routing.dijkstra.Node)}.
	 */
	@Test
	public void testInsert() {
		for (int i = 4; i < 7; i++) {
			routing.dijkstra.Node<String> n = new routing.dijkstra.Node("R" + i, i);
			fibHeap.insert(n);
		}
	}

	/**
	 * Test method for
	 * {@link routing.dijkstra.FibonacciHeap#delete(routing.dijkstra.Node)}.
	 */
	@Test
	public void testDelete() {
		for (int i = 4; i < 7; i++) {
			routing.dijkstra.Node<String> n = new routing.dijkstra.Node("R" + i, i);
			fibHeap.insert(n);
		}
		routing.dijkstra.Node<String> n = new routing.dijkstra.Node("R"+9,Double.POSITIVE_INFINITY);
		fibHeap.insert(n);
		 fibHeap.delete(n);
	}

	/**
	 * Test method for {@link routing.dijkstra.FibonacciHeap#extractMin()}.
	 */
	@Test
	public void testExtractMin() {
		
		for (int i = 4; i < 7; i++) {
			routing.dijkstra.Node<String> n = new routing.dijkstra.Node("R" + i, i);
			fibHeap.insert(n);
		}
		routing.dijkstra.Node<String> n = new routing.dijkstra.Node("R"+9,Double.POSITIVE_INFINITY);
		fibHeap.insert(n);
		routing.dijkstra.Node<String> min = fibHeap.extractMin();
		
	}

	/**
	 * Test method for
	 * {@link routing.dijkstra.FibonacciHeap#decreaseKey(routing.dijkstra.Node, double)}
	 * .
	 */
	@Test
	public void testDecreaseKey() {
		for (int i = 4; i < 7; i++) {
			routing.dijkstra.Node<String> n = new routing.dijkstra.Node("R" + i, i);
			fibHeap.insert(n);
		}
		routing.dijkstra.Node<String> n = new routing.dijkstra.Node("R"+9,Double.POSITIVE_INFINITY);
		fibHeap.insert(n);
		fibHeap.decreaseKey(n, 0);
	}

	/**
	 * Test method for
	 * {@link routing.dijkstra.FibonacciHeap#union(routing.dijkstra.FibonacciHeap, routing.dijkstra.FibonacciHeap)}
	 * .
	 */
	@Test
	public void testUnion() {
		for (int i = 4; i < 7; i++) {
			routing.dijkstra.Node<String> n = new routing.dijkstra.Node("R" + i, i);
			fibHeap.insert(n);
		}
		FibonacciHeap<String> fibHeap2 = new FibonacciHeap<String>();
		for (int i = 4; i < 7; i++) {
			routing.dijkstra.Node<String> n = new routing.dijkstra.Node("R" + i, i);
			fibHeap2.insert(n);
		}
		FibonacciHeap<String> fibHeapU = FibonacciHeap.union(fibHeap2, fibHeap2);
	}

}
