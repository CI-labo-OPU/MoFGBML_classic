package fgbml.problem;

import fgbml.Pittsburgh;
import ga.Population;
import ga.PopulationManager;
import method.ResultMaster;

@SuppressWarnings("rawtypes")
public abstract class OutputClass<T extends Pittsburgh> {
	// ************************************************************


	// ************************************************************


	// ************************************************************
	public abstract String outputPittsburgh(Population<T> population);

	public abstract String outputRuleSet(Population<T> population);

	/**
	 * Population/Offspring save method<br>
	 *
	 * @param manager : {@literal PopulationManager<Population<Pittsburgh>>}
	 * @param resultMaster : ResultMaster
	 * @param isPopulation : boolean : true:population, false:offspring
	 */
	public void savePopulationOrOffspring(	PopulationManager<Population<T>> manager,
													ResultMaster resultMaster,
													boolean isPopulation) {
		String individuals;
		String ruleSets;
		if(isPopulation) {
			individuals = outputPittsburgh(manager.getPopulation());
			ruleSets = outputRuleSet(manager.getPopulation());
			resultMaster.addPopulation(individuals);
			resultMaster.addRuleSetPopulation(ruleSets);
		} else {
			individuals = outputPittsburgh(manager.getOffspring());
			ruleSets = outputRuleSet(manager.getOffspring());
			resultMaster.addOffspring(individuals);
			resultMaster.addRuleSetOffspring(ruleSets);
		}

	}

	/**
	 * Population/Offspring save method<br>
	 *
	 * @param manager : {@literal PopulationManager<Population<MultiPittsburgh>>}
	 * @param resultMaster : ResultMaster
	 * @param isPopulation : boolean : true:population, false:offspring
	 */
	public void savePopulationOrOffspring(	Population<T> population,
											ResultMaster resultMaster,
											boolean isPopulation) {
		String individuals;
		String ruleSets;
		if(isPopulation) {
			individuals = outputPittsburgh(population);
			ruleSets = outputRuleSet(population);
			resultMaster.addPopulation(individuals);
			resultMaster.addRuleSetPopulation(ruleSets);
		}
		else {
			individuals = outputPittsburgh(population);
			ruleSets = outputRuleSet(population);
			resultMaster.addOffspring(individuals);
			resultMaster.addRuleSetOffspring(ruleSets);
		}
	}

}
