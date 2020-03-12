package data;

import java.io.Serializable;

/**
 *
 *
 */
public class SinglePattern extends Pattern implements Serializable{
	// ************************************************************

	// ************************************************************
	public SinglePattern() {
		this.conClasses = new int[1];
	}

	public SinglePattern(int id, double[] pattern) {
		this.id = id;
		Ndim = pattern.length - 1;
		x = pattern;

		this.conClasses = new int[1];
		conClasses[0] = (int)pattern[Ndim];
	}

	// ************************************************************

	public int getConClass() {
		return conClasses[0];
	}


}
