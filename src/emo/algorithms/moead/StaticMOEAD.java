package emo.algorithms.moead;

import java.util.Arrays;

import emo.algorithms.moead.scalarization.AOF;
import emo.algorithms.moead.scalarization.AOF2;
import emo.algorithms.moead.scalarization.PBI;
import emo.algorithms.moead.scalarization.Tchebycheff;
import emo.algorithms.moead.scalarization.WeightedSum;
import ga.Individual;
import ga.Population;
import main.Consts;
import main.Setting;
import method.StaticFunction;

public class StaticMOEAD {


	/**
	 * もしも，individualがEP内で非劣であれば，Deep CopyでEPにaddされる．<br>
	 * @param EP : Population :
	 * @param individual : Individual :
	 * @param optimizer :
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void updateEP(Population EP, Individual individual, int[] optimizer) {
		boolean dominated = false;
		int popSize = EP.getIndividuals().size();

		//Exist Check
		for(int p = 0; p < popSize; p++) {
			if(StaticFunction.sameGeneInt(EP.getIndividual(p), individual)) {
				return;
			}
		}

		for(int p = popSize - 1; 0 <= p; p--) {
//			if(StaticFunction.sameGeneInt(EP.getIndividual(p), individual)) {
//				//If an individual in EP has the same gene from given indiviudal,
//				//then the given individual is regarded as dominated.
//				//(namely will not be added into EP)
//				dominated = true;
//				break;
//			}
//			else
			if(StaticFunction.isDominate(individual, EP.getIndividual(p), optimizer)) {
				//Is individual dominating p?
				EP.getIndividuals().remove(p);
			}
			else if(StaticFunction.isDominate(EP.getIndividual(p), individual, optimizer)) {
				dominated = true;
			}

		}

		//Add individual to EP if no solutions in EP dominate individual.
		if(!dominated) {
			EP.addIndividual(individual);
		}
	}

	/**
	 * <h1>Update Ideal Point</h1>
	 * @param z : double[] : now ideal point
	 * @param individual : Individual :
	 */
	@SuppressWarnings("rawtypes")
	public static void updateIdeal(double[] z, Individual individual) {
		for(int o = 0; o < z.length; o++) {
			if(z[o] > individual.getFitness(o)) {
				z[o] = individual.getFitness(o);
			}
		}
	}

	/**
	 * <h1>Update Nadir Point</h1>
	 * @param e : double[] : now nadir point
	 * @param individual : Individual :
	 */
	@SuppressWarnings("rawtypes")
	public static void updateNadir(double[] e, Individual individual) {
		for(int o = 0; o < e.length; o++) {
			if(e[o] < individual.getFitness(o)) {
				e[o] = individual.getFitness(o);
			}
		}
	}

	/**
	 * <h1>Initialize Scalarize Function Instances</h1>
	 * Setting.emoTypeの値に応じて，スカラー化関数を初期化する．<br>
	 * @param objectiveNum : int :
	 * @param dataSize : int : for AOF
	 * @return ScalarizeFunction[] : weight vectors with Setting.emoType
	 */
	@SuppressWarnings("rawtypes")
	public static ScalarizeFunction[] initScalarizeFunctions(int objectiveNum, int dataSize) {
		ScalarizeFunction[] functions = null;

		if(Setting.emoType == Consts.WS) {
			double[][] vectors = StaticMOEAD.makeWeightVectors(objectiveNum, Consts.VECTOR_DIVIDE_NUM[objectiveNum]);
			int populationSize = vectors.length;
			functions = new ScalarizeFunction[populationSize];
			for(int i = 0; i < populationSize; i++) {
				functions[i] = new WeightedSum(i, objectiveNum);
				functions[i].setWeight(vectors[i]);
			}
		}
		else if(Setting.emoType == Consts.TCHEBY) {
			double[][] vectors = StaticMOEAD.makeWeightVectors(objectiveNum, Consts.VECTOR_DIVIDE_NUM[objectiveNum]);
			int populationSize = vectors.length;
			functions = new ScalarizeFunction[populationSize];
			for(int i = 0; i < populationSize; i++) {
				functions[i] = new Tchebycheff(i, objectiveNum);
				functions[i].setWeight(vectors[i]);
			}
		}
		else if(Setting.emoType == Consts.PBI) {
			double[][] vectors = StaticMOEAD.makeWeightVectors(objectiveNum, Consts.VECTOR_DIVIDE_NUM[objectiveNum]);
			int populationSize = vectors.length;
			functions = new ScalarizeFunction[populationSize];
			for(int i = 0; i < populationSize; i++) {
				functions[i] = new PBI(i, objectiveNum);
				functions[i].setWeight(vectors[i]);
			}
		}
		//TODO もうちょっと改善するべき
		else if(Setting.emoType == Consts.AOF) {
//			double[][] vectors = StaticMOEAD.makeWeightForAOF(Setting.populationSize);
//			int populationSize = vectors.length;
			int populationSize = Setting.populationSize;
			functions = new ScalarizeFunction[populationSize];
			for(int i = 0; i < populationSize; i++) {
				functions[i] = new AOF(i, dataSize);

				//This vector is defined to calculate the neighbor vectors.
				double[] vector = new double[] {1.0, functions[i].z[1]};	//(1, #of rules)
				functions[i].setWeight(vector);
//				functions[i].setWeight(vectors[i]);
			}
		}
		//TODO 2019.12. Trial
		else if(Setting.emoType == Consts.AOF2) {
//			double[][] vectors = StaticMOEAD.makeWeightForAOF(Setting.populationSize);
//			int populationSize = vectors.length;
			int populationSize = Setting.populationSize;
			functions = new ScalarizeFunction[populationSize];
			for(int i = 0; i < populationSize; i++) {
				functions[i] = new AOF2(i, dataSize);

				//This vector is defined to calculate the neighbor vectors.
				double[] vector = new double[] {1.0, functions[i].z[1]};	//(1, #of rules)
				functions[i].setWeight(vector);
//				functions[i].setWeight(vectors[i]);
			}
		}

		return functions;
	}

	/**
	 * <h1>Defining each set of neighbor vectors for each Scalarize function</h1>
	 * ScalarizeFunction[] functions内の各ベクトルに対して，近傍ベクトル集合を計算する．<br>
	 * @param T : int : Neighbor size
	 * @param functions : ScalarizeFunction[] :
	 */
	@SuppressWarnings("rawtypes")
	public static void calcNeighborhoods(ScalarizeFunction[] functions) {

		for(int i = 0; i < functions.length; i++) {
			functions[i].calcMatingNeighbors(functions);
			functions[i].calcUpdateNeighbors(functions);
		}

	}

	/**
	 * <h1>Initialize Weight vectors for AOF</h1>
	 * Each vector is 2-dimensional vector.<br>
	 * f1: Accuracy<br>
	 * f2: # of rules<br>
	 * <br>
	 * All vectors have the same weight vector (1, 0).<br>
	 * This static method returns vecSize same vectors (1, 0).<br>
	 *
	 * @param vecSize : int : #of vectors (=population size)
	 * @return
	 */
	public static double[][] makeWeightForAOF(int vecSize) {
		int objectiveNum = 2;	//Accuracy and #of rules.
		double[][] lambda = new double[vecSize][objectiveNum];

		for(int vec = 0; vec < vecSize; vec++) {
			lambda[vec][0] = 1.0;
			lambda[vec][1] = 0.0;
		}

		return lambda;
	}

	/**
	 *
	 * @param objectiveNum : int : #of objectives.
	 * @param H : int : devided size
	 * @return double[][] : lambda[popSize(=vecSize)][objectiveNum]
	 */
	public static double[][] makeWeightVectors(int objectiveNum, int H) {

		/** #of weight vector, namely population size */
		int vecNum = StaticFunction.combination(H + objectiveNum - 1, objectiveNum - 1);
		double[][] lambda = new double[vecNum][objectiveNum];

		double[] source = new double[H + objectiveNum - 1];
		Arrays.fill(source, 1.0);
		int[] partition = new int[objectiveNum - 1];
		boolean[] flg = new boolean[objectiveNum - 1];
		int index;

		//partitionの左寄せ
		for(int i = 0; i < objectiveNum - 1; i++) {
			partition[i] = i;
		}

		for(int vec = 0; vec < vecNum; vec++) {
			//仕切りの挿入
			Arrays.fill(source, 1.0);
			for(int i = 0; i < partition.length; i++) {
				source[partition[i]] = -1;
			}

			//重みベクトルの獲得
			index = 0;
			for(int i = 0; i < source.length; i++) {
				if(source[i] == -1) {
					index++;
					continue;
				}
				lambda[vec][index] += source[i];
			}

			//partition位置の更新
			for(int i = 0; i < partition.length; i++) {
				if( partition[(objectiveNum-1) - 1 - i] < (H + objectiveNum - 1)-1-i ) {
					partition[(objectiveNum-1) - 1 - i]++;
					flg[(objectiveNum-1) - 1 - i] = false;

					if(i != 0 && flg[(objectiveNum-1) - 1 - i + 1]) {
						//初期化する
						for(int j = (objectiveNum-1) - 1 - i + 1; j < partition.length; j++) {
							partition[j] = partition[j - 1] + 1;
							Arrays.fill(flg, false);
						}
					}
					break;
				} else {
					flg[(objectiveNum-1) - 1 - i] = true;
					continue;
				}
			}
		}

		for(int vec = 0; vec < vecNum; vec++) {
			for(int o = 0; o < objectiveNum; o++) {
				lambda[vec][o] /= (double)H;
			}
		}

		return lambda;
	}
}
