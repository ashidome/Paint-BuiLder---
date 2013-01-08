package com.katout.paint.draw;

public class NativeFunction {
	static {
		System.loadLibrary("jniimage");
	}
	
	public native boolean init(int x,int y);
	
	public native boolean destructor();

	public native boolean setCanvasSize(int x, int y);

	public native boolean deleteEditLayer();

	public native boolean addLayer();

	public native boolean deleteLayer();

	public native boolean selectLayer(int num);

	public native boolean setLayerMode(int mode);

	public native boolean Replace(int num, int move);

	public native boolean setvisible(int num, boolean truth);

	public native boolean setMask(int num, boolean truth);

	public native boolean setColor(int color);

	static public native int setBrush(char bmp[], int width, int height,int f);
	
	public native boolean setBrushSize(int size);

	public native boolean startDraw(int x, int y);

	public native boolean stopDraw(int x, int y);

	public native boolean draw(int x, int y);

	public native boolean setPosition(int x, int y);

	public native boolean setRadian(double rad);

	public native boolean setScale(double scale);
	
	public native boolean bucket(int x, int y, int t);
	
	public native int getCanvasHeight();
	
	public native int getCanvasWidth();
	
	public native boolean getRawdata(int[] canvas);
	
	public native boolean getBitmap(int[] canvas, int width, int height);
	
	public native boolean endDraw();
	
	public native boolean setMode(int num); // 0ならブラシ, 1なら消しゴム
	
	public native boolean joint(int layernum,char bmp[], int width, int height,int f,int size,int color,
			int[] points,int points_size);
	public native boolean getBrushRawSize(int[] size);
	
	public native boolean getBrushRawMap(char[] map);
	public native boolean getPreview(int num,int[] map,int Previeww,int Previewh);
}
