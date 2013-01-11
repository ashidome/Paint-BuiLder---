package com.katout.paint.book;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.katout.paint.R;
import com.katout.paint.draw.MainActivity;

public class DrawBookPagerAdapter extends PagerAdapter{
	static int NUM_VIEW;
	LayoutInflater mInflater;
	File dir;
	ArrayList<File> files;
	private Context context;
	
	public DrawBookPagerAdapter(Context context,String keypath){
		dir=new File(keypath);
		files= new ArrayList<File>();
		File[] temp = dir.listFiles();
		for(int i = 0; i < temp.length;i++){
			if(BooksAPI.isImage(temp[i].toString())){
				files.add(temp[i]);
			}
		}
		
		this.context=context;
		mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	
	//ViewPagerへViewの追加
	@Override
	public Object instantiateItem(View collection, int position){
		ViewPager pager = (ViewPager)collection;
		ArrayList<View> views = new ArrayList<View>();
		for(int i = 0; i < files.size(); i++){
			
			views.add(mInflater.inflate(R.layout.draw_book_view, pager, false));
			views.get(i).setTag(files.get(i).getPath());
			//サムネイルの作成
			ImageView img = (ImageView)views.get(i).findViewById(R.id.file_image);
			Bitmap bitmap = ImageCache.getImage(files.get(i).getPath());
			if(bitmap == null){
				FileInputStream f_input = null;
				BufferedInputStream buf = null;
				try{
					f_input = new FileInputStream(files.get(i).getPath());
					buf = new BufferedInputStream(f_input);
					ImageCache.getImage(files.get(i).getPath());
					bitmap = BitmapFactory.decodeStream(buf);
					f_input.close();
					buf.close();
					ImageCache.setImage(files.get(i).getPath(), bitmap);
				}
				catch(FileNotFoundException e){
					e.printStackTrace();
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
			img.setImageBitmap(bitmap);
			
			
			final Button file_name = (Button)views.get(i).findViewById(R.id.file_name);
			file_name.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Button button = (Button)v;
					Intent intent=new Intent(context,MainActivity.class);
					intent.putExtra("path", button.getTag().toString());
					intent.putExtra("newflag",false);//新規押した時、フォルダのパス
					intent.putExtra("name", button.getText().toString());
					context.startActivity(intent);
				}
			});
			img.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent=new Intent(context,MainActivity.class);
					intent.putExtra("path", file_name.getTag().toString());
					intent.putExtra("newflag",false);//新規押した時、フォルダのパス
					intent.putExtra("name", file_name.getText().toString());
					context.startActivity(intent);
				}
			});
			file_name.setText(files.get(i).getName());
			file_name.setTag(files.get(i).getParent());
			pager.addView(views.get(i), i);
		}
		return views.get(position);
	}
	
	//ViewPagerからViewを削除
	@Override
	public void destroyItem(View collection,int position,Object view){
		((ViewPager)collection).removeView((View)view);
	}
	
    //ページ数を返す
	@Override
	public int getCount() {	
		return files.size();
	}
	
	//ページを構成するViewの判定
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (View)arg1;
	}
	
	//状態を復帰するためのメソッド
	@Override
	public void restoreState(Parcelable parcel,ClassLoader classloader){
		
	}
	
	//状態を保存するためのメソッド
	@Override
	public Parcelable saveState(){
		return null;
	}
	
	//Viewの切り替わりが始まるタイミングで呼ばれる
	@Override
	public void startUpdate(View collection){
		
	}
	
	//Viewが切り替わったタイミングで呼ばれる
	@Override
	public void finishUpdate(View collection){
		
	}
}
