package com.katout.paint.draw.brush;


public class Brush {
	char bmp[][]; // ブラシ画像
	int frequency; // 間隔

	static {
		System.loadLibrary("jniimage");
	}

	static public native int setBrush(char bmp[], int width, int height);
}