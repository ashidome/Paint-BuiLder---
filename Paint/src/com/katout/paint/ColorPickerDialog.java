package com.katout.paint;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ColorPickerDialog extends Dialog implements OnSeekBarChangeListener{

	private OnColorChangedListener mListener;
	private int mInitialColor;
	
	private ColorPickerView colorView;
	private SeekBar alpha;
	private SeekBar red;
	private SeekBar green;
	private SeekBar blue;
	private Button okButton;
	private ImageButton dropper;
	

	// コンスタント
	public ColorPickerDialog(Context context, 
			int initialColor) {
		super(context);
		this.mInitialColor = initialColor;
	}
	
	public void setlisner(OnColorChangedListener listener) {
		mListener = listener;
	}

	// 起動時
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.colordialog);
		setTitle("Select Color");
		
		alpha = (SeekBar)findViewById(R.id.seekBar_alpha);
		alpha.setProgress((mInitialColor& 0xFF000000) >>> 24);
		
		red = (SeekBar)findViewById(R.id.seekBar_red);
		red.setProgress((mInitialColor& 0x00FF0000) >>> 16);
		
		green = (SeekBar)findViewById(R.id.seekBar_green);
		green.setProgress((mInitialColor& 0x0000FF00) >>> 8);
		
		blue = (SeekBar)findViewById(R.id.seekBar_blue);
		blue.setProgress(mInitialColor& 0x000000FF);
		
		alpha.setOnSeekBarChangeListener(this);
		red.setOnSeekBarChangeListener(this);
		green.setOnSeekBarChangeListener(this);
		blue.setOnSeekBarChangeListener(this);
		
		okButton = (Button)findViewById(R.id.ok_button);
		okButton.setBackgroundColor(mInitialColor);
		float[] hsv = new float [3];
		Color.colorToHSV(mInitialColor, hsv);
		if(hsv[2] > 0.5f){
			okButton.setTextColor(0xFF000000);
		}else{
			okButton.setTextColor(0xFFFFFFFF);
		}
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.colorChanged(colorView.getColor());
				dismiss();
			}
		});
		
		colorView = (ColorPickerView)findViewById(R.id.colorPicker);
		OnColorChangedListener l = new OnColorChangedListener() {
			public void colorChanged(int color) {
				okButton.setBackgroundColor(color);
				float[] hsv = new float [3];
				Color.colorToHSV(color, hsv);
				if(hsv[2] > 0.5f){
					okButton.setTextColor(0xFF000000);
				}else{
					okButton.setTextColor(0xFFFFFFFF);
				}
				
				int c_alpha = (color & 0xFF000000) >>> 24;
				int c_red 	= (color & 0x00FF0000) >>> 16;
				int c_green = (color & 0x0000FF00) >>>  8;
				int c_blue 	= (color & 0x000000FF);
				alpha.setProgress(c_alpha);
				red.setProgress(c_red);
				green.setProgress(c_green);
				blue.setProgress(c_blue);
			}

			@Override
			public void onDropper() {
				mListener.onDropper();
			}
		};
		colorView.setup(l, mInitialColor);
		
		dropper = (ImageButton) findViewById(R.id.dropper);
		dropper.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onDropper();
			}
		});
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int color = Color.argb(alpha.getProgress(), red.getProgress(), green.getProgress(), blue.getProgress());
		colorView.setAlpha(alpha.getProgress());
		colorView.setColor(color);
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

}