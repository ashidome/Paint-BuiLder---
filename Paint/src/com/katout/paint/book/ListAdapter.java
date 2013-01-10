package com.katout.paint.book;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.katout.paint.R;
import com.katout.paint.draw.MainActivity;

public class ListAdapter extends ArrayAdapter<FileData>{
	private LayoutInflater inflater;
	private Activity context;
	BookShelfActivity lv_activity;

	public ListAdapter(Activity context, ArrayList<FileData> filedata, BookShelfActivity lv_activity) {
		super(context, 0, filedata);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.lv_activity = lv_activity;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final FileData fd = this.getItem(position);
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.book_shelf_colum, null);
		}
		
		ImageView file_mode = (ImageView)convertView.findViewById(R.id.file_mode);
		if(BooksAPI.isBook(fd.getFullPath())){
			file_mode.setImageResource(R.drawable.bookmark);
		}else if(BooksAPI.isImage(fd.getName())){
			file_mode.setImageResource(R.drawable.image);
		}else{
			file_mode.setImageResource(R.drawable.folder);
		}

		TextView file_button = (TextView)convertView.findViewById(R.id.file_button);
		file_button.setText(fd.getName());
		file_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BooksAPI.isBook(fd.getFullPath())){
					//falseならお絵かき帳ディレクトリ
					Intent intent=new Intent(context,DrawBookActivity.class);
					intent.putExtra("path", fd.getFullPath()+"/");
					context.startActivity(intent);
					context.finish();
				}else if(BooksAPI.isImage(fd.getName())){
					Intent intent=new Intent(context,MainActivity.class);
					intent.putExtra("path", fd.getPath()+"/");
					intent.putExtra("newflag",false);//新規押した時、フォルダのパス
					intent.putExtra("name", fd.getName());
					context.startActivity(intent);
				}else{
					//trueならば本棚ディレクトリ
					lv_activity.ListViewLoader(lv_activity.listView, fd.getFullPath());
					lv_activity.pathChange(fd.getFullPath()+"/");
				}
			}
		});

		Button delete = (Button)convertView.findViewById(R.id.delete_button);
		delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(context)
				.setMessage("本当に削除しますか？")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new File(lv_activity.current_path);
						File file = new File(fd.getFullPath());
						if(!(file.delete())){
							Toast.makeText(context, "削除できませんでした", Toast.LENGTH_SHORT).show();
						}
						lv_activity.ListViewLoader(lv_activity.listView, lv_activity.current_path);
					}
				})
				.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//何もしない
					}
				})
				.show();
				//Toast.makeText(context,"中にディレクトリが存在するので削除できません",Toast.LENGTH_SHORT).show();
			}
		});	
		
		return convertView;
	}
}
