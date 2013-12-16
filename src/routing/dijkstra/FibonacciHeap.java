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
import java.util.List;
import java.util.Stack;
/**
*
* @author Bill Nizeyimana
*/
public class FibonacciHeap<I> {
	
	private static final double LogPhi = 1.0 / Math.log((1.0 + Math.sqrt(5.0)) / 2.0);
	public Node<I> min;
	public int n = 0;
	
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty()
	{
		return min == null;
	}
	/**
	 * Fibonacci-Heap-Insert(H,x)
	 *	degree[x] := 0 
	 *	p[x] := NIL
	 *	child[x] := NIL
	 *	prev[x] := x
	 *	next[x] := x
	 *	mark[x] := FALSE
	 *	concatenate the root list containing x with root list H 
	 *	if min[H] = NIL or value[x]<value[min[H]]
	 *		then min[H] := x 
	 *	n[H]:= n[H]+1
	 *
	 * @param x
	 */
	public void insert(Node<I> x)
	{
        if (min != null) {
            x.prev = min;
            x.next = min.next;
            min.next = x;
            x.next.prev = x;

            if (x.value < min.value) {
                min = x;
            }
        } else {
            min = x;
        }

        n++;
	}
	/**
	 * 
	 * @param object
	 * @param value
	 * @return
	 */
	public Node<I> insert(I object, double value)
	{
		Node<I> x = new Node<I>(object,value);
		
        if (min != null) {
            x.prev = min;
            x.next = min.next;
            min.next = x;
            x.next.prev = x;

            if (x.value < min.value) {
                min = x;
            }
        } else {
            min = x;
        }

        n++;
        return x;
	}
	/**
	 * Fibonacci-Heap-Delete(H,x)
	 * Fibonacci-Heap-Decrease-Key(H,x,-infinity) 
	 * Fibonacci-Heap-Extract-Min(H)
	 * 
	 * @param x
	 */
	public void delete(Node<I> x)
    {
        decreaseKey(x, Double.NEGATIVE_INFINITY);
        extractMin();
    }

	/**
	 * 	Fibonacci-Heap-Extract-Min(H) 
	 * 	z:= min[H]
	 *	if x <> NIL
	 *		then for each child x of z
	 *			do add x to the root list of H 
	 *				p[x]:= NIL
	 *			remove z from the root list of H 
	 *			if z = next[z]
	 *				then min[H]:=NIL 
	 *				else 
	 *					min[H]:=next[z]
	 *					CONSOLIDATE(H) 
	 *			n[H] := n[H]-1
	 *	return z
	 * @return
	 */
	public Node<I> extractMin()
	{
		Node<I> z = min;

        if (z != null) {
            int kids = z.degree;
            Node<I> x = z.child;
            Node<I> tempnext;

            
            while (kids > 0) {
                tempnext = x.next;

                x.prev.next = x.next;
                x.next.prev = x.prev;

                x.prev = min;
                x.next = min.next;
                min.next = x;
                x.next.prev = x;
                
                x.parent = null;
                x = tempnext;
                kids--;
            }
          
            z.prev.next = z.next;
            z.next.prev = z.prev;

            if (z == z.next) {
                min = null;
            } else {
                min = z.next;
                consolidate();
            }

            n--;
        }

        return z;
		
	}
	/**
	 * 	Fibonacci-Heap-Decrease-Key(H,x,k)
	 * 	if k > key[x]
	 * 		then error "new key is greater than current key"
	 * 	key[x] := k
	 *	y := p[x]
	 * 	if y <> NIL and key[x]<key[y]
	 *		then CUT(H, x, y) 
	 *			 CASCADING-CUT(H,y)
	 *	if key[x]<key[min[H]] 
	 *		then min[H] := x
	 * @param x
	 * @param k
	 */
	public void decreaseKey(Node<I> x, double k)
    {
        if (k > x.value) {
            throw new IllegalArgumentException("decreaseKey:: New value  : "+ k +" greater "+ x.value );
        }

        x.value = k;

        Node<I> y = x.parent;

        if ((y != null) && (x.value < y.value)) {
            cut(x, y);
            cascadingCut(y);
        }

        if (x.value < min.value) {
            min = x;
        }
    }
	/**
	 * CUT(H,x,y)
	 *	Remove x from the child list of y, decrementing degree[y]
	 *	Add x to the root list of H 
	 *	p[x]:= NIL
	 *	mark[x]:= FALSE
	 * @param x
	 * @param y
	 */
	private void cut(Node<I> x, Node<I> y)
	{
		 // remove x from childlist of y and decrement H
        x.prev.next = x.next;
        x.next.prev = x.prev;
        y.degree--;

         
        if (y.child == x) {
            y.child = x.next;
        }

        if (y.degree == 0) {
            y.child = null;
        }

        // add x to root list of heap
        x.prev = min;
        x.next = min.next;
        min.next = x;
        x.next.prev = x;

        x.parent = null;
        
        x.isMarked = false;
	}
	/**
	 * 
	 * CASCADING-CUT(H,y) 
	 * z:= p[y]
	 * if z <> NIL
	 * 	 then if mark[y] = FALSE
	 *		  then mark[y]:= TRUE 
	 *		  else CUT(H, y, z)
	 *			   CASCADING-CUT(H, z) 
	 * 
	 * @param y
	 */
	private void cascadingCut(Node<I> y)
	{
		  Node<I> z = y.parent;
	        if (z != null) {
	            if (!y.isMarked) {
	                y.isMarked = true;
	            } else {
	                cut(y, z);
	                cascadingCut(z);
	            }
	        }
	}
	/**
	 * CONSOLIDATE(H)
	 *	for i:=0 to D(n[H]) 
	 *		Do A[i] := NIL
	 * for each node w in the root list of H 
	 * 		do x:= w
	 *	d:= degree[x] 
	 *while A[d] <> NIL
	 *	do y:=A[d]
	 *	if value[x]>value[y]
	 *		then exchange x<->y 
	 *	Fibonacci-Heap-Link(H, y, x)
	 *  A[d]:=NIL
	 *	d:=d+1 A[d]:=x
	 *	min[H]:=NIL
	 *	for i:=0 to D(n[H])
	 * 	do if A[i]<> NIL
	 * 		then add A[i] to the root list of H
	 *	if min[H] = NIL or value[A[i]]<value[min[H]] 
	 *		then min[H]:= A[i]
	 * 
	 */
	private void consolidate()
	{
		 int arraySize =
		            ((int) Math.floor(Math.log(n) * LogPhi)) + 1;

		        List<Node<I>> array =
		            new ArrayList<Node<I>>(arraySize);

		         
		        for (int i = 0; i < arraySize; i++) {
		            array.add(null);
		        }
		        
		        int nbrRoots = 0;
		        Node<I> x = min;

		        if (x != null) {
		            nbrRoots++;
		            x = x.next;

		            while (x != min) {
		                nbrRoots++;
		                x = x.next;
		            }
		        }

		        while (nbrRoots > 0) {
		            
		            int d = x.degree;
		            Node<I> next = x.next;

		            for (;;) {
		                Node<I> y = array.get(d);
		                if (y == null) {
		                    break;
		                }

		                if (x.value > y.value) {
		                    Node<I> temp = y;
		                    y = x;
		                    x = temp;
		                }

		                link(y, x);
		                array.set(d, null);
		                d++;
		            }
		            array.set(d, x);
		            x = next;
		            nbrRoots--;
		        }

		        min = null;

		        for (int i = 0; i < arraySize; i++) {
		            Node<I> y = array.get(i);
		            if (y == null) {
		                continue;
		            }

		            if (min != null) {
		                 
		                y.prev.next = y.next;
		                y.next.prev = y.prev;

		                y.prev = min;
		                y.next = min.next;
		                min.next = y;
		                y.next.prev = y;

		                 
		                if (y.value < min.value) {
		                    min = y;
		                }
		            } else {
		                min = y;
		            }
		        }
	}
	/**
	 * Fibonacci-Heap-Link(H,y,x) 
	 * remove y from the root list of H
	 * make y a child of x 
	 * degree[x] := degree[x] + 1 
	 * mark[y] := FALSE
	 * @param y
	 * @param x
	 */
	 private void link(Node<I> y, Node<I> x)
	    {
	        // remove y from root list of heap
	        y.prev.next = y.next;
	        y.next.prev = y.prev;

	        y.parent = x;

	        if (x.child == null) {
	            x.child = y;
	            y.next = y;
	            y.prev = y;
	        } else {
	            y.prev = x.child;
	            y.next = x.child.next;
	            x.child.next = y;
	            y.next.prev = y;
	        }
	        
	        x.degree++;

	        y.isMarked = false;
	    }
	 
	 /**
	  * Fibonacci-Heap-Union(H1,H2) 
	  * H := Make-Fibonacci-Heap()
	  *	min[H] := min[H1]
	  *	Concatenate the root list of H2 with the root list of H
	  *	if (min[H1] = NIL) or (min[H2] <> NIL and min[H2] < min[H1])
	  *		then min[H] := min[H2] 
	  *	n[H] := n[H1] + n[H2] 
	  *	free the objects H1 and H2 
	  * return H
	  * @param h1
	  * @param h2
	  * @return
	  */
	 public static <I> FibonacciHeap<I> union( FibonacciHeap<I> h1, FibonacciHeap<I> h2)
		    {
		 FibonacciHeap<I> h = new FibonacciHeap<I>();

		        if ((h1 != null) && (h2 != null)) {
		            h.min = h1.min;

		            if (h.min != null) {
		                if (h2.min != null) {
		                    h.min.next.prev = h2.min.prev;
		                    h2.min.prev.next = h.min.next;
		                    h.min.next = h2.min;
		                    h2.min.prev = h.min;

		                    if (h2.min.value < h1.min.value) {
		                        h.min = h2.min;
		                    }
		                }
		            } else {
		                h.min = h2.min;
		            }

		            h.n = h1.n + h2.n;
		        }

		        return h;
		    }
	 /**
	  * 
	  */
	 public String toString()
	    {
	        if (min == null) {
	            return "FibonacciHeap=[]";
	        }
	 
	        Stack<Node<I>> stack = new Stack<Node<I>>();
	        stack.push(min);

	        StringBuffer buf = new StringBuffer(512);
	        buf.append("FibonacciHeap=[");
	 
	        while (!stack.empty()) {
	            Node<I> curr = stack.pop();
	            buf.append(curr);
	            buf.append(", ");

	            if (curr.child != null) {
	                stack.push(curr.child);
	            }

	            Node<I> start = curr;
	            curr = curr.next;

	            while (curr != start) {
	                buf.append(curr);
	                buf.append(", ");

	                if (curr.child != null) {
	                    stack.push(curr.child);
	                }

	                curr = curr.next;
	            }
	        }

	        buf.append(']');

	        return buf.toString();
	    }
}
