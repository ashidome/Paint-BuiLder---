package pbl.paint.connect;

import java.util.ArrayList;

import pbl.paint.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<RoomList>{
	
	private ArrayList<RoomList> room_list;
	private LayoutInflater mInflater;
	
	public ListAdapter(Context context, ArrayList<RoomList> room_list){
		super(context, 0, room_list);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.room_list = room_list;
	}
	
	//数の取得
	@Override
	public int getCount(){
		return room_list.size();
	}
	
	//要素の取得
	@Override
	public RoomList getItem(int pos){
		return room_list.get(pos);
	}
	
	//要素IDの取得
	@Override
	public long getItemId(int pos){
		return pos;
	}
	
	//セルのビューの生成
	@Override
	public View getView(int pos, View convertView, ViewGroup parent){
		RoomList roomlist = room_list.get(pos);
		
		//レイアウトの生成
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.room_list_colum, null);
		}
		
		TextView name = (TextView)convertView.findViewById(R.id.name);
		TextView num = (TextView)convertView.findViewById(R.id.num);
		TextView room_id = (TextView)convertView.findViewById(R.id.room_id);
		
		name.setText(roomlist.name);
		num.setText(roomlist.num+"人");
		room_id.setText("id="+roomlist.id);
		
		return convertView;
		
	}
}