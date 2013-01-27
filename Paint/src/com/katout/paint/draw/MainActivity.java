package com.katout.paint.draw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.katout.paint.ColorPickerDialog;
import com.katout.paint.OnColorChangedListener;
import com.katout.paint.R;
import com.katout.paint.conect.ConnectCore;
import com.katout.paint.conect.RoomDialog;
import com.katout.paint.draw.RecompositionAsyncTask.RePreviewLisner;
import com.katout.paint.draw.brush.SelectBrushDialog;
import com.katout.paint.draw.layer.LayerAdapter;

public class MainActivity extends Activity implements PaintView.MenuLiner ,RePreviewLisner{
	private NativeFunction			nativefunc;
	private ConnectCore				connectCore;
	private Handler					handler;
	private SurfaceView				surface;
	private PaintView				paint;
	private LinearLayout			paint_menu_t;
	private LinearLayout			paint_menu_b;
	private int						paint_menuH;
	
	private String 					path;
	private String					filename;
	private boolean 				new_flag;
	
	private LinearLayout			paint_layer_l;
	private LayerAdapter			layerAdapter;
	private int						paint_menuW;

	private SharedPreferences		sp;

	private ColorView				colorV_t;
	private ColorView				colorV_b;

	private SeekBar					seek_brush_t;
	private SeekBar					seek_brush_b;
	
	private Spinner					spinner_l;
	private boolean					spinerflag;
	
	private SeekBar					alpher_seek;
	private boolean	preview_Change_flag;
	private ImageView				preview;
	private int	previewwidth;
	private int	previewheight;
	private Bitmap	preview_bitmap;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = this.getIntent();
		path = intent.getStringExtra("path");
		new_flag = intent.getBooleanExtra("newflag", false);
		filename = intent.getStringExtra("name");
		
		nativefunc = new NativeFunction();
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		handler = new Handler();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		connectCore = new ConnectCore(this, handler, new ConnectCore.ShareMessageInterface() {
			@Override
			public void getMassage(ShareMessage message) {

				nativefunc.joint(message.layernum, message.bmp, message.width, 
						message.height, message.f, message.size, message.color, 
						message.points, message.points_size, message.mode);
			}
		});
		preview_Change_flag = true;

	}

	@Override
	protected void onPause() {
		super.onPause();
		paint.tread_flag = false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus){
			paint_menuH = paint_menu_t.getHeight();
			paint_menuW = paint_layer_l.getWidth();
			paint.setmenuwSize(paint_menuH, paint_menuW);
		}
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
			if (paint_layer_l.getVisibility() == View.GONE ||
					paint_layer_l.getVisibility() ==View.INVISIBLE) {
				paint_layer_l.setVisibility(View.VISIBLE);
			}
			paint_layer_l.layout((x * paint_menuW / 100) - paint_menuW,
					paint_layer_l.getTop(), (x * paint_menuW / 100),
					paint_layer_l.getTop() + paint_layer_l.getHeight());
			if(x>60 & preview_Change_flag){
				setPreView();
				
			}
		} else {
			paint_layer_l.layout(-paint_menuW, paint_layer_l.getTop(), 0,
					paint_layer_l.getTop() + paint_layer_l.getHeight());
			paint_layer_l.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		spinerflag = true;
		paint_menu_t = (LinearLayout) findViewById(R.id.paint_menu_t);
		paint_menu_b = (LinearLayout) findViewById(R.id.paint_menu_b);
		colorV_t = (ColorView) paint_menu_t.findViewById(R.id.colorview);
		colorV_b = (ColorView) paint_menu_b.findViewById(R.id.colorview);
		paint_layer_l = (LinearLayout) findViewById(R.id.layer_menu_l);
		preview = (ImageView)findViewById(R.id.preview);
		

		LinearLayout layer_l = (LinearLayout) paint_layer_l
				.findViewById(R.id.layer);
		layerAdapter = new LayerAdapter(this, layer_l,nativefunc);
		alpher_seek = (SeekBar)findViewById(R.id.seek_alphr);

		seek_brush_t = (SeekBar) paint_menu_t.findViewById(R.id.seek_brush);
		seek_brush_b = (SeekBar) paint_menu_b.findViewById(R.id.seek_brush);
		
		spinner_l = (Spinner)paint_layer_l.findViewById(R.id.spinner1);
		spinner_l.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
												View view, int position, long id) {
						Log.d("test", "onItemSelected:" + position);
						if(!spinerflag){
							layerAdapter.setLayermode(position);
							nativefunc.setLayerMode(position);
							new RecompositionAsyncTask(MainActivity.this, nativefunc,MainActivity.this).execute();
						}
						spinerflag = false;
					}
					@Override
					public void onNothingSelected(AdapterView<?> arg0) {}
				});
		alpher_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				layerAdapter.setAlpher(seekBar.getProgress());
				nativefunc.setLayerAlpha(seekBar.getProgress());
				new RecompositionAsyncTask(MainActivity.this, nativefunc,MainActivity.this).execute();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {}
		});

		
		
		seek_brush_t.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int brush = seekBar.getProgress()+1;
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
				int brush = seekBar.getProgress()+1;
				seek_brush_t.setProgress(brush);
				nativefunc.setBrushSize(brush);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {}
		});

		//初期に１枚のレイヤーを登録
		onAddLayer(null);
		// layerAdapter_r = new LayerAdapter(this, layer_list);
		// layer_l.setAdapter(layerAdapter_l);
		// layer_r.setAdapter(layerAdapter_r);
		surface = (SurfaceView) findViewById(R.id.surfaceView1);
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
				return nativefunc.deleteEditLayer();
			}

			@Override
			public boolean getBitmap(Bitmap map) {
				return nativefunc.getBitmap(map);
			}

			@Override
			public boolean init(int w, int h) {
				boolean init_flag = false;

				if(!new_flag){
					try {
						File srcFile = new File(path +"/"+ filename);
		    			FileInputStream fis;
						fis = new FileInputStream(srcFile);
						Bitmap bitmap = BitmapFactory.decodeStream(fis);
						int x = bitmap.getWidth();
						int y = bitmap.getHeight();
						int[] map = new int[x*y];
						bitmap.getPixels(map, 0, x, 0, 0, x, y);
						init_flag =  nativefunc.init(x, y,1,map);
						layerAdapter.setPreview();
					} catch (FileNotFoundException e) {
						init_flag =  nativefunc.init(w, h, 0, null);
					}
				}else {
					init_flag =  nativefunc.init(w, h, 0, null);
				}
				if(!init_flag){
					//初期化済みだった場合
					int num = nativefunc.getLayerNum();
					int[] alphas = new int[num];
					int[] modes = new int[num];
					nativefunc.getLayersData(modes, alphas);
					ArrayList<LinearLayout> layouts = layerAdapter.init_layers(modes, alphas);
					for(LinearLayout layout: layouts){
						layout.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								layerOnclickEvent(v);
							}
						});
					}
					int c = nativefunc.getCurrentNum();
					layerAdapter.selectLayer(c);
				}
				int color = sp.getInt("wid_back_color", Color.argb(255, 51, 181, 229));
				colorV_t.setColor(color);
				colorV_b.setColor(color);
				nativefunc.setColor(color);
				
				return true;
			}

			@Override
			public void bucket(int x, int y) {
				Log.e("test", "bucket!!");
				nativefunc.bucket(x, y, 255);
			}

			@Override
			public void endDraw(ArrayList<Integer> list) {
				nativefunc.endDraw();
				preview_Change_flag = true;
				if(connectCore.getInRoom()){
					ShareMessage j_message = new ShareMessage();
					//ブラシサイズ取得
					int[] size = new int[3];
					nativefunc.getBrushRawSize(size);
					j_message.bmp = new char[size[0] * size[1]];
					//ブラシマップ取得
					nativefunc.getBrushRawMap(j_message.bmp);
					
					j_message.color = colorV_t.getColor();
					j_message.f = size[2];
					j_message.width = size[0];
					j_message.height = size[1];
					j_message.size = seek_brush_b.getProgress();
					j_message.layernum =  layerAdapter.getCurrentlayer();
					j_message.points = new int[list.size()]; 
					PaintMode mode = paint.getMode();
					j_message.mode = 0;
					if(mode == PaintMode.Eraser){
						j_message.mode = 1;
					}
					int i = 0;
					for(Iterator<Integer> iter = list.iterator();iter.hasNext();){ 
						j_message.points[i] = ((Integer)iter.next()).intValue(); 
						i++;
					}
					j_message.points_size = j_message.points.length;
					String mes = j_message.getMessage();
					connectCore.shareMessage(mes);
				}
				layerAdapter.setPreviewflag();
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
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, event);
		} else {
			myFinish();
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
						// ネイティブに色情報を渡す
						nativefunc.setColor(color);
						colorV_t.setColor(color);
						colorV_b.setColor(color);
					}
				}, sp.getInt("wid_back_color", Color.argb(255, 51, 181, 229)));
		mColorPickerDialog.show();
	}

	public void onAddLayer(View v) {
		//ようは初期レイヤーの追加ではネイティブ呼ばない
		if(v != null){
			if(nativefunc.addLayer()){
				add();
			}
		}else{
			add();
		}
	}
	private void add(){
		LinearLayout layouts = layerAdapter.addLayer();
		layouts.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//レイヤーが選択された時
				layerOnclickEvent(v);
			}
		});
	}
	private void layerOnclickEvent(View v){
		//レイヤーが選択された時
		int temp = (Integer)v.getTag();
		nativefunc.selectLayer(temp);
		layerAdapter.selectLayer(temp);
		spinerflag = true;
		spinner_l.setSelection(layerAdapter.getLayermode());
		alpher_seek.setProgress(layerAdapter.getLayerAlpha());
		Toast.makeText(MainActivity.this, "tag:"+ temp, Toast.LENGTH_SHORT).show();
	}
	
	public void ondeleteLayer(View v) {
		boolean temp = layerAdapter.deleteLayer();
		if(temp){
			nativefunc.deleteLayer();
			new RecompositionAsyncTask(MainActivity.this, nativefunc,this).execute();
		}
	}

	public void onFile(View v) {
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// 表示項目の配列
		final CharSequence[] colors = { "初期位置", "保存" , "終了" };
		// タイトルを設定
		alertDialogBuilder.setTitle("メニュー");
		// 表示項目とリスナの設定
		alertDialogBuilder.setItems(colors, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					paint.setScale(1.0);
					paint.setPos(0, 0);
					nativefunc.setPositionD();
					
					
					break;
				case 1:
					if(new_flag){
						final EditText ed = new EditText(MainActivity.this);
						new AlertDialog.Builder(MainActivity.this)
						.setTitle("ファイルの作成")
						.setView(ed)
						.setPositiveButton("追加", new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dia,int which){
								filename = ed.getText().toString();
								save();
							}
						})
						.setNegativeButton("キャンセル", null)
						.show();
					}else{
						save();
					}
					
					break;
				case 2:
					myFinish();
					break;
				default:
					break;
				}
			}
		});

		// ダイアログを表示
		alertDialogBuilder.create().show();
	}

	public void onBrush(View v) {
		SelectBrushDialog dialog = new SelectBrushDialog(this,seek_brush_b.getProgress(),nativefunc);
		dialog.show();
		
		paint.setMode(PaintMode.Brush);
		nativefunc.setMode(0);
		
	}

	public void onBucket(View v) {
		paint.setMode(PaintMode.Bucket);
	}

	public void onEraser(View v) {
		paint.setMode(PaintMode.Eraser);
		nativefunc.setMode(1);
	}
	
	public void onShere(View v){
		if(connectCore.getInRoom()){
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	        // アラートダイアログのタイトルを設定します
	        alertDialogBuilder.setTitle("入室中");
	        // アラートダイアログのメッセージを設定します
	        alertDialogBuilder.setMessage("退出しますか？");
	        // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
	        alertDialogBuilder.setPositiveButton("YES",
	                new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    	connectCore.exit_room();
	                    }
	                });
	        // アラートダイアログの否定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
	        alertDialogBuilder.setNegativeButton("NO",null);
	        // アラートダイアログのキャンセルが可能かどうかを設定します
	        alertDialogBuilder.setCancelable(true);
	        AlertDialog alertDialog = alertDialogBuilder.create();
	        // アラートダイアログを表示します
	        alertDialog.show();
		}else{
		    RoomDialog dialog = new RoomDialog(this,connectCore);
		    dialog.show();
		}
		
	}
	
	@Override
	protected void onDestroy() {
		Log.d("test", "onDestroy");
		if(connectCore.getInRoom()){
			connectCore.exit_room();
		}
		
		super.onDestroy();
	}
	
	private void save(){
		try{
			int h =nativefunc.getCanvasHeight();
			int w = nativefunc.getCanvasWidth();

			int[] canvas = new int[h*w];
			nativefunc.getRawdata(canvas);
			Bitmap map = Bitmap.createBitmap(canvas, w, h, Bitmap.Config.ARGB_8888);

			 // 保存処理開始
			 FileOutputStream fos = null;
			 if(filename.indexOf(".")>0){
				 filename = filename.subSequence(0, filename.indexOf(".")).toString();
			 }
			 fos = new FileOutputStream(new File(path,filename + ".png"));

			 // pngで保存
			 map.compress(CompressFormat.PNG, 100, fos);

			 // 保存処理終了
			 fos.close();
			 Toast.makeText(MainActivity.this, "保存完了", Toast.LENGTH_LONG).show();


		}catch (Exception e) {
			Toast.makeText(MainActivity.this, "メモリー不足により保存準備ができません¥nレイヤーを減らしてください", Toast.LENGTH_LONG).show();
			// TODO: handle exception
		}
	}
	
	private void myFinish() {
		paint.tread_flag = false;
		while (paint.thread.isAlive())
			;
		nativefunc.destructor();
		finish();
	}
	
	@Override
	public void setPreView(){
		previewwidth = preview.getWidth();
		previewheight = preview.getHeight();

		if(preview_bitmap == null){
			preview_bitmap= Bitmap.createBitmap(previewwidth, previewheight, Bitmap.Config.ARGB_8888);
		}
		
		nativefunc.getPreview(-1, preview_bitmap);
		handler.post(new Runnable() {			
			@Override
			public void run() {
				preview.setImageBitmap(preview_bitmap);
			}
		});
		layerAdapter.setPreview(); 
		preview_Change_flag = false;
	}
	
	public void onVisibility(View v) {
		if(layerAdapter.getLayermode()==12){
			spinner_l.setSelection(0);
		}else{
			spinner_l.setSelection(12);
		}
		
	}
}
