package fgbml.mofgbml;

import fgbml.Results;

public class Result_MoFGBML implements Results {
	// ************************************************************
	public int sameParentCount = 0;

	// ************************************************************
	public Result_MoFGBML() {}

	// ************************************************************

	public int getSameParentCount() {
		return this.sameParentCount;
	}

	public void addSameParentCount(int count) {
		this.sameParentCount += count;
	}

	public void incrementSameParentCount() {
		this.sameParentCount++;
	}




}
