package fgbml.multilabel;

import data.MultiDataSetInfo;

public class MOP_Fmeasure extends Problem_MultiLabel{
	// ************************************************************


	// ************************************************************
	public MOP_Fmeasure(MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
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

		//Transform for Minimize Optimization
		double f1 = 100.0 - calcFmeasures(traID, classified)[0];

		double f2 = individual.getRuleSet().getRuleNum();

		double[] fitness = new double[] {f1, f2};
		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(MultiPittsburgh individual) {
		int[][] classified = getClassifiedParallel(traID, individual);

		//Transform for Minimize Optimization
		double f1 = 100.0 - calcFmeasures(traID, classified)[0];

		double f2 = individual.getRuleSet().getRuleNum();

		double[] fitness = new double[] {f1, f2};
		individual.setFitness(fitness);
	}


}
