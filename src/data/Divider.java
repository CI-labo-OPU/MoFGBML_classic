package data;

import java.net.InetSocketAddress;

import main.Consts;
import method.MersenneTwisterFast;

public class Divider {
	// ************************************************************
	static MersenneTwisterFast uniqueRnd;
	static int dpop;

	// ************************************************************


	// ************************************************************
	/**
	 * データセットをクラス比を保ったまま，subRate:validRateの割合のパターン数になるように分割する．<br>
	 * <br>
	 * データセットをクラス順にソートした後，各クラスごとに上からsubRate%をsub，残りvalidRate%をvalidとする．<br>
	 * <br>
	 * @param dataset : DataSetInfo : dataset
	 * @param setting : int : calclationType
	 * @param serverList
	 * @return DataSetInfo[] : Divided DataSetInfo : [0]:subtra, [1]:valid, [2]:Original
	 */
	public static SingleDataSetInfo[] divideSubAndValid(SingleDataSetInfo dataset, int setting, InetSocketAddress[] serverList) {

		double subRate = Consts.SUBRATE;

		int classNum = dataset.getCnum();
		int dataSize = dataset.getDataSize();
		SingleDataSetInfo[] divideDatas = new SingleDataSetInfo[3];

		//各クラスのサイズ
		int[] eachClassSize = new int[classNum];
		for(int i = 0; i < dataSize; i++) {
			eachClassSize[dataset.getPattern(i).getConClass()]++;
		}

		//各分割データセットにおける各クラスのパターン数
		int[] subClassSize = new int[classNum];
		int[] validClassSize = new int[classNum];
		for(int c = 0; c < classNum; c++) {
			subClassSize[c] = (int)(eachClassSize[c] * subRate);	//小数点以下切り捨て
			validClassSize[c] = eachClassSize[c] - subClassSize[c];
		}

		//各分割データセットの大きさ
		int subSize = 0;
		int validSize = 0;
		for(int c = 0; c < classNum; c++) {
			subSize += subClassSize[c];
			validSize += validClassSize[c];
		}

		//それぞれの分割データにクラスごとにデータを割り当てていく

		//Sub training dataset
		divideDatas[0] = new SingleDataSetInfo();
		divideDatas[0].setDataSize(subSize);
		divideDatas[0].setNdim(dataset.getNdim());
		divideDatas[0].setCnum(classNum);
		divideDatas[0].setSetting(setting);
		divideDatas[0].setServerList(serverList);

		//Validation dataset
		divideDatas[1] = new SingleDataSetInfo();
		divideDatas[1].setDataSize(validSize);
		divideDatas[1].setNdim(dataset.getNdim());
		divideDatas[1].setCnum(classNum);
		divideDatas[1].setSetting(setting);
		divideDatas[1].setServerList(serverList);

		//All dataset
		divideDatas[2] = dataset;

		//Sort by Class
		dataset.sortPattern();

		//各クラスごとに順番に各データに格納（シャローコピー）
		int index = 0;
		for(int c = 0; c < classNum; c++) {
			for(int p = 0; p < subClassSize[c]; p++) {
				divideDatas[0].addPattern(dataset.getPattern(index++));
			}
			for(int p = 0; p < validClassSize[c]; p++) {
				divideDatas[1].addPattern(dataset.getPattern(index++));
			}
		}

		return divideDatas;
	}

	/**
	 * 島モデルにおける各島への部分学習用データセットの生成<br>
	 * @param islandNum : int : #of island
	 * @param dataset : DataSetInfo : Original DataSetInfo
	 * @param setting
	 * @param serverList
	 * @return DataSetInfo[] : Divided DataSetInfos
	 */
	public static SingleDataSetInfo[] devideIsland(int islandNum, SingleDataSetInfo dataset, int setting, InetSocketAddress[] serverList) {

		int Cnum = dataset.getCnum();
		int dataSize = dataset.getDataSize();

		//#of patterns for each class
		int[] eachClassSize = new int[Cnum];
		for(int p = 0; p < dataSize; p++) {
			eachClassSize[dataset.getPattern(p).getConClass()]++;
		}

		//#of patterns for each class in each island sub datasets
		int[][] classDividedSize = new int[Cnum][islandNum];
		int remainAddPoint = 0;
		for(int c = 0; c < Cnum; c++) {
			for(int i = 0; i < islandNum; i++) {
				classDividedSize[c][i] = eachClassSize[c] / islandNum;
			}
			int remain = eachClassSize[c] % islandNum;
			for(int i = 0; i < remain; i++) {
				int point = remainAddPoint % islandNum;
				classDividedSize[c][point]++;
				remainAddPoint++;
			}
		}

		//Size of each island sub dataset
		int[] eachDataSize = new int[islandNum];
		for(int c = 0; c < Cnum; c++) {
			for(int i = 0; i < islandNum; i++) {
				eachDataSize[i] += classDividedSize[c][i];
			}
		}

		//Distributing patterns into each island
		SingleDataSetInfo[] divided = new SingleDataSetInfo[islandNum + 1];	//Each island + Original
		for(int i = 0; i < islandNum; i++) {
			divided[i] = new SingleDataSetInfo();
			divided[i].setDataSize(eachDataSize[i]);
			divided[i].setNdim(dataset.getNdim());
			divided[i].setCnum(Cnum);
			divided[i].setSetting(setting);
			divided[i].setServerList(serverList);
		}

		//Sort patterns by classes
		dataset.sortPattern();

		int index = 0;
		for(int c = 0; c < Cnum; c++) {
			for(int i = 0; i < islandNum; i++) {

				for(int p = 0; p < classDividedSize[c][i]; p++) {
					divided[i].addPattern( dataset.getPattern(index++) );
				}

			}
		}

		//End of DataSetInfo[] is Original dataset
		divided[islandNum] = dataset;

		return divided;
	}


}




























