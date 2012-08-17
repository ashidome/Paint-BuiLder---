package com.katout.paint;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ColorPickerDialog extends Dialog {
    private OnColorChangedListener mListener;
    private ColorPickerView colorView;
    private int mInitialColor;
    private Context context;
    public EditText text;
    
    //コンスタント
    public ColorPickerDialog(Context context, OnColorChangedListener listener, int initialColor) {
    	super(context);
    	mListener = listener;
    	mInitialColor = initialColor;
    	this.context = context;
    }
    
    //起動時
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color) {
                mListener.colorChanged(color);
                dismiss();
            }
        };

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams lp2 = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        ViewGroup.LayoutParams lp3 = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp3.width = 1;
        
        text = new EditText(context);
        LinearLayout linear = new LinearLayout(context);
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.addView(text,lp2);
        colorView = new ColorPickerView(getContext(), l, mInitialColor);
        linear.addView(colorView, lp3);
        setContentView(linear,lp);
        setTitle("Set Color");
        
        text.setText(colorView.intToString(mInitialColor));
        text.setInputType(InputType.TYPE_CLASS_TEXT);
        text.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try{
					int num = Color.parseColor(s.toString());
					colorView.setColor(num);
					colorView.invalidate();
				}catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO 自動生成されたメソッド・スタブ
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO 自動生成されたメソッド・スタブ
				
			}
		});
    }
    
    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    private class ColorPickerView extends View {

    	private Paint mPaint, mPaintC;
        private Paint mOKPaint;
        private final int[] mColors;
        private int[] mChroma;
        private OnColorChangedListener mListener;
        private Shader sg, lg;
        private int selectColor;
        private float selectHue = 0;
        private int selectAlpha = 0xFF;
         
        ColorPickerView(Context c, OnColorChangedListener l, int color) {
            super(c);
            mListener = l;
            selectColor = color;
            selectHue = getHue(selectColor);
            mColors = new int[] {
                0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
                0xFFFFFF00, 0xFFFF0000
            };
            
            mChroma = new int[] {
            		0xFF000000, 0xFF888888, 0xFFFFFFFF
            };
            
            sg = new SweepGradient(0, 0, mColors, null);
            lg = new LinearGradient(OK_X0, 0, OK_X1, 0, mChroma, null, Shader.TileMode.CLAMP);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setShader(sg);
            mPaint.setStrokeWidth(CENTER_RADIUS);

            mPaintC = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintC.setStyle(Paint.Style.FILL);
            mPaintC.setShader(lg);
            mPaintC.setStrokeWidth(2);
            
            mOKPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOKPaint.setStyle(Paint.Style.FILL);
            mOKPaint.setColor(selectColor);
            mOKPaint.setStrokeWidth(5);
        }
        
        private boolean mTrackingOK;
        private boolean mHighlightOK;
        
        private static final int CENTER_X = 150;
        private static final int CENTER_Y = 150;
        private static final int CENTER_RADIUS = 32;
        private static final float OK_X0 = - CENTER_X/2;
        private static final float OK_X1 =   CENTER_X/2;
        private static final float OK_Y0 = (float) (CENTER_X * 1.6);
        private static final float OK_Y1 = (float) (CENTER_X * 1.9);
        private static final float AlphaX0 = - CENTER_X/1.2f;
        private static final float AlphaY0 = CENTER_X*1.1f;
        private static final float AlphaX1 =CENTER_X/1.2f;
        private static final float AlphaY1 =CENTER_X*1.432f;
        
        /**
         * 内側の明暗入力の四角
         * @param canvas
         */
        private void drawSVRegion(Canvas canvas) {
        	final float RESOLUTION = (float)0.01;
        	
        	for(float y = 0; y < 1; y += RESOLUTION) {
            	mChroma = new int[10];

            	int i = 0;
            	for(float x = 0; i < 10; x += 0.1, i+=1) {
            		mChroma[i] = setHSVColor(selectHue, x, y);
            	}
                lg = new LinearGradient(OK_X0, 0, OK_X1, 0, mChroma, null, Shader.TileMode.CLAMP);
                mPaintC.setShader(lg);

                //canvas.drawRect(OK_X0, OK_X0 + (CENTER_X * y), OK_X1, OK_X0 + (float)(CENTER_X * (y)), mPaintC);
            	canvas.drawLine(OK_X0, OK_X0 + (CENTER_X * y), OK_X1, OK_X0 + (float)(CENTER_X * (y)), mPaintC);
            }
        }
        
        private void drawAlphaRegion(Canvas canvas){
        	Paint paint = new Paint();
        	paint.setStyle(Style.FILL);
        	//バック描画
        	float w = (AlphaY1 - AlphaY0) / 2;
        	for(int i = 0; i < 2; i++){
        		for(int j = 0; j < 10;j++){
        			if((i+j)%2 == 0){
        				paint.setColor(Color.WHITE);
        			}else{
        				paint.setColor(Color.BLACK);
        			}
        			canvas.drawRect(AlphaX0 + (w * j), AlphaY0  + (w * i),
        							AlphaX0 + (w * (j  + 1)), AlphaY0  + (w * (i + 1)), paint);
        		}
        	}
        	
        	//グラデーション情報を作成(0,100)から(240,200)に向けて緑→黄色でグラデーション
            Shader s = new LinearGradient(- CENTER_X/1.2f, CENTER_X*1.1f, CENTER_X/1.2f, CENTER_X*1.1f + w*2, 
            		 mOKPaint.getColor() & 0x00FFFFFF,mOKPaint.getColor() | 0xFF000000,  Shader.TileMode.CLAMP);  
            paint.setShader(s);//グラデーションをセット 
            canvas.drawRect(- CENTER_X/1.2f, CENTER_X*1.1f, CENTER_X/1.2f, CENTER_X*1.1f + w*2, paint);
        }
        
        
        
        @Override 
        protected void onDraw(Canvas canvas) {
        	canvas.translate(CENTER_X, CENTER_X);
        	float padding = 5;
        	
        	
            float r = CENTER_X - mPaint.getStrokeWidth() * 0.8f;
            
            
            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
            
            drawSVRegion(canvas);
            drawAlphaRegion(canvas);

            canvas.drawRoundRect(new RectF(OK_X0, OK_Y0, OK_X1, OK_Y1), 5, 5, mOKPaint);
            
            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(20);
            textPaint.setAntiAlias(true);
            //canvas.drawText("OK", 0 - 12, (float) (CENTER_X * 1.2) + 22, textPaint);
            canvas.drawText("OK", 0 - 14, (float) (OK_Y0 + OK_Y1)/2 + 2, textPaint);

            if (mTrackingOK) {
                int c = mOKPaint.getColor();
                mOKPaint.setStyle(Paint.Style.STROKE);
                
                if (mHighlightOK) 
                    mOKPaint.setAlpha(0xFF);
                else 
                    mOKPaint.setAlpha(0x80);

                //canvas.drawCircle(0, 0, CENTER_RADIUS + mOKPaint.getStrokeWidth(), mOKPaint);
                canvas.drawRoundRect(new RectF(OK_X0 - padding, OK_Y0 - padding, OK_X1 + padding, OK_Y1 + padding), 5, 5, mOKPaint);
                mOKPaint.setStyle(Paint.Style.FILL);
                mOKPaint.setColor(c);
            }                    
        }
        
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(CENTER_X * 2, (int)(CENTER_Y * 3));
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
            int i = (int)p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0),   Color.red(c1),   p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0),  Color.blue(c1),  p);
            
            return Color.argb(a, r, g, b);
        }
        
        
        private int setHSVColor(float hue, float saturation, float value) {
            float[] hsv = new float[3];
            if(hue >= 360)
            	hue = 359;
            else if(hue < 0)
            	hue = 0;

            if(saturation > 1)
            	saturation = 1;
            else if(saturation < 0)
            	saturation = 0;
            
            if(value > 1)
            	value = 1;
            else if(value < 0)
            	value = 0;

            hsv[0] = hue;
            hsv[1] = saturation;
            hsv[2] = value;
            
            return Color.HSVToColor(hsv);
        }

        private static final float PI = 3.1415927f;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - CENTER_X;
            float y = event.getY() - CENTER_Y;
            float r = (float)(java.lang.Math.sqrt(x*x + y*y));
            boolean inOK = false;
            boolean inAlpha = false;
            boolean inOval = false;
            boolean inRect = false;
            
            if(r <= CENTER_X) {
            	if(r > CENTER_X - CENTER_RADIUS)
            		inOval = true;            		
            	else if(x >= OK_X0 && x < OK_X1 && y >= OK_X0 && y < OK_X1)
            		inRect = true;
            }
            else if(x >= OK_X0 && x < OK_X1 && y >= OK_Y0 && y < OK_Y1){
            	inOK = true;
            }
            if(x >= AlphaX0 && x < AlphaX1 && y >= AlphaY0 && y < AlphaY1){
            	inAlpha = true;
            }
            	
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTrackingOK = inOK;
                    if (inOK) {
                        mHighlightOK = true;
                        invalidate();
                        break;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (mTrackingOK) {
                        if (mHighlightOK != inOK) {
                            mHighlightOK = inOK;
                            invalidate();
                        }
                    }else if(inOval) {
                        float angle = (float)java.lang.Math.atan2(y, x);
                        // need to turn angle [-PI ... PI] into unit [0....1]
                        float unit = angle/(2*PI);
                        if (unit < 0) {
                            unit += 1;
                        }
                        selectColor = interpColor(mColors, unit);
                        mOKPaint.setColor(selectColor);
                        mOKPaint.setAlpha(selectAlpha);
                        //mChroma[1] = selectColor;
                        selectHue = getHue(selectColor);
                        //lg = new LinearGradient(OK_X0, 0, OK_X1, 0, mChroma, null, Shader.TileMode.CLAMP);
                        //mPaintC.setShader(lg);
                        invalidate();
                    }else if(inRect){
                    	int selectColor2 = setHSVColor(selectHue, (x - OK_X0)/CENTER_X, (y - OK_X0)/CENTER_Y);
                    	selectColor = selectColor2;
                        mOKPaint.setColor(selectColor);
                        mOKPaint.setAlpha(selectAlpha);
                        invalidate();
                    }else if(inAlpha){
                    	selectAlpha = (int)((x + AlphaX1) / (AlphaX1 - AlphaX0) * 255);
                    	mOKPaint.setAlpha(selectAlpha);
                    	invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTrackingOK) {
                        if (inOK) {
                            mListener.colorChanged(mOKPaint.getColor());
                        }
                        mTrackingOK = false;    // so we draw w/o halo
                        invalidate();
                    }
                    break;
            }
            ColorPickerDialog.this.text.setText(intToString(mOKPaint.getColor()));
            return true;
        }
        
        public void setColor(int num){
        	mOKPaint.setColor(num);
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
    }

}