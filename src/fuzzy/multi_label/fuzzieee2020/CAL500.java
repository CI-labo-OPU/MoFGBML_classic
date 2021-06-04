package fuzzy.multi_label.fuzzieee2020;

import java.util.Arrays;

import data.Input;
import data.MultiDataSetInfo;
import data.MultiPattern;

public class CAL500 {
	public static void main(String[] args) {

		String dataName = "CAL500.dat";
//		String dataName = "a.dat";
		MultiDataSetInfo dataset = new MultiDataSetInfo();
		Input.inputMultiLabel(dataset, dataName);

		int M = dataset.getDataSize();

		int K = 1;

		double sum = 0;

		for(int m = 0; m < M; m++) {
			MultiPattern pattern = dataset.getPattern(m);
			int[] neighborID = kNeighbor(K, pattern, dataset);

			double ssum = 0;
			for(int k = 0; k < K; k++) {
				MultiPattern object = dataset.getPatternWithID( neighborID[k] );
				ssum += hammingDistance(pattern, object);
			}
			sum += (ssum/(double)K);
		}
		double ave = sum/M;

		System.out.println("sum: " + sum);
		System.out.println("ave: " + ave);
		System.out.println();

		//実験2
		K = 2;
		sum = 0;
		for(int m = 0; m < M; m++) {
			MultiPattern pattern = dataset.getPattern(m);
			int[] neighborID = kNeighbor(K, pattern, dataset);
			MultiPattern object1 = dataset.getPatternWithID(neighborID[0]);
			MultiPattern object2 = dataset.getPatternWithID(neighborID[1]);


//			double ssum = hammingDistance(pattern, object1);
//			double ssum = hammingDistance(pattern, object2);
//			double ssum = hammingDistance(object1, object2);
			double ssum = fillDistance(pattern, object1, object2);

			sum += ssum;

		}
		ave = sum/M;
		System.out.println("sum: " + sum);
		System.out.println("ave: " + ave);
		System.out.println();

	}

	public static double fillDistance(MultiPattern pattern, MultiPattern object1, MultiPattern object2) {
		double remainDistance = 0;

		int L = pattern.getLnum();

		for(int l = 0; l < L; l++) {
			int cP = pattern.getConClass(l);
			int c1 = object1.getConClass(l);
			int c2 = object2.getConClass(l);

			if( (cP != c1) && (cP == c2) ) {
				remainDistance += 1;
			}
		}

		return remainDistance;
	}

	public static int[] kNeighbor(int K, MultiPattern pattern, MultiDataSetInfo dataset) {
		int[] neighbor = new int[K];
		double[] distances = new double[K];
		Arrays.fill(distances, Double.MAX_VALUE);

		int M = dataset.getDataSize();

		for(int m = 0; m < M; m++) {
			MultiPattern object = dataset.getPattern(m);
			if(pattern.getID() == object.getID()) {
				continue;
			}

			double distance = distanceVec(pattern, object);
			for(int k = 0; k < K; k++) {
				if(distance < distances[k]) {
					distances[k] = distance;
					neighbor[k] = object.getID();
					break;
				}
			}

		}

		return neighbor;
	}

	public static double distanceVec(MultiPattern v1, MultiPattern v2) {
		double distance = 0;

		int N = v1.getNdim();
		for(int n = 0; n < N; n++) {
			double x1 = v1.getDimValue(n);
			double x2 = v2.getDimValue(n);
			distance += (x1 - x2) * (x1 - x2);
		}

		return distance;
	}


	public static double hammingDistance(MultiPattern v1, MultiPattern v2) {
		int[] c1 = v1.getConClass();
		int[] c2 = v2.getConClass();
		double distance = 0;

		int L = v1.getLnum();
		for(int l = 0; l < L; l++) {
			if(c1[l] != c2[l] ) {
				distance += 1;
			}
		}

		return distance;
	}
}
