package fuzzy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import data.SingleDataSetInfo;
import main.Setting;
import method.StaticFunction;

public class FuzzyPartitioning {
	// ************************************************************

	// ************************************************************

	// ************************************************************

	public static ArrayList<ArrayList<double[]>> startPartition(SingleDataSetInfo tra, int K, double F){
		ArrayList<ArrayList<double[]>> trapezoids = new ArrayList<ArrayList<double[]>>();

		for(int dim_i = 0; dim_i < tra.getNdim(); dim_i++) {
			//Step 0. Judge Categoric.
			if(tra.getPattern(0).getDimValue(dim_i) < 0) {
				//If it's categoric, do NOT partitinon.
				trapezoids.add(new ArrayList<double[]>());
				continue;
			}

			//Step 1. Sort patterns by attribute "dim_i"
			ArrayList<ForSortPattern> patterns = new ArrayList<ForSortPattern>();
			for(int p = 0; p < tra.getDataSize(); p++) {
				patterns.add( new ForSortPattern(tra.getPattern(p).getDimValue(dim_i),
												 tra.getPattern(p).getConClass()));
			}
			Collections.sort(patterns, new Comparator<ForSortPattern>() {
				@Override
				//Ascending Order
				public int compare(ForSortPattern o1, ForSortPattern o2) {
					if(o1.getX() > o2.getX()) {return 1;}
					else if(o1.getX() < o2.getX()) {return -1;}
					else {return 0;}
				}
			});

			//Step 2. Optimal Splitting.
			ArrayList<Double> partitions = optimalSplitting(patterns, K, tra.getCnum());

			//Step 3. Fuzzify partitions
			trapezoids.add(makeTrapezoids(partitions, F));
		}
		return trapezoids;
	}

	/**
	 * <h1>Class-entropy based searching optimal-partitionings</h1>
	 * @param patterns : {@literal ArrayList<ForSortPattern>} :
	 * @param K : int : Given number of partitions
	 * @param Cnum : int : #of classes
	 * @return
	 */
	public static ArrayList<Double> optimalSplitting(ArrayList<ForSortPattern> patterns, int K, int Cnum) {
		double D = patterns.size();

		ArrayList<Double> partitions = new ArrayList<>();
		partitions.add(0.0);
		partitions.add(1.0);

		//Step 1. Collect class changing point.
		ArrayList<Double> candidate = new ArrayList<>();
		double point = 0;
		candidate.add(point);
		for(int p = 1; p < patterns.size(); p++) {
			if(patterns.get(p-1).getConClass() != patterns.get(p).getConClass()) {
				point = 0.5 * (patterns.get(p-1).getX() + patterns.get(p).getX());
			}
			if(!candidate.contains(point)) {
				candidate.add(point);
			}
		}
		candidate.remove(0);

		//Step 2. Search K partitions which minimize class-entropy.
		for(int k = 2; k <= K; k++) {
			double[] entropy = new double[candidate.size()];

			//Calculate class-entropy for all candidates.
			for(int i = 0; i < candidate.size(); i++) {
				point = candidate.get(i);

				//Step 1. Count #of patterns in each partition.
				//D_jh means #of patterns which is in partition j and whose class is h.
				double[][] Djh = new double[k][Cnum];
				double[] Dj = new double[k];

				ArrayList<Double> range = new ArrayList<>();
				Collections.sort(partitions);	//Ascending Order
				boolean yetContain = true;
				for(int r = 0; r < partitions.size(); r++) {
					if(yetContain && point < partitions.get(r)) {
						range.add(point);
						yetContain = false;
					}
					range.add(partitions.get(r));
				}
				for(int part = 0; part < k; part++) {
					final double LEFT = range.get(part);
					final double RIGHT = range.get(part+1);
					for(int c = 0; c < Cnum; c++) {
						final int CLASSNUM = c;
						try {
							Optional<Double> partSum = Setting.forkJoinPool.submit( () ->
							patterns.parallelStream()
									.filter(p -> p.getConClass() == CLASSNUM)
									.filter(p -> LEFT <= p.getX() && p.getX() <= RIGHT)
									.map(p -> {
										if(p.getX() == 0.0 || p.getX() == 1.0) {return 1.0;}
										else if(p.getX() == LEFT || p.getX() == RIGHT) {return 0.5;}
										else {return 1.0;}
									})
									.reduce( (l,r) -> l+r)
									).get();
							Djh[part][c] = partSum.orElse(0.0);
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
						//Without Classes
						Dj[part] += Djh[part][c];
					}
				}

				//Step 2. Calculate class-entropy.
				double sum = 0.0;
				for(int j = 0; j < k; j++) {
					double subsum = 0.0;
					for(int h = 0; h < Cnum; h++) {
						if(Dj[j] != 0.0 && (Djh[j][h] / Dj[j]) > 0.0) {
							double log = (Djh[j][h] / Dj[j]) * StaticFunction.log( (Djh[j][h] / Dj[j]), 2.0);
							subsum += (Djh[j][h] / Dj[j]) * StaticFunction.log( (Djh[j][h] / Dj[j]), 2.0);
						}
					}
					sum += (Dj[j] / D) * subsum;
				}
				entropy[i] = -sum;
			}

			//Find minimize class-entropy.
			double min = entropy[0];
			int minIndex = 0;
			for(int i = 1; i < candidate.size(); i++) {
				if(entropy[i] < min) {
					min = entropy[i];
					minIndex = i;
				}
			}
			partitions.add(candidate.get(minIndex));
			candidate.remove(minIndex);
			if(candidate.size() == 0) {
				break;
			}
		}
		Collections.sort(partitions);	//Ascending Order
		return partitions;
	}

	public static ArrayList<double[]> makeTrapezoids(ArrayList<Double> partitions, double F) {
		ArrayList<double[]> trapezoids = new ArrayList<>();

		ArrayList<Double> newPoints = new ArrayList<>();

		//Step 1. Fuzzify each partition without edge of domain.
		for(int i = 1; i < partitions.size() - 1; i++) {
			double left = partitions.get(i - 1);
			double point = partitions.get(i);
			double right = partitions.get(i + 1);
			newPoints.addAll(fuzzify(left, point, right, F));
		}

		//Step 2. Take 4 points as trapezoids in order from head of newPoints.
		newPoints.add(0, 0.0);
		newPoints.add(0, 0.0);
		newPoints.add(1.0);
		newPoints.add(1.0);

		int head = 0;
		int K = (newPoints.size() - 2) / 2;
		for(int i = 0; i < K; i++) {
			double[] trapezoid = new double[4];
			for(int j = 0; j < 4; j++) {
				trapezoid[j] = newPoints.get(head + j);
			}
			trapezoids.add(trapezoid);
			head += 2;
		}

		return trapezoids;
	}

	/**
	 * <h1>Fuzzifying Partition</h1>
	 * Fuzzify two partitions [left, point] and [point, right].<br>
	 *
	 * @param left : double : Domain Left
	 * @param point : double : Crisp Point
	 * @param right : double : Domain Right
	 * @param F : double : Grade of overwraping
	 * @return {@literal ArrayList<Double} : fuzzfied two point
	 */
	public static ArrayList<Double> fuzzify(double left, double point, double right, double F) {
		ArrayList<Double> two = new ArrayList<>();

		//Step 1. Minimize Range (left-point) or (point-right)
		if( (point-left) < (right-point) ) {
			//point is closer to left than right, then right moves.
			right = point + (point-left);
		} else {
			//point is closer to right than left, then left moves.
			left = point - (right-point);
		}

		//Step 2. Make most fuzzified partition and most crisp partition.
		double ac_F0 = point;
		double ac_F1 = 0.5 * (left + point);
		double bd_F0 = point;
		double bd_F1 = 0.5 * (right + point);

		//Step 3. Make F graded fuzzified partition
		double ac_F = ac_F0 + (ac_F1 - ac_F0)*F;
		double bd_F = bd_F0 + (bd_F1 - bd_F0)*F;

		//Step 4. Get Fuzzified two point which has membership value 1.0.
		two.add(ac_F);
		two.add(bd_F);

		return two;
	}


}


class ForSortPattern{
	double x;
	double index;
	int conClass;

	ForSortPattern(double x, int conClass){
		this.x = x;
		this.conClass = conClass;
	}

	double getX(){
		return x;
	}

	int getConClass() {
		return conClass;
	}

	double getIndex() {
		return index;
	}
}
