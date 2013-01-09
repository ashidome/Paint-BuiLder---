package pbl.paint;

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
import android.widget.Toast;

public class ListAdapter extends ArrayAdapter<FileData>{
	private LayoutInflater inflater;
	private Context context;
	ListViewActivity lv_activity;

	public ListAdapter(Context context, ArrayList<FileData> filedata, ListViewActivity lv_activity) {
		super(context, 0, filedata);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.lv_activity = lv_activity;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final FileData fd = this.getItem(position);
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_view_colum, null);
		}
		
		Button file_mode = (Button)convertView.findViewById(R.id.file_mode);
		File file = new File(fd.getPath());
		if(!(file.canWrite())){
			file_mode.setBackgroundResource(R.drawable.book);
		}

		Button file_button = (Button)convertView.findViewById(R.id.file_button);
		file_button.setText(fd.getName());
		file_button.setTag(fd.getMode());
		file_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File file = new File(fd.getPath());
				if(file.canWrite()){
					lv_activity.ListViewLoader(lv_activity.listView, fd.getPath());
					lv_activity.pathChange(fd.getPath()+"/");
				}
				else{
					Intent intent=new Intent(context,SelectViewActivity.class);
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
						new File(lv_activity.current_path).setWritable(true);
						File file = new File(fd.getPath());
						file.setWritable(true);
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
