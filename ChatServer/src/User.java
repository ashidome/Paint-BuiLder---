import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class User extends Thread{

	private Socket socket;//ソケット
	private Room user_room;//ユーザが属するルーム
	private String user_ip;//ユーザのIPアドレス

	//コンストラクタ
	public User(Socket socket){
		super();
		this.socket = socket;
		ChatServer.users.add(this);
	}

	//処理
	public void run(){
		InputStream in = null;
		InetAddress inetaddr = null;
		String data;
		int size;
		byte[] w = new byte[10240];
		try{
			//ストリーム
			inetaddr = socket.getInetAddress();
			user_ip = inetaddr.toString().substring(1);
			System.out.println("Connected:" + user_ip);
			in = socket.getInputStream();
			while(true){
				try{
					//受信待ち
					size = in.read(w);

					//受信しなければ切断
					if(size <= 0)
						throw new IOException();

					//読み込み
					data = new String (w, 0, size, "UTF8");
					int command = Integer.valueOf(data.substring(0,1));
					String message = data.substring(1);
					String s_message = "";
					int room_num;

					switch(command){
					case 0:
						//部屋作成
						user_room = new Room(message);
						ChatServer.rooms.add(user_room);
						System.out.println("部屋作成 :id = " + user_room.getID());
						sendMessageAll(makeRoomInfo());
						break;

					case 1:
						//メンバー入室
						try{
							room_num = Integer.valueOf(message);
							for(Room room :ChatServer.rooms ){
								if(room.getID() == room_num){
									System.out.println("現在のメンバー数:" + room.AddMember(this));
									user_room = room;
									System.out.println("部屋入室 :id =" + user_room.getID());
									break;
								}
							}
							s_message = "1";
							System.out.println("IPリストの送信");
							System.out.println("Member number:"+user_room.getIplistSize());
							for(int i=0;i<user_room.getIplistSize();i++){
								s_message += user_room.getIplistValue(i);
								s_message += ",";
							}
							System.out.println(s_message);
							sendMessage(s_message);
						}
						catch(Exception e){
							System.err.println(e);
							sendMessage(s_message+"NONE");
						}
						sendMessageAll(makeRoomInfo());
						break;
					case 2:
						//メンバー退室
						for(Room room:ChatServer.rooms){
							boolean state = false;
							for(int i=0;i<room.getIplistSize();i++){
								if(room.getIplistValue(i).equals(user_ip)){
									System.out.println("部屋退出：id = " + room.getID());
									System.out.println("現在のメンバー数：" + room.DelMember(this));
									sendMessage("2"+room.getName());
									state = true;
									break;
								}
							}
							if(state) break;
						}
						sendMessageAll(makeRoomInfo());
						break;
					case 3:
						//部屋削除(メンバー0人の場合)
						room_num = Integer.valueOf(message);
						for(Room room :ChatServer.rooms ){
							if(room.getID()==room_num){
								if(room.getIplistSize()==0){
									System.out.println("部屋削除 :id =" + room.getID());
									ChatServer.rooms.remove(room);
									sendMessageAll("50");
									sendMessageAll(makeRoomInfo());
								}
								else{
									System.out.println("部屋は使用中のため削除不可");
									sendMessage("51");
								}
								break;
							}
						}
						break;
					case 4:
						System.out.println("部屋情報送信");
						sendMessageAll(makeRoomInfo());
					default:
					}
				}
				catch (IOException e){
					System.out.println("Disconnected:" + user_ip);
					socket.close();
					ChatServer.users.remove(this);
					return;
				}
			}
		}catch (IOException e){
			System.err.println(e);
		}
	}

	//メッセージを送信
	public void sendMessage(String message){
		try{
			OutputStream out = socket.getOutputStream();
			byte[] w = message.getBytes("UTF8");
			out.write(w);
			out.flush();
		}catch (IOException e){}
	}

	//接続しているメンバー全員に一斉配信
	public void sendMessageAll(String message){
		for(User user:ChatServer.users){
			user.sendMessage(message);
		}
	}

	public String makeRoomInfo(){
		String message ="4{\"room\": [";
		for(int i = 0;i < ChatServer.rooms.size();i++){
			Room room = ChatServer.rooms.get(i);
			message += "{\"id\": " + room.getID()+",";
			message +=  "\"name\":\"" + room.getName() + "\",";
			message += "\"num\": " + room.getIplistSize() + "}";
			if(i != ChatServer.rooms.size() -1){
				message +=",";
			}
		}
		message +="]}";

		return message;
	}

	//UserIPの取得
	public String getUser_ip() {
		return user_ip;
	}
}

