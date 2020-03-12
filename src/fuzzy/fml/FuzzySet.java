package fuzzy.fml;

import jfml.term.FuzzyTermType;

/**
 * <h1>JFMLのFuzzyTermTypeクラスのWrapperクラス</h1>
 * <p>FuzzyTermTypeを用いて，1つのファジィ集合を定義する</p>
 *
 */
public class FuzzySet {
	// ************************************************************
	String name;
	int shapeType;

	FuzzyTermType term;

	float[] params;

	// ************************************************************
	public FuzzySet(String name, int shapeType, float[] params) {
		this.name = name;
		this.shapeType = shapeType;
		this.params = new float[params.length];
		for(int i = 0; i < params.length; i++) {
			this.params[i] = params[i];
		}
		make();
	}

	// ************************************************************
	public void make() {
		this.term = new FuzzyTermType(name, shapeType, params);
	}

	public FuzzyTermType getTerm() {
		return this.term;
	}

	public double calcMembership(double x) {
		return term.getMembershipValue((float)x);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setShapeType(int shapeType) {
		this.shapeType = shapeType;
	}

	public int getShapeType() {
		return this.shapeType;
	}

	public void setParams(int i, float param) {
		if(this.params.length > i) {
			this.params[i] = param;
		}
	}

	public void setParams(float[] params) {
		this.params = new float[params.length];
		for(int i = 0; i < params.length; i++) {
			this.params[i] = params[i];
		}
	}

}
