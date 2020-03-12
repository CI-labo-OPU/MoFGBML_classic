package emo.algorithms.moead.scalarization;

import emo.algorithms.moead.ScalarizeFunction;
import main.Consts;

@SuppressWarnings("rawtypes")
public class WeightedSum extends ScalarizeFunction {
	// ************************************************************


	// ************************************************************
	/**
	 *
	 * @param id
	 * @param objectiveNum
	 */
	public WeightedSum(int id, int objectiveNum) {
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

		double value = 0.0;
		for(int o = 0; o < objectiveNum; o++) {
			value += weight[o] * Math.abs(f[o] - z[o]);
		}

		return value;
	}

}
