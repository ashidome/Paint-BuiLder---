package pbl.paint;
//最初のアクティビティ

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;


public class MainActivity extends Activity implements View.OnClickListener{

	private Button paint_button;
	private Button com_button;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		//レイアウトの生成
		setContentView(R.layout.menu_layout);
		paint_button = (Button)this.findViewById(R.id.paint_button);
		paint_button.setOnClickListener(this);
		com_button = (Button)this.findViewById(R.id.com_button);
		com_button.setOnClickListener(this);
	}

	public void onClick(View v){
		if(v == paint_button){
			Intent intent=new Intent(this,ListViewActivity.class);//ファイル選択画面に移動
			startActivity(intent);
		}
		else if(v == com_button){
			Intent intent=new Intent(this,pbl.paint.connect.RoomSelectActivity.class);//通信用アクティビティに変える
			startActivity(intent);
		}
	}

	public Bitmap res2bmp(Context context,int resID){
		return BitmapFactory.decodeResource(context.getResources(),resID);
	}
}