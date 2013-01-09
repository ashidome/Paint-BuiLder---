package com.katout.paint.draw;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShareMessage {
	int		layernum;
	char[]	bmp;
	int		width;
	int		height;
	int		f;
	int		size;
	int[]	points;
	int		points_size;
	int		color;
	int		mode;

	public void setMessage(String root) {
		try {
			JSONObject j_root = new JSONObject(root);
			layernum = j_root.getInt("layernum");
			width = j_root.getInt("width");
			height = j_root.getInt("height");
			f = j_root.getInt("f");
			size = j_root.getInt("size");
			points_size = j_root.getInt("points_size");
			color = j_root.getInt("color");
			mode = j_root.getInt("mode");

			JSONArray j_points = j_root.getJSONArray("points");
			int t = j_points.length();
			points = new int[t];
			for (int i = 0; i < t; i++) {
				points[i] = j_points.getInt(i);
			}

			JSONArray j_bmp = j_root.getJSONArray("bmp");
			t = j_bmp.length();
			bmp = new char[t];
			for (int i = 0; i < t; i++) {
				bmp[i] = (char) j_bmp.getInt(i);
			}
		} catch (JSONException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public String getMessage() {

		try {
			JSONObject j_root = new JSONObject();
			j_root.put("layernum", layernum);
			j_root.put("width", width);
			j_root.put("height", height);
			j_root.put("f", f);
			j_root.put("size", size);
			j_root.put("points_size", points_size);
			j_root.put("color", color);
			j_root.put("mode", mode);

			JSONArray j_bmp = new JSONArray();
			for (int i = 0; i < bmp.length; i++) {
				j_bmp.put((int) bmp[i]);
			}
			j_root.put("bmp", j_bmp);

			JSONArray j_points = new JSONArray();
			for (int i = 0; i < points.length; i++) {
				j_points.put(points[i]);
			}
			j_root.put("points", j_points);

			return j_root.toString();
		} catch (JSONException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
	}
}
