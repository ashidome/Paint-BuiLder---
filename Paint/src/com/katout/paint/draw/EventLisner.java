package com.katout.paint.draw;


public interface EventLisner {

	// レイヤ周り
	boolean setCanvasSize(int x, int y);

	boolean deleteEditLayer();

	boolean addLayer(int num);

	boolean deleteLayer(int num);

	boolean EditLayer(int num);

	boolean setLayerMode(int num, int mode);

	boolean Replace(int num, int move);

	boolean setvisible(int num, boolean truth);

	boolean setMask(int num, boolean truth);

	// 描画周り
	boolean setBrush(Brush brush);

	boolean setColor(int color);

	boolean setBrushSize(int size);

	boolean startDraw(int x, int y);

	boolean stopDraw(int x, int y);

	boolean draw(int x, int y);

	// 移動周り
	boolean setPosition(int x, int y);

	boolean setRadian(double rad);

	boolean setScale(double scale);

}
