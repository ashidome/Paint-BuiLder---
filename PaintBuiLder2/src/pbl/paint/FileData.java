package pbl.paint;

public class FileData {	
	
	private String path;
	private String name;
	private int mode;
	
	public FileData(String path, String name) {
		this.path = path;
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public int getMode() {
		return mode;
	}

	public String getName() {
		return name;
	}
}
