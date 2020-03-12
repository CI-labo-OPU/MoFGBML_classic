package emo.problems.knapsack_NSGA2;

import java.util.ArrayList;

import ga.Population;
import method.Output;

public class Output_knapsack {

	public static void population(String fileName, Population<Individual_knapsack> population) {
		String c = ",";
		String str = "";
		ArrayList<String> strs = new ArrayList<String>();

		int popSize = population.getIndividuals().size();
		int geneNum = population.getIndividual(0).getGeneNum();
		int objectiveNum = population.getIndividual(0).getObjectiveNum();

		//pop,f0,f1,rank,c0,c1,crowding,feasible,x0,x1,x2,...
		str += "pop" + c;
		for(int o = 0; o < objectiveNum; o++) {
			str += "f" + String.valueOf(o) + c;
		}
		str += "rank" + c;
		for(int o = 0; o < objectiveNum; o++) {
			str += "c" + String.valueOf(o) + c;
		}
		str += "crowding" + c;
		str += "feasible" + c;
		for(int i = 0; i < geneNum; i++) {
			str += "x" + String.valueOf(i) + c;
		}
		strs.add(str);

		for(int p = 0; p < popSize; p++) {
			str = "";
			//pop
			str += String.valueOf(p);
			//f_i
			for(int o = 0; o < objectiveNum; o++) {
				str += c + String.valueOf(population.getIndividual(p).getFitness(o));
			}
			//rank
			str += c + String.valueOf(population.getIndividual(p).getRank());
			//constraint value
			for(int o = 0; o < objectiveNum; o++) {
				str += c + String.valueOf(population.getIndividual(p).getConstraint(o));
			}
			//crowding distance
			str += c + String.valueOf(population.getIndividual(p).getCrowding());
			//feasible
			str += c + String.valueOf(population.getIndividual(p).isFeasible());
			//x_i
			for(int i = 0; i < geneNum; i++) {
				str += c + String.valueOf(population.getIndividual(p).getGene(i));
			}

			strs.add(str);
		}

		Output.writeln(fileName, strs);

	}

}
