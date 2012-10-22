package com.katout.paint.draw;

public interface EventLisner {
	boolean init(int x, int y);

	// 描画周り
	boolean startDraw(int x, int y);

	boolean draw(int x, int y);

	// 移動周り
	boolean setPosition(int x, int y);

	boolean setRadian(double rad);

	boolean setScale(double scale);

	boolean deleteEditLayer();

	boolean getBitmap(int[] canvas, int width, int height);

}
