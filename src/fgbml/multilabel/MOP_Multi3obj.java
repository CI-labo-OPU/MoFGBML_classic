package fgbml.multilabel;

import data.MultiDataSetInfo;

public class MOP_Multi3obj extends Problem_MultiLabel{
	// ************************************************************


	// ************************************************************
	public MOP_Multi3obj(MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
		super(Dtra, Dtst);

		this.objectiveNum = 3;
		this.optimizer = new int[] {MIN, MIN, MIN};

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

		double[] fitness = new double[] {f1, f2, f3};
		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(MultiPittsburgh individual) {
		int[][] classified = getClassifiedParallel(traID, individual);

		double f1 = calcExactMatchError(traID, classified);
		//Transform for Minimize Optimization
		double f2 = 100.0 - calcFmeasures(traID, classified)[0];
		double f3 = calcHammingLoss(traID, classified);

		double[] fitness = new double[] {f1, f2, f3};
		individual.setFitness(fitness);
	}


}
