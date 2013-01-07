package pbl.paint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SelectViewActivity extends Activity implements OnClickListener{
	ViewPager vp;
	View other;
	//Button zatumu;
	PagerAdapter pa;
	LinearLayout layout;
	private String keypath="data/data/pbl.paint/test/",ppath;
	private LayoutInflater inflater;
	EditText ed;
	String debag="0";
	//private String name;
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.selectmain);
		Intent intent=this.getIntent();
		ppath=intent.getStringExtra("path");
		vp=(ViewPager)findViewById(R.id.view_pager);
		
		pa=new SVPagerAdapter(this,ppath);
		vp.setAdapter(pa);
		
		
		//ボタンのセット
		Button b1=(Button)findViewById(R.id.button1);
		b1.setTag("sinki");
		b1.setOnClickListener(this);
		Button b2=(Button)findViewById(R.id.button3);
		b2.setTag("sakuzyo");
		b2.setOnClickListener(this);
		Button b4=(Button)findViewById(R.id.button4);
		b4.setTag("left");
		b4.setOnClickListener(this);
		Button b5=(Button)findViewById(R.id.button2);
		b5.setTag("right");
		b5.setOnClickListener(this);
		
		
	
    
    }
	
	
	//ボタンの反応
	public void onClick(View v){
		ed=new EditText(this);
        

		if(v.getTag()=="sinki"){
			Intent intent=new Intent(this,TestActivity.class);
    		intent.putExtra("path", ppath);
			this.startActivity(intent);
    		//Toast.makeText(this,ppath,Toast.LENGTH_SHORT).show();
    	
			/*
			showTDialog(this,"名前を入力してください",ed,new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dia,int which){
					File f=new File(ppath+ed.getText().toString()+".jpg");
					if(!f.exists()){
				try {
					FileOutputStream file=new FileOutputStream(ppath+ed.getText().toString()+".jpg");
					pagerLoader();
					debag="作成しました";
					showToast();
				} catch (FileNotFoundException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
					}else{
						debag="同じ名前のファイルが存在します";
						showToast();
					}
				}
			});
			*/
		}else if(v.getTag()=="sakuzyo"){
			//Toast.makeText(this,"削除",Toast.LENGTH_SHORT).show();
			showYesNoDialog("本当に削除しますか？",new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dia,int which){
    				if((which==DialogInterface.BUTTON_POSITIVE)){
			        other=vp.getChildAt(vp.getCurrentItem());
			        String s=other.getTag().toString();
			        File file=new File(s);
			        file.delete();
			        pagerLoader();
    				}
    			}});
			
		}else if(v.getTag()=="left"){
			vp.setCurrentItem(vp.getCurrentItem()-1);
			//Toast.makeText(this,"左へ",Toast.LENGTH_SHORT).show();
		}else if(v.getTag()=="right"){
			vp.setCurrentItem(vp.getCurrentItem()+1);
			//Toast.makeText(this,"右へ",Toast.LENGTH_SHORT).show();
		}
	}
	//テキストダイアログ
	public void showTDialog(Context cot,String title,EditText edtext,android.content.DialogInterface.OnClickListener onClickListener){
		AlertDialog.Builder ad=new AlertDialog.Builder(cot);
		
		
		ad.setTitle(title);
		ad.setView(edtext);
		ad.setPositiveButton("追加",  onClickListener);
		ad.show();
	
	}
	public Bitmap res2bmp(Context context,int resID){
    	return BitmapFactory.decodeResource(context.getResources(),resID);
    }
	public void pagerLoader(){
		vp.removeAllViews();
		vp=(ViewPager)findViewById(R.id.view_pager);
		vp.setAdapter(pa=new SVPagerAdapter(this,ppath));
	}
	public void showToast(){
		Toast.makeText(this,debag,Toast.LENGTH_SHORT).show();
	}
	public void showYesNoDialog(String title/*,String text*/,DialogInterface.OnClickListener listener){
		AlertDialog.Builder ad=new AlertDialog.Builder(this);
		ad.setTitle(title);
		//ad.setMessage(text);
		ad.setPositiveButton("Yes", listener);
		ad.setNegativeButton("No", listener);
		ad.show();
	}

}
