//お絵かき帳のアクティビティ
package pbl.paint;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SelectViewActivity extends Activity implements OnClickListener{

	private ViewPager vp;
	private View other;
	private PagerAdapter pa;
	private String path;

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		
		Intent intent = this.getIntent();
		path = intent.getStringExtra("path");
		
		setContentView(R.layout.selectmain);
		
		vp = (ViewPager)findViewById(R.id.view_pager);
		pa = new SVPagerAdapter(this,path);
		vp.setAdapter(pa);

		//ボタンのセット
		Button b1=(Button)findViewById(R.id.button1);
		b1.setTag("new");
		b1.setOnClickListener(this);
		Button b2=(Button)findViewById(R.id.button3);
		b2.setTag("delete");
		b2.setOnClickListener(this);
		Button b4=(Button)findViewById(R.id.button4);
		b4.setTag("left");
		b4.setOnClickListener(this);
		Button b5=(Button)findViewById(R.id.button2);
		b5.setTag("right");
		b5.setOnClickListener(this);
	}
	
	public void onClick(View v){
		if(v.getTag()=="new"){
			Intent intent=new Intent(this,TestActivity.class);
			intent.putExtra("path", path);
			intent.putExtra("newflag",true);//新規押した時、フォルダのパス
			this.startActivity(intent);
			
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
		}else if(v.getTag()=="delete"){
			File file = new File(path);
			File[] dir = file.listFiles();
			if(dir.length!=0){
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
					}
				});
			}
			else{
				Toast.makeText(this,"削除するファイルがありません",Toast.LENGTH_SHORT).show();
			}
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
		vp.setAdapter(pa=new SVPagerAdapter(this,path));
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
