package com.katout.paint.draw.layer;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.katout.paint.R;

public class LayerAdapter{
	private LayoutInflater mInflater;
	public Handler handler;
	private LinearLayout layouts;
	private ArrayList<LayerData> layers;//左右にあるレイヤーのレイアウト
	private int layernum;
	private int currentlayer;
	
	public LayerAdapter(Context context, LinearLayout layouts) {
		this.layouts = layouts;
		layers = new ArrayList<LayerData>();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layernum = 0;
		currentlayer = 0;
	}
	
	public LinearLayout addLayer(LayerData data){
		//左右あるかここでは２回回している
		layouts.removeAllViews();
		
		LinearLayout temp;
		LayerData layerdata = new LayerData();
		layerdata.layout = (LinearLayout) mInflater.inflate(
				R.layout.layer_column, null);
		layerdata.alpha = 100;
		layerdata.layermode = 0;
		layers.add(currentlayer, layerdata);
		temp = layerdata.layout;
			
		for(int j = layers.size() - 1; j > -1 ; j--){
			layouts.addView(layers.get(j).layout);
			layers.get(j).layout.setTag(j);
		}
		if(layernum!=0){
			currentlayer++;
		}
		layernum++;
		selectLayer(currentlayer);
		return temp;
	}
	/** 
	 * 選択中のレイヤーに色付け
	 */
	public void selectLayer(int num){
		currentlayer = num;
		for(int j = 0; j < layers.size();j++){
			if(j != currentlayer){
				layers.get(j).layout.setBackgroundColor(0xffffffff);
			}else{
				layers.get(j).layout.setBackgroundColor(0xffdddddd);
			}
		}
	}
	
	public boolean deleteLayer(){
		if(layernum <= 1){
			return false;
		}
		layernum--;
		layers.remove(currentlayer);
		currentlayer--;
		if(currentlayer <0){
			currentlayer = 0;
		}
		layouts.removeAllViews();
		for(int j = layers.size() - 1; j > -1 ; j--){
			layouts.addView(layers.get(j).layout);
			layers.get(j).layout.setTag(j);
		}
		selectLayer(currentlayer);
		return true;
	}
	
	public void setLayermode(int num){
		layers.get(currentlayer).layermode = num;
	}
	
	public int getLayermode(){
		return layers.get(currentlayer).layermode;
	}
}
