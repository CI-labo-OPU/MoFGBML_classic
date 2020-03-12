package fgbml.multilabel.label_power_set;

import data.SingleDataSetInfo;
import fgbml.SinglePittsburgh;

public class MOP_LPS_single extends Problem_LPS_single {
	// ************************************************************

	// ************************************************************
	public MOP_LPS_single(SingleDataSetInfo Dtra) {
		super(Dtra);

		this.objectiveNum = 2;
		this.optimizer = new int[] {MIN, MIN};

		doMemorizeMissPatterns = new boolean[] {true};
		setTrain(Dtra);
	}

	// ************************************************************
	@Override
	public void evaluate(SinglePittsburgh individual) {
		double f1 = calcMissRate(traID, individual);
		double f2 = individual.getRuleSet().getRuleNum();
		double[] fitness = new double[] {f1, f2};

		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(SinglePittsburgh individual) {
		double f1 = calcMissRateParallel(traID, individual);
		double f2 = individual.getRuleSet().getRuleNum();
		double[] fitness = new double[] {f1, f2};

		individual.setFitness(fitness);
	}
}
