package fgbml.multitask;

import data.MultiDataSetInfo;

public class MOP1 extends Problem_MultiLabel{
	// ************************************************************


	// ************************************************************
	public MOP1(MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
		super(Dtra, Dtst);

		this.objectiveNum = 2;
		this.optimizer = new int[] {MIN, MIN};

		doMemorizeMissPatterns = new boolean[] {true, false};
		setTrain(Dtra);
		setTest(Dtst);
	}

	// ************************************************************

	@Override
	public void evaluate(MultiPittsburgh individual) {
		int[][] classified = getClassified(traID, individual);

		double f1 = 100.0 - calcSubsetAccuracy(traID, classified);
		double f2 = individual.getRuleSet().getRuleNum();

		double[] fitness = new double[] {f1, f2};
		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(MultiPittsburgh individual) {
		int[][] classified = getClassifiedParallel(traID, individual);

		double f1 = 100.0 - calcSubsetAccuracy(traID, classified);
		double f2 = individual.getRuleSet().getRuleNum();

		double[] fitness = new double[] {f1, f2};
		individual.setFitness(fitness);
	}

}
