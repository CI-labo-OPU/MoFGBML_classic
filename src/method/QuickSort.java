package method;

public class QuickSort {

	/**
	 *
	 * @param a
	 * @param order
	 * 1: Ascending order, -1: Descending order
	 */
	public static void sort(double[] values, int[] index, int order) {

		double[] a = new double[values.length];
		for(int i = 0; i < a.length; i++) {
			a[i] = values[i];
		}

		quickSort(a, index, 0, a.length-1, order);
	}

	//Quick Sort
	public static void quickSort(double[] a, int[] index, int i, int j, int order) {
		if(i == j) {
			return;
		}
		int p = pivot(a, index, i, j, order);
		if(p != -1) {
			int k = partition(a, index, i, j, a[p], order);
			quickSort(a, index, i, k-1, order);
			quickSort(a, index, k, j, order);
		}
	}

	//Divide Partition
	public static int partition(double[] a, int[] index, int i, int j, double x, int order) {
		int l = i, r = j;
		while(l <= r) {
			while(l <= j && order*a[l] < order*x) {
				l++;
			}
			while(r >= i && order*a[r] >= order*x) {
				r--;
			}

			if(l>r) break;

			int tt = index[l];
			index[l] = index[r];
			index[r] = tt;
			double t = a[l];
			a[l] = a[r];
			a[r] = t;
			l++;
			r--;
		}
		return l;
	}

	//Select Axis Point
	public static int pivot(double[] a, int[] index, int i, int j, int order) {
		int k = i+1;
		while(k <= j && a[i] == a[k]) {
			k++;
		}
		if(k > j) {
			return -1;
		}
		if(order * a[i] >= order * a[k]) {
			return i;
		}
		return k;
	}



}
