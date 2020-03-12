package method;

import java.util.ArrayList;

import emo.algorithms.nsga2.Individual_nsga2;
import ga.Individual;
import ga.Population;
import main.Setting;

public class StaticFunction {

	/**
	 * <h1>Judging which a and b have same gene.</h1>
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean sameGeneInt(Individual<Integer> a, Individual<Integer> b) {

		if(a.getGeneNum() != b.getGeneNum()) {
			return false;
		}

		int geneNum = a.getGeneNum();
		boolean same = false;



		for(int i = 0; i < geneNum; i++) {
			int aa = (Integer) a.getGene(i);
			int bb = (Integer) b.getGene(i);
			if(aa == bb) {
				same = true;
			} else {
				same = false;
				break;
			}
		}

		return same;
	}

	/**
	 * <h1>Judging which a and b have same gene.</h1>
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean sameGeneDouble(Individual<Double> a, Individual<Double> b) {

		if(a.getGeneNum() != b.getGeneNum()) {
			return false;
		}

		int geneNum = a.getGeneNum();
		boolean same = false;

		for(int i = 0; i < geneNum; i++) {
			double aa = (Double) a.getGene(i);
			double bb = (Double) b.getGene(i);
			if(aa == bb) {
				same = true;
			} else {
				same = false;
				break;
			}
		}

		return same;
	}

	/**
	 * <h1>Distance between 2 vectors.</h1>
	 * @param vector1 : double[] :
	 * @param vector2 : double[] :
	 * @return double : distance between vector1 and vector2.
	 */
	public static double distanceVectors(double[] vector1, double[] vector2) {
		if(vector1.length != vector2.length) {
			return -1;
		}

		double sum = 0.0;
		for(int n = 0; n < vector1.length; n++) {
			sum += (vector1[n] - vector2[n]) * (vector1[n] - vector2[n]);
		}

		return Math.sqrt(sum);
	}


	/**
	 * <h1>Calculation norm</h1>
	 * @param vector : double[]
	 * @return double : norm of vector
	 */
	public static double vectorNorm(double[] vector) {
		double norm = 0.0;

		double sum = 0.0;
		for(int i = 0; i < vector.length; i++) {
			sum += vector[i] * vector[i];
		}

		norm = Math.sqrt(sum);

		return norm;
	}

	/**
	 * <h1>Calculate Inner Product of a and b.</h1>
	 * @param a : double[]
	 * @param b : double[]
	 * @return double : Inner Product
	 */
	public static double innerProduct(double[] a, double[] b) {
		if(a.length != b.length) {
			return -1;
		}

		double[] ab = new double[a.length];

		for(int i = 0; i < a.length; i++) {
			ab[i] = a[i] * b[i];
		}

		return StaticFunction.vectorNorm(ab);
	}

	public static double RecallMetric(int[] classified, int[] answer) {
		double correctAssociate = 0.0;
		double answerAssociate = 0.0;

		for(int i = 0; i < classified.length; i++) {
			if(classified[i] == 1 && answer[i] == 1) {
				correctAssociate++;
			}
			if(answer[i] == 1) {
				answerAssociate++;
			}
		}

		if(answerAssociate == 0) {
			return 0;
		}

		return correctAssociate / answerAssociate;
	}

	public static double PrecisionMetric(int[] classified, int[] answer) {
		double correctAssociate = 0.0;
		double classifiedAssociate = 0.0;

		for(int i = 0; i < classified.length; i++) {
			if(classified[i] == 1 && answer[i] == 1) {
				correctAssociate++;
			}
			if(classified[i] == 1) {
				classifiedAssociate++;
			}
		}

		if(classifiedAssociate == 0) {
			return 0;
		}

		return correctAssociate / classifiedAssociate;

	}

	/**
	 * <h1>Hamming Distance</h1>
	 * @param a : int[] :
	 * @param b : int[] :
	 * @return double :
	 */
	public static double HammingDistance(int[] a, int[] b) {
		double distance = 0.0;
		for(int i = 0; i < a.length; i++) {
			if(a[i] != b[i]) {
				distance++;
			}
		}
		return distance;
	}

	/**
	 * Judging the termination criteria.<br>
	 * If this method returns "true", the termination criteria is satisfied.<br>
	 * @param genCount : int : Generation Count
	 * @param evaCount : int : Evaluation Count
	 * @return : boolean : true: termination criteria is met.
	 */
	public static boolean terminationJudge(int genCount, int evaCount) {
		if(Setting.terminationCriteria) {
			//Generation Count
			if(genCount >= Setting.generationNum) {
				return true;
			} else {
				return false;
			}
		} else {
			//Evaluation Count
			if(evaCount >= Setting.evaluationNum) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * @param optimizer : minimize: 1, maximize: -1
	 * @return boolean : Is p dominating q ?
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isDominate(Individual p, Individual q, int[] optimizer) {
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

	/**
	 * Shallow Copy
	 * @param P : Population :
	 * @param optimizer :  minimize: 1, maximize: -1
	 * @return {@literal ArrayList<Individual_nsga2>}
	 */
	@SuppressWarnings("rawtypes")
	public static ArrayList<Individual_nsga2> getNonDominatedSolution(Population P, int[] optimizer) {
		ArrayList<Individual_nsga2> nonDominatedSet = new ArrayList<>();

		int popSize = P.getIndividuals().size();

		boolean dominated;

		//for each p in P
		for(int p = 0; p < popSize; p++) {
			dominated = false;

			//for each q in P (Does individual which dominates p exists?)
			for(int q = 0; q < popSize; q++) {
				if(p == q) continue;

				if(isDominate((Individual)P.getIndividual(q), (Individual)P.getIndividual(p), optimizer)) {
					//Is q dominating p? (An individual which dominates p exists.)
					dominated = true;
					break;	//Goto next p
				}
			}

			if(!dominated) {	//p is NOT dominated any other individual in P.
				nonDominatedSet.add((Individual_nsga2)P.getIndividual(p));
			}
		}

		return nonDominatedSet;

	}

	/**
	 * <h1>非復元抽出</h1><br>
	 * @param box : int : 元のデータサイズ
	 * @param want : int : 抽出したいindexの数
	 * @param rnd
	 * @return int[] : 非復元抽出したwant個のindex
	 */
	public static Integer[] sampringWithout(int box, int want, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		Integer[] answer = new Integer[want];

		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < box; i++) {
			list.add(i);
		}

		for(int i = 0; i < want; i++) {
			int index = uniqueRnd.nextInt(list.size());
			answer[i] = list.get(index);
			list.remove(index);
		}

		return answer;
	}

	/**
	 * <h1>log関数 底の変換公式</h1>
	 * @param a : double : 引数
	 * @param b : double : 底
	 * @return double : log_b (a)
	 */
	public static double log(double a, double b) {
		return (Math.log(a) / Math.log(b));
	}

	/**
	 * <h1>組合せの総数, nCr</h1>
	 * @param n
	 * @param r
	 * @return int : nCr
	 */
	public static int combination(int n, int r) {
		int ans = 1;
		for(int i = 1; i <= r; i++) {
			ans = ans * (n-i + 1) / i;
		}
		return ans;
	}

}
