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
import android.widget.Toast;

public class SVPagerAdapter extends PagerAdapter{
	private LayoutInflater mInflater;
	private File dir;
	private File[] files;
	private Context context;
	
	public SVPagerAdapter(Context context,String keypath){
		dir = new File(keypath);
		files = dir.listFiles();
		this.context = context;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public void destroyItem(View collection,int position,Object view){
		((ViewPager)collection).removeView((View)view);
	}
	
	@Override
	public void finishUpdate(View collection){
		
	}
	
    //作る数
	@Override
	public int getCount() {
		return files.length;
	}
	
	@Override
	public Object instantiateItem(View collection,int position){
		ViewPager pager=(ViewPager)collection;
		View[] views=new View[files.length];
		for(int i = 0; i < files.length; i++){
			views[i] = mInflater.inflate(R.layout.selectview, pager, false);
			views[i].setTag(files[i].getPath());
			
			ImageView img=(ImageView)views[i].findViewById(R.id.imageView1);
			FileInputStream f=null;
			BufferedInputStream buf=null;
			try{
				f=new FileInputStream(files[i].getPath());
				buf=new BufferedInputStream(f);
				Bitmap bitmap=BitmapFactory.decodeStream(buf);
				img.setImageBitmap(bitmap);
				f.close();
				buf.close();
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
			
			
			Button button=(Button)views[i].findViewById(R.id.button1);
			button.setText(files[i].getName());
			button.setTag(files[i].getPath());
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent=new Intent(context,TestActivity.class);
		    		intent.putExtra("path",v.getTag().toString()+"/");
		    		intent.putExtra("newflag", false);//パスは新規じゃない画像ファイルのパス
					context.startActivity(intent);
		    		//showToast(v.getTag().toString());
		    		
				}
				
			});
			pager.addView(views[i], i);
		}
		return views[position];
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0==(View)arg1;
	}
	@Override
	public void restoreState(Parcelable parcel,ClassLoader classloader){
		
	}
	@Override
	public Parcelable saveState(){
		return null;
	}
	@Override
	public void startUpdate(View collection){
		
	}
	public void showToast(String debag){
		Toast.makeText(context,debag,Toast.LENGTH_SHORT).show();
	}

}
