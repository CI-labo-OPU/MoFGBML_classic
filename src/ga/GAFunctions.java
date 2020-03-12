package ga;

import java.util.ArrayList;

import data.DataSetInfo;
import data.Pattern;
import fgbml.Michigan;
import fgbml.Pittsburgh;
import fgbml.problem.FGBML;
import fuzzy.Rule;
import fuzzy.RuleSet;
import fuzzy.StaticFuzzyFunc;
import main.Consts;
import method.MersenneTwisterFast;
import method.StaticFunction;

public class GAFunctions {

	/**
	 * Uniform Crossover: UX<br>
	 *
	 * @param a : Individual : Parent A
	 * @param b : Individual : Parent B
	 * @param p_u : double : Probability of Uniform Crossover
	 * @param p_X : double : Crossover Probability
	 * @param rnd
	 * @return Integer[] : New Gene Array
	 */
	@SuppressWarnings("rawtypes")
	public static Integer[] uniformCrossover(Individual a, Individual b, double p_X, double p_u, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		int geneNum = a.getGeneNum();
		Integer[] newGene = new Integer[geneNum];

		if(uniqueRnd.nextDoubleIE() < p_X) {
			for(int i = 0; i < geneNum; i++) {
				if(uniqueRnd.nextDoubleIE() < p_u) {
					newGene[i] = (Integer)a.getGene(i);
				} else {
					newGene[i] = (Integer)b.getGene(i);
				}
			}
		} else {//Non-Crossover
			Individual parent;
			if(uniqueRnd.nextBoolean()) {
				parent = a;
			} else {
				parent = b;
			}
			for(int i = 0; i < geneNum; i++) {
				newGene[i] = (Integer)parent.getGene(i);
			}
		}

		return newGene;
	}

	/**
	 * <h1>Tournament Selection for Single-Objective Optimization</h1>
	 * @param P : Population
	 * @param optimizer : int : minimize: 1, maximize: -1
	 * @param tournamentSize
	 * @param rnd
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Individual[] tournamentSelection(Population P, int optimizer, int tournamentSize, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		int popSize = P.getIndividuals().size();
		Individual[] parents = new Individual[2];

		Individual winner;
		Individual candidate;

		for(int i = 0; i < parents.length; i++) {
			winner = (Individual)P.getIndividual(uniqueRnd.nextInt(popSize));
			for(int j = 1; j < tournamentSize; j++) {
				candidate = (Individual)P.getIndividual(uniqueRnd.nextInt(popSize));
				if(optimizer*candidate.getFitness(0) > optimizer*winner.getFitness(0)) {
					winner = candidate;
				}
			}
			parents[i] = winner;
		}

		return parents;
	}

	/**
	 *
	 * @param gene
	 * @param pm : double : Mutation Rate
	 * @param rnd
	 */
	public static void bitFlipMutation(Integer[] gene, double pm, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		for(int i = 0; i < gene.length; i++) {
			if(uniqueRnd.nextDoubleIE() < pm) {
				if(gene[i] == 0) gene[i] = 1;
				else gene[i] = 0;
			}
		}
	}

	/**
	 * <h1>Michigan type Genetic Algorithm for FGBML</h1>
	 * Note: 新しく生成したルールについて結論部の学習は行われない．<br>
	 * Note: This method for Single-Label Classification
	 * @param mop : FGBML :
	 * @param parent : Pittsburgh : 親個体
	 * @param rnd
	 * @return Pittsburgh : 生成された子個体
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Pittsburgh michiganCrossover( FGBML mop, Pittsburgh parent, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		Pittsburgh child = parent.newInstance();
		child.deepCopy(parent);

		//Step 1. Number of All of Generating rules
		//20% or only 1.
		int newNum = 0;
		if(Consts.RATE_OR_ONLY) {
			newNum = 1;
		} else {
			newNum = (int)((child.getRuleNum() - 0.00001) * Consts.RULE_CHANGE_RT) + 1;
		}

		//Step 2. Numbers of GA and Heuristic Generating rules
		int heuNum = 0;
		if(newNum % 2 == 0) {
			heuNum = newNum/2;
		} else {
			int plus = uniqueRnd.nextInt(2);
			heuNum = (newNum-1)/2 + plus;
		}

		Michigan[] michigan = new Michigan[newNum];

		//Step 3. Heuristic Rule Generation
		//誤識別パターンが足りない or 無い場合はランダムなパターンをRuleSet.missPatternsリストに追加
		int lack = heuNum - parent.getRuleSet().getMissPatterns().size();
		for(int i = 0; i < lack; i++) {
			parent.getRuleSet().getMissPatterns().add(
							((Pattern)mop.getTrain().getPattern(
							uniqueRnd.nextInt(mop.getTrain().getDataSize())
							)).getID()
							);
		}
		//保持していた誤識別パターンからヒューリスティック生成に使用するパターンを非復元抽出
		Integer[] missPatternsIdx = StaticFunction.sampringWithout(parent.getRuleSet().getMissPatterns().size(), heuNum, uniqueRnd);
		for(int i = 0; i < heuNum; i++) {
			michigan[i] = parent.getMichigan(0).newInstance(parent.getNdim(), parent.getObjectiveNum());
			michigan[i].initHeuristic((Pattern)mop.getTrain().getPatternWithID(parent.getRuleSet().getMissPattern(missPatternsIdx[i])), uniqueRnd);
			michigan[i].gene2rule();
		}
		//足りない場合に追加していたランダムパターンを削除しておく
		for(int i = 0; i < lack; i++) {
			parent.getRuleSet().getMissPatterns().remove(parent.getRuleSet().getMissPatterns().size() - 1);
		}

		//Step 4. GA Rule Generation - Michigan Type GA
		//Tournament Selcection用Population
		Population<Michigan> P = new Population<>();
		for(int i = 0; i < parent.getRuleNum(); i++) {
			P.addIndividual(parent.getMichigan(i));
		}
		for(int i = heuNum; i < newNum; i++) {
			//Step 1. Matting Selection
			int tournamentSize = 2;
			//Single-objective tournament selection by rule fitness (times of being chosen winner)
			Individual[] parents = GAFunctions.tournamentSelection(P, Consts.MAXIMIZE, tournamentSize, uniqueRnd);

			//Step 2. Crossover (Uniform Crossover)
			double p_X = Consts.RULE_CROSS_RT;	//Crossover Rate
			double p_u = 0.5;					//UX Rate
			Integer[] gene = GAFunctions.uniformCrossover(parents[0], parents[1], p_X, p_u, uniqueRnd);

			//Step 3. Mutation (changing to another fuzzy set)
			double pm = 1.0 / (double)gene.length;	// (1/Ndim)
			GAFunctions.michiganMutation(gene, pm, mop.getTrain(), uniqueRnd);

			//Step 4. Generate a new rule
			michigan[i] = parent.getMichigan(0).newInstance(parent.getNdim(), parent.getObjectiveNum());
			michigan[i].setGene(gene);
			michigan[i].gene2rule();
		}

		//Step 5. RuleSet Update
		child.sortMichiganByFitness();	//Sorting by Rule Fitness

		//Add generated rules.
		if(Consts.DO_ADD_RULES) {
			int replaceNum = 0;
			if(Consts.MAX_RULE_NUM < (child.getRuleNum() + newNum)) {
				replaceNum = (child.getRuleNum() + newNum) - Consts.MAX_RULE_NUM;
			}
			//Replace rule from bottom of list.
			for(int i = 0; i < replaceNum; i++) {
				child.getRuleSet().setMicRule((child.getRuleNum()-1) - i, michigan[i].getRule());
			}
			//Add rules
			for(int i = replaceNum; i < newNum; i++) {
				child.getRuleSet().addRule(michigan[i].getRule());
			}
		}
		// Replace rules by fitness values.
		else {
			for(int i = 0; i < newNum; i++) {
				//Replace rule from bottom of list.
				child.getRuleSet().setMicRule((child.getRuleNum()-1) - i, michigan[i].getRule());
			}
		}

		child.ruleset2michigan();	//Michigan Individual
		child.michigan2pittsburgh();	//Pittsburgh Individual

		return child;
	}

	/**
	 * <h1>Pittsburgh type Genetic Algorithm for FGBML</h1>
	 * Note: This method for Single-Label Classification.
	 *
	 * @param parent : Pittsburgh[] : Two Parents
	 * @param rnd
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Pittsburgh pittsburghCrossover(Pittsburgh[] parent, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());

		Pittsburgh mom = parent[0];	//Shallow Copy
		Pittsburgh dad = parent[1];	//Shallow Copy

		Pittsburgh child = null;

		//Do Crossover
		if(uniqueRnd.nextDouble() < Consts.RULESET_CROSS_RT) {
			/** #of rules inherited from MOM */
			int Nmom = uniqueRnd.nextInt(mom.getRuleNum()) + 1;
			/** #of rules inherited from DAD */
			int Ndad = uniqueRnd.nextInt(dad.getRuleNum()) + 1;

			//Reducing excess of rules
			if( (Nmom + Ndad) > Consts.MAX_RULE_NUM) {
				int delNum = (Nmom + Ndad) - Consts.MAX_RULE_NUM;
				for(int i = 0; i < delNum; i++) {
					if(uniqueRnd.nextBoolean()) {
						Nmom--;
					}
					else {
						Ndad--;
					}
				}
			}

			//Instance
			child = mom.newInstance(mom.getNdim(), (Nmom + Ndad) ,mom.getObjectiveNum());

			//Select inheriting rules
			Integer[] indexMom = StaticFunction.sampringWithout(mom.getRuleNum(), Nmom, uniqueRnd);
			Integer[] indexDad = StaticFunction.sampringWithout(dad.getRuleNum(), Ndad, uniqueRnd);

			//Inheriting
			RuleSet ruleSet = (RuleSet)mom.getRuleSet().newInstance();
			for(int i = 0; i < Nmom; i++) {
				Rule rule = mom.getRuleSet().getMicRule(0).newInstance();
				rule.deepCopy(mom.getRuleSet().getMicRule(indexMom[i]));
				ruleSet.addRule(rule);
			}
			for(int i = 0; i < Ndad; i++) {
				Rule rule = dad.getRuleSet().getMicRule(0).newInstance();
				rule.deepCopy(dad.getRuleSet().getMicRule(indexDad[i]));
				ruleSet.addRule(rule);
			}
			ruleSet.calcRuleLength();
			child.setRuleSet(ruleSet);

			child.ruleset2michigan();	//Michigan Individual
			child.michigan2pittsburgh();	//Pittsburgh Individual
		}

		//Don't Crossover
		else {
			if(uniqueRnd.nextBoolean()) {
				child = mom.newInstance(mom);	//Deep Copy
			} else {
				child = dad.newInstance(dad);	//Deep Copy
			}
		}

		return child;
	}

	/**
	 * <h1>Michigan Type Mutation</h1>
	 * @param gene : Integer[]
	 * @param pm : double : Mutation Rate
	 * @param rnd
	 */
	@SuppressWarnings("rawtypes")
	public static void michiganMutation(Integer[] gene, double pm, DataSetInfo dataset, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < gene.length; i++) {
			if(uniqueRnd.nextDouble() < pm) {
				//To judge which attribute i is categorical or numerical.
				double randPattern = ((Pattern)dataset.getPattern(uniqueRnd.nextInt(dataset.getDataSize()))).getDimValue(i);

				if(randPattern >= 0.0) {
					//Attribute i is Numerical
					list.clear();
					int fuzzySetNum = StaticFuzzyFunc.kb.getFSs(i).length;	//Including "don't care"
					//make list
					for(int j = 0; j < fuzzySetNum; j++) {
						if(j != gene[i]) {
							list.add(j);
						}
					}
					//mutation
					int newFuzzySet = list.get( uniqueRnd.nextInt(list.size()) );
					gene[i] = newFuzzySet;
				} else {
					//Attribute i is Categorical
					gene[i] = (int)randPattern;
				}

			}
		}
	}

	/**
	 * <h1>Pittsburgh Type Mutation</h1>
	 * @param individual : Pittsburgh : Objective Individual
	 * @param rnd
	 */
	@SuppressWarnings("rawtypes")
	public static void pittsburghMutation(Pittsburgh individual, DataSetInfo dataset, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		ArrayList<Integer> list = new ArrayList<Integer>();

		int ruleNum = individual.getRuleNum();
		for(int i = 0; i < ruleNum; i++) {
			// probability = 1/ruleNum
			if(uniqueRnd.nextInt(ruleNum) == 0) {

				//Objective Dimension
				int mutationDim = uniqueRnd.nextInt(individual.getNdim());

				//To judge which attribute i is categorical or numerical.
				double randPattern = ((Pattern)dataset.getPattern(uniqueRnd.nextInt(dataset.getDataSize()))).getDimValue(mutationDim);

				if(randPattern >= 0.0) {
					//Attribute mutationDim is Numeric.

					//#of Defined Fuzzy Sets at mutationDim
					int fuzzySetNum = StaticFuzzyFunc.kb.getFSs(mutationDim).length;
					//make List
					list.clear();
					for(int j = 0; j < fuzzySetNum; j++) {
						if(j != individual.getRuleSet().getMicRule(i).getRule(mutationDim)) {
							list.add(j);
						}
					}
					//mutation
					int newFuzzySet = list.get( uniqueRnd.nextInt(list.size()) );
					individual.getRuleSet().getMicRule(i).setRule(mutationDim, newFuzzySet);
				} else {
					//Attribute mutationDim is Categoric.
					individual.getRuleSet().getMicRule(i).setRule(mutationDim, (int)randPattern);
				}

			}
		}

	}

}
