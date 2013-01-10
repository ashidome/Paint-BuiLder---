package com.katout.paint.book;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.katout.paint.R;

public class BookShelfActivity extends Activity implements View.OnClickListener{

	private ArrayList<FileData> Items;
	public final String root_path = Environment.getExternalStorageDirectory().toString();
	public String current_path;

	private Button back;
	private Button add_shelf;//shelf:本棚
	private Button add_book;//book:お絵かき帳
	private EditText editText;
	private TextView title;
	
	public ListView listView;

	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.book_shelf_layout);
		current_path = new String(root_path) + "/paint";
		back = (Button)findViewById(R.id.back);
		title = (TextView)findViewById(R.id.title);
		back.setOnClickListener(this);

		add_shelf = (Button)findViewById(R.id.add_shelf);
		add_shelf.setOnClickListener(this);
		
		add_book = (Button)findViewById(R.id.add_book);
		add_book.setOnClickListener(this);
		
		BooksAPI.makeDirectory("paint", root_path);
		
		setmyTitle(current_path);
		File[] filelist = new File(current_path).listFiles();
		
		Items = new ArrayList<FileData>();
		Items.clear();
		for(int i = 0 ; i < filelist.length; i++){
			Items.add(new FileData(filelist[i].toString(),filelist[i].getName()));
		}

		//リストビューの生成
		listView = (ListView)findViewById(R.id.listView1);
		listView.setScrollingCacheEnabled(false);
		listView.setAdapter(new ListAdapter(this, Items,this));
	}

	public void onClick(View v){
		if(v == back){
			if(current_path.equals(root_path)){
				pathChange(root_path);
			}else{
				pathChange((new File(current_path).getParentFile()).getPath());
				//Toast.makeText(this,subpath,Toast.LENGTH_SHORT).show();
				ListViewLoader(listView,current_path);
			}
		}
		else if(v == add_shelf){
			showMakeFileDialog(0,"本棚の作成");
		}
		else if(v == add_book){
			showMakeFileDialog(1,"お絵かき帳の作成");
		}
	}

	//ファイル名を入力するダイアログを表示
	public void showMakeFileDialog(final int file_mode, String title){
		editText = new EditText(BookShelfActivity.this);
		new AlertDialog.Builder(BookShelfActivity.this)
		.setTitle(title)
		.setView(editText)
		.setPositiveButton("追加",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(editText.getText().toString().equals("")){
					Toast.makeText(BookShelfActivity.this,"名前が入力されていません",Toast.LENGTH_SHORT).show();
				}
				else{
					BooksAPI.makeDirectory(editText.getText().toString(),current_path);
					ListViewLoader(listView, current_path);
				}
			}
		})
		.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//何もしない	
			}
		})
		.show();
	}
	
	public void pathChange(String path){
		current_path = path;
		setmyTitle(path);
	}

	public void setmyTitle(CharSequence string) {
		string = string.subSequence(root_path.length(), string.length());
		title.setText(string+"/");
	};



	//リストビューの再描画
	public void ListViewLoader(ListView listview,String path){
		ArrayList<FileData> items = new ArrayList<FileData>();
		items.clear();
		File[] files =new File(path).listFiles();
		
		for(int i = 0 ; i < files.length; i++){
			items.add(new FileData(files[i].toString(), files[i].getName()));
		}

		//リストビューの生成
		listview.setScrollingCacheEnabled(false);
		listview.setAdapter(new ListAdapter(this, items,this));
	}
}
