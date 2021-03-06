package emo.problems;

import main.Consts;

/**
 *
 *
 * @param <T>
 * T: Individualを継承したクラス
 */

public abstract class MOP<T> {
	// ************************************************************
	protected int MIN = Consts.MINIMIZE;	// for Optimizer
	protected int MAX = Consts.MAXIMIZE;	// for Optimizer

	protected int[] optimizer;

	protected int objectiveNum;

	// ************************************************************
	public MOP() {}

	// ************************************************************
	public abstract void evaluate(T t);

	public abstract void evaluateParallel(T t);

	public void setObjectiveNum(int objectiveNum) {
		this.objectiveNum = objectiveNum;
	}

	public int getObjectiveNum() {
		return this.objectiveNum;
	}

	public int[] getOptimizer() {
		return this.optimizer;
	}

}
