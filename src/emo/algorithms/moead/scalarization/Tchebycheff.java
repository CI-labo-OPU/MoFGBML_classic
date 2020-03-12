package emo.algorithms.moead.scalarization;

import emo.algorithms.moead.ScalarizeFunction;
import main.Consts;

@SuppressWarnings("rawtypes")
public class Tchebycheff extends ScalarizeFunction {
	// ************************************************************


	// ************************************************************
	/**
	 *
	 * @param id
	 * @param objectiveNum
	 */
	public Tchebycheff(int id, int objectiveNum) {
		super(id, objectiveNum);
	}

	// ************************************************************

	@Override
	public double function(double[] _f) {
		double[] f = null;
		if(Consts.DO_NORMALIZE) {
			f = normalize(_f);
		} else {
			//Do not normalize
			f = _f;
		}

		double max = -Double.MAX_VALUE;
		for(int o = 0; o < objectiveNum; o++) {
			double value = weight[o] * Math.abs(f[o] - z[o]);
			if(max < value) {
				max = value;
			}
		}

		return max;
	}

}
