/*待ち用スレッド*/
package pbl.paint.connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.util.Log;

public class ServerThread extends Thread {
	private Socket socket;//ソケット
	private String str;
	private MessageLisner lisner;
	private String userIP;
	private boolean state = true; 

	//コンストラクタ
	public ServerThread(MessageLisner lisner, Socket socket) {
		super();
		this.lisner = lisner;
		this.socket = socket;
		Connections.s_threads.add(this);
	}

	//処理
	public void run() {

		InputStream in = null;
		InetAddress inetaddr = null;
		int size;
		byte[] w = new byte[10240];
		try {
			//ストリーム
			inetaddr = socket.getInetAddress();
			userIP = inetaddr.toString().substring(1);
			in = socket.getInputStream();
			
			lisner.showConnect(userIP);
			while (socket != null && socket.isConnected()) {
				//受信待ち
				size = in.read(w);
				if (size <= 0) {
					continue;
				}
				str = new String(w, 0, size, "UTF8");
				Log.d("test","come from "+userIP+", message:"+str);
				if(str.equals("FIN")||!(state)){
					break;
				}
				lisner.getMessage(str,0,userIP);
			}
			Log.e("test", "通信が切断されました(server):"+userIP);
			socket.close();
			socket = null;
			lisner.showDisconnect(userIP);
			Connections.s_threads.remove(this);
		} catch (IOException e) {
			Log.e("test","通信エラー(server)："+userIP+",50002");
			lisner.showDisconnect(userIP);
			Connections.s_threads.remove(this);
		}
	}

	public void disconnect(){
		sendMessage("FIN");
		state = false;
		Log.i("test","通信を切断しました(server): "+ userIP);
		Connections.s_threads.remove(this);
	}


	//メッセージの送信
	public void sendMessage(final String message) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					if(socket != null && socket.isConnected()){
						OutputStream out = socket.getOutputStream();
						byte[] w = message.getBytes("UTF8");
						out.write(w);
						out.flush();
						Log.d("test","send to " + userIP + ", message:" + message);
					}
				} catch (IOException e) {
					Log.e("test","送信エラー："+userIP);
				}
			}
		});
		thread.start();
	}

	public interface MessageLisner {
		public void getMessage(String message,int flag,String IP);
		public void showConnect(String IP);
		public void showDisconnect(String IP);
	}
	
	/*private boolean serverAuth(InputStream in){
		byte[] buf = new byte[1024];
		
		return true;
	}*/
}
