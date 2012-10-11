package com.katout.paint.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View{
	private Paint paint;
	private int selectColor;
	private int w = 60;
	private int h = 60;

	public ColorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		selectColor = 0x5500FF99;
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		w = getWidth();
		h = getHeight();
		invalidate();
	}
	
	public void setColor(int color){
		this.selectColor = color;
		invalidate();
	}
	
	public int getColor(){
		return selectColor;
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		// TODO 自動生成されたメソッド・スタブ
		super.onWindowVisibilityChanged(visibility);
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		paint.setColor(Color.BLACK);
		canvas.drawRect(0, 0, w/2, h/2, paint);
		canvas.drawRect(w/2, h/2,w,h,paint);
		
		paint.setColor(selectColor);
		canvas.drawRect(0, 0, w, h, paint);
		
		super.onDraw(canvas);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO 自動生成されたメソッド・スタブ
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	

}
