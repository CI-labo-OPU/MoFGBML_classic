package data;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @param <T> : Patternクラスを継承したクラス
 */
public abstract class DataSetInfo<T> {
	// ************************************************************
	int DataSize;	//#of Patterns
	int Ndim;		//#of Features
	int Cnum;		//#of Classes

	ArrayList<T> patterns = new ArrayList<T>();

	int setting = 0;
	InetSocketAddress[] serverList = null;

	// ************************************************************
	public DataSetInfo() {}

	//並列分散実装用
//	public DataSetInfo(int Datasize, int Ndim, int Cnum, int setting, InetSocketAddress[] serverList){
//		this.DataSize = Datasize;
//		this.Ndim = Ndim;
//
//		this.setting = setting;
//		this.serverList = serverList;
//	}
//
//	public DataSetInfo(int Ndim, int Cnum, int DataSize, ArrayList<Pattern> patterns) {
//		this.Ndim = Ndim;
//		this.DataSize = DataSize;
//		this.patterns = patterns;
//	}

	// ************************************************************

	public abstract DataSetInfo<T> newInstance();

	public void setNdim(int Ndim) {
		this.Ndim = Ndim;
	}

	public int getNdim() {
		return this.Ndim;
	}

	public void setCnum(int Cnum) {
		this.Cnum = Cnum;
	}

	public int getCnum() {
		return this.Cnum;
	}

	public void setDataSize(int DataSize) {
		this.DataSize = DataSize;
	}

	public int getDataSize() {
		return this.DataSize;
	}

	public void setPattern(ArrayList<T> patterns) {
		this.patterns = patterns;
	}

	public ArrayList<T> getPatterns(){
		return this.patterns;
	}

	public T getPattern(int index) {
		return this.patterns.get(index);
	}

	public T getPatternWithID(int id) {
		List<T> list = this.patterns.stream()
						.filter(p -> ((Pattern)p).getID() == id)
						.collect( Collectors.toList() );
		return list.get(0);
	}

	/** Shallow Copy */
	public void addPattern(T pattern) {
		patterns.add(pattern);
	}

	public int getSetting() {
		return this.setting;
	}

	public void setSetting(int setting) {
		this.setting = setting;
	}

	public InetSocketAddress[] getServerList() {
		return this.serverList;
	}

	public void setServerList(InetSocketAddress[] serverList) {
		this.serverList = serverList;
	}

}
