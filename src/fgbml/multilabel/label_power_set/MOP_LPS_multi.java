package fgbml.multilabel.label_power_set;

import data.MultiDataSetInfo;
import fgbml.SinglePittsburgh;
import ga.Population;

public class MOP_LPS_multi extends Problem_LPS_multi{
	// ************************************************************


	// ************************************************************
	public MOP_LPS_multi(MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
		super(Dtra, Dtst);

		this.objectiveNum = 2;
		this.optimizer = new int[] {MIN, MIN};

		doMemorizeMissPatterns = new boolean[] {false, false};
		setTrain(Dtra);
		setTest(Dtst);
	}

	// ************************************************************

	@Override
	public void evaluate(SinglePittsburgh individual) {
		int[][] classified = getClassified(traID, individual);

		double f1 = calcExactMatchError(traID, classified);
		double f2 = individual.getRuleSet().getRuleNum();

		double[] fitness = new double[] {f1, f2};
		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(SinglePittsburgh individual) {
		int[][] classified = getClassifiedParallel(traID, individual);

		double f1 = calcExactMatchError(traID, classified);
		double f2 = individual.getRuleSet().getRuleNum();

		double[] fitness = new double[] {f1, f2};
		individual.setFitness(fitness);
	}

	@Override
	public void setAppendix(Population<SinglePittsburgh> population) {

	}
}
