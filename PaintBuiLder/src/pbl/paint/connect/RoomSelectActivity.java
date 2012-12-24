package pbl.paint.connect;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pbl.paint.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class RoomSelectActivity extends Activity implements ConnectionAPI.MessageLisner, View.OnClickListener{

	public static final String IP = "192.168.11.4";//サーバのIPアドレス
	private ListView roomList;
	private Button addRoom;
	private Button refresh;
	private ListAdapter adpter;
	private EditText editText;
	private Handler handler;

	private ArrayList<RoomList> room_list;//部屋リスト
	private ConnectionAPI server_connection;//サーバとの接続用
	private String ip_list;


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		room_list = new ArrayList<RoomList>();
		server_connection = new ConnectionAPI(this);

		//レイアウトの指定
		setContentView(R.layout.room_list_layout);

		//コンポーネントの指定
		roomList = (ListView)findViewById(R.id.room_list);
		adpter = new ListAdapter(this, room_list);
		roomList.setAdapter(adpter);
		handler = new Handler();

		//部屋リストを押したときの操作
		roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				ListView listView = (ListView)parent;
				final RoomList Item = (RoomList)listView.getItemAtPosition(position);

				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RoomSelectActivity.this);
				// タイトルを設定
				alertDialogBuilder.setTitle("");
				// メッセージを設定
				alertDialogBuilder.setMessage("部屋 "+Item.name+" に参加しますか？？");
				// Positiveボタンとリスナを設定
				alertDialogBuilder.setPositiveButton("はい",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						server_connection.sendMessage("1"+Item.id);
						//IPリストを受信するまで待機
						while(true){
							if(ip_list != null){
								break;
							}
						}
						if(!(ip_list.equals("NONE"))){
							Intent intent = new Intent(RoomSelectActivity.this, ChatActivity.class);
							intent.putExtra("iplist", ip_list);
							intent.putExtra("id", Item.id);
							ip_list = null;
							startActivity(intent);
						}
						else{
							Toast.makeText(RoomSelectActivity.this, "部屋「"+Item.name+"」に入れません", Toast.LENGTH_SHORT).show();
						}
					}
				});
				// Negativeボタンとリスナを設定
				alertDialogBuilder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {	
					}
				});
				// ダイアログを表示
				alertDialogBuilder.create().show();
			}
		});

		//部屋リストを長押しした時の操作
		roomList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
				ListView listView = (ListView)parent;
				final RoomList Item = (RoomList)listView.getItemAtPosition(position);

				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RoomSelectActivity.this);
				alertDialogBuilder.setTitle("部屋削除");
				alertDialogBuilder.setMessage("部屋「"+Item.name+"」を削除しますか？");
				alertDialogBuilder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						server_connection.sendMessage("3"+Item.id);
					}
				});
				alertDialogBuilder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {	
					}
				});

				alertDialogBuilder.show();
				return true;
			}
		});

		addRoom = (Button)findViewById(R.id.button1);
		addRoom.setOnClickListener(this);
		refresh = (Button)findViewById(R.id.button2);
		refresh.setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		server_connection.connect(IP, 50001);
	}

	@Override
	public void onStop(){
		server_connection.disconnect();
		super.onStop();
	}

	public void getMessage(String data) {
		int command = Integer.valueOf(data.substring(0,1));
		final String message = data.substring(1);

		switch(command){
		case 1:
			//部屋に入室し、IPアドレス一覧を受信
			ip_list = new String(message);
			Log.d("ip", ip_list);
			break;
		case 2:
			//部屋から退出
			handler.post(new Runnable(){
				public void run(){
					Toast.makeText(RoomSelectActivity.this,"部屋「"+message+"」から退出しました",Toast.LENGTH_SHORT).show();
				}
			});
			break;
		case 3: 
			//メッセージの受信
			handler.post(new Runnable(){
				public void run(){
					Toast.makeText(RoomSelectActivity.this, message, Toast.LENGTH_LONG).show();
				}
			});
			break;
		case 4:
			//部屋リストの取得
			room_list.clear();
			try {
				JSONObject root = new JSONObject(message);
				JSONArray array = root.getJSONArray("room");
				for(int i = 0; i < array.length();i++){
					RoomList room = new RoomList();
					JSONObject j_room = array.getJSONObject(i);
					room.id = j_room.getInt("id");
					room.name = j_room.getString("name");
					room.num = j_room.getInt("num");
					room_list.add(room);
				}
				handler.post(new Runnable() {
					public void run() {
						adpter.notifyDataSetChanged();
						roomList.invalidate();
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case 5:
			handler.post(new Runnable() {
				public void run() {
					if(message.equals("0")){
						Toast.makeText(RoomSelectActivity.this, "部屋を削除しました", Toast.LENGTH_LONG).show();
					}
					else if(message.equals("1")){
						Toast.makeText(RoomSelectActivity.this,"部屋は使用中のため削除不可",Toast.LENGTH_SHORT).show();
					}
				}
			});
			break;
		default:
		}
	}
	
	public void showIsConnect(final boolean state){
		handler.post(new Runnable(){
			public void run(){
				if(state){
					Toast.makeText(RoomSelectActivity.this,"接続完了",Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(RoomSelectActivity.this,"接続失敗",Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	public void onClick(View v) {
		if(v == addRoom){
			editText = new EditText(this);
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RoomSelectActivity.this);
			// タイトルを設定
			alertDialogBuilder.setTitle("部屋作成");
			// メッセージを設定
			alertDialogBuilder.setView(editText);
			// Positiveボタンとリスナを設定
			alertDialogBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				//クリック時に呼ばれる
				public void onClick(DialogInterface dialog, int which) {
					server_connection.sendMessage("0"+editText.getText().toString());
					Toast.makeText(RoomSelectActivity.this, "部屋「"+editText.getText().toString()+"」を作成しました", Toast.LENGTH_SHORT).show();
				}
			});
			//ダイアログの表示
			alertDialogBuilder.create().show();
		}
		else if(v == refresh){
			server_connection.sendMessage("4");
			handler.post(new Runnable(){
				public void run(){
					Toast.makeText(RoomSelectActivity.this, "部屋情報を更新しました", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
