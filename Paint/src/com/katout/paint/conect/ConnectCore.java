package com.katout.paint.conect;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.widget.ListView;
import android.widget.Toast;

import com.katout.paint.draw.ShareMessage;

public class ConnectCore implements ConnectionAPI.MessageLisner{
	public static final String IP 	= "157.7.129.168";//サーバのIPアドレス
	private static final int PORT	= 20;
	
	private ConnectionAPI		server_connection;			// サーバとの接続用
	private Handler				handler;
	private Context				context;
	
	private ListView			roomList;					//部屋リストビュー
	private ListAdapter			adpter;						// リストアダプタ
	private ArrayList<RoomList>	room_list;					// 部屋リスト
	private String	ip_list;
	private boolean 			connect_flag;
	private boolean				inroom_flag;
	private ShareMessageInterface my_interface;
	
	public interface ShareMessageInterface{
		void getMassage(ShareMessage message);
	}
	
	public ConnectCore(Context context, Handler handler, ShareMessageInterface my_interface) {
		this.context = context;
		this.handler = handler;
		server_connection = new ConnectionAPI(this);
		connect_flag = false;
		inroom_flag = false;
		this.my_interface = my_interface;
	}
	
	/**
	 * サーバーに接続する
	 */
	private void connectServer(){
		if(!connect_flag){
			server_connection.connect(IP, PORT);
		}
	}
	/**
	 * サーバーと切断する
	 */
	public void disconnectServer(){
		if(connect_flag){
			server_connection.disconnect();
			connect_flag = false;
		}
	}
	
	/**
	 * 部屋の作成
	 */
	public void makeRoom(String name){
		server_connection.sendMessage("0"+name);
	}
	
	/**
	 * 部屋の削除
	 */
	public void removeRoom(int id){
		server_connection.sendMessage("3" + id);
	}
	
	/**
	 * 部屋リストの更新
	 */
	public void room_refresh(){
		server_connection.sendMessage("4");
	}
	
	
	/**
	 * 部屋に入る
	 */
	public boolean enter_room(RoomList Item){
		server_connection.sendMessage("1" + Item.id);
		// IPリストを受信するまで待機
		while (true) {
			if (ip_list != null) {
				break;
			}
		}
		if (!(ip_list.equals("NONE"))) {
//			Intent intent = new Intent(
//					context,
//					ChatActivity.class);
//			intent.putExtra("iplist", ip_list);
//			intent.putExtra("id", Item.id);
//			ip_list = null;
//			//TODO startActivity(intent);
			inroom_flag = true;
		} else {
			return false;
		}
		return true;
	}
	
	/**
	 * 部屋から退出
	 */
	public void exit_room(){
		if(inroom_flag){
			server_connection.sendMessage("2");
			disconnectServer();
			inroom_flag = false;
			
		}
	}
	
	/**
	 * 部屋に入っているかどうか
	 * @return
	 */
	public boolean getInRoom(){
		return inroom_flag;
	}
	
	
	/**
	 * ダイアログが作られた時呼ぶ
	 * @param roomList
	 * @param adpter
	 * @param room_list
	 */
	public void setRoomListLayout(ListView roomList, ListAdapter adpter, 
									ArrayList<RoomList> room_list){
		this.roomList = roomList;
		this.adpter = adpter;
		this.room_list = room_list;
		connectServer();
		
	}
	/**
	 * ダイアログが破棄された時呼ぶ
	 */
	public void removeRoomListLayout(){
		roomList = null;
		adpter = null;
		room_list = null;
		if(!inroom_flag){
			server_connection.disconnect();
			connect_flag = false;
		}
	}
	
	/**
	 * ルームにメッセージを送る
	 */
	public void shareMessage(String message) {
		server_connection.sendMessage("56" + message);
	}
	
	
	
	
	
	
	
	
	public void getMessage(String data) {
		int command = Integer.valueOf(data.substring(0,1));
		final String message = data.substring(1);

		switch(command){
		case 1:
			//部屋に入室し、IPアドレス一覧を受信
			ip_list = new String(message);
			break;
		case 2:
			//部屋から退出
			handler.post(new Runnable(){
				public void run(){
					Toast.makeText(context,"部屋「"+message+"」から退出しました",Toast.LENGTH_SHORT).show();
				}
			});
			break;
		case 3: 
			//メッセージの受信
			handler.post(new Runnable(){
				public void run(){
					Toast.makeText(context, message, Toast.LENGTH_LONG).show();
				}
			});
			break;
		case 4:
			if(room_list != null){
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
			}
			break;
		case 5:
			handler.post(new Runnable() {
				public void run() {
					if(message.equals("0")){
						Toast.makeText(context, "部屋を削除しました", Toast.LENGTH_LONG).show();
					}
					else if(message.equals("1")){
						Toast.makeText(context,"部屋は使用中のため削除不可",Toast.LENGTH_SHORT).show();
					}
				}
			});
			break;
		case 6:
			ShareMessage share = new ShareMessage();
			share.setMessage(message);
			my_interface.getMassage(share);
		default:
		}
	}
	
	public void showIsConnect(final boolean state){
		handler.post(new Runnable(){
			public void run(){
				if(state){
					Toast.makeText(context,"接続完了",Toast.LENGTH_SHORT).show();
					connect_flag = true;
				}
				else{
					Toast.makeText(context,"接続失敗",Toast.LENGTH_SHORT).show();
					connect_flag = false;
				}
			}
		});
	}


}
