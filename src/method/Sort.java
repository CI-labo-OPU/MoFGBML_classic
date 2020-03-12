package method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Sort {

	/**
	 *
	 * @param array
	 * @param order
	 * 0: Ascending order, 1: Descending order
	 * @return
	 */
	public static int[] sort(double[] array, int order) {
		ArrayList<Element> a = new ArrayList<Element>();
		for(int i = 0; i < array.length; i++) {
			a.add(new Element(array[i], i));
		}
		if(order == 0) {
			Collections.sort(a, new MyComp());
		} else if(order == 1) {
			Collections.sort(a, new MyComp().reversed());
		}


		int[] index = new int[array.length];
		for(int i = 0; i < array.length; i++) {
			index[i] = a.get(i).index;
		}

		return index;
	}

	static class MyComp implements Comparator<Element>{
		@Override
		public int compare(Element o1, Element o2) {
			if(o1.value < o2.value) {
				return -1;
			} else if(o1.value > o2.value) {
				return 1;
			} else {
				if(o1.index > o2.index) {
					return -1;
				} else {
					return 1;
				}
			}
		}

	}
	static class Element {
		double value;
		int index;

		public Element(double value, int index) {
			this.value = value;
			this.index = index;
		}
	}
}
