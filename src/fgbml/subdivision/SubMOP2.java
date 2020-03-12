package fgbml.subdivision;

import data.SingleDataSetInfo;
import fgbml.SinglePittsburgh;

public class SubMOP2 extends Problem_Subdivision{
	// ************************************************************


	// ************************************************************
	public SubMOP2(SingleDataSetInfo Dtra, SingleDataSetInfo Dtst, SingleDataSetInfo Dsubtra, SingleDataSetInfo Dvalid) {
		super(Dtra, Dtst, Dsubtra, Dvalid);

		this.objectiveNum = 2;

		this.optimizer = new int[] {MIN, MIN};

		doMemorizeMissPatterns = new boolean[] {true, false, false, false};
		setTrain(Dtra);
		setTest(Dtst);
	}

	// ************************************************************

	@Override
	public void evaluate(SinglePittsburgh individual) {
		double f1 = traMissRate(individual);
		double f2 = individual.getRuleSet().getRuleLength();
		double[] fitness = new double[] {f1, f2};

		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(SinglePittsburgh individual) {
		double f1 = traMissRateParallel(individual);
		double f2 = individual.getRuleSet().getRuleLength();
		double[] fitness = new double[] {f1, f2};

		individual.setFitness(fitness);
	}


}
