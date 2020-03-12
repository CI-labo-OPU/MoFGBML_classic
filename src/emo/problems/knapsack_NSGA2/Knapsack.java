package emo.problems.knapsack_NSGA2;

import java.util.ArrayList;
import java.util.List;

import emo.problems.MOP;
import method.MersenneTwisterFast;
import method.Output;

public class Knapsack extends MOP<Individual_knapsack> {
	// ************************************************************
	MersenneTwisterFast uniqueRnd;

	int itemNum;
	int constNum;

	double[][] profit;
	double[][] weight;

	double[] capacity;

	double[] throwPreference;

	// ************************************************************
	public Knapsack(int objectiveNum, int itemNum, MersenneTwisterFast rnd) {
		this.uniqueRnd = new MersenneTwisterFast(rnd.nextInt());

		this.objectiveNum = objectiveNum;
		this.constNum = objectiveNum;
		this.itemNum = itemNum;

		profit = new double[objectiveNum][itemNum];
		weight = new double[objectiveNum][itemNum];
		capacity = new double[objectiveNum];
	}

	// ************************************************************
	/**
	 * Defining each item's profit and weight.<br>
	 * Also defining the capacities of knapsacks.<br>
	 */
	public void init() {
		double min = 10;
		double max = 100;

		for(int o = 0; o < objectiveNum; o++) {
			double sum = 0;
			for(int i = 0; i < itemNum; i++) {
				profit[o][i] = min + (uniqueRnd.nextDoubleII() * (max - min));
				weight[o][i] = min + (uniqueRnd.nextDoubleII() * (max - min));

				sum += weight[o][i];
			}
			capacity[o] = sum * 0.5;
		}

		throwPreference = new double[itemNum];
		for(int i = 0; i < itemNum; i++) {
			max = -Double.MAX_VALUE;
			for(int o = 0; o < objectiveNum; o++) {
				double q = profit[o][i] / weight[o][i];
				if(max < q) {
					max = q;
				}
			}
			throwPreference[i] = max;
		}
	}

	public void initFromData() {
		String fileName = "KP.dat";
		List<double[]> lines = Output.input(fileName);

		int j = 0;
		for(int o = 0; o < 2; o++) {
			for(int i = 0; i < 500; i++) {
				weight[o][i] = lines.get(j)[0];
				profit[o][i] = lines.get(j + 1)[0];
				j += 2;
			}

		}

		for(int o = 0; o < objectiveNum; o++) {
			double sum = 0;
			for(int i = 0; i < itemNum; i++) {
				sum += weight[o][i];
			}
			capacity[o] = sum * 0.5;
		}

		throwPreference = new double[itemNum];
		for(int i = 0; i < itemNum; i++) {
			double max = -Double.MAX_VALUE;
			for(int o = 0; o < objectiveNum; o++) {
				double q = profit[o][i] / weight[o][i];
				if(max < q) {
					max = q;
				}
			}
			throwPreference[i] = max;
		}
	}

	public boolean geneFeasible(Integer[] gene) {
		for(int o = 0; o < objectiveNum; o++) {
			double sum = 0;
			for(int i = 0; i < itemNum; i++) {
				if(gene[i] == 1) {
					sum += weight[o][i];
				}
			}
			if(sum > capacity[o]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void evaluate(Individual_knapsack individual) {
		double[] fitness = new double[objectiveNum];
		double[] constraintValue = new double[objectiveNum];
		double constraint = 0;

		for(int o = 0; o < objectiveNum; o++) {
			double sumWeight = 0;
			fitness[o] = 0;

			for(int i = 0; i < itemNum; i++) {
				fitness[o] += profit[o][i] * (double)individual.getGene(i);
				sumWeight += weight[o][i] * (double)individual.getGene(i);
			}

			if(sumWeight > capacity[o]) {
				individual.setFeasible(false);
				constraintValue[o] = sumWeight-capacity[o];
				constraint += constraintValue[o];
			}

		}

		individual.setFitness(fitness);
		individual.setConstraint(constraintValue);
		if(constraint > 0) {
			individual.setFeasible(false);
		} else {
			individual.setFeasible(true);
		}
		individual.setSumConst(constraint);
	}

	public void evaluateParallel(Individual_knapsack individual) {

	}

	/**
	 * Output this problem.
	 * @param fileName String
	 */
	public void outputProblem(String fileName) {
		String c = ",";
		String str;
		ArrayList<String> strs = new ArrayList<String>();

		for(int o = 0; o < objectiveNum; o++) {
			str = "object_" + String.valueOf(o);
			strs.add(str);

			str = "capacity_" + String.valueOf(o);
			str += c + String.valueOf(capacity[o]);
			strs.add(str);

			str = "";
			for(int i = 0; i < itemNum; i++) {
				str += c + "item" + String.valueOf(i);
			}
			strs.add(str);

			str = "profit";
			for(int i = 0; i < itemNum; i++) {
				str += c + String.valueOf(profit[o][i]);
			}
			strs.add(str);

			str = "weight";
			for(int i = 0; i < itemNum; i++) {
				str += c + String.valueOf(weight[o][i]);
			}
			strs.add(str);

			strs.add("");
		}

		Output.writeln(fileName, strs);

	}

	public double[][] getProfit(){
		return this.profit;
	}

	public double[][] getWeight(){
		return this.weight;
	}

	public double[] getThrowPreference() {
		return this.throwPreference;
	}

}
