package fgbml.subdivision;

import data.SingleDataSetInfo;
import fgbml.SinglePittsburgh;

public class SubMOP11 extends Problem_Subdivision{
	// ************************************************************


	// ************************************************************
	public SubMOP11(SingleDataSetInfo Dtra, SingleDataSetInfo Dtst, SingleDataSetInfo Dsubtra, SingleDataSetInfo Dvalid) {
		super(Dtra, Dtst, Dsubtra, Dvalid);

		this.objectiveNum = 3;

		this.optimizer = new int[] {MIN, MAX, MIN};

		doMemorizeMissPatterns = new boolean[] {true, false, false, false};
		setTrain(Dtra);
		setTest(Dtst);
	}

	// ************************************************************

	@Override
	public void evaluate(SinglePittsburgh individual) {
		double f1 = traMissRate(individual);
		double f2 = individual.getRuleSet().getRuleNum();
		double f3 = individual.getRuleSet().getRuleLength();
		double[] fitness = new double[] {f1, f2, f3};

		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(SinglePittsburgh individual) {
		double f1 = traMissRateParallel(individual);
		double f2 = individual.getRuleSet().getRuleNum();
		double f3 = individual.getRuleSet().getRuleLength();
		double[] fitness = new double[] {f1, f2, f3};

		individual.setFitness(fitness);
	}


}
