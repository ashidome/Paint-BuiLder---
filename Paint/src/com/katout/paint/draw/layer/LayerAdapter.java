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
	private LinearLayout[] layouts;
	private ArrayList<LayerData>[] layers;//左右にあるレイヤーのレイアウト
	private int layernum;
	private int currentlayer;
	
	public LayerAdapter(Context context, LinearLayout[] layouts) {
		this.layouts = layouts;
		layers= new ArrayList[layouts.length];
		for(int i = 0;i<layouts.length; i++){
			layers[i] = new ArrayList<LayerData>();
		}
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layernum = 0;
		currentlayer = 0;
	}
	
	public LinearLayout[] addLayer(LayerData data){
		//左右あるかここでは２回回している
		for(int i = 0; i < layouts.length; i++){
			layouts[i].removeAllViews();
		}
		LinearLayout[] temp = new LinearLayout[layouts.length];
		for(int i = 0; i < layouts.length; i++){
			LayerData layerdata = new LayerData();
			layerdata.layout = (LinearLayout) mInflater.inflate(R.layout.layer_column, null);
			layerdata.alpha=100;
			layerdata.layermode=0;
			layers[i].add(currentlayer, layerdata);
			temp[i] = layerdata.layout ;
		}
		for(int i = 0; i < layouts.length; i++){
			for(int j = layers[i].size() - 1; j > -1 ; j--){
				layouts[i].addView(layers[i].get(j).layout);
				layers[i].get(j).layout.setTag(j);
			}
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
		for(int i = 0; i < layers.length; i++){
			for(int j = 0; j < layers[i].size();j++){
				if(j != currentlayer){
					layers[i].get(j).layout.setBackgroundColor(0xffffffff);
				}else{
					layers[i].get(j).layout.setBackgroundColor(0xffdddddd);
				}
			}
		}
	}
	
	public boolean deleteLayer(){
		if(layernum <= 1){
			return false;
		}
		layernum--;
		for(int i = 0; i < layouts.length; i++){
			layers[i].remove(currentlayer);
		}
		currentlayer--;
		if(currentlayer <0){
			currentlayer = 0;
		}
		for(int i = 0; i < layouts.length; i++){
			layouts[i].removeAllViews();
			for(int j = layers[i].size() - 1; j > -1 ; j--){
				layouts[i].addView(layers[i].get(j).layout);
				layers[i].get(j).layout.setTag(j);
			}
		}
		selectLayer(currentlayer);
		return true;
	}
	
	public void setLayermode(int num){
		for(int i = 0; i < layouts.length; i++){
			layers[i].get(currentlayer).layermode = num;
		}
	}
	
	public int getLayermode(){
		return layers[0].get(currentlayer).layermode;
	}
}
