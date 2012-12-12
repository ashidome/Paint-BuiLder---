import java.util.ArrayList;


public class Room {
	
	private static int count = 0;
	private int ID;
	private final String name;
	private ArrayList<String> iplist;
	
	//コンストラクタ
	public Room(String name){
		ID = count;
		count++;
		this.name = name;
		iplist = new ArrayList<String>();
	}
	
	public int AddMember(User mem){
		if(mem != null){
			iplist.add(mem.getUser_ip());
			return iplist.size();
		}
		else{
			return -1;
		}
	}
	
	public int DelMember(User mem){
		if(mem != null){
			iplist.remove(mem.getUser_ip());
			return iplist.size();
		}
		else{
			return -1;
		}

	}
	
	public String getName(){
		return name;
	}

	public int getID() {
		return ID;
	}
	
	public int getIplistSize(){
		return iplist.size();
	}

	public String getIplistValue(int pos) {
		return iplist.get(pos);
	}
	
	public void setID(int new_id){
		ID = new_id;
	}
}
