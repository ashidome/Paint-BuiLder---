package com.katout.paint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {
	private Paint mPaint, mPaintC;
	private final int[] mColors;
	private int[] mChroma;
	private OnColorChangedListener mListener;
	private Shader sg, lg;
	private int selectColor = 0xFFFFFF00;
	private float selectHue = 0;
	private int Alpha = 255;


	private static final int CENTER_X = 100;
	private static final int CENTER_Y = 100;
	private static final int Rect_X = 50;
	private static final int CENTER_RADIUS = 24;

	public ColorPickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		selectHue = getHue(selectColor);
		mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF,
				0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };

		mChroma = new int[] { 0xFF000000, 0xFF888888, 0xFFFFFFFF };

		sg = new SweepGradient(0, 0, mColors, null);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setShader(sg);
		mPaint.setStrokeWidth(CENTER_RADIUS);

		mPaintC = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintC.setStyle(Paint.Style.FILL);
		mPaintC.setShader(lg);
		mPaintC.setStrokeWidth(2);

		
		OvalRect = new RectF();
	}
	
	public void setup(OnColorChangedListener l, int color) {
		mListener = l;
		selectColor = color;
		selectHue = getHue(color);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(CENTER_X * 2, (int) (CENTER_Y * 2));
	}




	private void drawSVRegion(Canvas canvas) {
		final float RESOLUTION = (float)0.01;
    	
    	for(float y = 0; y < 1; y += RESOLUTION) {
        	mChroma = new int[10];

        	int i = 0;
        	for(float x = 0; i < 10; x += 0.1, i+=1) {
        		mChroma[i] = setHSVColor(selectHue, x, y);
        	}
            lg = new LinearGradient(-Rect_X, 0, Rect_X, 0, mChroma, null, Shader.TileMode.CLAMP);
            mPaintC.setShader(lg);

            //canvas.drawRect(OK_X0, OK_X0 + (CENTER_X * y), OK_X1, OK_X0 + (float)(CENTER_X * (y)), mPaintC);
        	canvas.drawLine(-Rect_X, -Rect_X + (CENTER_X * y), Rect_X, -Rect_X + (float)(CENTER_X * (y)), mPaintC);
        }
	}

	
	private RectF OvalRect;

	@Override
	protected void onDraw(Canvas canvas) {
		float r = CENTER_X - mPaint.getStrokeWidth() * 0.5f;

		OvalRect.set(-r, -r, r, r);
		canvas.translate(CENTER_X, CENTER_X);
		canvas.drawOval(OvalRect, mPaint);

		drawSVRegion(canvas);
		
	}

	private float getHue(int color) {
		float hsv[] = new float[3];
		Color.colorToHSV(color, hsv);
		return hsv[0];
	}

	private int ave(int s, int d, float p) {
		return s + java.lang.Math.round(p * (d - s));
	}

	private int interpColor(int colors[], float unit) {
		if (unit <= 0) {
			return colors[0];
		}
		if (unit >= 1) {
			return colors[colors.length - 1];
		}

		float p = unit * (colors.length - 1);
		int i = (int) p;
		p -= i;

		// now p is just the fractional part [0...1) and i is the index
		int c0 = colors[i];
		int c1 = colors[i + 1];
		int r = ave(Color.red(c0), Color.red(c1), p);
		int g = ave(Color.green(c0), Color.green(c1), p);
		int b = ave(Color.blue(c0), Color.blue(c1), p);

		return Color.argb(Alpha, r, g, b);
	}


	private int setHSVColor(float hue, float saturation, float value) {
		float[] hsv = new float[3];
		if (hue >= 360)
			hue = 359;
		else if (hue < 0)
			hue = 0;

		if (saturation > 1)
			saturation = 1;
		else if (saturation < 0)
			saturation = 0;

		if (value > 1)
			value = 1;
		else if (value < 0)
			value = 0;

		hsv[0] = hue;
		hsv[1] = saturation;
		hsv[2] = value;
		
		return Color.HSVToColor(Alpha, hsv);
	}

	private static final float PI = 3.1415927f;
	private boolean inOval;
	private float x;
	private float y;
	@SuppressLint("FloatMath")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		x = event.getX() - CENTER_X;
		y = event.getY() - CENTER_Y;
		float r = (float) (java.lang.Math.sqrt(x * x + y * y));

		
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(r <= CENTER_X) {
	        	if(r > CENTER_X - CENTER_RADIUS){
	        		inOval = true;            		
	        	}else{
	        		inOval = false;
	        	}
	        }
			break;
		case MotionEvent.ACTION_MOVE:
			if (inOval) {
				float angle = (float) java.lang.Math.atan2(y, x);
				// need to turn angle [-PI ... PI] into unit [0....1]
				float unit = angle / (2 * PI);
				if (unit < 0) {
					unit += 1;
				}
				selectColor = interpColor(mColors, unit);
				// mChroma[1] = selectColor;
				selectHue = getHue(selectColor);
				// lg = new LinearGradient(OK_X0, 0, OK_X1, 0, mChroma, null,
				// Shader.TileMode.CLAMP);
				// mPaintC.setShader(lg);
				invalidate();
			} else {
				int selectColor2 = setHSVColor(selectHue, x / CENTER_X*2, y / CENTER_Y*2);
				selectColor = selectColor2;
				invalidate();
			}
			mListener.colorChanged(selectColor);
			break;
		}
		return true;
	}
	
	public String intToString(int num){
		String ans = "#";
		for(int i = 0;i < 8;i++){
			int temp = (num & 0xF0000000);
			temp = temp >>> 28; 
			if(temp <10){
				ans = ans + temp;
			}else{
				switch (temp) {
				case 10:
					ans = ans + "A";
					break;
				case 11:
					ans = ans + "B";
					break;
				case 12:
					ans = ans + "C";
					break;
				case 13:
					ans = ans + "D";
					break;
				case 14:
					ans = ans + "E";
					break;
				case 15:
					ans = ans + "F";
					break;
				}
			}
			num = num<<4;
		}
		return ans;
	}

	public void setColor(int num){
			selectHue = getHue(num);
			selectColor = num;
			mListener.colorChanged(selectColor);
			invalidate();
    }
	
	public void setAlpha(int num){
		Alpha = num;
	}

	public int getColor() {
		return selectColor;
	}

}