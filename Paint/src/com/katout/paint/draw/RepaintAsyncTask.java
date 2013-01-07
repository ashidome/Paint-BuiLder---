package com.katout.paint.draw;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

public class RepaintAsyncTask extends AsyncTask<String, Integer, Long>
		implements OnCancelListener {

	final String	TAG	= "MyAsyncTask";
	ProgressDialog	dialog;
	Context			context;
	NativeFunction nativefunc;
	int position;

	public RepaintAsyncTask(Context context, NativeFunction nativefunc,int position) {
		this.context = context;
		this.nativefunc = nativefunc;
		this.position = position;
	}

	@Override
	protected void onPreExecute() {
		Log.d(TAG, "onPreExecute");
		dialog = new ProgressDialog(context);
		dialog.setTitle("Please wait");
		dialog.setMessage("Progress...");
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	protected Long doInBackground(String... params) {

		nativefunc.setLayerMode(position);
		return 123L;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		
	}

	@Override
	protected void onCancelled() {
		Log.d(TAG, "onCancelled");
		try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		dialog.dismiss();
	}

	@Override
	protected void onPostExecute(Long result) {
		Log.d(TAG, "onPostExecute - " + result);
		dialog.dismiss();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		Log.d(TAG, "Dialog onCancell... calling cancel(true)");
		this.cancel(true);
	}
}
