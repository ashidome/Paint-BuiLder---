package com.katout.paint.draw.brush;

import android.graphics.Bitmap;
import android.graphics.Color;

public class BitmapEffector {
	/**
	 * ビットマップをグレイスケールし、結果ビットマップを返す。ARGB8888を前提。
	 * 
	 * @param src
	 * @return
	 */
	public static final char[] grayScale(Bitmap src) {
		final int GRAY_Y_R = 2990;
		final int GRAY_Y_G = 5870;
		final int GRAY_Y_B = 1140;

		try {

			int width = src.getWidth();
			int height = src.getHeight();
			int pixelsize = width * height;
			int srcPixels[] = new int[pixelsize];
			char dstPixels[] = new char[pixelsize];
			int offset, targetRed, targetGreen, targetBlue;

			src.getPixels(srcPixels, 0, width, 0, 0, width, height);

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					offset = x + y * width;

					targetRed = Color.red(srcPixels[offset]);
					targetGreen = Color.green(srcPixels[offset]);
					targetBlue = Color.blue(srcPixels[offset]);

					dstPixels[offset] = (char) ((targetRed * GRAY_Y_R / 10000)
							+ (targetGreen * GRAY_Y_G / 10000) + (targetBlue
							* GRAY_Y_B / 10000));
				}
			}
			return dstPixels;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

}