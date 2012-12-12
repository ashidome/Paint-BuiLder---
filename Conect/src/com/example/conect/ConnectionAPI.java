/* マッチングサーバ接続用API */

package com.example.conect;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.util.Log;

public class ConnectionAPI {

	private Socket socket; // ソケット
	private InputStream in; // 入力ストリーム
	private OutputStream out; // 出力ストリーム
	private MessageLisner lisner;
	private String userIP;
	private String localIP;

	public ConnectionAPI(MessageLisner lisner) {
		this.lisner = lisner;
	}

	// 接続
	public void connect(final String ip, final int port) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				
				userIP = new String(ip);
				int size;
				String str;
				byte[] w = new byte[1024];
				try {
					// ソケット接続
					Log.d("test", "接続中:" +ip+","+port);
					socket = new Socket(ip, port);
					localIP = InetAddress.getLocalHost().getHostAddress();
					in = socket.getInputStream();
					out = socket.getOutputStream();
					Log.d("test", "接続完了："+ip+","+port);
					sendMessage("2");
					
					// 受信ループ
					while (socket != null && socket.isConnected()) {
						// データ受信
						size = in.read(w);
						if (size <= 0)
							continue;
						str = new String(w, 0, size, "UTF-8");
						Log.d("test", "message:" + str);
						
						lisner.getMessage(str);
					}
				} catch (Exception e) {
					Log.e("error", "通信失敗しました"+ip+","+port);
					lisner.showIsConnect(false);
				}
			}
		});
		thread.start();
	}

	// 切断
	public void disconnect() {
		try {
			socket.close();
			socket = null;
		} catch (Exception e) {
		}
	}

	//ボタンクリックイベントの処理
	public void sendMessage(final String message) {
		//スレッドの作成
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					//データ送信
					if (socket != null && socket.isConnected()) {
						byte[] w = message.getBytes("UTF8");
						out.write(w);
						out.flush();
						Log.d("test", "send to " + userIP +", message:" + message);
					}
				} catch (Exception e) {
					lisner.showIsConnect(false);
				}
			}
		});
		thread.start();
	}

	public interface MessageLisner {
		void getMessage(String message);
		void showIsConnect(boolean state);
	}

	
	public String getUserIP(){
		return userIP;
	}
	
	public String getLocalIP(){
		return localIP;
	}
}
