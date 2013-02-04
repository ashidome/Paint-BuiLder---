package com.katout.paint.draw.layer;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.katout.paint.R;
import com.katout.paint.draw.NativeFunction;
import com.katout.paint.draw.RecompositionAsyncTask;
import com.katout.paint.draw.RecompositionAsyncTask.RePreviewLisner;

public class LayerAdapter extends ArrayAdapter<LayerData>{
	private Context context;
	private LayoutInflater mInflater;
	public Handler handler;
	private ArrayList<LayerData> layers;//左右にあるレイヤーのレイアウト
	private int layernum;
	private int currentlayer;
	private int previewheight;
	private int previewwidth;
	private NativeFunction func;
	private Spinner	spinner_l;
	private RePreviewLisner lisner;
	private SeekBar	alpher_seek;
	private boolean fastFlag;
	private ArrayAdapter adapter;
	
	public LayerAdapter(Context context, ArrayList<LayerData> list, 
			NativeFunction func, RePreviewLisner lisner) {
		super(context,0, list);
		layers = list;
		this.lisner = lisner;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layernum = 0;
		currentlayer = 1;
		this.context = context;
		this.func = func;
		handler = new Handler();
		fastFlag = false;
	}
	
	@Override
    public int getItemViewType (int position) {
		if(position == 0){
			return 0;
		}else {
			return 1;
		}
	}
	@Override
    public int getViewTypeCount () {
        //セルは2種類
        return 2;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(position == 0){
			//レイヤー設定の場合
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.layer_menu, null);
				adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item,context.getResources().getStringArray(R.array.SpinnerItems));
		        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				spinner_l = (Spinner)convertView.findViewById(R.id.spinner1);
				spinner_l.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
							
							@Override
							public void onItemSelected(AdapterView<?> parent,View view, int position, long id) {
								Log.d("test", "onItemSelected:" + position);
								if(fastFlag){
									setLayermode(position);
									
									new RecompositionAsyncTask(context, func,lisner).execute();
									notifyDataSetChanged();
								}else{
									fastFlag = true;
								}
							}
							@Override
							public void onNothingSelected(AdapterView<?> arg0) {}
						});
				spinner_l.setAdapter(adapter);
				alpher_seek = (SeekBar)convertView.findViewById(R.id.seek_alphr);
				alpher_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						setAlpher(seekBar.getProgress());
						new RecompositionAsyncTask(context, func,lisner).execute();

						notifyDataSetChanged();
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {}
				});
		
			}
		}else{
			LayerData item = this.getItem(position);
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.layer_column, null);
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
			previewwidth = img.getWidth();
			previewheight = img.getHeight();
			Bitmap bitmap = item.preview ;
			if(bitmap == null & previewwidth >0){
				bitmap= Bitmap.createBitmap(previewwidth, previewheight, Bitmap.Config.ARGB_8888);
				item.preview = bitmap;
			}
			final Bitmap bitmap2 = bitmap;
			
			func.getPreview(layernum - currentlayer, bitmap);
			handler.post(new Runnable() {
				@Override
				public void run() {
					img.setImageBitmap(bitmap2);
				}
			});
			item.tempEdit = false;
		}
		
		return convertView;
	}
	public void addLayer(){
		LayerData layerdata = new LayerData();
		layerdata.tempEdit = true;
		if(layers.size() == 0){
			layers.add(layerdata);
			currentlayer = 0;
		}else{
			layers.add(currentlayer, layerdata);
		}
		layernum++;

		layerdata.alpha = 255;
		layerdata.layermode = 0;
		
		notifyDataSetChanged();
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
		notifyDataSetChanged();
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

	private void setAlpher(int progress) {
		LayerData data = layers.get(currentlayer);
		data.alpha = progress;
		func.setLayerAlpha(progress);
	}

	public int getLayerAlpha() {
		return layers.get(currentlayer).alpha;
	}
	
	public void init_layers(int[] mode, int[] alpha){
		layers.get(0).layermode = mode[0];
		layers.get(0).alpha = alpha[0];
		
		if(mode.length > 1){
			for(int i = 1; i < mode.length; i++){
				LayerData data = new LayerData();
				data.tempEdit = true;
				data.alpha = alpha[i];
				data.layermode = mode[i];
				layers.add(data);
			}
		}
		notifyDataSetChanged();
	}

	public void selectLayer(int c) {
		currentlayer = c ;
		
		func.selectLayer(layernum - currentlayer);
		spinner_l.setSelection(getLayermode());
		alpher_seek.setProgress(getLayerAlpha());
	}
	public void onVisibility() {
		if(getLayermode()==12){
			setLayermode(0);
			spinner_l.setSelection(0,true);
//			adapter.notifyDataSetChanged();
//			spinner_l.invalidate();
		}else{
			setLayermode(12);
			spinner_l.setSelection(12,true);
//			adapter.notifyDataSetChanged();
//			spinner_l.invalidate();
		}
		
	}
}

