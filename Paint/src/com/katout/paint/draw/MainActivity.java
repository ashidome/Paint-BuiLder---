package com.katout.paint.draw;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.katout.paint.ColorPickerDialog;
import com.katout.paint.OnColorChangedListener;
import com.katout.paint.R;
import com.katout.paint.draw.layer.LayerAdapter;
import com.katout.paint.draw.layer.LayerData;

public class MainActivity extends Activity implements PaintView.MenuLiner {
	private NativeFunction nativefunc;
	private Handler handler;
	private PaintView paint;
	private LinearLayout paint_menu_t;
	private LinearLayout paint_menu_b;
	private int paint_menuH;

	private LinearLayout paint_layer_r;
	private LinearLayout paint_layer_l;
	private ListView layer_r;
	private ListView layer_l;
	private ArrayList<LayerData> layer;
	private LayerAdapter layerAdapter;
	private int paint_menuW;

	private SharedPreferences sp;

	private ColorView colorV_t;
	private ColorView colorV_b;
	
	private SeekBar seek_brush_t;
	private SeekBar seek_brush_b;

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
		paint_menuW = paint_layer_l.getWidth();
		paint.setmenuwSize(paint_menuH, paint_menuW);
		int color = sp.getInt("wid_back_color", Color.argb(65, 0, 0, 0));
		colorV_t.setColor(color);
		colorV_b.setColor(color);
		nativefunc.setColor(color);

	}

	@Override
	public void paintMenuPos(int h, int y, boolean animation) {
		if (y > 20) {
			int visible = paint_menu_t.getVisibility();
			if (visible == View.GONE || visible == View.INVISIBLE
					|| paint_menu_b.getVisibility() == View.VISIBLE) {
				paint_menu_b.setVisibility(View.INVISIBLE);
				paint_menu_t.setVisibility(View.VISIBLE);
			}
			paint_menu_t.layout(paint_menu_t.getLeft(), y - paint_menuH,
					paint_menu_t.getLeft() + paint_menu_t.getWidth(), y);
		} else if (y < -20) {
			int visible = paint_menu_b.getVisibility();
			if (visible == View.GONE || visible == View.INVISIBLE
					|| paint_menu_t.getVisibility() == View.VISIBLE) {
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
		if (x > 20) {
			if (paint_layer_l.getVisibility() == View.GONE
					|| paint_layer_r.getVisibility() == View.INVISIBLE) {
				paint_layer_r.setVisibility(View.INVISIBLE);
				paint_layer_l.setVisibility(View.VISIBLE);
			}
			paint_layer_l.layout((x * paint_menuW / 100) - paint_menuW,
					paint_layer_l.getTop(), (x * paint_menuW / 100),
					paint_layer_l.getTop() + paint_layer_l.getHeight());
		} else if (x < -20) {
			if (paint_layer_r.getVisibility() == View.GONE
					|| paint_layer_l.getVisibility() == View.INVISIBLE) {
				paint_layer_l.setVisibility(View.INVISIBLE);
				paint_layer_r.setVisibility(View.VISIBLE);
			}
			paint_layer_r.layout(w + (x * paint_menuW / 100),
					paint_layer_r.getTop(), w + (x * paint_menuW / 100)
							+ paint_menuW, paint_layer_r.getTop()
							+ paint_layer_r.getHeight());
		} else {
			paint_layer_l.layout(-paint_menuW, paint_layer_l.getTop(), 0,
					paint_layer_l.getTop() + paint_layer_l.getHeight());

			paint_layer_r.layout(w + paint_menuW, paint_layer_r.getTop(), w,
					paint_layer_r.getTop() + paint_layer_r.getHeight());
			paint_layer_l.setVisibility(View.INVISIBLE);
			paint_layer_r.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		paint_menu_t = (LinearLayout) findViewById(R.id.paint_menu_t);
		paint_menu_b = (LinearLayout) findViewById(R.id.paint_menu_b);
		colorV_t = (ColorView) paint_menu_t.findViewById(R.id.colorview);
		colorV_b = (ColorView) paint_menu_b.findViewById(R.id.colorview);
		paint_layer_l = (LinearLayout) findViewById(R.id.layer_menu_l);
		paint_layer_r = (LinearLayout) findViewById(R.id.layer_menu_r);

		LinearLayout layer_l = (LinearLayout)paint_layer_l.findViewById(R.id.layer);
		LinearLayout layer_r = (LinearLayout)paint_layer_r.findViewById(R.id.layer);
		layerAdapter = new LayerAdapter(this, new LinearLayout[]{layer_l,layer_r});
		
		seek_brush_t = (SeekBar) paint_menu_t.findViewById(R.id.seek_brush);
		seek_brush_b = (SeekBar) paint_menu_b.findViewById(R.id.seek_brush);
		
		seek_brush_t.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int brush = seekBar.getProgress();
				seek_brush_b.setProgress(brush);
				nativefunc.setBrushSize(brush);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {}
		});
		
		seek_brush_b.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int brush = seekBar.getProgress();
				seek_brush_t.setProgress(brush);
				nativefunc.setBrushSize(brush);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {}
		});
		
		//初期に１枚のレイヤーを登録
		layerAdapter.addLayer(new LayerData());
//		layerAdapter_r = new LayerAdapter(this, layer_list);
//		layer_l.setAdapter(layerAdapter_l);
//		layer_r.setAdapter(layerAdapter_r);
		SurfaceView surface = (SurfaceView) findViewById(R.id.surfaceView1);
		surface.setZOrderOnTop(false);
		paint = new PaintView(this, surface, this, new EventLisner() {
			@Override
			public boolean startDraw(int x, int y) {
				return nativefunc.startDraw(x, y);
			}

			@Override
			public boolean draw(int x, int y) {
				return nativefunc.draw(x, y);
			}

			@Override
			public boolean setPosition(int x, int y) {
				return nativefunc.setPosition(x, y);
			}

			@Override
			public boolean setRadian(double rad) {
				return true;// nativefunc.setRadian(rad);
			}

			@Override
			public boolean setScale(double scale) {
				return nativefunc.setScale(scale);
			}

			@Override
			public boolean deleteEditLayer() {
				return true;// nativefunc.deleteEditLayer();
			}

			@Override
			public boolean getBitmap(int[] canvas, int width, int height) {
				return nativefunc.getBitmap(canvas, width, height);
			}

			@Override
			public boolean init(int x, int y) {
				return nativefunc.init(x, y);
			}

			@Override
			public void bucket(int x, int y) {
				Log.e("test", "bucket!!");
				nativefunc.bucket(x, y, 255);
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
				paint_layer_l.setVisibility(View.INVISIBLE);
				paint_layer_r.setVisibility(View.INVISIBLE);
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
						//ネイティブに色情報を渡す
						nativefunc.setColor(color);
						colorV_t.setColor(color);
						colorV_b.setColor(color);
					}
				}, sp.getInt("wid_back_color", Color.argb(65, 0, 0, 0)));
		mColorPickerDialog.show();
	}

	public void onAddLayer(View v){
		layerAdapter.addLayer(new LayerData());
	}
	
	public void onBrush(View v){
		paint.setMode(PaintMode.Brush);
	}
	public void onBucket(View v){
		paint.setMode(PaintMode.Bucket);
	}
	public void onEraser(View v){
		paint.setMode(PaintMode.Eraser);
	}
}

