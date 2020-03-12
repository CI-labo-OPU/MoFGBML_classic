package data;

public abstract class Pattern {
	// ************************************************************
	int id;
	double[] x;
	int[] conClasses;

	int Ndim;

	// ************************************************************
	public Pattern() {}

	// ************************************************************
	public double[] getX() {
		return this.x;
	}

	public double getDimValue(int i) {
		return this.x[i];
	}

	public int[] getConClasses() {
		return this.conClasses;
	}

	public int getConClass(int index) {
		return this.conClasses[index];
	}

	public int getNdim() {
		return this.Ndim;
	}

	public int getID() {
		return this.id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public Class<?> getEntity(){
		return this.getClass();
	}

}
