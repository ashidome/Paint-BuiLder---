package com.katout.paint.draw.brush;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.katout.paint.R;

public class BrushAdpter extends ArrayAdapter<Brush>{
	private LayoutInflater mInflater;
	
	public BrushAdpter(Context context, int textViewResourceId, ArrayList<Brush> objects) {
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Brush item = (Brush)getItem(position);
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.brush_column, null);
		}
		
		ImageView icon = (ImageView) convertView.findViewById(R.id.imageView1);
		icon.setImageBitmap(item.bitmap);
		
		TextView text = (TextView)convertView.findViewById(R.id.textView1);
		text.setText("頻度 = " + item.frequency);
		return convertView;
	}

}
