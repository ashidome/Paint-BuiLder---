package com.katout.paint.draw.layer;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.katout.paint.R;
import com.katout.paint.draw.NativeFunction;

public class LayerAdapter{
	private Context context;
	private LayoutInflater mInflater;
	public Handler handler;
	private LinearLayout layouts;
	private ArrayList<LayerData> layers;//左右にあるレイヤーのレイアウト
	private int layernum;
	private int currentlayer;
	private int previewheight;
	private int previewwidth;
	private NativeFunction func;
	
	public LayerAdapter(Context context, LinearLayout layouts, NativeFunction func) {
		this.layouts = layouts;
		layers = new ArrayList<LayerData>();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layernum = 0;
		currentlayer = 0;
		this.context = context;
		this.func = func;
	}
	
	public LinearLayout addLayer(){
		//左右あるかここでは２回回している
		layouts.removeAllViews();
		
		LinearLayout temp;
		LayerData layerdata = new LayerData();
		if(layers.size() <= currentlayer+1){
			layers.add(layerdata);
		}else{
			layers.add(currentlayer+1, layerdata);
		}
		
		
		layerdata.layout = (LinearLayout) mInflater.inflate(
				R.layout.layer_column, null);
		temp = layerdata.layout;
			
		for(int j = layers.size()-1; j >= 0   ; j--){
			layouts.addView(layers.get(j).layout);
			layers.get(j).layout.setTag(j );
		}
		layernum++;
		if(layernum>1){
			currentlayer++;
		}
		setAlpher(255);
		setLayermode(0);
		
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
	
	public void setPreview(){
		ImageView img = (ImageView) layers.get(currentlayer).layout.findViewById(R.id.preview_image);
		previewwidth = img.getWidth();
		previewheight = img.getHeight();
		Bitmap bitmap = layers.get(currentlayer).preview ;
		if(bitmap == null){
			bitmap= Bitmap.createBitmap(previewwidth, previewheight, Bitmap.Config.ARGB_8888);
			layers.get(currentlayer).preview = bitmap;
		}
		
		func.getPreview(currentlayer, bitmap);
		img.setImageBitmap(bitmap);
		
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
		LayerData data = layers.get(currentlayer);
		data.layermode = num;
		TextView text = (TextView)data.layout.findViewById(R.id.layer_mode);
		text.setText(context.getResources().getStringArray(R.array.SpinnerItems)[data.layermode]);
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
		TextView text = (TextView)data.layout.findViewById(R.id.layer_alpha);
		text.setText("不透明度："+progress);
	}

	public int getLayerAlpha() {
		return layers.get(currentlayer).alpha;
	}
	
	public ArrayList<LinearLayout> init_layers(int[] mode, int[] alpha){
		ArrayList<LinearLayout> returns = new ArrayList<LinearLayout>();
		layers.get(0).layermode = mode[0];
		layers.get(0).alpha = alpha[0];
		
		if(mode.length > 1){
			for(int i = 1; i < mode.length; i++){
				LayerData data = new LayerData();
				data.layout = (LinearLayout) mInflater.inflate(R.layout.layer_column, null);
				
				data.alpha = alpha[i];
				TextView text = (TextView)data.layout.findViewById(R.id.layer_alpha);
				text.setText("不透明度："+data.alpha);
				data.layermode = mode[i];
				text = (TextView)data.layout.findViewById(R.id.layer_mode);
				text.setText(context.getResources().getStringArray(R.array.SpinnerItems)[data.layermode]);
				layers.add(data);
				returns.add(data.layout);
			}
		}
		layouts.removeAllViews();
		for(int j = layers.size() - 1; j > -1 ; j--){
			layouts.addView(layers.get(j).layout);
			layers.get(j).layout.setTag(j);
		}
		selectLayer(currentlayer);
		return returns;
	}
}
