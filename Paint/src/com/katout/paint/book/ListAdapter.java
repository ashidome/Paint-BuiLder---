package com.katout.paint.book;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.katout.paint.R;

public class ListAdapter extends ArrayAdapter<FileData>{
	private LayoutInflater inflater;
	private Context context;
	BookShelfActivity lv_activity;

	public ListAdapter(Context context, ArrayList<FileData> filedata, BookShelfActivity lv_activity) {
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
		
		Button file_mode = (Button)convertView.findViewById(R.id.file_mode);
		File file = new File(fd.getPath());
		if(!(file.canWrite())){
			file_mode.setBackgroundResource(R.drawable.book);
		}

		TextView file_button = (TextView)convertView.findViewById(R.id.file_button);
		file_button.setText(fd.getName());
		file_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BooksAPI.isBook(fd.getPath())){
					//trueならば本棚ディレクトリ
					lv_activity.ListViewLoader(lv_activity.listView, fd.getPath());
					lv_activity.pathChange(fd.getPath()+"/");
				}
				else{
					//falseならお絵かき帳ディレクトリ
					Intent intent=new Intent(context,DrawBookActivity.class);
					intent.putExtra("path", fd.getPath()+"/");
					context.startActivity(intent);
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
						File file = new File(fd.getPath());
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
