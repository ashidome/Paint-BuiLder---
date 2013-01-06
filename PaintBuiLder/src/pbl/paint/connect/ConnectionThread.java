/*接続用スレッド*/
package pbl.paint.connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.util.Log;


public class ConnectionThread extends Thread{

	private Socket socket;
	private String userIP;
	private int port;
	private MessageLisner lisner;
	private boolean state = true;

	public ConnectionThread(MessageLisner lisner,String IP, int port){
		this.lisner = lisner;
		userIP = IP;
		this.port = port;
		Connections.c_threads.add(this);
	}

	public void run() {
		InputStream in = null;
		int size;
		String str;
		byte[] w = new byte[1024];
		try {
			// ソケット接続
			Log.d("test", "接続中:" +userIP+","+port);
			socket = new Socket(userIP, port);
			in = socket.getInputStream();

			Log.d("test", "接続完了："+userIP+","+port);
			lisner.showConnect(userIP);

			// 受信ループ
			while (socket != null && socket.isConnected()) {
				// データ受信
				size = in.read(w);
				if (size <= 0)
					continue;
				
				str = new String(w, 0, size, "UTF-8");
				Log.d("test","come from "+userIP+", message:"+str);
				if(str.equals("FIN")||!(state)){
					break;
				}
				lisner.getMessage(str,1,userIP);
			}
			socket.close();
			socket = null;
			Log.e("test", "通信が切断されました(client)："+userIP+","+port);
			lisner.showDisconnect(userIP);
			Connections.c_threads.remove(this);
		} catch (IOException e) {
			Log.e("test", "通信エラー(client)："+userIP+","+port);
			lisner.showDisconnect(userIP);
			Connections.c_threads.remove(this);
		}
	}

	// 切断
	public void disconnect() {
		sendMessage("FIN");
		state = false;
		Log.i("test","通信を切断しました(client)："+userIP);
		Connections.c_threads.remove(this);
	}

	public void sendMessage(final String message) {
		//スレッドの作成
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					//データ送信
					if (socket != null && socket.isConnected()) {
						OutputStream out = socket.getOutputStream();
						byte[] w = message.getBytes("UTF8");
						out.write(w);
						out.flush();
						Log.d("test", "send to " + userIP +", message:" + message);
					}
				} catch (IOException e) {
					Log.e("test","送信エラー："+userIP);
				}
			}
		});
		thread.start();
	}
	
	public interface MessageLisner {
		void getMessage(String message, int flag, String IP);
		void showConnect(String IP);
		void showDisconnect(String IP);
	}
}
