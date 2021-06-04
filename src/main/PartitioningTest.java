package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import data.Input;
import data.SingleDataSetInfo;
import fuzzy.FuzzyPartitioning;
import method.Output;
import method.StaticFunction;

public class PartitioningTest {


	public static void main(String[] args) {
		int core = 4;
		Setting.forkJoinPool = new ForkJoinPool(core);

		String sep = File.separator;
		String dataset = "phoneme";
		String dataFile = "dataset" + sep + dataset + sep + "a0_0_"+dataset+"-10tra.dat";
		SingleDataSetInfo tra = new SingleDataSetInfo();
		Input.inputFile(tra, dataFile);


		int K = 5;
		double F = 0.0;
		ArrayList<ArrayList<double[]>> trapezoids = new ArrayList<ArrayList<double[]>>();
		ArrayList<ArrayList<Double>> splits = new ArrayList<>();
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
			splits.add(partitions);

			//Step 3. Fuzzify partitions
			trapezoids.add(FuzzyPartitioning.makeTrapezoids(partitions, F));
		}

		output(dataset, F, splits, trapezoids);
		System.out.println();

	}

	public static void output(String dataset, double F, ArrayList<ArrayList<Double>> splits, ArrayList<ArrayList<double[]>> trapezoids) {
		String sep = File.separator;
		String c = ",";
		int Ndim = splits.size();

		String fileName;
		String str;
		ArrayList<String> strs;

		//Splits
		strs = new ArrayList<>();
		str = "";
		for(int i = 0; i < Ndim; i++) {
			str = "dim" + i;
			ArrayList<Double> split = splits.get(i);
			for(int j = 0; j < split.size(); j++) {
				str += c + split.get(j);
			}
			strs.add(str);
		}
		fileName = "result" + sep + "partitioningTest" + sep + dataset+"_split.csv";
		Output.writeln(fileName, strs);

		//Trapezoids
		strs = new ArrayList<>();
		str = "";
		//header
		str = "dim,k,a,b,c,d";
		strs.add(str);
		//data
		for(int i = 0; i < Ndim; i++) {
			ArrayList<double[]> trapezoid = trapezoids.get(i);
			int K = trapezoid.size();
			for(int k = 0; k < K; k++) {
				str = "";
				str += i + c + k;
				double[] membership = trapezoid.get(k);
				for(int j = 0; j < membership.length; j++) {
					str += c + membership[j];
				}
				strs.add(str);
			}
		}
		fileName = "result" + sep + "partitioningTest" + sep + dataset+"_trapezoid_F"+F+".csv";
		Output.writeln(fileName, strs);

	}

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
