package pbl.paint;
//最初のアクティビティ


import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.AbsoluteLayout;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.content.Intent;


public class PaintActivity extends Activity implements View.OnClickListener{
    /** Called when the activity is first created. */
	public final static int WC=AbsoluteLayout.LayoutParams.WRAP_CONTENT;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean requestWindowFeature = requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //レイアウトの生成
        AbsoluteLayout layout=new AbsoluteLayout(this);
        layout.setBackgroundResource(R.drawable.sensu);
        setContentView(layout);
        
        TextView tv1=new TextView(this);
        setText("PaintBuilder",36.0f,255,255,255,tv1);
        tv1.setLayoutParams(new AbsoluteLayout.LayoutParams(WC, WC, 100, 0));
        layout.addView(tv1);
        
        layout.addView(makeButton("描画モード","byoga",170,150));
        layout.addView(makeButton("通信モード","network",170,300));
        
        ListViewActivity ly=new ListViewActivity();
        ly.filecre("test", "data/data/pbl.paint/", 1);
        File a=new File("data/data/pbl.paint/");
        a.setWritable(true);
    }
    
    public void setText(String text,float TxSize,int r,int g,int b,TextView t){
    	t.setText(text);
        t.setTextSize(TxSize);
        t.setTextColor(Color.rgb(r, g, b));
        
    }
    private Button makeButton(String text,String tag,int x,int y){
    	Button button=new Button(this);
    	button.setText(text);
    	button.setTag(tag);
    	button.setOnClickListener(this);
    	
    	button.setLayoutParams(new AbsoluteLayout.LayoutParams(WC,WC,x,y));
    	return button;
    }
    public void onClick(View v){
    	String tag=(String)v.getTag();
    	if(tag.equals("byoga")){
    		Intent intent=new Intent(this,ListViewActivity.class);
    		startActivity(intent);
    	}else if(tag.equals("network")){
    		Intent intent=new Intent(this,TestActivity.class);
    		startActivity(intent);
    	}
    }
    public Bitmap res2bmp(Context context,int resID){
    	return BitmapFactory.decodeResource(context.getResources(),resID);
    }
}