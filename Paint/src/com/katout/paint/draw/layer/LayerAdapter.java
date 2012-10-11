package com.katout.paint.draw.layer;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.katout.paint.R;

public class LayerAdapter{
	private LayoutInflater mInflater;
	public Handler handler;
	private Context context;
	private LinearLayout[] layouts;
	
	public LayerAdapter(Context context, LinearLayout[] layouts) {
		this.layouts = layouts;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}
	
	public void addLayer(LayerData data){
		for(int i = 0; i < layouts.length; i++){
			LinearLayout layout = (LinearLayout) mInflater.inflate(R.layout.layer_column, null);
			layouts[i].addView(layout);
		}
		
	}

}
