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
	private ArrayList<LinearLayout>[] layers;//左右にあるレイヤーのレイアウト
	private int layernum;
	private int currentlayer;
	
	public LayerAdapter(Context context, LinearLayout[] layouts) {
		this.layouts = layouts;
		layers= new ArrayList[layouts.length];
		for(int i = 0;i<layouts.length; i++){
			layers[i] = new ArrayList<LinearLayout>();
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
			LinearLayout layout = (LinearLayout) mInflater.inflate(R.layout.layer_column, null);
			layers[i].add(currentlayer, layout);
			temp[i] = layout;
		}
		for(int i = 0; i < layouts.length; i++){
			for(int j = layers[i].size() - 1; j > -1 ; j--){
				layouts[i].addView(layers[i].get(j));
				layers[i].get(j).setTag(j);
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
					layers[i].get(j).setBackgroundColor(0xffffffff);
				}else{
					layers[i].get(j).setBackgroundColor(0xffdddddd);
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
				layouts[i].addView(layers[i].get(j));
				layers[i].get(j).setTag(j);
			}
		}
		selectLayer(currentlayer);
		return true;
	}
}
