package fgbml.mofgbml;

import java.util.ArrayList;

import fgbml.SinglePittsburgh;
import ga.PopulationManager;
import method.StaticFunction;

public class FAN2021 {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int[] checkFAN2021(PopulationManager manager) {

		int[] ans = new int[2];
		//[0]: populationの非重複（正味）個体数
		//[1]: 生成された子個体が次世代に残る割合

		ArrayList<SinglePittsburgh> population = manager.getPopulation().getIndividuals();
		ArrayList<SinglePittsburgh> offspring = manager.getOffspring().getIndividuals();

		//非重複個体数
		ArrayList<SinglePittsburgh> truePopulation = new ArrayList<>();
		for(int i = 0; i < population.size(); i++) {
			boolean duplicated = false;

			for(int j = 0; j < truePopulation.size(); j++) {
				if(StaticFunction.sameGeneInt(population.get(i), truePopulation.get(j))) {
					duplicated = true;
					break;
				}
			}

			if(!duplicated) {
				truePopulation.add(population.get(i));
			}
		}
		ans[0] = truePopulation.size();

		//生成された子個体が次世代に残る割合
		int updatedNum = 0;
		for(int i = 0; i < offspring.size(); i++) {
			boolean updated = false;
			for(int j = 0; j < truePopulation.size(); j++) {
				if(StaticFunction.sameGeneInt(offspring.get(i), truePopulation.get(j))) {
					updated = true;
					break;
				}
			}

			if(updated) {
				updatedNum++;
			}
		}
		ans[1] = updatedNum;

		return ans;
	}
}
//if(StaticFunction.sameGeneInt(parent[0], parent[1])) {
//	p = 1.0;
//	sameParentFlag = true;	//同じ親が選ばれた
//}