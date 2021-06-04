package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import data.CILAB_Format;
import data.CrossValidation;
import data.Input;
import data.MultiDataSetInfo;
import data.SingleDataSetInfo;
import data.artificial.Richromatic;
import emo.algorithms.moead.ScalarizeFunction;
import emo.algorithms.moead.StaticMOEAD;
import emo.algorithms.moead.scalarization.WeightedSum;
import fgbml.multilabel.MOP_ExactMatchError;
import fgbml.multilabel.MultiPittsburgh;
import fuzzy.SingleRule;
import fuzzy.SingleRuleSet;
import fuzzy.StaticFuzzyFunc;
import fuzzy.fml.FuzzySet;
import fuzzy.fml.params.HomoTriangle_2_3_4_5;
import fuzzy.multi_label.MultiRule;
import fuzzy.multi_label.MultiRuleSet;
import ga.RealGene;
import method.MersenneTwisterFast;
import method.Output;
import method.StaticFunction;

abstract class ClassLabel<G>{
	public abstract G get();
	public abstract void set(G element);
}

class ConsequentClass extends ClassLabel<Integer>{
	Integer classLabel;
	public Integer get() {
		return this.classLabel;
	}

	public void set(Integer element) {
		this.classLabel = element;
	}
}

class ConsequentClassVector extends ClassLabel<Integer[]>{
	Integer[] classLabel;
	public Integer[] get() {
		return this.classLabel;
	}

	public void set(Integer[] element) {
		this.classLabel = element;
	}
}

class Consequent{
	@SuppressWarnings("rawtypes")
	ClassLabel classLabel;

	@SuppressWarnings("rawtypes")
	public ClassLabel getConsequentClass() {
		return this.classLabel;
	}
	@SuppressWarnings("rawtypes")
	public void setConsequentClass(ClassLabel element) {
		this.classLabel = element;
	}
}

public class Test2 {
	public static void main(String[] args) {

//		args = new String[] {"./", "consts", "setting", "iris", "5", "TEST2"};
//		loadPropertyTest(args);

//		checkReadRuleSet(args);

//		checkInitWeightVector(args);

//		checkCalcNeighbors(args);

//		checkEx4Weight(args);

//		checkEx4K(args);

//		checkTuple(args);

//		checkClassifyParallel(args);

//		checkSameIndividuals(args);

//		checkRadixSort(args);

//		checkSame(args);

//		checkMakeFormat(args);

//		checkMakeARFF(args);

//		checkLoadRuleSetFile(args);

//		checkRichromatic(args);

//		Richromatic.makeRichromatic();
//		Richromatic.makeGrid();

//		checkHomoTriangle(args);

//		checkCrossValidation(args);

//		checkCILAB2ARFF(args);
//		String arg = "iris 0 0 tra";
//		String[] test = arg.split(" ");
//		translateSubdivision2ARFF(args);
//		translateTrainTest(args);
//		translateARFF2CILAB(args);

//		MultiLabel_realExamine.main(args);
//		Examine1.main(args);
//		Examine2.main(args);
//		Examine3.main(args);
//		Examine4.main(args);
//		Examine5_MLC45_results.main(args);
//		Examine6.main(args);

//		Examine7.main(args);


//		StaticFuzzyFunc.homogeneousInit(1);
//		StaticFuzzyFunc.outputFML("fuzzy_2-9.xml");

//		args = new String[] {"birds", "1", "1"};
//		CILAB_Format.main(args);

//		arrayRemove(args);

//		variableArgument(new int[] {1, 2});

		testGetGenerics(args);
	}

	public static void testGetGenerics(String[] args) {
		ConsequentClassVector C = new ConsequentClassVector();
		C.set(new Integer[]{1, 2});
		Consequent con = new Consequent();
		con.setConsequentClass(C);
		ConsequentClassVector answer = (ConsequentClassVector)con.getConsequentClass();
		Integer[] c = answer.get();
		System.out.println(c);
	}

	public static void variableArgument(int... x) {
		System.out.println(x);
	}

	public static void arrayRemove(String[] args) {
		String[] a = {"1", "2", "3"};
		for(String str : a) {
			System.out.println(str);
		}
		System.out.println();
		ArrayList<String> tmp = new ArrayList<>(Arrays.asList(a));
		tmp.remove(0);
		String[] newArgs = tmp.toArray(new String[0]);
		for(String str : newArgs) {
			System.out.println(str);
		}

	}

	public static void loadPropertyTest(String[] args) {
		String dir = args[0];

		String constsSource = args[1];
		String settingsSource = args[2];

		Consts.setConsts(dir, constsSource);
		Setting.setSettings(dir, settingsSource);

		Setting.dataName = args[3];
		Setting.parallelCores = Integer.parseInt(args[4]);
		Setting.saveDir = args[5];
		//並列用fork join pool 生成
		if(Setting.calclationType == 0) {
			Setting.forkJoinPool = new ForkJoinPool(Setting.parallelCores);
		}
	}

	public static void checkRichromatic(String[] args) {
		int h = 500;
		int dataSize = 20000;

		double a = 0.3;
		double b = 0.33;
		double r = 0.3;
		String name = "test_richromatic";

		Richromatic dataset = new Richromatic(name, a, b, r, h, dataSize);
		dataset.makeRichromatic(true);
		dataset.makeGrid();
	}

	public static void translateARFF2CILAB(String[] args) {
		String dataName = args[0];
		int Ndim = Integer.parseInt(args[1]);
		int Cnum = Integer.parseInt(args[2]);
		boolean isMulti = Boolean.parseBoolean(args[3]);
		String fileName = dataName + ".arff";

		CILAB_Format.arff2cilabo(fileName, dataName, Ndim, Cnum, isMulti);
	}

	public static void translateTrainTest(String[] args) {
		String dataName = args[0];
		String rep = args[1];
		String cv = args[2];
		String type = args[3];

		String traFile = "a" + rep + "_" + cv + "_" + dataName + "-10" + type + ".dat";

		SingleDataSetInfo Dtra = new SingleDataSetInfo();

		Input.inputFile(Dtra, traFile);

		String infoFile = dataName + "_info.txt";
		String headerFile = dataName + "_header.txt";
		boolean isMulti = false;
		CILAB_Format.cilabo2arff(Dtra, infoFile, headerFile, dataName, isMulti);
	}

	public static void translateSubdivision2ARFF(String[] args) {
		String dataName = args[0];
		String rep = args[1];
		String cv = args[2];
		String subFile = args[3];

		String traFile = "a" + rep + "_" + cv + "_" + dataName + "-10tra.dat";
		SingleDataSetInfo Dtra = new SingleDataSetInfo();
		Input.inputFile(Dtra, traFile);

		String dividedFile = "a" + rep + cv + "_" + subFile + ".csv";
		SingleDataSetInfo divided = new SingleDataSetInfo();
		Input.inputSubdata(Dtra, divided, dividedFile);

		String infoFile = dataName + "_info.txt";
		String headerFile = dataName + "_header.txt";
		boolean isMulti = false;

		CILAB_Format.cilabo2arff(divided, infoFile, headerFile, dataName, isMulti);
	}

	public static void checkCILAB2ARFF(String[] args) {
		String dataName = "flags";
		String fileName = dataName + ".dat";
		String infoFile = dataName + "_info.txt";
		String headerFile = dataName + "_header.txt";

		MultiDataSetInfo dataset = new MultiDataSetInfo();
		Input.inputMultiLabel(dataset, fileName);

		CILAB_Format.cilabo2arff(dataset, infoFile, headerFile, dataName, true);
//		CILAB_Format.cilabo2arff(fileName, infoFile, headerFile, dataName, true);
	}

	public static void checkCrossValidation(String[] args) {
		int seed = 2019;
		String dataName = "iris";
		String fileName = dataName + ".dat";
		int repeat = 2;
		int cv = 10;

		CrossValidation.cilabo_CrossValidation(seed, fileName, dataName, repeat, cv);

		System.out.println();
	}

	public static void checkHomoTriangle(String[] args) {
		float[][] params = HomoTriangle_2_3_4_5.getParams();

		StaticFuzzyFunc.homogeneousInit(1);
		FuzzySet[][] a = StaticFuzzyFunc.kb.getFSs();


		System.out.println();
	}

	public static void checkLoadRuleSetFile(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);

//		Consts.MULTI_CF_TYPE = 0;
//		String CFtype = "CFmean";
		Consts.MULTI_CF_TYPE = 1;
		String CFtype = "CFvector";

		String fileName = "ruleset_" + CFtype + ".txt";
		int Ndim = 2;
		int Lnum = 3;

		List<String> lines = null;
		try ( Stream<String> line = Files.lines(Paths.get(fileName)) ) {
			lines = line.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if(lines == null) {
			return;
		}

		//Dataset
		String dataFile = "richromatic_20000.dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, dataFile);

		//Initialize Fuzzy Sets
		StaticFuzzyFunc.initFuzzy(Dtra);

		//RuleSet
		lines.remove(0);	//---

		int ruleNum = lines.size();
		MultiRuleSet ruleset = new MultiRuleSet();
		MultiRule[] rules = new MultiRule[ruleNum];
		for(int r = 0; r < ruleNum; r++) {
			String[] line = lines.get(r).split(",");
			int[] rule = new int[Ndim];
			String[] separate = line[0].split(" +");
			for(int n = 0; n < Ndim; n++) {
				rule[n] = Integer.parseInt(separate[1+n]);
			}
			rules[r] = new MultiRule(rule, Lnum);
			ruleset.addRule(rules[r]);
		}

		ruleset.learning(Dtra, forkJoinPool);

		ruleset.removeRule();
		ruleset.radixSort();
		ruleset.calcRuleLength();

		int[][] classified = new int[Dtra.getDataSize()][Lnum];

		for(int p = 0; p < Dtra.getDataSize(); p++) {
			classified[p] = ruleset.classify(Dtra.getPattern(p), true);
		}

		/* ********************************************************* */
		String verticalFile = "richromatic_grid_vertical_h-500.csv";
		String horizontalFile = "richromatic_grid_horizontal_h-500.csv";

		MultiDataSetInfo Dtst_vertical = new MultiDataSetInfo();
		MultiDataSetInfo Dtst_horizontal = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtst_vertical, verticalFile);
		Input.inputMultiLabel(Dtst_horizontal, horizontalFile);

		int[][] tstClassified_vertical = new int[Dtst_vertical.getDataSize()][];
		int[][] tstClassified_horizontal = new int[Dtst_horizontal.getDataSize()][];

		for(int p = 0; p < Dtst_vertical.getDataSize(); p++) {
			tstClassified_vertical[p] = ruleset.classify(Dtst_vertical.getPattern(p), false);
		}
		for(int p = 0; p < Dtst_horizontal.getDataSize(); p++) {
			tstClassified_horizontal[p] = ruleset.classify(Dtst_horizontal.getPattern(p), false);
		}

		ArrayList<String> strs = new ArrayList<>();
		String str = "";

		//vertical
		str += Dtst_vertical.getDataSize() + "," + Ndim + "," + Lnum;
		strs.add(str);
		for(int p = 0; p < Dtst_vertical.getDataSize(); p++) {
			str = "";
			str += Dtst_vertical.getPattern(p).getDimValue(0);
			str += "," + Dtst_vertical.getPattern(p).getDimValue(1);
			for(int l = 0; l < tstClassified_vertical[p].length; l++) {
				str += "," + tstClassified_vertical[p][l];
			}
			str += ",";
			strs.add(str);
		}
		String output = "classified_richromatic_vertical_" + CFtype + ".csv";
		Output.writeln(output, strs);

		//horizontal
		strs = new ArrayList<>();
		str = "";
		str += Dtst_horizontal.getDataSize() + "," + Ndim + "," + Lnum;
		strs.add(str);
		for(int p = 0; p < Dtst_horizontal.getDataSize(); p++) {
			str = "";
			str += Dtst_horizontal.getPattern(p).getDimValue(0);
			str += "," + Dtst_horizontal.getPattern(p).getDimValue(1);
			for(int l = 0; l < tstClassified_horizontal[p].length; l++) {
				str += "," + tstClassified_horizontal[p][l];
			}
			str += ",";
			strs.add(str);
		}
		output = "classified_richromatic_horizontal_" + CFtype + ".csv";
		Output.writeln(output, strs);

		System.out.println();
	}

	public static void checkMakeARFF(String[] args) {
//		String dataName = "flags";
//		boolean isMulti = true;

		String dataName = "iris";
		boolean isMulti = false;

		String csvFile = dataName + ".csv";
		String infoFile = dataName + "_info.txt";
		String headerFile = dataName + "_header.txt";

		CILAB_Format.cilabo2arff(csvFile, infoFile, headerFile, dataName, isMulti);

	}

	public static void checkMakeFormat(String[] args) {

		String dataName = "flags";
		String fileName = dataName + ".arff";
		int Ndim = 19;
		int Cnum = 7;
		boolean multiLabel = true;

//		String dataName = "iris";
//		String fileName = dataName + ".arff";
//		int Ndim = 4;
//		int Cnum = 3;
//		boolean multiLabel = false;

//		String dataName = "genbase";
//		String fileName = dataName + ".arff";
//		int Ndim = 1186;
//		int Cnum = 27;
//		boolean multiLabel = true;

//		String dataName = "bands";
//		String fileName = dataName + ".arff";
//		int Ndim = 19;
//		int Cnum = 2;
//		boolean multiLabel = false;

		CILAB_Format.arff2cilabo(fileName, dataName, Ndim, Cnum, multiLabel);
	}

	public static void checkSame(String[] args) {
		boolean same = false;

		String fileName = "all_data.dat";
		List<String> answer_S = null;
		try ( Stream<String> line = Files.lines(Paths.get(fileName)) ) {
			answer_S = line.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}

		fileName = "bands.csv";
		List<String> my_S = null;
		try ( Stream<String> line = Files.lines(Paths.get(fileName)) ) {
			my_S = line.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}

		ArrayList<double[]> answer = new ArrayList<>();

		for(int i = 0; i < answer_S.size(); i++) {
			String[] data = answer_S.get(i).split(",");
			double[] a = new double[data.length + 1];
			for(int j = 0; j < data.length; j++) {
				a[j] = Double.parseDouble(data[j]);
			}
			a[data.length] = (double)i;
			answer.add(a);
		}

		ArrayList<double[]> my = new ArrayList<>();
		for(int i = 0; i < my_S.size(); i++) {
			String[] data = my_S.get(i).split(",");
			double[] a = new double[data.length + 1];
			for(int j = 0; j < data.length; j++) {
				a[j] = Double.parseDouble(data[j]);
			}
			a[data.length] = (double)i;
			my.add(a);
		}


		ArrayList<Integer> index = new ArrayList<>();

		ArrayList<double[]> newMy = new ArrayList<>();
		for(int i = 0; i < my.size(); i++) {
			double[] a = Arrays.copyOf(my.get(i), my.get(i).length);
			newMy.add(a);
		}

		for(int i = 0; i < answer.size(); i++) {
			double min = Double.MAX_VALUE;
			int minIndex = 0;
			int minJ = 0;
			for(int j = 0; j < my.size(); j++) {
				double[] vec1 = Arrays.copyOf(answer.get(i), answer.get(i).length-1);
				double[] vec2 = Arrays.copyOf(my.get(j), my.get(j).length-1);

				double distance = StaticFunction.distanceVectors(vec1, vec2);
				if(min > distance) {
					min = distance;
					minJ = j;
					minIndex = (int)my.get(j)[my.get(j).length-1];
				}
			}

			my.remove(minJ);

			index.add(minIndex);

		}

		my = new ArrayList<>();
		for(int i = 0; i < index.size(); i++) {
			my.add(newMy.get(index.get(i)));
		}

		for(int i = 0; i < answer.size(); i++) {
			for(int n = 0; n < answer.get(i).length - 1; n++) {
				double a;
				if( 0.000000000000001 > Math.abs(answer.get(i)[n] - my.get(i)[n]) ) {
					a = Math.abs(answer.get(i)[n] - my.get(i)[n]);
					same = true;
				}
				else {
					a = Math.abs(answer.get(i)[n] - my.get(i)[n]);
					same = false;
					break;
				}
			}

			if(!same) {
				break;
			}
		}


		System.out.println(same);
		System.out.println(same);
		System.out.println(same);

	}

	public static void checkRadixSort(String[] args) {
		SingleRuleSet ruleset = new SingleRuleSet();
		SingleRule rule;
		int[] r;
		r = new int[] {1, 11};
		rule = new SingleRule(r);
		ruleset.addRule(rule);

		r = new int[] {2, 5};
		rule = new SingleRule(r);
		ruleset.addRule(rule);

		r = new int[] {1, 11};
		rule = new SingleRule(r);
		ruleset.addRule(rule);

		ruleset.radixSort();

		System.out.println();
	}

	public static void checkSameIndividuals(String[] args) {
		int geneNum = 3;
		int objectiveNum = 2;

		RealGene a = new RealGene(geneNum, objectiveNum);
		RealGene b = new RealGene(geneNum, objectiveNum);
		Double[] gene = new Double[] {1.0, 2.0, 3.0};
		a.setGene(gene);
		gene = new Double[] {1.1, 2.0, 3.0};
		b.setGene(gene);

		boolean same = StaticFunction.sameGeneDouble(a, b);

		System.out.println(same);
		System.out.println();
	}


	public static void checkClassifyParallel(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);
		Setting.forkJoinPool = forkJoinPool;
		int seed = 3;
		MersenneTwisterFast rnd = new MersenneTwisterFast(seed);

		String sep = File.separator;
		String dataset = "emotions";
		String fileName = "dataset" + sep + dataset + sep + "a0_0_" + dataset + "-10tra.dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, fileName);

		fileName = "dataset" + sep + dataset + sep + "a0_0_" + dataset + "-10tst.dat";
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtst, fileName);

		StaticFuzzyFunc.homogeneousInit(Dtra.getNdim());

		MOP_ExactMatchError mop = new MOP_ExactMatchError(Dtra, Dtst);

		int ruleNum = 3;
		MultiPittsburgh individual = new MultiPittsburgh(Dtra.getNdim(), ruleNum, mop.getObjectiveNum());
		individual.setCnum(Dtra.getCnum());
		int[] antecedent = new int[Dtra.getNdim()];
		MultiRule rule1 = new MultiRule(antecedent, Dtra.getCnum());
		MultiRule rule2 = new MultiRule(antecedent, Dtra.getCnum());
		MultiRule rule3 = new MultiRule(antecedent, Dtra.getCnum());
		MultiRuleSet ruleSet = new MultiRuleSet();
		ruleSet.addRule(rule1);
		ruleSet.addRule(rule2);
		ruleSet.addRule(rule3);
		individual.setRuleSet(ruleSet);

//		individual.initHeuristic(Dtra, rnd);
		individual.learning(Dtra, forkJoinPool);
//		individual.getRuleSet().removeRule();
//		individual.getRuleSet().calcRuleLength();
		individual.ruleset2michigan();
		individual.michigan2pittsburgh();


		for(int i = 0; i < Dtra.getDataSize(); i++) {
			individual.getRuleSet().classifyParallel(Dtra.getPattern(i), true);
		}

		System.out.println();
	}

	public static void checkTuple(String[] args) {
		Object[] tuple = new Object[2];
		double value = 1.2;
		int[] conclusion = new int[] {1, 0, 1};
		tuple[0] = value;
		tuple[1] = conclusion;

		System.out.println();
	}

	public static void checkEx4Weight(String[] args) {
		int M = 4;
		int H = 29;
		double[][] weight = StaticMOEAD.makeWeightVectors(M, H);

		double[] weightK = new double[] {0.241379310344827, 0, 0.241379310344827, 0.517241379310344};


		String sep = File.separator;
		String fileName = "5_2" + sep + "dtlz2_" + M + "obj.txt";

		List<double[]> lines = inputAsList(fileName, " ");
		ArrayList<double[]> bests = new ArrayList<>();

		int vecSize = weight.length;
		int popSize = lines.size();

//		String no = "1";
//		fileName = "5_2" + sep + "ans"+no + sep + "vec"+no + "_" + M + "obj.txt";
//		lines = inputAsList(fileName, "\t");
//		weight = new double[lines.size()][];
//		vecSize = weight.length;
//		for(int v = 0; v < weight.length; v++) {
//			weight[v] = lines.get(v);
//		}


//		fileName = "5_2" + sep + "dtlz2_" + M + "obj.txt";
//		lines = inputAsList(fileName, " ");
//		bests = new ArrayList<>();

//		vecSize = weight.length;
//		popSize = lines.size();


		//重みベクトル四捨五入
//		for(int v = 0; v < vecSize; v++) {
//			for(int o = 0; o < M; o++) {
//				double a = weight[v][o];
//				a = a * 1000000;
//				a = Math.round(a);
//				weight[v][o] = a / 1000000;
//			}
//		}

		for(int p = 0; p < vecSize; p++) {
			double[] best = null;

			double max = -Double.MAX_VALUE;
			for(int i = 0; i < popSize; i++) {
				double value = 0.0;
				for(int o = 0; o < M; o++) {
					value += lines.get(i)[o] * weight[p][o];
				}

				if(max <= value) {
					max = value;
					best = lines.get(i);
				}
			}

			bests.add(best);
		}

		ArrayList<String> strs = new ArrayList<>();
		String str;

		for(int i = 0; i < bests.size(); i++) {
			str = "";
			for(int o = 0; o < M; o++) {
				str += String.valueOf(bests.get(i)[o]) + ",";
			}
			strs.add(str);
		}

		fileName = "omo_M-" + M + "_H-" + H + "obj.csv";
		Output.writeln(fileName, strs);


		strs = new ArrayList<>();
		str = "";
		for(int v = 0; v < vecSize; v++) {
			str = "";
			for(int o = 0; o < M; o++) {
				str += weight[v][o] + ",";
			}
			strs.add(str);
		}
		fileName = "omo_weight_M-" + M + "_H-" + H + ".csv";
		Output.writeln(fileName, strs);
	}

	public static void checkCalcNeighbors(String[] args) {
		int objectiveNum = 2;
		int H = 99;

		double[][] vectors = StaticMOEAD.makeWeightVectors(objectiveNum, H);

		ScalarizeFunction[] functions = new ScalarizeFunction[vectors.length];
		for(int i = 0; i < vectors.length; i++) {
			functions[i] = new WeightedSum(i, objectiveNum);
			functions[i].setWeight(vectors[i]);
		}

		int T = 5;
		for(int i = 0; i < vectors.length; i++) {
			functions[i].calcMatingNeighbors(functions);
		}

		System.out.println();
	}

	public static void checkInitWeightVector(String[] args) {

		int objectiveNum = 3;
		int H = 13;
		StaticMOEAD.makeWeightVectors(objectiveNum, H);;

	}

	public static void checkReadRuleSet(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);

		int seed = 3;
		MersenneTwisterFast rnd = new MersenneTwisterFast(seed);

		String sep = File.separator;
		String dataset = "emotions";
		String fileName = "dataset" + sep + dataset + sep + "a0_0_" + dataset + "-10tra.dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, fileName);

		fileName = "dataset" + sep + dataset + sep + "a0_0_" + dataset + "-10tst.dat";
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtst, fileName);

		StaticFuzzyFunc.homogeneousInit(Dtra.getNdim());

		MOP_ExactMatchError mop = new MOP_ExactMatchError(Dtra, Dtst);

		int ruleNum = 1;
		MultiPittsburgh individual = new MultiPittsburgh(Dtra.getNdim(), ruleNum, mop.getObjectiveNum());
		individual.setCnum(Dtra.getCnum());
		MultiRuleSet ruleSet = new MultiRuleSet();

		int[] antecedent = new int[] {0,0,0,0,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,1,1,0,0,0,0,0,1,6,3,0,0,6,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,3,0,2,0,0,0,0,0,0};
		MultiRule rule = new MultiRule(antecedent, Dtra.getCnum());

		ruleSet.addRule(rule);

		ruleSet.calcRuleLength();
		individual.setRuleSet(ruleSet);
		individual.ruleset2michigan();
		individual.michigan2pittsburgh();

		individual.learning(Dtra, forkJoinPool);

		int[][] classifiedTra = mop.getClassified(0, individual);
		int[][] classifiedTst = mop.getClassified(1, individual);

		double exactTra = mop.calcExactMatchError(0, classifiedTra);
		double exactTst = mop.calcExactMatchError(1, classifiedTst);

		double fTra = mop.calcFmeasures(0, classifiedTra)[0];
		double fTst = mop.calcFmeasures(1, classifiedTst)[0];

		double hammingTra = mop.calcHammingLoss(0, classifiedTra);
		double hammingTst = mop.calcHammingLoss(1, classifiedTst);


		System.out.println();

	}

	//list用
	public static void writeln(String fileName, ArrayList<String> strs) {
		String[] array = (String[]) strs.toArray(new String[0]);
		writeln(fileName, array);
	}

	//配列用
	public static void writeln(String fileName, String array[]){

		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
			for(int i=0; i<array.length; i++){
				 pw.println(array[i]);
			}
			pw.close();
	    }
		catch (IOException ex){
			ex.printStackTrace();
	    }
	}

	/**
	 *
	 * @param fileName
	 * @return : List{@literal <double[]>}
	 */
	public static List<double[]> inputAsList(String fileName, String separater) {
		List<double[]> lines = new ArrayList<double[]>();
		try ( Stream<String> line = Files.lines(Paths.get(fileName)) ) {
		    lines =
		    	line.map(s ->{
		    	String[] numbers = s.split(separater);
		    	double[] nums = new double[numbers.length];

		    	//値が無い場合の例外処理
		    	for (int i = 0; i < nums.length; i++) {
		    		//if (numbers[i].matches("^([1-9][0-9]*|0|/-)(.[0-9]+)?$") ){
		    			nums[i] = Double.parseDouble(numbers[i]);
		    		//}else{
		    		//	nums[i] = 0.0;
		    		//}
				}
		    	return nums;
		    })
		    .collect( Collectors.toList() );

		} catch (IOException e) {
		    e.printStackTrace();
		}

		return lines;
	}
}
