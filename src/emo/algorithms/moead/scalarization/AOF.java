package emo.algorithms.moead.scalarization;

import emo.algorithms.moead.ScalarizeFunction;
import main.Consts;
import main.Setting;

/**
 * <h1>Accuracy Oriented scalarizing Function</h1>
 *
 * Reference:
 *   Title: "Multiobjective fuzzy genetics-based machine learning based on MOEA/D with its modifications"
 *   Authors: Y. Nojima, K. Arahari, S. Takemura, et al.
 *   Journal: IEEE International Conference on Fuzzy Systems
 *   Year: 2017
 *
 */
@SuppressWarnings("rawtypes")
public class AOF extends ScalarizeFunction{
	// ************************************************************

	/** data size */
	int patternNum = 0;

	// ************************************************************
	/**
	 *
	 * @param id
	 */
	@SuppressWarnings("unchecked")
	public AOF(int id, int patternNum) {
		super(id, 2);	// #of objective = 2
		this.patternNum = patternNum;
		if(Consts.IS_AOF_VECTOR_INT) {
			this.z = new double[] {0.0, id+1};	//(0, #of rules)
		}
		else {
			double k = (double)(id+1) * ((double)Consts.MAX_RULE_NUM / (double)Setting.populationSize);
			this.z = new double[] {0.0, k};	//(0, id * Max/populationSize)
		}
	}

	// ************************************************************

	@Override
	public double function(double[] f) {
		double k = z[1];
		double P = 100 * Math.max(f[1] - k, 0.0);

		/**
		 * Reference:
		 *   "Multiobjective Fuzzy Genetics-Based Machine Learning based on MOEA/D with its Modifications"
		 *   Formula (6)
		 */
		double g =  f[0]
					+ P
					+ (f[1] / (double)patternNum);

		return g;
	}

	public void setPatternNum(int patternNum) {
		this.patternNum = patternNum;
	}

	@Override
	public void setZ(double[] z) {
		//Ignore.
	}



}
