package com.katout.paint.draw.layer;

import java.util.ArrayList;

import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

public class MyListView {
	private LinearLayout root;
	private LayerAdapter adapter;
	private ArrayList<View> layouts;
	private AdapterView.OnItemClickListener lisner;
	
	public MyListView(LinearLayout root, LayerAdapter adapter) {
		this.root = root;
		this.adapter = adapter;
		layouts = new ArrayList<View>();
	}
	public void setOnClickLisner(AdapterView.OnItemClickListener lisner) {
		this.lisner = lisner;
	}
	
	public void invalidate() {
		int count = adapter.getCount();
		int list_count = layouts.size();
		for(int i = 0; i < count; i++){
			boolean flag = true;
			View convertView = null;
			if(i < list_count){
				convertView = layouts.get(i);
				flag = false;
			}
			convertView = adapter.getView(i, convertView, root);
			if(flag){
				root.addView(convertView);
				layouts.add(convertView);
				final int num = i;
				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						lisner.onItemClick(null, v, num, 0);
					}
				});
			}
		}
		while(count < list_count){
			root.removeView(layouts.get(count));
			layouts.remove(count);
			count++;
		}
		root.requestLayout();
	}

	public void invalidate_select() {
		int num = adapter.getCurrentlayer();
		adapter.getView(num, layouts.get(num), root);
	}
	
	
}
