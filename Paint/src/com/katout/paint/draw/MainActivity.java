package com.katout.paint.draw;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.katout.paint.ColorPickerDialog;
import com.katout.paint.OnColorChangedListener;
import com.katout.paint.R;

public class MainActivity extends Activity implements PaintView.MenuLiner {
	private NativeFunction nativefunc;
	private Handler handler;
	private PaintView paint;
	private LinearLayout paint_menu_t;
	private int paint_menuH;
	private LinearLayout paint_menu_b;
	private SharedPreferences sp;

	private ColorView colorV_t;
	private ColorView colorV_b;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		handler = new Handler();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		nativefunc = new NativeFunction();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		paint_menuH = paint_menu_t.getHeight();
		paint.setmenuwSize(paint_menuH, 100);
		colorV_t.setColor(sp.getInt("wid_back_color", Color.argb(65, 0, 0, 0)));
		colorV_b.setColor(sp.getInt("wid_back_color", Color.argb(65, 0, 0, 0)));

	}

	@Override
	public void paintMenuPos(int h, int y, boolean animation) {
		if (y > 20) {
			int visible = paint_menu_t.getVisibility();
			if (visible == View.GONE
					|| paint_menu_b.getVisibility() == View.INVISIBLE) {
				paint_menu_b.setVisibility(View.INVISIBLE);
				paint_menu_t.setVisibility(View.VISIBLE);
			}
			paint_menu_t.layout(paint_menu_t.getLeft(), y - paint_menuH,
					paint_menu_t.getLeft() + paint_menu_t.getWidth(), y);
		} else if (y < -20) {
			if (paint_menu_b.getVisibility() == View.GONE
					|| paint_menu_b.getVisibility() == View.INVISIBLE) {
				paint_menu_t.setVisibility(View.INVISIBLE);
				paint_menu_b.setVisibility(View.VISIBLE);
			}
			paint_menu_b.layout(paint_menu_b.getLeft(), h + y,
					paint_menu_b.getLeft() + paint_menu_b.getWidth(), h + y
							+ paint_menuH);
		} else {
			paint_menu_t.layout(paint_menu_t.getLeft(), -paint_menuH,
					paint_menu_t.getLeft() + paint_menu_t.getWidth(), 0);

			paint_menu_b.layout(paint_menu_b.getLeft(), h,
					paint_menu_b.getLeft() + paint_menu_b.getWidth(), h
							+ paint_menuH);
			paint_menu_t.setVisibility(View.INVISIBLE);
			paint_menu_b.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void layerMenuPos(int w, int x, boolean animation) {

	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		paint_menu_t = (LinearLayout) findViewById(R.id.paint_menu_t);
		paint_menu_b = (LinearLayout) findViewById(R.id.paint_menu_b);
		colorV_t = (ColorView) paint_menu_t.findViewById(R.id.colorview);
		colorV_b = (ColorView) paint_menu_b.findViewById(R.id.colorview);
		SurfaceView surface = (SurfaceView) findViewById(R.id.surfaceView1);
		paint = new PaintView(this, surface, this, new EventLisner() {

			@Override
			public boolean startDraw(int x, int y) {
				return true;//nativefunc.startDraw(x, y);
			}

			@Override
			public boolean draw(int x, int y) {
				return true;//nativefunc.draw(x, y);
			}

			@Override
			public boolean setPosition(int x, int y) {
				return true;//nativefunc.setPosition(x, y);
			}

			@Override
			public boolean setRadian(double rad) {
				return true;//nativefunc.setRadian(rad);
			}

			@Override
			public boolean setScale(double scale) {
				return true;//nativefunc.setScale(scale);
			}

			@Override
			public boolean deleteEditLayer() {
				return true;//nativefunc.deleteEditLayer();
			}

			@Override
			public boolean getBitmap(int[] canvas, int width, int height) {
				return false;//nativefunc.getBitmap(canvas, width, height);
			}
		});
	}

	@Override
	public void setup() {
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.post(new Runnable() {
			@Override
			public void run() {
				paint_menu_t.setVisibility(View.INVISIBLE);
				paint_menu_b.setVisibility(View.INVISIBLE);
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, event);
		} else {
			paint.tread_flag = false;
			while (paint.thread.isAlive())
				;
			finish();
			return false;
		}
	}

	public void onSelectColor(View v) {
		ColorPickerDialog mColorPickerDialog;

	    
		mColorPickerDialog = new ColorPickerDialog(this,
				new OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						Editor ed = sp.edit();
						ed.putInt("wid_back_color", color);
						ed.commit();
						// TODO 色の設定
						colorV_t.setColor(color);
						colorV_b.setColor(color);
					}
				}, sp.getInt("wid_back_color", Color.argb(65, 0, 0, 0)));
		WindowManager.LayoutParams lp = mColorPickerDialog.getWindow().getAttributes();  
		DisplayMetrics metrics = getResources().getDisplayMetrics();  
	    int dialogWidth = (int) (metrics.widthPixels * 0.8);  
	    lp.width = dialogWidth;  
	    mColorPickerDialog.getWindow().setAttributes(lp); 
	    mColorPickerDialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		mColorPickerDialog.show();
	}
}
