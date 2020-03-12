package data;

import java.io.Serializable;

public class MultiPattern extends Pattern implements Serializable{
	// ************************************************************

	// ************************************************************
	public MultiPattern() {}

	public MultiPattern(int id, double[] pattern, int Ndim, int Lnum) {
		this.id = id;
		this.Ndim = Ndim;
		x = pattern;

		//Label
		this.conClasses = new int[Lnum];
		for(int i = 0; i < Lnum; i++) {
			conClasses[i] = (int)pattern[Ndim + i];
		}
	}

	// ************************************************************

	public int[] getConClass() {
		return conClasses;
	}

	public int getLnum() {
		return conClasses.length;
	}

}
