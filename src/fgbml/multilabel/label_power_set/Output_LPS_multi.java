package fgbml.multilabel.label_power_set;

import fgbml.SinglePittsburgh;

public class Output_LPS_multi {

	public String outputPittsburgh(SinglePittsburgh individual) {
		String ln = System.lineSeparator();
		String strs = "";
		String str = "";

		int objectiveNum = individual.getObjectiveNum();

		//Header
		str = "id";
		for(int o = 0; o < objectiveNum; o++) {
			str += "," + "f" + String.valueOf(o);
		}
		str += "," + "rank";
		str += "," + "crowding";
		//Appendix
		str += "," + "ExactMatchError_Dtra";
		str += "," + "Fmeasure_Dtra";
		str += "," + "Recall_Dtra";
		str += "," + "Precision_Dtra";
		str += "," + "HammingLoss_Dtra";
		str += "," + "ExactMatchError_Dtst";
		str += "," + "Fmeasure_Dtst";
		str += "," + "Recall_Dtst";
		str += "," + "Precision_Dtst";
		str += "," + "HammingLoss_Dtst";
		str += "," + "ruleNum";
		str += "," + "ruleLength";
		strs += str + ln;

		//id
		str = String.valueOf(0);
		//fitness
		for(int o = 0; o < objectiveNum; o++) {
			str += "," + individual.getFitness(o);
		}
		//rank
		str += "," + individual.getRank();
		//crowding distance
		str += "," + individual.getCrowding();
		//Exact-Match for Dtra
		str += "," + individual.getAppendix(0);
		//F-measure for Dtra
		str += "," + individual.getAppendix(1);
		//Recall for Dtra
		str += "," + individual.getAppendix(2);
		//Precision for Dtra
		str += "," + individual.getAppendix(3);
		//Hamming Loss for Dtra
		str += "," + individual.getAppendix(4);
		//Exact-Match for Dtst
		str += "," + individual.getAppendix(5);
		//F-measure for Dtst
		str += "," + individual.getAppendix(6);
		//Recall for Dtst
		str += "," + individual.getAppendix(7);
		//Precision for Dtst
		str += "," + individual.getAppendix(8);
		//Hamming Loss for Dtst
		str += "," + individual.getAppendix(9);
		//ruleNum
		str += "," + individual.getRuleSet().getRuleNum();
		//ruleLength
		str += "," + individual.getRuleSet().getRuleLength();

		strs += str + ln;

		return strs;
	}

	public String outputRuleSet(SinglePittsburgh individual) {
		String strs = "";

		return strs;
	}
}
