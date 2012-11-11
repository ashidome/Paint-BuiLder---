package com.katout.paint.draw;

import android.graphics.Bitmap;

public class Brush {
	char bmp[][]; // ブラシ画像
	int frequency; // 間隔

	static {
		System.loadLibrary("jniimage");
	}

	static public native int setBrush(char bmp[], int width, int height);
}