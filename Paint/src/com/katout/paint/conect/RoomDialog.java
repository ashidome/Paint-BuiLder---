package com.katout.paint.conect;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

import com.katout.paint.R;

public class RoomDialog extends Dialog implements View.OnClickListener{
	private ConnectCore			core;
	private ListView			roomList;					//部屋リストビュー
	private ListAdapter			adpter;						// リストアダプタ
	private ArrayList<RoomList>	room_list;					// 部屋リスト
	
	private Button				addRoom;
	private Button				refresh;
	private EditText			editText;
	private Handler				handler;
	private Context				context;



	public RoomDialog(Context context,ConnectCore core) {
		super(context);
		this.context = context;
		this.core = core;
	}
	@Override
	protected void onStart() {
		super.onStart();
		core.setRoomListLayout(roomList, adpter, room_list);
	}
	@Override
	protected void onStop() {
		super.onStop();
		core.removeRoomListLayout();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Select Brush");

		room_list = new ArrayList<RoomList>();

		// レイアウトの指定
		setContentView(R.layout.conect_layout);
		getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

		// コンポーネントの指定
		roomList = (ListView) findViewById(R.id.listView1);
		addRoom = (Button) findViewById(R.id.button1);
		refresh = (Button) findViewById(R.id.button2);

		
		adpter = new ListAdapter(context, room_list);
		roomList.setAdapter(adpter);
		handler = new Handler();

		// 部屋リストを押したときの操作
		roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				final RoomList Item = (RoomList) listView
						.getItemAtPosition(position);

				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						context);
				// タイトルを設定
				alertDialogBuilder.setTitle("");
				// メッセージを設定
				alertDialogBuilder.setMessage("部屋 " + Item.name + " に参加しますか？？");
				// Positiveボタンとリスナを設定
				alertDialogBuilder.setPositiveButton("はい",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								if(core.enter_room(Item)){
								}else{
									Toast.makeText(context,
											"部屋「" + Item.name + "」に入れません",
											Toast.LENGTH_SHORT).show();
								}
							}
						});
				// Negativeボタンとリスナを設定
				alertDialogBuilder.setNegativeButton("いいえ",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
							}
						});
				// ダイアログを表示
				alertDialogBuilder.create().show();
			}
		});

		// 部屋リストを長押しした時の操作
		roomList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				final RoomList Item = (RoomList) listView
						.getItemAtPosition(position);

				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
				alertDialogBuilder.setTitle("部屋削除");
				alertDialogBuilder.setMessage("部屋「" + Item.name + "」を削除しますか？");
				alertDialogBuilder.setPositiveButton("はい",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								core.removeRoom(Item.id);
							}
						});
				alertDialogBuilder.setNegativeButton("いいえ",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
							}
						});

				alertDialogBuilder.show();
				return true;
			}
		});
		
		addRoom.setOnClickListener(this);
		refresh.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		if(v == addRoom){
			editText = new EditText(context);
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			// タイトルを設定
			alertDialogBuilder.setTitle("部屋作成");
			// メッセージを設定
			alertDialogBuilder.setView(editText);
			// Positiveボタンとリスナを設定
			alertDialogBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				//クリック時に呼ばれる
				public void onClick(DialogInterface dialog, int which) {
					core.makeRoom(editText.getText().toString());
					Toast.makeText(context, "部屋「"+editText.getText().toString()+"」を作成しました", Toast.LENGTH_SHORT).show();
				}
			});
			//ダイアログの表示
			alertDialogBuilder.create().show();
		} else if(v == refresh){
			/*		部屋リストの更新			*/
			core.room_refresh();
			handler.post(new Runnable(){
				public void run(){
					Toast.makeText(context, "部屋情報を更新しました", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

}
