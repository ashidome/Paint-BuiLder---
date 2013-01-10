package com.katout.paint.book;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;

public class ImageCache {
	private static HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();

	// キャッシュより画像データを取得
	public static Bitmap getImage(String key) {
		SoftReference<Bitmap> ref = cache.get(key);
		if (ref != null) {
			return ref.get();
		}
		return null;
	}

	// キャッシュに画像データを設定
	public static void setImage(String key, Bitmap image) {
		cache.put(key, new SoftReference<Bitmap>(image));
	}

	// キャッシュの初期化（リスト選択終了時に呼び出し、キャッシュで使用していたメモリを解放する）
	public static void clearCache() {
		cache.clear();
		cache = null;
		cache = new HashMap<String, SoftReference<Bitmap>>();
	}
}
