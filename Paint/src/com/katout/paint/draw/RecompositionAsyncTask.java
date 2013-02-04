package com.katout.paint.draw;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

public class RecompositionAsyncTask extends AsyncTask<String, Integer, Long>
		implements OnCancelListener {

	final String	TAG	= "MyAsyncTask";
	ProgressDialog	dialog;
	Context			context;
	NativeFunction nativefunc;
	private RePreviewLisner lisner;
	private long startTime;
	private long endTime;
	
	public interface RePreviewLisner{
		void setPreView();
	}

	public RecompositionAsyncTask(Context context, NativeFunction nativefunc,RePreviewLisner lisner) {
		this.context = context;
		this.nativefunc = nativefunc;
		this.lisner = lisner;
	}

	@Override
	protected void onPreExecute() {
		Log.d(TAG, "onPreExecute");
		startTime = System.currentTimeMillis();
		dialog = new ProgressDialog(context);
		dialog.setTitle("Please wait");
		dialog.setMessage("Progress...");
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	protected Long doInBackground(String... params) {
		
		nativefunc.Recomposition();
		lisner.setPreView();
		return 123L;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		
	}

	@Override
	protected void onCancelled() {
		Log.d(TAG, "onCancelled");
		endTime = System.currentTimeMillis();
		try {
			if(endTime - startTime  > 300){
				Thread.sleep(300 - (endTime - startTime));
			}
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		dialog.dismiss();
	}

	@Override
	protected void onPostExecute(Long result) {
		Log.d(TAG, "onPostExecute - " + result);
		if(dialog != null){
			dialog.dismiss();
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		Log.d(TAG, "Dialog onCancell... calling cancel(true)");
		this.cancel(true);
	}
}
