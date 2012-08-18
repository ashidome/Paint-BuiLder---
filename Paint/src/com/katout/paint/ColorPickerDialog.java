package com.katout.paint;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ColorPickerDialog extends Dialog {
	private LinearLayout mInflater;

	private OnColorChangedListener mListener;
	private ColorPickerView colorView;
	private int mInitialColor;
	private Context context;
	public EditText text;
	
	public int initialColor;

	// コンスタント
	public ColorPickerDialog(Context context, OnColorChangedListener listener,
			int initialColor) {
		super(context);
		this.initialColor = initialColor;
		mListener = listener;
		mInitialColor = initialColor;
		this.context = context;
	}

	// 起動時
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.colordialog);
		setTitle("Select Color");
		
		colorView = (ColorPickerView)findViewById(R.id.colorPicker);
		OnColorChangedListener l = new OnColorChangedListener() {
			public void colorChanged(int color) {
				mListener.colorChanged(color);
				dismiss();
			}
		};
		colorView.setup(l, initialColor);
//
//		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
//				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		ViewGroup.LayoutParams lp2 = new ViewGroup.LayoutParams(
//				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		ViewGroup.LayoutParams lp3 = new ViewGroup.LayoutParams(
//				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		lp3.width = 1;
//
//		text = new EditText(context);
//		LinearLayout linear = new LinearLayout(context);
//		linear.setOrientation(LinearLayout.VERTICAL);
//		// linear.addView(text,lp2);
//		// colorView = new ColorPickerView(getContext(), l, mInitialColor);
//		linear.addView(colorView, lp3);
//		setContentView(linear, lp);
//		setTitle("Set Color");
//
//		text.setText(colorView.intToString(mInitialColor));
//		text.setInputType(InputType.TYPE_CLASS_TEXT);
//		text.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before,
//					int count) {
//				try {
//					int num = Color.parseColor(s.toString());
//					colorView.setColor(num);
//					colorView.invalidate();
//				} catch (Exception e) {
//					// TODO: handle exception
//				}
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				// TODO 自動生成されたメソッド・スタブ
//
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//				// TODO 自動生成されたメソッド・スタブ
//
//			}
//		});
	}

}