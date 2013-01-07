package pbl.paint;

public class Settei {
	private static int filemode,gazotype,gazosize;
	Settei(){
		filemode=0;
		gazotype=1;
		gazosize=1;
	}
	public void setFilemode(int i){
		filemode=i;
	}
	public void setGazosize(int i){
		gazosize=i;
	}
	public void setGazotype(int i){
		gazotype=i;
	}
	public int getFilemode(){
		return filemode;
	}

}
