package fgbml.multilabel;

import data.MultiDataSetInfo;

public class MOP_weighted4obj extends Problem_MultiLabel{
	// ************************************************************
	double[] weight = {1, 1, 1};

	// ************************************************************
	public MOP_weighted4obj(MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
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

		double subsetAccuracy = 100 - calcExactMatchError(traID, classified);
		double hammingLoss = calcHammingLoss(traID, classified);
		double fMeasure = calcFmeasures(traID, classified)[0];

		double weightedF =  -weight[0]*subsetAccuracy
							+weight[1]*hammingLoss
							-weight[2]*fMeasure;

		double numberOfRule = individual.getRuleSet().getRuleNum();

		double[] fitness = new double[] {weightedF, numberOfRule};
		individual.setFitness(fitness);

	}

	@Override
	public void evaluateParallel(MultiPittsburgh individual) {
		int[][] classified = getClassifiedParallel(traID, individual);

		double subsetAccuracy = 100 - calcExactMatchError(traID, classified);
		double hammingLoss = calcHammingLoss(traID, classified);
		double fMeasure = calcFmeasures(traID, classified)[0];

		double weightedF =  -weight[0]*subsetAccuracy
							+weight[1]*hammingLoss
							-weight[2]*fMeasure;

		double numberOfRule = individual.getRuleSet().getRuleNum();

		double[] fitness = new double[] {weightedF, numberOfRule};
		individual.setFitness(fitness);
	}

}
