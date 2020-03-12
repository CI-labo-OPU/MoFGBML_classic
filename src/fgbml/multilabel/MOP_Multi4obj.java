package fgbml.multilabel;

import data.MultiDataSetInfo;

public class MOP_Multi4obj extends Problem_MultiLabel{
	// ************************************************************


	// ************************************************************
	public MOP_Multi4obj(MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
		super(Dtra, Dtst);

		this.objectiveNum = 4;
		this.optimizer = new int[] {MIN, MIN, MIN, MIN};

		doMemorizeMissPatterns = new boolean[] {true, false};
		setTrain(Dtra);
		setTest(Dtst);
	}

	// ************************************************************
	@Override
	public void evaluate(MultiPittsburgh individual) {
		int[][] classified = getClassified(traID, individual);

		double f1 = calcExactMatchError(traID, classified);
		//Transform for Minimize Optimization
		double f2 = 100.0 - calcFmeasures(traID, classified)[0];
		double f3 = calcHammingLoss(traID, classified);

		double f4 = individual.getRuleSet().getRuleNum();

		double[] fitness = new double[] {f1, f2, f3, f4};
		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(MultiPittsburgh individual) {
		int[][] classified = getClassifiedParallel(traID, individual);

		double f1 = calcExactMatchError(traID, classified);
		//Transform for Minimize Optimization
		double f2 = 100.0 - calcFmeasures(traID, classified)[0];
		double f3 = calcHammingLoss(traID, classified);

		double f4 = individual.getRuleSet().getRuleNum();

		double[] fitness = new double[] {f1, f2, f3, f4};
		individual.setFitness(fitness);
	}
}
