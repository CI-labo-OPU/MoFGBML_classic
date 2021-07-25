package fgbml.multitask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import emo.algorithms.nsga2.Individual_nsga2;
import fgbml.Pittsburgh;
import ga.GAFunctions;
import ga.Population;
import ga.PopulationManager;
import main.Consts;
import main.Setting;
import method.MersenneTwisterFast;
import method.StaticFunction;

@SuppressWarnings("rawtypes")
public class Task {
	// ************************************************************
	// Task ID
	int taskID;

	// MOP
	Problem_MultiLabel mop;

	// Population
	PopulationManager<Population<MultiPittsburgh>> manager = new PopulationManager<>();

	// ************************************************************
	public Task(int taskID, Problem_MultiLabel mop) {
		this.taskID = taskID;
		this.mop = mop;
	}

	// ************************************************************

	public Population initialize(MersenneTwisterFast rnd) {
		int Ndim = mop.getTrain().getNdim();

		Population<MultiPittsburgh> population = new Population<>();

		for(int pop = 0; pop < Setting.populationSize; pop++) {
			// 個体生成
			MultiPittsburgh individual = (MultiPittsburgh)(new MultiPittsburgh()).newInstance(Ndim, Consts.INITIATION_RULE_NUM, mop.getObjectiveNum());

			//ルール集合生成
			while(true) {
				//RuleSet Generation
				if(Consts.DO_HEURISTIC_GENERATION) {
					//Heuristic Rule Generation
					individual.initHeuristic(mop.getTrain(), rnd);
				}
				else {
					//Random Antecedent Generation
					individual.initRand(rnd);
				}

				//Consequent Part Learning
				individual.getRuleSet().learning(mop.getTrain(), Setting.forkJoinPool);

				//Reducing Same Rules
				individual.getRuleSet().removeRule();
				individual.getRuleSet().radixSort();
				individual.getRuleSet().calcRuleLength();
				if(individual.getRuleSet().getMicRules().size() != 0) {
					break;
				}
			}

			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
			individual.initAppendix(mop.getAppendixNum());
			population.addIndividual(individual);
		}

		this.manager.setPopulation(population);

		return this.getPopulation();
	}

	public Population populationEvaluation() {
		manager.populationEvaluation(this.mop);
		return this.getPopulation();
	}

	public Population offspringEvaluation() {
		manager.offspringEvaluation(this.mop);
		return this.getOffspring();
	}

	public void nonDominatedSort(Population<MultiPittsburgh> population) {
		ArrayList<ArrayList<Individual_nsga2>> F_ = NSGA2.nonDominatedSort(population, this.mop.getOptimizer());
		for(int i = 0; i < F_.size(); i++) {
			NSGA2.crowdingDistanceAssignment(F_.get(i));
		}
	}

	public Population normalMakeOffspring(MersenneTwisterFast rnd) {
		Population<MultiPittsburgh> offspring = new Population<>();
		for(int q = 0; q < Setting.offspringSize; q++) {
			MultiPittsburgh child = null;

			while(true) {

				/* ************************************************************************ */
				//Step 1. Mating Selection
				int tournamentSize = 2;
				int parentSize = 2;
				Individual_nsga2[] parent = NSGA2.tournamentSelection(manager.getPopulation(), parentSize, tournamentSize, rnd);

				//TODO ピッツバーグとミシガンの選ばれた回数カウント

				//Step 2. Crossover
					//GA type Selection (Michigan or Pittsburgh)
				if(rnd.nextDouble() < (double)Consts.RULE_OPE_RT) {
					//Michigan Type Crossover (Child Generation)
					child = (MultiPittsburgh)GAFunctions.michiganCrossover(mop, (Pittsburgh)parent[0], rnd);
				} else {
					//TODO 同じ親個体（決定変数が等しい）の場合、強制でミシガン操作
					//TODO 同じ親個体が選ばれた回数カウント

					//Pittsburgh Type Crossover (Child Generation)
					Pittsburgh[] cast = new Pittsburgh[parentSize];
					for(int i = 0; i < parentSize; i++) {
						cast[i] = (Pittsburgh)parent[i];	//Shallow Copy
					}
					child = (MultiPittsburgh)GAFunctions.pittsburghCrossover(cast, rnd);
				}

				//Step 3. Mutation
				GAFunctions.pittsburghMutation(child, mop.getTrain(), rnd);
				/* ************************************************************************ */


				//Step 4. Learning
				child.getRuleSet().learning(mop.getTrain(), Setting.forkJoinPool);

				//Step 5. Rule Deletion
				child.getRuleSet().removeRule();
				child.getRuleSet().radixSort();
				child.getRuleSet().calcRuleLength();

				//If child don't have any rule, the child should not be evaluated.
				//Then new child will be generated.
				if(child.getRuleSet().getMicRules().size() != 0) {
					break;
				}
			}

			child.ruleset2michigan();
			child.michigan2pittsburgh();
			child.initAppendix(this.mop.getAppendixNum());
			offspring.addIndividual(child);
		}

		this.manager.setOffspring(offspring);
		return this.getOffspring();
	}

	public void environmentalSelection() {
		NSGA2.mainFrame(this.manager, this.mop.getOptimizer());
	}

	public void evaluationInThisTask(MultiPittsburgh individual) {
		this.mop.evaluate(individual);
	}

	@SuppressWarnings("unchecked")
	public Population<MultiPittsburgh> makeMatingPool(TaskManager world, MersenneTwisterFast rnd) {
		Population<MultiPittsburgh> matingPool = new Population<>();
		int taskNum = world.getTaskNum();

		//1. 自タスクの個体群から (Setting.popSize - (taskNum-1)*world.getNumMigration() )だけ
		//   トーナメント選択でmatingPoolに個体を追加
		Population<MultiPittsburgh> popInThisTask = this.getPopulation();	//自タスクの個体群取得
		int num = Setting.populationSize - (world.getTaskNum()-1)*world.getNumMigration();
		for(int j = 0; j < num; j++) {
			// トーナメント選択
			int tournamentSize = 2;
			int getSize = 1;
			Individual_nsga2[] chosen = NSGA2.tournamentSelection(popInThisTask, getSize, tournamentSize, rnd);
			MultiPittsburgh individual = (MultiPittsburgh)chosen[0];
			// 選択した個体をmatingPoolに追加
			matingPool.addIndividual(individual);
		}

		//2. 他タスクからnumMigrationだけ複雑順に個体を選択して、
		//   自タスクで評価して、matingPoolに入れる
		for(int i = 0; i < taskNum; i++) {
			if(i == this.taskID) {
				continue;
			}
			Task task = world.getTask(i);
			Population<MultiPittsburgh> pop = task.getPopulation();
			//他タスクの個体群を複雑順にソート
			List<MultiPittsburgh> sortedList = pop.getIndividuals().stream()
			.sorted(new Comparator<MultiPittsburgh>() {
				@Override
				public int compare(MultiPittsburgh p1, MultiPittsburgh p2) {
					int rank_p1 = p1.getRank();
					int rank_p2 = p2.getRank();
					int ruleNum1 = p1.getRuleNum();
					int ruleNum2 = p2.getRuleNum();
					int[] optimizer = task.getMOP().getOptimizer();
					double f_p1 = p1.getFitness(0)*optimizer[0];
					double f_p2 = p2.getFitness(0)*optimizer[0];

					if(rank_p1 > rank_p2) {
						return 1;
					}
					else if(rank_p1 < rank_p2) {
						return -1;
					}
					else {
						if(ruleNum1 > ruleNum2) {
							return -1;
						}
						else if(ruleNum1 < ruleNum2) {
							return 1;
						}
						else {
							if(f_p1 > f_p2) {
								return 1;
							}
							else if(f_p1 < f_p2) {
								return -1;
							}
							else {
								return 0;
							}
						}
					}
				}
			}).collect(Collectors.toList());
			for(int j = 0; j < world.getNumMigration(); j++) {
				MultiPittsburgh individual = new MultiPittsburgh(sortedList.get(j));	//Deep copy
				// 移住個体individualを自タスクで評価
				evaluationInThisTask(individual);
				// matingPoolに追加
				matingPool.addIndividual(individual);
				// 合併個体群用に自タスクの親個体群に移住個体を追加
				popInThisTask.addIndividual(individual);
			}
		}

		//3. 親個体群プールを非劣ソート&混雑度計算する
		this.nonDominatedSort(matingPool);

		return matingPool;
	}

	public Population makeOffspring(Population<MultiPittsburgh> matingPool, int taskNum, int numMigration, MersenneTwisterFast rnd) {
		Population<MultiPittsburgh> offspring = new Population<>();

		int makeSize = Setting.offspringSize - (taskNum*numMigration);

		for(int q = 0; q < makeSize; q++) {
			MultiPittsburgh child = null;

			while(true) {

				/* ************************************************************************ */
				//Step 1. Mating Selection
				int parentSize = 2;
				// トーナメント選択
//				int tournamentSize = 2;
//				Individual_nsga2[] parent = NSGA2.tournamentSelection(matingPool, parentSize, tournamentSize, rnd);
				//ランダム選択に変更
				Individual_nsga2[] parent = new Individual_nsga2[parentSize];
				Integer[] indexes = StaticFunction.sampringWithout(matingPool.getIndividuals().size(), parentSize, rnd);
				for(int i = 0; i < parentSize; i++) {
					parent[i] = matingPool.getIndividual(indexes[i]);
				}

				//Step 2. Crossover
					//GA type Selection (Michigan or Pittsburgh)
				if(rnd.nextDouble() < (double)Consts.RULE_OPE_RT) {
					//Michigan Type Crossover (Child Generation)
					child = (MultiPittsburgh)GAFunctions.michiganCrossover(mop, (Pittsburgh)parent[0], rnd);
				} else {
					//Pittsburgh Type Crossover (Child Generation)
					Pittsburgh[] cast = new Pittsburgh[parentSize];
					for(int i = 0; i < parentSize; i++) {
						cast[i] = (Pittsburgh)parent[i];	//Shallow Copy
					}
					child = (MultiPittsburgh)GAFunctions.pittsburghCrossover(cast, rnd);
				}

				//Step 3. Mutation
				GAFunctions.pittsburghMutation(child, mop.getTrain(), rnd);
				/* ************************************************************************ */

				//Step 4. Learning
				child.getRuleSet().learning(mop.getTrain(), Setting.forkJoinPool);

				//Step 5. Rule Deletion
				child.getRuleSet().removeRule();
				child.getRuleSet().radixSort();
				child.getRuleSet().calcRuleLength();

				//If child don't have any rule, the child should not be evaluated.
				//Then new child will be generated.
				if(child.getRuleSet().getMicRules().size() != 0) {
					break;
				}
			}

			child.ruleset2michigan();
			child.michigan2pittsburgh();
			child.initAppendix(this.mop.getAppendixNum());
			offspring.addIndividual(child);
		}

		this.manager.setOffspring(offspring);
		return this.getOffspring();
	}

	public void calcAppendix(Population<MultiPittsburgh> population) {
		this.mop.setAppendix(population);
	}

	public int getTaskID() {
		return this.taskID;
	}

	public Population getPopulation() {
		return this.manager.getPopulation();
	}

	public Population getOffspring() {
		return this.manager.getOffspring();
	}

	public PopulationManager getManager() {
		return this.manager;
	}

	public Problem_MultiLabel getMOP() {
		return this.mop;
	}

}
