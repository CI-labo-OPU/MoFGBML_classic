package emo.algorithms.moead.scalarization;

import emo.algorithms.moead.ScalarizeFunction;
import main.Consts;
import method.StaticFunction;

@SuppressWarnings("rawtypes")
public class PBI extends ScalarizeFunction {
	// ************************************************************


	// ************************************************************
	/**
	 *
	 * @param id
	 * @param objectiveNum
	 */
	public PBI(int id, int objectiveNum) {
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

		double d1, d2;
		/** New vector (f - z) */
		double[] fz = new double[objectiveNum];
		for(int o = 0; o < objectiveNum; o++) {
			fz[o] = f[o] - z[o];
		}

		double normWeight = StaticFunction.vectorNorm(weight);
		double normFZ = StaticFunction.vectorNorm(fz);
		double innerProduct = StaticFunction.innerProduct(weight, fz);

		double cos = innerProduct / (normWeight * normFZ);
		double sin = Math.sqrt(1 - (cos*cos));

		d1 = normFZ * sin;
		d2 = normFZ * cos;

		return d1 + Consts.MOEAD_THETA * d2;
	}
}
