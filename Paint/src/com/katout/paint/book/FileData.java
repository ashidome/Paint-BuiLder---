package com.katout.paint.book;

public class FileData {	
	
	private String path;//ディレクトのパス
	private String name;//ディレクト名
	//ディレクトの種類(本棚 or お絵かき帳)はディレクトリが書き込み可能かどうかで判断(trueならば本棚、falseならばお絵かき帳)
	
	public FileData(String path, String name) {
		this.path = path;
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}
}
