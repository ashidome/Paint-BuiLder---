package com.example.conect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity implements
ServerThread.MessageLisner, ConnectionThread.MessageLisner,
OnClickListener {

	private final static String BR = System.getProperty("line.separator");

	private TextView lblMessage; // 受信ラベル

	private EditText edtSend; // 送信エディットテキスト
	private Button btnSend; // 送信ボタン

	volatile Thread runner = null;
	private ServerThread thread;
	private ConnectionThread connection;
	private ServerSocket server;

	private final Handler handler = new Handler();
	private String[] ip_list;
	private String localIP;
	private int port = 50002;
	private int length;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Connections.s_threads.clear();
		Connections.c_threads.clear();

		// インテントからパラメータの取得
		String tmp = "";
		Bundle extras = getIntent().getExtras();
		if (extras != null)
			tmp = extras.getString("iplist");

		ip_list = tmp.split(",");
		length = ip_list.length;
		for (int i = 0; i < length; i++) {
			Log.i("test", "User IP:"+ip_list[i]);
		}

		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		localIP = ((ipAddress >> 0) & 0xFF) 
				+ "." +((ipAddress >> 8) & 0xFF)
				+ "." +((ipAddress >> 16) & 0xFF)
				+ "." +((ipAddress >> 24) & 0xFF);
		Log.d("test", "Wi-Fi IP:"+localIP);
		setTitle(localIP);
		

		// レイアウトの指定
		setContentView(R.layout.chat_layout);

		// コンポーネントの関連付け
		edtSend = (EditText) this.findViewById(R.id.editText1);
		btnSend = (Button) this.findViewById(R.id.button1);
		lblMessage = (TextView) this.findViewById(R.id.textView1);

		btnSend.setOnClickListener(this);
		lblMessage.setText("");
		
		Log.d("test","onCreate");
	}

	@Override
	public void onStart() {
		super.onStart();
		if (runner == null) {
			runner = new Thread() {
				public void run() {
					try {
						if (length > 1) {
							for (int i = 0; i < (length-1); i++) {
								connection = new ConnectionThread(ChatActivity.this, ip_list[i], port);
								connection.start();
							}
						}
						server = new ServerSocket(port);
						while(!(server.isClosed())){
							try {
								Socket socket = server.accept();
								Log.d("test","受信待機中");
								thread = new ServerThread(ChatActivity.this, socket);
								thread.start();
							} catch (IOException e) {
								server.close();
								e.printStackTrace();
							}
						}
					} catch (IOException e) {
						if(server != null)
							try {
								server.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						e.printStackTrace();
					}
				}
			};
			runner.start();
		}
		Log.d("test","onStart");
	}
	
	//チャット画面終了時に呼び出し
	//コネクションをすべて切断、マッチングサーバへ削除コマンドを送信
	@Override
	public void onDestroy(){
		if (length > 1) {
			for (ConnectionThread c_thread:Connections.c_threads) {
				c_thread.disconnect();
			}
		}
		for(ServerThread s_thread:Connections.s_threads){
			s_thread.disconnect();
		}
		try{
			server.close();
		}
		catch(IOException e){}
		Log.d("test","onDestroy");
		
		super.onDestroy();
	}

	// ボタンクリックイベントの処理
	public void onClick(View v) {
		Log.d("test",localIP+":c_threads size:"+Connections.c_threads.size());
		Log.d("test",localIP+":s_threads size:"+Connections.s_threads.size());
		if (v == btnSend) {
			sendMessageAll("3"+edtSend.getText().toString());
			handler.post(new Runnable() {
				public void run() {
					lblMessage.setText(localIP + ":"
							+ edtSend.getText().toString() + BR
							+ lblMessage.getText());
					edtSend.setText("", TextView.BufferType.NORMAL);
				}
			});
		}
	}



	public void sendMessageAll(String message){
		int state = 0;
		if(Connections.s_threads != null){
			for(ServerThread s_thread: Connections.s_threads){
				s_thread.sendMessage(message);
				state = 1;
			}
		}
		if (Connections.c_threads != null) {
			for (ConnectionThread connection : Connections.c_threads) {
				connection.sendMessage(message);
				state = 1;
			}
		}
		if(state!=1){
			handler.post(new Runnable(){
				public void run(){
					Toast.makeText(ChatActivity.this, "送信失敗", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	public void getMessage(final String data,final int flag, final String IP) {
		int command;
		try{
			command = Integer.valueOf(data.substring(0, 1));
		}
		catch(Exception e){
			command = -1;
		}
		final String message = data.substring(1);
		switch (command) {
		case 3:
			handler.post(new Runnable() {
				public void run() {
					switch(flag){//flagが0ならばServerThread, 1ならばConnectionThread
					case 0:
						lblMessage.setText(IP + ":"
								+ message + BR + lblMessage.getText());
						break;
					case 1:
						lblMessage.setText(IP + ":"
								+ message + BR + lblMessage.getText());
						break;
					}
				}
			});
			break;
		}
	}

	public void showConnect(final String IP){
		handler.post(new Runnable(){
			public void run(){
				lblMessage.setText(IP+"が入室しました。"+BR+lblMessage.getText());
			}
		});
	}

	public void showDisconnect(final String IP){
		handler.post(new Runnable(){
			public void run(){
				lblMessage.setText(IP+"が退出しました。"+BR+lblMessage.getText());
			}
		});
	}
}
