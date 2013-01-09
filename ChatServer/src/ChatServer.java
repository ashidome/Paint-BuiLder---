import java.io.*;
import java.net.*;
import java.util.ArrayList;

//チャットサーバ
public class ChatServer {
	public static ArrayList<User> users = new ArrayList<User>();//ユーザ
	public static ArrayList<Room> rooms = new ArrayList<Room>();//ルーム
	private static final int PORT = 20;
	
	//開始
	public void start(int port){
		ServerSocket server;//サーバソケット
		Socket socket;//ソケット
		User thread;//スレッド
		
		try{
			server = new ServerSocket(port);
			System.out.println("ChatServer start");
			System.out.println("IPAddress:"+InetAddress.getLocalHost().getHostAddress());
			System.out.println("Port:"+port);
			System.out.printf("\n\n");
			
			while(true){
				try{
					//接続待機
					socket=server.accept();
					
					//チャットサーバスレッド開始
					thread = new User(socket);
					thread.start();
				}
				catch (IOException e){
				}
			}
		}catch(IOException e){
			System.err.println(e);
		}
	}
	
	//メイン
	public static void main(String[] args) {
		ChatServer server=new ChatServer();
		server.start(PORT);
	}
	
	public static void removeMember(Room user_room, User u){
		if(user_room.DelMember(u) == 0){
			rooms.remove(user_room);
		}
		
	}
}
