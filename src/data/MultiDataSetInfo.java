package data;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MultiDataSetInfo extends DataSetInfo<MultiPattern>{
	// ************************************************************

	// ************************************************************
	public MultiDataSetInfo() {}

	//並列分散実装用
	@Deprecated
	public MultiDataSetInfo(int Datasize, int Ndim, int Cnum, int setting, InetSocketAddress[]	serverList) {
		this.DataSize = Datasize;
		this.Ndim = Ndim;
		this.Cnum = Cnum;

		this.setting = setting;
		this.serverList = serverList;
	}

	@Deprecated
	public MultiDataSetInfo(int Ndim, int Cnum, int DataSize, ArrayList<MultiPattern> patterns) {
		this.Ndim = Ndim;
		this.Cnum = Cnum;
		this.DataSize = DataSize;
		this.patterns = patterns;
	}

	// ************************************************************

	public DataSetInfo<MultiPattern> newInstance(){
		return new MultiDataSetInfo();
	}

	@Deprecated
	public void addPattern(int id, double[] pattern, int Ndim, int Lnum) {
		patterns.add(new MultiPattern(id, pattern, Ndim, Lnum));
	}

	@Deprecated
	public void addPattern(int id, String[] line, int Ndim, int Lnum) {
		double[] pattern = new double[line.length];
		for(int i = 0; i < line.length; i++) {
			pattern[i] = Double.parseDouble(line[i]);
		}
		addPattern(id, pattern, Ndim, Lnum);
	}

	public void sortPattern() {
		Collections.sort(this.patterns, new patternComparatorByConClass() );
	}

	/**
	 * Sort by Conclusion Class
	 *
	 */
	public class patternComparatorByConClass implements Comparator<MultiPattern> {
		@Override
		public int compare(MultiPattern a, MultiPattern b) {
			double no1 = a.getConClass(0);
			double no2 = b.getConClass(0);

			if(no1 > no2) {
				return 1;
			} else if(no1 == no2) {
				return 0;
			} else {
				return -1;
			}
		}
	}


}
