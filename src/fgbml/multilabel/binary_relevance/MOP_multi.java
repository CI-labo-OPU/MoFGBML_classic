package fgbml.multilabel.binary_relevance;

import data.MultiDataSetInfo;

public class MOP_multi extends Problem_BR_Multi{
	// ************************************************************


	// ************************************************************
	public MOP_multi(MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
		super(Dtra, Dtst);

		this.objectiveNum = 2;
		this.optimizer = new int[] {MIN, MIN};

		doMemorizeMissPatterns = new boolean[] {false, false};
		setTrain(Dtra);
		setTest(Dtst);
	}

	// ************************************************************


	@Override
	public void evaluate(BRpittsburgh individual) {
		int[][] classified = getClassified(traID, individual);

		double f1 = calcExactMatchError(traID, classified);
		double f2 = individual.getBRruleset().getRuleNum();

		double[] fitness = new double[] {f1, f2};
		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(BRpittsburgh individual) {
		int[][] classified = getClassifiedParallel(traID, individual);

		double f1 = calcExactMatchError(traID, classified);
		double f2 = individual.getBRruleset().getRuleNum();

		double[] fitness = new double[] {f1, f2};
		individual.setFitness(fitness);
	}

}
