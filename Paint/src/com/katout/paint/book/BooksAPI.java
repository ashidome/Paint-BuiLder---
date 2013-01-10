package com.katout.paint.book;

import java.io.File;

public class BooksAPI {
	private static String filename = ".PaintBuilder";
	
	static public String makeDirectory(String name,String backpath) {
		String path = backpath + name;//作成するディレクトリのパス
		File file = new File(path);
		try{
			if(!file.exists()){// ディレクトリが存在しない場合新規作成
				file.mkdir();
				file = new File(backpath, filename);
				file.createNewFile();
			}
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
		return path;
	}
	
	public static boolean isBook(String path){
		File[] files = new File(path).listFiles();
		for(File file:files){
			if(file.getName().equals(filename)){
				return true;
			}
		}
		return false;
	}
}
