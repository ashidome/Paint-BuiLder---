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
		new PreviewAsyncTask(context, func, img, item, layernum - position -1).execute();
		
		
		convertView.setTag(position);
		return convertView;
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
	
	public void addLayer(){
		LayerData layerdata = new LayerData();
		
		if(layers.size() == 0){
			layers.add(layerdata);
			currentlayer = 0;
		}else{
			layers.add(currentlayer, layerdata);
		}
		layernum++;
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
	
	public void setPreviewflag(){
		layers.get(currentlayer).tempEdit = true;
	
	}
	
	public int getCurrentlayer() {
		return currentlayer;
	}
	
	public void selectLayer(int c) {
		currentlayer = c ;
		
		func.selectLayer(layernum - currentlayer - 1);
	}
	
	
	
	
	public boolean setLayermode(int num){
		LayerData data = layers.get(currentlayer);
		if(data.layermode == num){
			return false;
		}
		data.layermode = num;
		func.setLayerMode(num);
		return true;
	}
	
	public int getLayermode(){
		return layers.get(currentlayer).layermode;
	}

	public boolean setAlpher(int progress) {
		LayerData data = layers.get(currentlayer);
		if(data.alpha == progress){
			return false;
		}
		data.alpha = progress;
		func.setLayerAlpha(progress);
		return true;
	}

	public int getLayerAlpha() {
		return layers.get(currentlayer).alpha;
	}
	
	public boolean setAlpha_save(boolean value){
		LayerData data = layers.get(currentlayer);
		if(data.alpha_save == value){
			return false;
		}
		data.alpha_save = value;
		//func.setAlphaSave(value);
		return true;
	}
	
	public boolean getAlpha_save(){
		return layers.get(currentlayer).alpha_save;
	}
	
	public boolean setUnder_clip(boolean value){
		LayerData data = layers.get(currentlayer);
		if(data.under_clip == value){
			return false;
		}
		data.under_clip = value;
		//func.setUnderClip(value);
		return true;
	}
	
	public boolean getUnder_clip(){
		return layers.get(currentlayer).under_clip;
	}
	



}

