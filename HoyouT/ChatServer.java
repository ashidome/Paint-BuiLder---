import java.io.*;
import java.net.*;

//�`���b�g�T�[�o
public class ChatServer {

	//�J�n
	public void start(int port){
		ServerSocket server;//�T�[�o�\�P�b�g
		Socket socket;//�\�P�b�g
		ChatServerThread thread;//�X���b�h
		
		try{
			server = new ServerSocket(port);
			System.out.println("ChatServer start");
			System.out.println("IPAddress:"+InetAddress.getLocalHost().getHostAddress());
			System.out.println("Port:"+port);
			
			while(true){
				try{
					//�ڑ��ҋ@
					socket=server.accept();
					
					//�`���b�g�T�[�o�X���b�h�J�n
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
