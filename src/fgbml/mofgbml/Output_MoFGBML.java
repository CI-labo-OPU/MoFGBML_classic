package fgbml.mofgbml;

import java.io.File;
import java.util.ArrayList;

import fgbml.SinglePittsburgh;
import fgbml.problem.OutputClass;
import ga.Population;
import method.Output;
import method.ResultMaster;

public class Output_MoFGBML extends OutputClass<SinglePittsburgh> {

	/**
	 * <h1>Output Information of Individual</h1>
	 * Header:<br>
	 * id, f0, f1, ..., Dtra, Dtst, ruleNum, ruleLength, rank, crowding
	 * @param fileName : String
	 * @param population : Population{@literal <Pittsburgh>} : population
	 */
	@Override
	public String outputPittsburgh(Population<SinglePittsburgh> population) {
		String ln = System.lineSeparator();
		String strs = "";
		String str = "";

		int popSize = population.getIndividuals().size();
		int objectiveNum = population.getIndividual(0).getObjectiveNum();

		//Header
		str = "id";
		for(int o = 0; o < objectiveNum; o++) {
			str += "," + "f" + String.valueOf(o);
		}
		//Appendix
		str += "," + "Dtra";
		str += "," + "Dtst";
		str += "," + "ruleNum";
		str += "," + "ruleLength";

		//EMO
		str += "," + "rank";
		str += "," + "crowding";
		strs += str + ln;

		//Population
		for(int p = 0; p < popSize; p++) {
			//id
			str = String.valueOf(p);
			//fitness
			for(int o = 0; o < objectiveNum; o++) {
				str += "," + population.getIndividual(p).getFitness(o);
			}
			//Dtra
			str += "," + population.getIndividual(p).getAppendix(0);
			//Dtst
			str += "," + population.getIndividual(p).getAppendix(1);
			//ruleNum
			str += "," + population.getIndividual(p).getRuleSet().getRuleNum();
			//ruleLength
			str += "," + population.getIndividual(p).getRuleSet().getRuleLength();

			//rank
			str += "," + population.getIndividual(p).getRank();
			//crowding distance
			str += "," + population.getIndividual(p).getCrowding();

			strs += str + ln;
		}

		return strs;
	}

	@Override
	public String outputRuleSet(Population<SinglePittsburgh> population) {
		String ln = System.lineSeparator();
		String row = "***************************************";
		String hyphen = "---";

		String strs = "";
		String str = "";

		int popSize = population.getIndividuals().size();
		int Ndim = population.getIndividual(0).getNdim();
		int objectiveNum = population.getIndividual(0).getObjectiveNum();

		for(int pop = 0; pop < popSize; pop++) {
			int ruleNum = population.getIndividual(pop).getRuleNum();

			strs += row + ln;
			strs += "pop_" + pop + ln;
			strs += "ruleNum: " + ruleNum + ln;
			strs += "rank: " + population.getIndividual(pop).getRank() + ln;
			strs += "crowding: " + population.getIndividual(pop).getCrowding() + ln;
			for(int o = 0; o < objectiveNum; o++) {
				strs += "f"+o+": " + population.getIndividual(pop).getFitness(o) + ln;
			}
			strs += hyphen + ln;

			//Rules
			for(int rule = 0; rule < ruleNum; rule++) {
				//id
				str = "Rule_" + String.format("%02d", rule) + ":";
				//rule
				for(int n = 0; n < Ndim; n++) {
					str += " " + String.format("%2d", population.getIndividual(pop).getRuleSet().getMicRule(rule).getRule(n));
				}
				//class
				str += ", " + "Class: " + String.format("%2d", population.getIndividual(pop).getRuleSet().getMicRule(rule).getConc());
				//cf
				str += ", " + "CF: " + population.getIndividual(pop).getRuleSet().getMicRule(rule).getCf();
				//fitness
				str += ", " + "Fitness: " + population.getIndividual(pop).getRuleSet().getMicRule(rule).getFitness();

				strs += str + ln;
			}
			strs += row + ln;
			strs += "" + ln;
		}

		return strs;
	}

	public void outputFAN2021(String trialRoot, ResultMaster resultMaster) {
		String sep = File.separator;
		ArrayList<String> strs = new ArrayList<>();
		String str;
		String fileName;

		int sameParentCount = resultMaster.getSameParentCount();
		int[] offspringNumWithRuleNum = resultMaster.getOffspringNumWithRuleNum();
		ArrayList<Integer> truePopSize = resultMaster.getTruePopSize();
		ArrayList<Integer> updatedNum = resultMaster.getUpdatedNum();

		//sameParentCount
		fileName = trialRoot + sep + "FAN_sameParentCount_"+sameParentCount+".txt";
		str = String.valueOf(sameParentCount);
		strs.add(str);
		Output.writeln(fileName, strs);

		// offspringNumWithRuleNum
		strs = new ArrayList<>();
		str = "";
		fileName = trialRoot + sep + "FAN_offspringNumWithRuleNum.csv";
		//header
		str = "ruleNum,count";
		strs.add(str);
		//body
		for(int i = 0; i < offspringNumWithRuleNum.length; i++) {
			str = String.valueOf(i+1);
			str += "," + String.valueOf(offspringNumWithRuleNum[i]);
			strs.add(str);
		}
		Output.writeln(fileName, strs);

		//truePopSize
		strs = new ArrayList<>();
		str = "";
		fileName = trialRoot + sep + "FAN_truePopSize.csv";
		//header
		str = "generation,truePopSize";
		strs.add(str);
		//body
		for(int i = 0; i < truePopSize.size(); i++) {
			str = String.valueOf(i+1);
			str += "," + String.valueOf(truePopSize.get(i));
			strs.add(str);
		}
		Output.writeln(fileName, strs);

		//updatedNum
		strs = new ArrayList<>();
		str = "";
		fileName = trialRoot + sep + "FAN_updatedNum.csv";
		//header
		str = "generation,updatedNum";
		strs.add(str);
		//body
		for(int i = 0; i < updatedNum.size(); i++) {
			str = String.valueOf(i+1);
			str += "," + String.valueOf(updatedNum.get(i));
			strs.add(str);
		}
		Output.writeln(fileName, strs);
	}









}
