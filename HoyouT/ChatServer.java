import java.io.*;
import java.net.*;

//チャットサーバ
public class ChatServer {

	//開始
	public void start(int port){
		ServerSocket server;//サーバソケット
		Socket socket;//ソケット
		ChatServerThread thread;//スレッド
		
		try{
			server = new ServerSocket(port);
			System.out.println("ChatServer start");
			System.out.println("IPAddress:"+InetAddress.getLocalHost().getHostAddress());
			System.out.println("Port:"+port);
			
			while(true){
				try{
					//接続待機
					socket=server.accept();
					
					//チャットサーバスレッド開始
					thread = new ChatServerThread(socket);
					thread.start();
				}
				catch (IOException e){
				}
			}
		}catch(IOException e){
			System.err.println(e);
		}
	}
	
	public static void main(String[] args) {
		ChatServer server=new ChatServer();
		server.start(8080);
	}
}
