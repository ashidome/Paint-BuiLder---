package com.katout.paint.draw;

public class NativeFunction {
	static {
		System.loadLibrary("jniimage");
	}
	
	public native boolean init(int x,int y);

	public native boolean setCanvasSize(int x, int y);

	public native boolean deleteEditLayer();

	public native boolean addLayer(int num);

	public native boolean deleteLayer(int num);

	public native boolean EditLayer(int num);

	public native boolean setLayerMode(int num, int mode);

	public native boolean Replace(int num, int move);

	public native boolean setvisible(int num, boolean truth);

	public native boolean setMask(int num, boolean truth);

	public native boolean setColor(int color);

	public native boolean setBrushSize(int size);

	public native boolean startDraw(int x, int y);

	public native boolean stopDraw(int x, int y);

	public native boolean draw(int x, int y);

	public native boolean setPosition(int x, int y);

	public native boolean setRadian(double rad);

	public native boolean setScale(double scale);

	public native boolean getBitmap(int[] canvas, int width, int height);
}
