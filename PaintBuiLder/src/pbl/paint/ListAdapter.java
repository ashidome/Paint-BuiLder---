package pbl.paint;

import java.io.File;
import pbl.paint.ListViewActivity;
import java.util.ArrayList;
import java.util.List;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.Color;

public class ListAdapter extends ArrayAdapter<FileData> /*implements View.OnClickListener*/{
	private LayoutInflater inflater;
	private Context context;
	private ListView lv;
	private String keypath,path;
	ListViewActivity A;
	private String a;

	public ListAdapter(Context context, List<FileData> objects,ListViewActivity lv,String keypath) {
		super(context, 0, objects);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.A=lv;
		this.keypath=keypath;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final FileData data = this.getItem(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listview, null);
		}
		Button button=(Button)convertView.findViewById(R.id.button1);
		File f=new File(data.path);
		button.setText(f.getName());
    	button.setTag(data.mode);
    	
    	button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Sfile s=new Sfile(data.path);
				
				if(!s.canWrite()/*data.mode.equals("0")*/){
					//s.setWritable(true);
					//Toast.makeText(context,data.mode,Toast.LENGTH_SHORT).show();
				A.ListViewLoader(A.listView, data.path);
				A.pathchange(data.path+"/");
				    //s.setWritable(false);
				}else if(s.canWrite()/*data.mode.equals("1")*/){
					//SelectViewActivity sa=new SelectViewActivity();
					
					Intent intent=new Intent(context,SelectViewActivity.class);
		    		intent.putExtra("path", data.path+"/");
					context.startActivity(intent);
		    	
				}else{
					
				}
			}
		});
    	Button button2=(Button)convertView.findViewById(R.id.button2);
    	button2.setTag(data.path);
    	button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				a=(String)v.getTag();
				File fi=new File(a);
				File[] sa=fi.listFiles();
				if(sa.length==0){
				showYesNoDialog(context,"本当に削除しますか？",v,new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dia,int which){
    				if(which==DialogInterface.BUTTON_NEGATIVE){
    					
    				}else{
    					Sfile f2=new Sfile(A.subpath);
    					f2.setWritable(true);
    					
    					Sfile f=new Sfile(a);
    		    		f.delete();
    		    		f2.setWritable(false);
    		    	    //layout.removeView(lv);
    					A.ListViewLoader(A.listView, A.subpath);
    					//layout.addView(lv);
    					
    		    	}
    				}
    			});
					
				}else{
					showToast("中にフォルダが存在するので削除できません");
				}
			}
				
    	});	
    	Button button3=(Button)convertView.findViewById(R.id.button3);
    	if(f.canWrite()){
    		button3.setBackgroundResource(R.drawable.book);
    	}
		//TextView text = (TextView) convertView.findViewById(R.id.text);
		//text.setText(data.path);
		return convertView;
		
	}
	public void showYesNoDialog(Context context,String title,View v/*,String text*/,DialogInterface.OnClickListener listener){
		AlertDialog.Builder ad=new AlertDialog.Builder(context);
		ad.setTitle(title);
		//ad.setMessage(text);
		ad.setPositiveButton("Yes", listener);
		ad.setNegativeButton("No", listener);
		ad.show();
	}
	public void showToast(String debag){
		Toast.makeText(context,debag,Toast.LENGTH_SHORT).show();
	}

}
