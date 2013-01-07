package pbl.paint;

import java.io.File;
import android.content.AsyncTaskLoader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import pbl.paint.ListAdapter;
import pbl.paint.Sfile;

import pbl.paint.FileData;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.AbsoluteLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.EditText;


public class ListViewActivity extends Activity implements View.OnClickListener{
	
	private ArrayList<FileData> items;
	private final static int WC=LinearLayout.LayoutParams.WRAP_CONTENT;
	public String keypath="data/data/pbl.paint/test/",re="data/data/pbl.paint/";
    public String subpath=keypath;
	private EditText ed;
	public ListView listView;
	private LinearLayout layout;
	private Sfile dir;
	private File[] files;
	private Settei settei;
	private RadioGroup mode;
	
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.listviewact);
		
		//ImageView img=(ImageView)findViewById(R.drawable.hondana);
		//Bitmap bit=img.getDrawingCache();
		
		
		Button b1=(Button)findViewById(R.id.button1);
		b1.setTag("back");
		b1.setOnClickListener(this);
		
		Button b2=(Button)findViewById(R.id.button2);
		b2.setTag("tuika");
		b2.setOnClickListener(this);
		
		dir=new Sfile(keypath);
		files=dir.listFiles();
		
		items=new ArrayList<FileData>();
		for(int i = 0 ; i < files.length; i++){
			items.add(new FileData(files[i].toString(),"0"));
		}
		settei=new Settei();
		//リストビューの生成
		listView=(ListView)findViewById(R.id.listView1);
		listView.setScrollingCacheEnabled(false);
		listView.setBackgroundColor(Color.BLACK);
		listView.setAdapter(new ListAdapter(this, items,this,keypath));
		
		
		
		
	}
	
	public void pathchange(String sv){
		subpath=sv;
		
		
	}
	
	private Button makeButton(String text,String tag){
    	Button button=new Button(this);
    	button.setText(text);
    	button.setTag(tag);
    	button.setOnClickListener(this);
    	button.setLayoutParams(new LinearLayout.LayoutParams(480,WC));
    	return button;
    }
    public void onClick(View v){
    	String tag=(String)v.getTag();
    	String text=(String)((TextView) v).getText();
    	String i;
    	
    	if(tag.equals("byoga")){
    		Intent intent=new Intent(this,ListViewActivity.class);
    		startActivity(intent);
    	}else if(tag.equals("back")){
    		if(subpath.equals(keypath)){
    			pathchange(keypath);
    		}else{
    		Sfile r=new Sfile(subpath);
    		
    		
    		pathchange((r.getParentFile()).getPath()+"/");
    		//Toast.makeText(this,subpath,Toast.LENGTH_SHORT).show();
    		ListViewLoader(listView,subpath);
    		}
    		
    	}else if(tag.equals("tuika")){
    		ed=new EditText(this);
    		ed.setText("");
    		mode=new RadioGroup(this);
    		//Toast.makeText(this,String.valueOf(settei.getFilemode()),Toast.LENGTH_SHORT).show();
    		showTDialog(this,"名前を入力してください",ed,new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dia,int which){
    				if(ed.getText().toString().equals("")){
    					showToast("名前を入力してください");
    				}else{
    				
    				filecre(ed.getText().toString(),subpath,settei.getFilemode());
    				ListViewLoader(listView, subpath);
    				settei.setFilemode(0);
    				//int i=showDialog(ListViewActivity.this,"",ed.getText().toString());
    				}
    			}
    		});
    		showDialog(this,mode,new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dia,int which){
    				
    				}
    		},new RadioGroup.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					settei.setFilemode(checkedId);
				}
			});
    		
    	}
    	//Toast.makeText(this,String.valueOf(settei.getFilemode()),Toast.LENGTH_SHORT).show();
    }
	public Bitmap res2bmp(Context context,int resID){
    	return BitmapFactory.decodeResource(context.getResources(),resID);
    }
	
	//フォルダ作成
	public void filecre(String name,String path,int mode){
		String fpath=path+name;
		
		Sfile file=new Sfile(fpath,name,path,mode);
		Sfile pare =new Sfile(path);
		pare.setWritable(true);
		try{
			if(!file.exists()){		// フォルダが存在しない場合新規作成
				file.mkdir();
				
				file.setWritable(file.smode());
			}
		}catch(SecurityException ex){
			//file.setReadable(true);
			ex.printStackTrace();
			
		}
		pare.setWritable(false);
	}
	public void showTDialog(Context cot,String title,EditText edtext,OnClickListener onClickListener){
		AlertDialog.Builder ad=new AlertDialog.Builder(cot);
		
		
		ad.setTitle(title);
		ad.setView(edtext);
		ad.setPositiveButton("追加",  onClickListener);
		ad.show();
		
		
	}
	public void showDialog(Context con,RadioGroup mode,OnClickListener onClickListener,OnCheckedChangeListener onCheckedChangeListener){
		AlertDialog.Builder ad=new AlertDialog.Builder(con);
        RadioButton mode0=new RadioButton(this);
		
		mode0.setId(0);
		mode0.setText("本棚を作成");
		mode0.setTextColor(Color.rgb(255, 255, 255));
		
		RadioButton mode1=new RadioButton(this);
		mode1.setId(1);
		mode1.setText("お絵かき帳を作成");
		mode1.setTextColor(Color.rgb(255, 255, 255));
		
		mode.addView(mode0);
		mode.addView(mode1);
		mode.check(0);
		mode.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
		mode.setOnCheckedChangeListener(onCheckedChangeListener);
		ad.setTitle("作成するフォルダの選択");
		ad.setView(mode);
		ad.setPositiveButton("決定",  onClickListener);
		ad.show();
		
	}
	public void ListViewLoader(ListView lv,String path/*,ArrayList<FileData> data*/){
		
		ArrayList<FileData> data=new ArrayList<FileData>();
		Sfile dir=new Sfile(path);
		
		
		File[] files =dir.listFiles();
		
		for(int i = 0 ; i < files.length; i++){
			data.add(new FileData(files[i].toString(),"0"/*files[i].getmode()*/));
		}
		
		//リストビューの生成
				lv.setScrollingCacheEnabled(false);
				lv.setBackgroundColor(Color.BLACK);
				lv.setAdapter(new ListAdapter(this, data,this,keypath));
		//dir.setReadable(false);
		
	}
	public String checkmode(int mode){
		String i="2";
		if(mode==1){
			i="1";
		}else if(mode==0){
			i="0";
		}else{
			
		}
		return i;
	}
	public void showToast(String debag){
		Toast.makeText(this,debag,Toast.LENGTH_SHORT).show();
	}

}
