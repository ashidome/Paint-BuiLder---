package com.katout.paint.draw.layer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.katout.paint.draw.NativeFunction;

public class PreviewAsyncTask extends AsyncTask<String, Integer, Bitmap>
		implements OnCancelListener {

	final String TAG = "PreviewAsyncTask";
	ProgressDialog dialog;
	Context context;
	private NativeFunction n_func;

	private final ImageView img;
	private final  LayerData data;
	private final int num;


	public PreviewAsyncTask(Context context, NativeFunction n_func, ImageView img, LayerData data,int num) {
		this.context = context;
		this.n_func = n_func;
		this.img = img;
		this.data = data;
		this.num = num;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		Log.d(TAG, "doInBackground start-----" + num);
		int previewwidth = img.getWidth();
		int previewheight = img.getHeight();
		Bitmap bitmap = data.preview ;
		if(bitmap == null && previewwidth >0){
			bitmap= Bitmap.createBitmap(previewwidth, previewheight, Bitmap.Config.ARGB_8888);
			data.preview = bitmap;
		}

		if(bitmap != null && data.tempEdit){
			Log.d(TAG, "	getpreview");
			if(n_func.getPreview(num, bitmap)){
				data.tempEdit = false;
				return bitmap;
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

	}



	@Override
	protected void onPostExecute(Bitmap result) {
		if(data.preview != null){
			img.setImageBitmap(data.preview);
		}

		Log.d(TAG, "doInBackground end-----");
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		Log.d(TAG, "Dialog onCancell... calling cancel(true)");
		this.cancel(true);
	}
}