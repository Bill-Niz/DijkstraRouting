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

public class Node<I> {
	
	public Node<I> parent;
	public Node<I> child;
	public Node<I> prev;
	public Node<I> next;
	public I object;
	public Boolean isMarked = false;
	public double value;
	public int degree = 0;
	
	/**
	 *
	 * @author Bill Nizeyimana
	 */
	
	/**
	 * Create an instance of Node
	 * 
	 * @param object
	 * @param value
	 */
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
}
