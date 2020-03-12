package ga;

public class IntGene extends Individual<Integer>{


	public IntGene() {}

	public IntGene(int geneNum, int objectiveNum) {
		super(geneNum, objectiveNum);
	}

	public IntGene(IntGene individual) {
		super(individual);
	}

	@Override
	public void deepCopySpecific(Object individual) {
	}

}
