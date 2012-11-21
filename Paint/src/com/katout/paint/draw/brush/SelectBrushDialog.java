package com.katout.paint.draw.brush;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.katout.paint.R;
import com.katout.paint.draw.NativeFunction;

public class SelectBrushDialog extends Dialog{
	private ListView listview;
	private ArrayList<Brush> list;
	
	public interface SelectBrushLisner{
		void setBrush(char bmp[], int width, int height, int f);
	}

	public SelectBrushDialog(Context context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_brush_dialog);
		setTitle("Select Brush");
		
		Button cancel = (Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dismiss();
			}
		});
		
		list = new  ArrayList<Brush>();
		Resources r = getContext().getResources();
		//標準ブラシの作成
		Brush br = new Brush();
		br.bitmap = BitmapFactory.decodeResource(r, R.drawable.full);
		br.frequency = 30;
		list.add(br);
		
		br = new Brush();
		br.bitmap = BitmapFactory.decodeResource(r, R.drawable.circle);
		br.frequency = 30;
		list.add(br);
		
		listview = (ListView)findViewById(R.id.listView1);
		BrushAdpter adpter = new BrushAdpter(getContext(), 0, list);
		listview.setAdapter(adpter);
		
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                ListView listView = (ListView) parent;
                // クリックされたアイテムを取得します
                Brush item = (Brush) listView.getItemAtPosition(position);
                NativeFunction.setBrush(BitmapEffector.grayScale(item.bitmap), item.bitmap.getWidth(),
                		item.bitmap.getHeight(), item.frequency);
               
			}
		});
	}

}
