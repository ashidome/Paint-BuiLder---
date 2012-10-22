package com.katout.paint.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * ペイント画面を管理するクラス
 *
 * @author katout
 *
 */
public class PaintView implements SurfaceHolder.Callback, View.OnTouchListener,
		Runnable {

	private enum State {
		Non, DrawStart, Drawing, Move_Zoom_tarn, thirdWait, PaintSetting, LayerSetting
	}

	public interface MenuLiner {
		void paintMenuPos(int h, int y, boolean animation);

		void layerMenuPos(int w, int x, boolean animation);

		void setup();

	}

	private SurfaceView sv;
	public boolean tread_flag;
	private SurfaceHolder holder;
	public Thread thread;
	private State state;
	private MenuLiner menu_lisner;
	private EventLisner event_lisner;

	// タッチまわり
	private int touch_count; // 前フレームのタッチ数
	private int[][] points;
	private int[] bitmap;

	private int aveX; // 前フレームの平均x座標
	private int aveY; // 前フレームの平均y座標
	private int pre_threeX; // 3点時の始めの平均座標x
	private int pre_threeY; // 3点時の始めの平均座標y
	private double preVecterSize; // 前フレームの２点間距離
	private double pre_rad; // 前フレームの２点の角度

	private int nowPosX; // 現在の左上のx座標
	private int nowPosY; // 現在の左上のy座標

	private double rad; // 現在の画面角度
	private double Scale; // 現在の拡大率
	private int nowMenuPosX; // 現在のメニューの位置
	private int nowMenuPosY; // 現在のメニューの位置
	private int menuW = 0;
	private int menuH = 200;
	private int w;
	private int h;

	public PaintView(Context context, SurfaceView sv, MenuLiner menu_lisner,
			EventLisner event_lisner) {
		this.sv = sv;
		this.menu_lisner = menu_lisner;
		this.event_lisner = event_lisner;

		tread_flag = true;
		// サーフェイスホルダーの作成
		holder = sv.getHolder();
		holder.addCallback(this);
		sv.setOnTouchListener(this);

		state = State.Non;
		touch_count = 0;

		points = new int[2][10];
		nowPosX = 0;
		nowPosY = 0;
		Scale = 1.0;
		rad = 1.0;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//タッチの数を取得
		int temp_touch_count = event.getPointerCount();

		//とりあえずすべて配列に保存
		for (int i = 0; i < temp_touch_count; i++) {
			points[0][i] = (int) event.getX(i);
			points[1][i] = (int) event.getY(i);
		}

		//タッチの数の変化により分岐
		if (temp_touch_count != touch_count) {
			// タッチの数に変化があった場合
			if (temp_touch_count > 1) {
				// すでに描画中だった場合その変種を削除
				if (state == State.Drawing) {
					event_lisner.deleteEditLayer();
				}
				switch (temp_touch_count) {
				case 2:
					//2本指になった瞬間、各項目の初期化
					state = State.Move_Zoom_tarn;
					aveX = (points[0][0] + points[0][1]) / 2;
					aveY = (points[1][0] + points[1][1]) / 2;

					preVecterSize = VecSize(points[0][0], points[1][0],
							points[0][1], points[1][1]);
					pre_rad = Math.atan2(points[1][1] - points[1][0],
							points[0][1] - points[0][0]);
					break;
				case 3:
					//3本指になった瞬間、各項目の初期化
					state = State.thirdWait;
					pre_threeX = (points[0][0] + points[0][1] + points[0][2]) / 3;
					pre_threeY = (points[1][0] + points[1][1] + points[1][2]) / 3;
					break;
				default:
					break;
				}
			} else {
				//タッチ数が１になった場合で待機中からの遷移
				if (state == State.Non) {
					state = State.DrawStart;
					event_lisner.startDraw((int )(-nowPosX + points[0][0]/Scale), 
							(int )(-nowPosY + points[1][0]/Scale));
				}
			}
		} else {
			// タッチの数に変化がなかった場合

			// １本指の場合
			if (temp_touch_count == 1
					&& event.getAction() == MotionEvent.ACTION_MOVE) {
				if (state == State.DrawStart) {
					state = State.Drawing;
				}
				event_lisner.draw((int )(-nowPosX + points[0][0]/Scale), 
						(int )(-nowPosY + points[1][0]/Scale));
			}

			if (temp_touch_count == 2) {
				/**************************** 移動 ******************************/
				// スクリーン座標での移動量の計算
				int temp_ave_x = (points[0][0] + points[0][1]) / 2;
				int temp_ave_y = (points[1][0] + points[1][1]) / 2;

				// 実際のキャンパスサイズになおして現在の位置を計算
				nowPosX += (temp_ave_x - aveX) / Scale;
				nowPosY += (temp_ave_y - aveY) / Scale;
				// TODO 端処理を書け

				event_lisner.setPosition(nowPosX - (int)(w/Scale), nowPosY - (int)(h/Scale));

				aveX = temp_ave_x;
				aveY = temp_ave_y;

				/**************************** 拡大 ******************************/
				double temp_Vec = VecSize(points[0][0], points[1][0],
						points[0][1], points[1][1]);
				double temp_Scale = ((w * Scale) + (temp_Vec - preVecterSize)
						* Scale)
						/ w;
				event_lisner.setScale(temp_Scale);
				Scale = temp_Scale;
				preVecterSize = temp_Vec;

				/**************************** 回転 ******************************/
				double temp_rad = Math.atan2(points[1][1] - points[1][0],
						points[0][1] - points[0][0]);
				rad += temp_rad - pre_rad;
				pre_rad = temp_rad;
				event_lisner.setRadian(temp_Scale);

			}
			/**************************** メニュー ******************************/
			if (temp_touch_count == 3) {
				int t_three_startX = (points[0][0] + points[0][1] + points[0][2]) / 3;
				int t_three_startY = (points[1][0] + points[1][1] + points[1][2]) / 3;
				if (nowMenuPosX != 0) {
					nowMenuPosX += t_three_startX - pre_threeX;
					if (nowMenuPosX > menuW) {
						nowMenuPosX = menuW;
					}
					if (nowMenuPosX < -menuW) {
						nowMenuPosX = -menuW;
					}
					pre_threeX = t_three_startX;
					pre_threeY = t_three_startY;


				} else if (nowMenuPosY != 0) {
					nowMenuPosY += t_three_startY - pre_threeY;
					if (nowMenuPosY > menuH) {
						nowMenuPosY = menuH;
					}
					if (nowMenuPosY < -menuH) {
						nowMenuPosY = -menuH;
					}
					pre_threeY = t_three_startY;
					pre_threeX = t_three_startX;



				} else {
					if (t_three_startX - pre_threeX > 30) {
						nowMenuPosX = t_three_startX - pre_threeX - 30;
						pre_threeX = t_three_startX;
						// TODO visibleMenu();
					}
					if (t_three_startX - pre_threeX < -30) {
						nowMenuPosX = t_three_startX - pre_threeX + 30;
						pre_threeX = t_three_startX;
						// TODO visibleMenu();
					}
					if (t_three_startY - pre_threeY > 30) {
						nowMenuPosY = t_three_startY - pre_threeY - 30;
						pre_threeY = t_three_startY;
						// TODO visibleMenu();
					}
					if (t_three_startY - pre_threeY < -30) {
						nowMenuPosY = t_three_startY - pre_threeY + 30;
						pre_threeY = t_three_startY;
						// TODO visibleMenu();
					}
				}
				Log.e("test", "w = " + nowMenuPosX + ":"+menuW);
				menu_lisner.layerMenuPos(w, nowMenuPosX * 100 / menuW, false);
				menu_lisner.paintMenuPos(h, nowMenuPosY, false);
			}
		}

		// 最後にタッチの数を保存
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (nowMenuPosX > menuW * 0.8) {
				nowMenuPosX = menuW;
			} else if (nowMenuPosX < menuW * -0.8) {
				nowMenuPosX = -menuW;
			} else {
				nowMenuPosX = 0;
			}
			if (nowMenuPosY > menuH * 0.8) {
				nowMenuPosY = menuH;
			} else if (nowMenuPosY < menuH * -0.8) {
				nowMenuPosY = -menuH;
			} else {
				nowMenuPosY = 0;
			}
			menu_lisner.layerMenuPos(w, nowMenuPosX * 100 / menuW, false);
			menu_lisner.paintMenuPos(h, nowMenuPosY, false);
			touch_count = 0;
			state = State.Non;
			if (state == State.Drawing) {
				// TODO event_lisner.stopDraw(points[0][0], points[1][0]);
			}
		} else {
			touch_count = temp_touch_count;
		}
		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		tread_flag = true;
		w = sv.getWidth();
		h = sv.getHeight();
		bitmap = new int[w * h];
		event_lisner.init(w, h);
		thread = new Thread(this);
		// ここでrun()が呼ばれる
		thread.start();


	}

	@Override
	public void run() {
		menu_lisner.setup();
		while (tread_flag) {
			UpDate();
			Draw();
		}
	}

	private void UpDate() {
		// TODO 自動生成されたメソッド・スタブ

	}

	private void Draw() {
		Paint paint = new Paint();
		Canvas canvas = sv.getHolder().lockCanvas();
		canvas.drawColor(Color.WHITE);

		boolean temp_flag = event_lisner.getBitmap(bitmap, w, h);
		if (temp_flag) {
			canvas.drawBitmap(bitmap, 0, w, 0, 0, w, h, true, paint);
		}
		for (int i = 0; i < touch_count; i++) {
			canvas.drawLine(0, points[1][i], w, points[1][i], paint);
			canvas.drawLine(points[0][i], 0, points[0][i], h, paint);
		}

		paint.setTextSize(30);
		String debugText = "State = " + state + "\n";
		canvas.drawText(debugText, 5, 30, paint);
		debugText = "Pos X: " + nowPosX + "	,Y: " + nowPosY + "\n";
		canvas.drawText(debugText, 5, 60, paint);

		debugText = "preVecterSize = " + preVecterSize + "\n";
		canvas.drawText(debugText, 5, 90, paint);
		debugText = "Scale = " + (int) (Scale * 100) + "%\n";
		canvas.drawText(debugText, 5, 120, paint);
		debugText = "Rad = " + rad;
		canvas.drawText(debugText, 5, 150, paint);
		debugText = "menux = " + nowMenuPosX;
		canvas.drawText(debugText, 5, 180, paint);
		debugText = "menuY = " + nowMenuPosY;
		canvas.drawText(debugText, 5, 210, paint);

		holder.unlockCanvasAndPost(canvas);
	}


	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		tread_flag = false;
	}

	private double VecSize(int x, int y, int x2, int y2) {
		int tempx = x2 - x;
		int tempy = y2 - y;

		return Math.sqrt(tempx * tempx + tempy * tempy);
	}

	public void setmenuwSize(int height, int wigth) {
		menuH = height;
		menuW = (int) (wigth * 0.4f);
	}

}
