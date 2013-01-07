package pbl.paint;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;

public class Sfile extends File{
	private  int mode;
	private  String name;
	private  String backpath;
	
    
	public Sfile(String path,String name,String backpath,int mode) {
		super(path);
		this.mode=mode;
		this.name=name;
		this.backpath=backpath;
		// TODO 自動生成されたコンストラクター・スタブ
	}
	
	
	public Sfile(String tag){
		super(tag);
		
		
		// TODO 自動生成されたコンストラクター・スタブ
	}
	public String getmode(){
		String i="2";
		if(mode==1){
			i="1";
		}else if(mode==0){
			i="0";
		}else{
			
		}
		return i;
	}
	public boolean smode(){
		boolean i=false;
		
		if(mode==1){
			i=true;
		}else if(mode==0){
			i=false;
		}
		return i;
	}
	public String getname(){
		return name;
	}
	public String getbackpath(){
		return backpath;
	}
	public void setmode(int i){
		mode=i;
	}
	

}
