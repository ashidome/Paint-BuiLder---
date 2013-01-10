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
import android.widget.Toast;

import com.katout.paint.R;
import com.katout.paint.draw.MainActivity;

public class DrawBookActivity extends Activity implements OnClickListener{

	private ViewPager viewPager;
	private View other;
	private PagerAdapter pa;
	private String path;

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		Intent intent = this.getIntent();
		path = intent.getStringExtra("path");
		if(path == null){
			path = Environment.getExternalStorageDirectory().toString() + "/paint";
			BooksAPI.isBook(path);
		}

		setContentView(R.layout.draw_book_layout);

		viewPager = (ViewPager)findViewById(R.id.view_pager);
		pa = new DrawBookPagerAdapter(this,path);
		viewPager.setAdapter(pa);

		//ボタンのセット
		Button create = (Button)findViewById(R.id.create);
		create.setTag("create");
		create.setOnClickListener(this);
		Button delete = (Button)findViewById(R.id.delete);
		delete.setTag("delete");
		delete.setOnClickListener(this);
		Button left = (Button)findViewById(R.id.left);
		left.setTag("left");
		left.setOnClickListener(this);
		Button right = (Button)findViewById(R.id.right);
		right.setTag("right");
		right.setOnClickListener(this);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
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
		else if(v.getTag() == "left"){
			viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
		}
		else if(v.getTag() == "right"){
			viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
		}
	}

	public void pagerLoader(){
		viewPager.removeAllViews();
		viewPager = (ViewPager)findViewById(R.id.view_pager);
		viewPager.setAdapter(pa = new DrawBookPagerAdapter(this,path));
	}
}
