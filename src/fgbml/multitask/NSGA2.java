package fgbml.multitask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import emo.algorithms.nsga2.Individual_nsga2;
import ga.Individual;
import ga.Population;
import ga.PopulationManager;
import main.Setting;
import method.MersenneTwisterFast;
import method.Sort;

@SuppressWarnings("rawtypes")
public class NSGA2 {
	/**
	 * 各世代で子個体生成後にNSGA-II MainFrameを呼び出す．<br>
	 * mainFrame実行後，managerのpopulationが更新されている．<br>
	 *
	 * @param manager
	 * @param optimizer :  minimize: 1, maximize: -1
	 * @param rnd
	 */
	@SuppressWarnings({ "unchecked" })
	public static void mainFrame(PopulationManager manager, int[] optimizer) {
		Population<Individual_nsga2> margePopulation = new Population<Individual_nsga2>();
		//Add Population
		int popSize = Setting.populationSize;
		for(int i = 0; i < manager.getPopulation().getIndividuals().size(); i++) {
			margePopulation.addIndividual((Individual_nsga2)((Population)manager.getPopulation()).getIndividual(i));
		}

		//Add Offspring
		int offspringSize = Setting.offspringSize;
		for(int i = 0; i < manager.getOffspring().getIndividuals().size(); i++) {
			margePopulation.addIndividual((Individual_nsga2)((Population)manager.getOffspring()).getIndividual(i));
		}

		//Set UniqueID
		for(int i = 0; i < margePopulation.getIndividuals().size(); i++) {
			margePopulation.getIndividual(i).setID(i);
		}

		ArrayList<ArrayList<Individual_nsga2>> F_ = nonDominatedSort(margePopulation, optimizer);
		//Assignment Crowding Distance in Each Front
		for(int i = 0; i < F_.size(); i++) {
			crowdingDistanceAssignment(F_.get(i));
		}
		int i = 0;	//Current Front
		Population<Individual_nsga2> nextPopulation = new Population<Individual_nsga2>();
		while((nextPopulation.getIndividuals().size() + F_.get(i).size()) <= popSize) {
			for(int j = 0; j < F_.get(i).size(); j++) {
				nextPopulation.addIndividual(F_.get(i).get(j));
			}
			i++;
		}

		//Sort F_i by NSGA-II Selection Criteria
		Collections.sort(F_.get(i), new Comparator<Individual_nsga2>() {
			@Override
			public int compare(Individual_nsga2 o1, Individual_nsga2 o2) {
				if(selectionCriteria(o1, o2)) {
					return -1;
				} else {
					return 1;
				}
			}
		});

		//Population Update
		int addNum = popSize - nextPopulation.getIndividuals().size();
		ArrayList<Individual_nsga2> reCrowding = new ArrayList<Individual_nsga2>();
		for(int p = 0; p < addNum; p++) {
			reCrowding.add(F_.get(i).get(p));
		}
		//Reassignment Crowding Distance
		crowdingDistanceAssignment(reCrowding);
		for(int p = 0; p < addNum; p++) {
			nextPopulation.addIndividual(reCrowding.get(p));
		}

		manager.setPopulation(nextPopulation);
	}

	/**
	 * <h1>Mating Selection by Tournament Selection</h1>
	 * Selected Two Parents from P by NSGA-II Criteria<br>
	 * @param P : Population
	 * @param parentSize : int : #of parent
	 * @param tournamentSize : int : #of candidate
	 * @param rnd
	 * @return : Individual_nsga2[] : Chosen Parents
	 */
	public static Individual_nsga2[] tournamentSelection(Population P, int parentSize, int tournamentSize, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		int popSize = P.getIndividuals().size();
		Individual_nsga2[] parents = new Individual_nsga2[parentSize];

		Individual_nsga2 winner;
		Individual_nsga2 candidate;

		for(int i = 0; i < parents.length; i++) {
			winner = (Individual_nsga2)P.getIndividual(uniqueRnd.nextInt(popSize));
			for(int j = 1; j < tournamentSize; j++) {
				candidate = (Individual_nsga2)P.getIndividual(uniqueRnd.nextInt(popSize));
				if(selectionCriteria(candidate, winner)) {
					winner = candidate;
				}
			}
			parents[i] = winner;
		}
		return parents;
	}

	/**
	 * Selection Criteria with NSGA-II specific parameters, rank and crowding.<br>
	 * First criteria is that the Pareto rank is better.<br>
	 * Second criteria is Crowded-Comparison Operater.<br>
	 * 	The operator is composed two criteria.<br>
	 *	First, lower Pareto rank is better.<br>
	 *	Second (if Pareto rank is same), higher crowding distance is better.<br>
	 *
	 * @param a
	 * @param b
	 * @return true: winner a, false: winner b.
	 */
	public static boolean selectionCriteria(Individual_nsga2 a, Individual_nsga2 b) {
		boolean winner = true;
		if(a.getRank() < b.getRank()) {
			winner = true;
		} else if(a.getRank() > b.getRank()) {
			winner = false;
		} else {
			if(a.getCrowding() > b.getCrowding()) {
				winner = true;
			} else if(a.getCrowding() < b.getCrowding()) {
				winner = false;
			} else {// a.crowding == b.crowding
				if(a.getID() < b.getID()) {
					winner = true;
				} else {
					winner = false;
				}
			}
		}

		return winner;
	}

	/**
	 * Assignment Rank for Each Individual
	 *
	 * @param P
	 * @param optimizer :  minimize: 1, maximize: -1
	 * @return
	 * Sets of solutions in each front.
	 */
	public static ArrayList<ArrayList<Individual_nsga2>> nonDominatedSort(Population P, int[] optimizer) {
		int popSize = P.getIndividuals().size();

		//The number of solutions that dominate p
		int[] n_ = new int[popSize];

		//Individual q in "S_p" is dominated by p
		@SuppressWarnings("unchecked")
		ArrayList<Integer>[] S_ = new ArrayList[popSize];

		//The set of solutions in Rank i.
		ArrayList<ArrayList<Integer>> F_ = new ArrayList<ArrayList<Integer>>();
		F_.add(new ArrayList<Integer>());	//Initialize the first front

		ArrayList<ArrayList<Individual_nsga2>> FF_ = new ArrayList<ArrayList<Individual_nsga2>>();
		FF_.add(new ArrayList<Individual_nsga2>());

		//for each p in pop
		for(int p = 0; p < popSize; p++) {
			S_[p] = new ArrayList<Integer>();
			n_[p] = 0;

			//for each q in pop
			for(int q = 0; q < popSize; q++) {
				if(p == q) continue;

				if(isDominate((Individual)P.getIndividual(p), (Individual)P.getIndividual(q), optimizer)) {
					//Is p dominating q?
					S_[p].add(q);
				}
				else if(isDominate((Individual)P.getIndividual(q), (Individual)P.getIndividual(p), optimizer)) {
					//Is p dominated by q?
					n_[p]++;
				}
			}
			if(n_[p] == 0) {
				((Individual_nsga2)P.getIndividual(p)).setRank(0);
				F_.get(0).add(p);
				FF_.get(0).add((Individual_nsga2)P.getIndividual(p));
			}
		}

		int i = 0; //Initialize the front counter
		while(F_.get(i).size() != 0) {
			F_.add(new ArrayList<Integer>());	//new Front produced
			FF_.add(new ArrayList<Individual_nsga2>());
			for(int pp = 0; pp < F_.get(i).size(); pp++) {
				//Index p from original population P
				int p = F_.get(i).get(pp);

				for(int qq = 0; qq < S_[p].size(); qq++) {
					//Index q from original population P
					int q = S_[p].get(qq);
					n_[q]--;

					if(n_[q] == 0) {
						((Individual_nsga2)P.getIndividual(q)).setRank(i + 1);
						F_.get(i+1).add(q);
						FF_.get(i+1).add((Individual_nsga2)P.getIndividual(q));
					}
				}
			}
			i++;
		}

		return FF_;
	}

	/**
	 * Assignment Crowding Distance for Each Individual
	 * @param P : ArrayList{@literal <Individual_nsga2>} : Individuals which have same ranks. (Population in same front)
	 */
	public static void crowdingDistanceAssignment(ArrayList<Individual_nsga2> P) {
		int popSize = P.size();
		if(popSize == 0) {
			return;
		}

		//Initialize Distance
		for(int i = 0; i < popSize; i++) {
			P.get(i).setCrowding(0.0);
		}

		int objective = P.get(0).getObjectiveNum();
		for(int o = 0; o < objective; o++) {

			double max = Double.NEGATIVE_INFINITY;
			double min = Double.POSITIVE_INFINITY;

			double[] fitness = new double[popSize];
			int[] order = new int[popSize];
			for(int p = 0; p < popSize; p++) {
				order[p] = p;
				fitness[p] = P.get(p).getFitness(o);
				if(max < fitness[p] ){
					max = fitness[p];
				}
				if(min > fitness[p]) {
					min = fitness[p];
				}
			}
			order = Sort.sort(fitness, 0);

			P.get(order[0]).addCrowding(Double.POSITIVE_INFINITY);
			P.get(order[order.length-1]).addCrowding(Double.POSITIVE_INFINITY);

			//If all fitness value in objective o is same
			boolean sameAll = false;
			for(int i = 1; i < fitness.length; i++) {
				if(fitness[i-1] == fitness[i]) {
					sameAll = true;
				} else {
					sameAll = false;
					break;
				}
			}
			for(int i = 1; i < popSize - 1; i++) {
				if(sameAll) {
					double distance = 0;
					P.get(order[i]).addCrowding(distance);
				} else {
					double distance = (P.get(order[i+1]).getFitness(o) - P.get(order[i-1]).getFitness(o)) / (max - min);
					P.get(order[i]).addCrowding(distance);
				}
			}

		}

	}

	/**
	 * @param optimizer : minimize: 1, maximize: -1
	 * @return boolean : Is p dominating q ?
	 */
	public static  boolean isDominate(Individual p, Individual q, int[] optimizer) {
		boolean isDominate = false;

		for(int o = 0; o < optimizer.length; o++) {
			if(optimizer[o] * p.getFitness(o) > optimizer[o] * q.getFitness(o)) {
				isDominate = false;
				break;
			}
			else if(optimizer[o] * p.getFitness(o) < optimizer[o] * q.getFitness(o)) {
				isDominate = true;
			}
		}

		return isDominate;
	}
}
