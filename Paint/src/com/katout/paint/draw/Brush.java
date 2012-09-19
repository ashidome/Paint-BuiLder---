package com.katout.paint.draw;

import android.graphics.Bitmap;

public class Brush {
	char bmp[][]; // ブラシ画像
	int interval; // 間隔
	int radian; // 角度

	static {
		System.loadLibrary("jniimage");
	}

	static public native int setBrush();
}
