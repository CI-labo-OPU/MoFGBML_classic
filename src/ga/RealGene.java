package ga;

public class RealGene extends Individual<Double>{

	public RealGene() {}

	public RealGene(int geneNum, int objectiveNum) {
		super(geneNum, objectiveNum);
	}

	public RealGene(RealGene individual) {
		super(individual);
	}

	@Override
	public void deepCopySpecific(Object individual) {
	}

}
