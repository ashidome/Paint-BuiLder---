//お絵かき帳のアクティビティ
package com.katout.paint.book;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.katout.paint.R;
import com.katout.paint.draw.MainActivity;

public class DrawBookActivity extends Activity implements OnClickListener{

	private ViewPager viewPager;
	private View other;
	private PagerAdapter pa;
	private String path;
	private SeekBar seek;
	private int pagenum;

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		Intent intent = this.getIntent();
		path = intent.getStringExtra("path");
		BooksAPI.makeDirectory("paint", Environment.getExternalStorageDirectory().toString());
		if(path == null){
			path = Environment.getExternalStorageDirectory().toString() + "/paint";
		}

		setContentView(R.layout.draw_book_layout);
		seek = (SeekBar)findViewById(R.id.seek);
		seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {}
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				if(pagenum > 1){
					viewPager.setCurrentItem(arg1);
				}else{
					arg0.setProgress(1);
				}
				
			}
		});

		//ボタンのセット
		Button create = (Button)findViewById(R.id.create);
		create.setTag("create");
		create.setOnClickListener(this);
		Button delete = (Button)findViewById(R.id.delete);
		delete.setTag("delete");
		delete.setOnClickListener(this);
//		Button left = (Button)findViewById(R.id.left);
//		left.setTag("left");
//		left.setOnClickListener(this);
//		Button right = (Button)findViewById(R.id.right);
//		right.setTag("right");
//		right.setOnClickListener(this);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		pagerLoader();
		pagenum = pa.getCount();
		
		if(pagenum < 2){
			seek.setMax(2);
			seek.setProgress(1);
		}else{
			seek.setMax(pagenum-1);
			seek.setProgress(0);
		}
		
	}

	@Override
	public void onClick(View v){
		if(v.getTag() == "create"){//新規を押した場合の処理
			Intent intent=new Intent(DrawBookActivity.this,MainActivity.class);
			intent.putExtra("path", path);
			intent.putExtra("newflag",true);//新規押した時、フォルダのパス
			intent.putExtra("name", "");
			DrawBookActivity.this.startActivity(intent);
		}
		else if(v.getTag() == "delete"){
			File file = new File(path);
			File[] dir = file.listFiles();
			if(dir.length != 0){
				new AlertDialog.Builder(DrawBookActivity.this)
				.setTitle("本当に削除しますか？")
				.setPositiveButton("OK",new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						other = viewPager.getChildAt(viewPager.getCurrentItem());
						File file = new File(other.getTag().toString());
						if(file.delete()){
							Toast.makeText(DrawBookActivity.this,"削除しました",Toast.LENGTH_SHORT).show();
						}
						pagerLoader();
					}
				})
				.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//何もしない
					}
				})
				.show();
			}
			else{
				Toast.makeText(this,"削除するファイルがありません",Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void pagerLoader(){
		if(viewPager != null){
			viewPager.removeAllViews();
		}
		viewPager = (ViewPager)findViewById(R.id.view_pager);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				if(pagenum > 1){
					seek.setProgress(arg0);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO 自動生成されたメソッド・スタブ
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO 自動生成されたメソッド・スタブ
				
			}
		});
		viewPager.setAdapter(pa = new DrawBookPagerAdapter(this,path));
	}
	
	public void onBack(View v) {
		Intent intent=new Intent(this,BookShelfActivity.class);
		intent.putExtra("path", new File(path).getParent().toString());
		startActivity(intent);
		finish();
	}
}
