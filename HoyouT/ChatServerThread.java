import java.io.*;
import java.net.*;
import java.util.*;

//チャットサーバスレッド
public class ChatServerThread extends Thread{
	
	private static List<ChatServerThread> threads = new ArrayList<ChatServerThread>();//スレッド群
	private Socket socket;//ソケット
	
	//コンストラクタ
	public ChatServerThread(Socket socket){
		super();
		this.socket = socket;
		threads.add(this);
	}
	
	//処理
	public void run(){
		InputStream in = null;
		String message;
		int size;
		byte[] w = new byte[10240];
		try{
			//ストリーム
			System.out.println("ChatServerThread start");
			in = socket.getInputStream();
			while(true){
				try{
					//受信待ち
					size = in.read(w);
					
					//切断
					if(size <= 0)
						throw new IOException();
					
					//読み込み
					message = new String (w, 0, size, "UTF8");
					
					//全員にメッセージ送信
					sendMessageAll(message);
				}
				catch (IOException e){
					System.out.println("ChatServerThread stop");
					socket.close();
					threads.remove(this);
					return;
				}
			}
		}catch (IOException e){
			System.err.println(e);
		}
	}
	
	//全員にメッセージを送信
	public void sendMessageAll(String message){
		ChatServerThread thread;
		for(int i=0; i<threads.size(); i++){
			thread=(ChatServerThread)threads.get(i);
			if(thread.isAlive()) thread.sendMessage(this, message);
		}
		System.out.println(message);
	}
	
	//メッセージ送信
	public void sendMessage(ChatServerThread talker, String message){
		try{
			OutputStream out = socket.getOutputStream();
			byte[] w = message.getBytes("UTF8");
			out.write(w);
			out.flush();
		}catch (IOException e){}
	}
}

