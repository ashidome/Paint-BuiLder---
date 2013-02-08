package com.katout.paint.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Dropper {
	private static final int SIZE = 100;
	
	private Paint paint;
	private Bitmap bitmap;
	private int mem_color;
	private RectF oval1;

	public Dropper(Bitmap bitmap) {
		this.bitmap = bitmap;
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(SIZE/3);
		oval1 = new RectF(0, 0, 1, 1);
	}

	public int getcolor(int x, int y) {
		return bitmap.getPixel(x, y);
	}
	
	public void setColor(int mem_color) {
		this.mem_color = mem_color;
	}

	public void drawCircle(Canvas canvas, int x, int y) {
		oval1.set(x - SIZE, y - SIZE, x + SIZE, y + SIZE);

		paint.setColor(mem_color);
		canvas.drawArc(oval1, 180, 360, true, paint);

		paint.setColor(getcolor(x, y));
		canvas.drawArc(oval1, 0, 180, true, paint);
	}
}
