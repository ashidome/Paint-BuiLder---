package com.katout.paint.draw;

import android.graphics.Bitmap;

public class Brush {
	Bitmap bmp; // ブラシ画像
	int interval; // 感覚
	int radian; // 角度

	static {
		System.loadLibrary("jniimage");
	}

	public native int setBrush();
}
