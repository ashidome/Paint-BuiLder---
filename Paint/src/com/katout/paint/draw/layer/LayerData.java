package com.katout.paint.draw.layer;

import android.graphics.Bitmap;

public class LayerData {
	public int alpha;
	public int layermode;
	public Bitmap preview;
	public boolean tempEdit;
	public boolean alpha_save;
	public boolean under_clip;

	public LayerData() {
		alpha = 255;
		layermode = 0;
		preview = null;
		tempEdit = true;
		alpha_save = false;
		under_clip = false;

		preview= Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
	}
}
