package fgbml.mofgbml;

import data.SingleDataSetInfo;
import fgbml.SinglePittsburgh;

public class MOP1 extends Problem_MoFGBML {
	// ************************************************************


	// ************************************************************
	public MOP1(SingleDataSetInfo Dtra, SingleDataSetInfo Dtst) {
		super(Dtra, Dtst);

		this.objectiveNum = 2;
		this.optimizer = new int[] {MIN, MIN};

		doMemorizeMissPatterns = new boolean[] {true, false};

		setTrain(Dtra);
		setTest(Dtst);
	}

	// ************************************************************

	@Override
	public void evaluate(SinglePittsburgh individual) {
		double f1 = getMissRate(traID, individual);
		double f2 = individual.getRuleSet().getRuleNum();
		double[] fitness = new double[] {f1, f2};

		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(SinglePittsburgh individual) {
		double f1 = getMissRateParallel(traID, individual);
		double f2 = individual.getRuleSet().getRuleNum();
		double[] fitness = new double[] {f1, f2};

		individual.setFitness(fitness);
	}


}
