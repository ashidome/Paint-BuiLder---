package com.katout.paint.draw.layer;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.katout.paint.R;
import com.katout.paint.draw.NativeFunction;
import com.katout.paint.draw.RecompositionAsyncTask.RePreviewLisner;

public class LayerAdapter extends ArrayAdapter<LayerData>{
	private Context context;
	private LayoutInflater mInflater;
	public Handler handler;
	private ArrayList<LayerData> layers;//左右にあるレイヤーのレイアウト
	private int layernum;
	private int currentlayer;
	private NativeFunction func;

	
	public LayerAdapter(Context context, ArrayList<LayerData> list, 
			NativeFunction func, RePreviewLisner lisner) {
		super(context,0, list);
		layers = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layernum = 0;
		currentlayer = 0;
		this.context = context;
		this.func = func;
		handler = new Handler();
	}
	
	private int count=0;
	 
	@Override
	public int getCount(){
	    return count;
	}
	 
	@Override
	public void notifyDataSetChanged(){
	    super.notifyDataSetChanged();
	    count=layers.size();
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayerData item = this.getItem(position);
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.layer_column, null);
			convertView.setTag(position);
		}
		
		//選択による背景色
		if(position != currentlayer){
			convertView.setBackgroundColor(0xffffffff);
		}else{
			convertView.setBackgroundColor(0xffdddddd);
		}
		//レイヤーモード
		TextView text = (TextView)convertView.findViewById(R.id.layer_mode);
		text.setText(context.getResources().getStringArray(R.array.SpinnerItems)[item.layermode]);
		
		//不透明度
		text = (TextView)convertView.findViewById(R.id.layer_alpha);
		text.setText("不透明度：" + item.alpha);
		
		
		//プレビューのセット
		final ImageView img = (ImageView) convertView.findViewById(R.id.preview_image);
		if(item.tempEdit | (Integer)convertView.getTag() != position){
			new PreviewAsyncTask(context, func, img, item, layernum - position -1).execute();
		}
		
		convertView.setTag(position);
		return convertView;
	}
	public void addLayer(){
		LayerData layerdata = new LayerData();
		layerdata.tempEdit = true;

		layerdata.alpha = 255;
		layerdata.layermode = 0;
		
		if(layers.size() == 0){
			layers.add(layerdata);
			currentlayer = 0;
		}else{
			layers.add(currentlayer, layerdata);
		}
		layernum++;

	
	}
	
	public void setPreviewflag(){
		layers.get(currentlayer).tempEdit = true;
	
	}
	
	
	public boolean deleteLayer(){
		if(layernum <= 1){
			return false;
		}
		layers.remove(currentlayer);
		if(currentlayer >= layernum){
			currentlayer--;
		}
		layernum--;
	
		return true;
	}
	
	public void setLayermode(int num){
		LayerData data = layers.get(currentlayer);
		data.layermode = num;
		func.setLayerMode(num);
	
	}
	
	public int getLayermode(){
		return layers.get(currentlayer).layermode;
	}
	
	public int getCurrentlayer() {
		return currentlayer;
	}

	public void setAlpher(int progress) {
		LayerData data = layers.get(currentlayer);
		data.alpha = progress;
		func.setLayerAlpha(progress);
	
	}

	public int getLayerAlpha() {
		return layers.get(currentlayer).alpha;
	}
	
	public void init_layers(int[] mode, int[] alpha){
		for(int i = 0; i < mode.length; i++){
			LayerData data = new LayerData();
			data.tempEdit = true;
			data.alpha = alpha[i];
			data.layermode = mode[i];
			layers.add(data);
		}
	
	}

	public void selectLayer(int c) {
		currentlayer = c ;
		
		func.selectLayer(layernum - currentlayer - 1);
	
	}
}

