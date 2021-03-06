package pbl.paint;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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

public class DrawBookPagerAdapter extends PagerAdapter{
	static int NUM_VIEW;
	LayoutInflater mInflater;
	File dir;
	File[] files;
	private Context context;
	
	public DrawBookPagerAdapter(Context context,String keypath){
		dir=new File(keypath);
		files=dir.listFiles();
		this.context=context;
		mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	//ViewPagerへViewの追加
	@Override
	public Object instantiateItem(View collection, int position){
		ViewPager pager = (ViewPager)collection;
		View[] views = new View[files.length];
		
		for(int i = 0; i < files.length; i++){
			views[i] = mInflater.inflate(R.layout.draw_book_view, pager, false);
			views[i].setTag(files[i].getPath());
			
			//サムネイルの作成
			ImageView img = (ImageView)views[i].findViewById(R.id.file_image);
			FileInputStream f_input = null;
			BufferedInputStream buf = null;
			try{
				f_input = new FileInputStream(files[i].getPath());
				buf = new BufferedInputStream(f_input);
				Bitmap bitmap = BitmapFactory.decodeStream(buf);
				img.setImageBitmap(bitmap);
				f_input.close();
				buf.close();
			}
			catch(FileNotFoundException e){
				e.printStackTrace();
			}
			catch(IOException e){
				e.printStackTrace();
			}
			
			Button file_name = (Button)views[i].findViewById(R.id.file_name);
			file_name.setText(files[i].getName());
			file_name.setTag(files[i].getPath());
			file_name.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent=new Intent(context,TestActivity.class);
					intent.putExtra("path",v.getTag().toString()+"/");
					context.startActivity(intent);
				}
			});
			pager.addView(views[i], i);
		}
		return views[position];
	}
	
	//ViewPagerからViewを削除
	@Override
	public void destroyItem(View collection,int position,Object view){
		((ViewPager)collection).removeView((View)view);
	}
	
    //ページ数を返す
	@Override
	public int getCount() {	
		return files.length;
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
