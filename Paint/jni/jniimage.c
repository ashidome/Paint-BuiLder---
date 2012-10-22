#include <jni.h>
#include <android/log.h>
//#include <android/bitmap.h>
#include <stdlib.h>
#include <math.h>
#include <stdbool.h>

#define i_printf(...) __android_log_print(ANDROID_LOG_INFO, "hj", __VA_ARGS__)

void brush_draw(int x, int y);
int distance(int x1, int x2, int y1, int y2);
void setColor(int color);
void Bezier(int x1, int y1, int x2, int y2, int x3, int y3);
void Bicubic(int sx, int sy);
double First_Neighborhood(double d);
double Second_Neighborhood(double d);
void Bilinear(int x, int y);
void fill(int x, int y);

/*
 * レイヤ周り
 */
struct Display {
	int height;
	int width;
	int x;
	int y;
};

struct Canvas {
	int flag;
	int height;
	int width;
};

struct Layers {
	int layer_num;
};

struct Layer {

};

struct DrawPoints {
	int x, y; //始点
	int x2, y2; //次点
	int flag;
};

static struct Display disp;
static struct Canvas c;
static struct Laler layers;
static struct DrawPoints dp;
static int Color;
static int Size;
static int theta;

//テスト用
static int **brush;
static char **brush_map;
static int bx = 10;
static int by = 10;
static int **img;
static int **imgs;
static int frequency = 30;
static double scale = 1.0;
static int scale_flag = 0;

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setCanvasSize(
		JNIEnv* env, jobject obj, jint jx, jint jy) {
	i_printf("setCanvasSize\n");
	if (c.flag) {
		c.flag = false;
		c.height = jx;
		c.width = jy;
		return true;
	} else {
		return false;
	}
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_deleteEditLayer(
		JNIEnv* env, jobject obj) {
	i_printf("deleteEditLayer\n");
	//TODO 編集レイヤーの消去
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_addLayer(
		JNIEnv* env, jobject obj, jint num) {
	i_printf("addLayer\n");
	//TODO レイヤーの追加
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_deleteLayer(
		JNIEnv* env, jobject obj, jint num) {
	i_printf("deleteLayer\n");
	//TODO レイヤーの消去
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_EditLayer(
		JNIEnv* env, jobject obj, jint num) {
	i_printf("EditLayer\n");
	//TODO 編集するレイヤーの選択
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setLayerMode(
		JNIEnv* env, jobject obj, jint num, jint mode) {
	i_printf("setLayerMode\n");
	//TODO レイヤーモードの選択
	return true;

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_Replace(
		JNIEnv* env, jobject obj, jint num, jint move) {
	i_printf("Replace\n");
	//TODO レイヤーの順序変更
	return true;

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setVisible(
		JNIEnv* env, jobject obj, jint num, jboolean truth) {
	i_printf("setVisible\n");
	//TODO 可視不可視の変更
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setMask(
		JNIEnv* env, jobject obj, jint num, jboolean truth) {
	i_printf("setMask\n");
	//TODO マスクレイヤーの作成
	return true;

}

/*
 * 描画周り
 */
JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_Brush_setBrush(
		JNIEnv* env, jobject obj, jobject brush) {
	i_printf("setBrush\n");
	//TODO ブラシのセット
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setColor(
		JNIEnv* env, jobject obj, jint jcolor) {
	i_printf("setColor\n");
	setColor(jcolor);
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setBrushSize(
		JNIEnv* env, jobject obj, jint jsize) {
	i_printf("setBrushSize\n");
	if (jsize > 0) {
		Size = jsize;
		return true;
	} else {
		return false;
	}
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_startDraw(
		JNIEnv* env, jobject obj, jint jx, jint jy) {
	i_printf("startDraw\n");
	int i, j;

	//始点の保持
	dp.x = jx;
	dp.y = jy;

	//始点の描画
	brush_draw(dp.x, dp.y);

	//次点フラグをオフ（次のdraw呼出ポイントを2点目とする）
	dp.flag = 0;
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_draw(
		JNIEnv* env, jobject obj, jint jx, jint jy) {
	i_printf("draw\n");

	//3点目である場合
	if (dp.flag == 1) {
		//3点Bezier曲線描画
		Bezier(dp.x, dp.y, dp.x2, dp.y2, jx, jy);

		//2点目を次回の1点目にする
		dp.x = dp.x2;
		dp.y = dp.y2;
		//3点目を次回の2点目にする
		dp.x2 = jx;
		dp.y2 = jy;
	} else {
		//2点目である場合
		dp.x2 = jx;
		dp.y2 = jy;
	}
	dp.flag = 1;
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_getBitmap(
		JNIEnv* env, jobject obj, jintArray color, jint jw, jint jh) {
	int i, j;
	int flag = 0;
	jint* colors = (*env)->GetIntArrayElements(env, color, 0);
	//i_printf("getBitmap\n");

	//画面サイズの代入
	disp.width = jw;
	disp.height = jh;
	//i_printf( "disp.x = %d, disp.y = %d", disp.x, disp.y);

	int xx, yy;
	int x, y;
	int s;

	//浮動小数点演算対策
	s = scale * 1000;

	//imgの二次元配列を一次元配列に変換し代入
	for (i = 0; i < disp.width; i++) {
		//拡縮元座標の算出
		xx = (i + disp.x) * 1000 / s;
		x = (int) xx;
		for (j = 0; j < disp.height; j++) {
			//拡縮元座標の算出
			yy = (j + disp.y) * 1000 / s;
			y = (int) yy;
			if ((xx < c.width) && (yy < c.height) && (xx > 0) && (yy > 0)) {
				colors[j * disp.width + i] = img[x][y];
			} else {
				colors[j * disp.width + i] = 0xFF000000;
				flag = 1;
			}
		}
	}
	(*env)->ReleaseIntArrayElements(env, color, colors, 0);
	if (flag == 0) {
		return true;
	} else {
		return true;
	}
}

/*
 * 移動周り
 */
JNIEXPORT jboolean
JNICALL Java_com_katout_paint_draw_NativeFunction_setPosition(JNIEnv* env,
		jobject obj, jint jx, jint jy) {
	i_printf("setPosition\n");
	disp.x = -(scale * jx + disp.width);
	disp.y = -(scale * jy + disp.height);
	//i_printf( "disp.x = %d, disp.y = %d", disp.x, disp.y);

	return true;
}

JNIEXPORT jboolean
JNICALL Java_com_katout_paint_draw_NativeFunction_setRadian(JNIEnv* env,
		jobject obj, jdouble rad) {
	i_printf("setRadian\n");
	return true;
}

JNIEXPORT jboolean
JNICALL Java_com_katout_paint_draw_NativeFunction_setScale(JNIEnv* env,
		jobject obj, jdouble jscale) {
	i_printf("setScale\n");
	scale = jscale;
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_init(
		JNIEnv* env, jobject obj, jint x, jint y) {
	i_printf("init\n");
	int i, j;
	c.width = x;
	c.height = y;
	disp.x = 0;
	disp.y = 0;

	//img配列の確保と初期化
	img = (int **) malloc(sizeof(int*) * c.width);
	for (i = 0; i < c.width; i++) {
		img[i] = (int*) malloc(sizeof(int) * c.height);
	}
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			img[i][j] = 0xFFFFFFFF;
		}
	}

	//拡縮後img配列の確保と初期化
	imgs = (int **) malloc(sizeof(int*) * c.width);
	for (i = 0; i < c.width; i++) {
		imgs[i] = (int*) malloc(sizeof(int) * c.height);
	}
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			imgs[i][j] = 0xFFFFFFFF;
		}
	}

	//brush_map配列の確保と初期化
	brush_map = (char **) malloc(sizeof(char*) * bx);
	for (i = 0; i < bx; i++) {
		brush_map[i] = (char*) malloc(sizeof(char) * by);
	}
	for (i = 0; i < bx; i++) {
		for (j = 0; j < by; j++) {
			brush_map[i][j] = 255;
		}
	}

	brush = (int **) malloc(sizeof(int*) * bx);
	for (i = 0; i < bx; i++) {
		brush[i] = (int*) malloc(sizeof(int) * by);
	}

	for (i = 0; i < bx; i++) {
		for (j = 0; j < by; j++) {
			brush[i][j] = brush_map[i][j];
		}
	}
	return true;
}

void brush_draw(int x, int y) {
	int i, j;
	//描画
	i_printf( "width = %d,height = %d,x = %d,y = %d", c.width, c.height, x, y);
	for (i = 0; i < bx; i++) {
		for (j = 0; j < by; j++) {
			if (((x + i) > 0) && ((x + i) < c.width) && ((y + j) > 0)
					&& ((y + j) < c.height)) {
				//i_printf("img[%d][%d] = %d", x+i, y+j, img[x+i][y+j]);
				img[x + i][y + j] = brush[i][j];
			}
		}
	}
}

/*
 * 2点間の距離を求める関数
 */
int distance(int x1, int x2, int y1, int y2) {
	int root = (int) sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	return root;
}

/*
 * 与えられた色をブラシに適用する関数
 */
void setColor(int color) {
	int a, rgb;
	int i, j;
	Color = color;

	//アルファ値の抽出
	a = (Color & 0xFF000000) >> 24;

	//RGB値の抽出
	rgb = (Color & 0x00FFFFFF);
	for (i = 0; i < bx; i++) {
		for (j = 0; j < by; j++) {
			brush[i][j] = (a * brush_map[i][j] / 255 << 24) | rgb;
		}
	}
}

/*
 * 3点Bezier曲線を描画する関数
 */
void Bezier(int x1, int y1, int x2, int y2, int x3, int y3) {
	int i;
	int x, y;
	double t;

	//frequencyが大きいほど緻密に描画する
	for (i = 0; i <= frequency; i++) {
		t = (double) i / frequency;
		x = (1 - t) * (1 - t) * x1 + 2 * t * (1 - t) * x2 + t * t * x3;
		y = (1 - t) * (1 - t) * y1 + 2 * t * (1 - t) * y2 + t * t * y3;
		brush_draw(x, y);
	}
}

/*
 * バイキュービック法による拡縮関数
 */
void Bicubic(int sx, int sy) {
	double nscale;	//倍率
	int x, y;
	int ix, iy;
	double wx, wy;
	int x0, y0;
	double xx, yy;
	double data;
	int tmpy, tmpx;
	double w;
	double dx, dy;

	nscale = 1.0 / scale;

	//バイキュービック法
	for (y = sy; y < sy + disp.width; y++) {
		for (x = sx; x < sx + disp.height; x++) {
			//拡大縮小比率から変換先ピクセルに対応する変換元の座標を計算する
			xx = nscale * (double) x;	//変換元の比率
			ix = (int) xx;	//変換元座標

			yy = nscale * (double) y;
			iy = (int) yy;

			//printf("ix = %d, iy = %d\n",ix,iy);
			data = 0.0;

			//対象となるいちの周囲４＊４ピクセルの値を重みつけして足し合わせる
			for (tmpy = iy - 1; tmpy <= iy + 2; tmpy++) {
				for (tmpx = ix - 1; tmpx <= ix + 2; tmpx++) {

					//printf("tmpx = %d, tmpy = %d\n",tmpx,tmpy);
					dx = xx - (double) tmpx;
					//printf("xx = %1f, tmpx = %1f\n",xx,(double)tmpx);
					//printf("dx = %1f\n",dx);
					if (dx < 0) {
						dx *= -1;
					}
					dy = yy - (double) tmpy;
					//printf("dy = %1f\n",dy);
					//printf("yy = %1f, tmpy = %d\n",yy,tmpy);
					if (dy < 0) {
						dy *= -1;
					}

					//printf("dx = %1f, dy = %1f\n",dx,dy);
					//横方向の重みを計算
					if (dx < 1.0) {
						wx = First_Neighborhood(dx);	//第一近傍か
					} else {
						wx = Second_Neighborhood(dx);
					}
					if (dy < 1.0) {
						wy = First_Neighborhood(dy);	//第一近傍か
					} else {
						wy = Second_Neighborhood(dy);
					}
					w = wx * wy; //重み計算
					//printf("w = %1f\n",w);

					//縦横が変換元のピクセルからはみ出ないように位置を矯正
					x0 = tmpx;
					if ((x0 < 0) || (x0 > (c.height - 1))) {
						x0 = ix;
					}
					y0 = tmpy;
					if ((y0 < 0) || (y0 > (c.width - 1))) {
						y0 = iy;
					}
					//printf("x0 = %d, y0 = %d\n",x0,y0);
					//重みを乗じて16ピクセル分を足し合わせていく
					data = data + (double) img[y0][x0] * w;
					//printf("data = %1f\n",data);
				}
			}
			if (data > 255.0) {
				data = 255.0;
			} else if (data < 0.0) {
				data = 0.0;
			}
			//printf("data = %1f\n",data);
			if ((y >= 0) && (y <= c.width) && (x >= 0) && (x <= c.height)) {
				imgs[y - sy][x - sx] = data;
			}
			//printf("image_dec[y][x] = %s\n",g[y][x]);
		}
	}
}

//第一近傍
double First_Neighborhood(double d) {
	return (d - 1.0) * (d * d - d - 1.0);
}

//第二近傍
double Second_Neighborhood(double d) {
	return -1 * (d - 1.0) * (d - 2.0) * (d - 2.0);
}

/*
 * バイリニア法による拡縮
 * 引数のx,yは共に0以上
 */
void Bilinear(int x, int y) {
	int w = c.width;
	int h = c.height;
	int i, j;
	double nscale = 1 / scale;
	double xx, yy;
	int x0, y0;
	double fx, fy;
	int x1, y1;
	double f0, f1, f2, f3;
	int c0, c1, c2, c3;

	for (i = y; i < y + disp.height; i++) {
		yy = i * nscale;
		y0 = (int) yy;
		fy = yy - y0;
		if ((y0 + 1) < h) {
			y1 = y0 + 1;
		} else {
			y1 = y0;
		}
		for (j = x; j < x + disp.width; j++) {
			xx = j * nscale;
			x0 = (int) xx;
			fx = xx - x0;

			f0 = (1.0 - fx) * (1.0 - fy);
			f1 = fx * (1.0 - fy);
			f2 = (1.0 - fx) * fy;
			f3 = f3 = fx * fy;

			if ((x0 + 1) < w) {
				x1 = x0 + 1;
			} else {
				x1 = x0;
			}

			if (((y0 + 1) < c.height) && ((x0 + 1) < c.width)) {
				c0 = img[y0][x0];
				c1 = img[y0][x1];
				c2 = img[y1][x0];
				c3 = img[y1][x1];
			} else {
				c0 = 0;
				c1 = 0;
				c2 = 0;
				c3 = 0;
			}
			imgs[i - y][j - x] = c0 * f0 + c1 * f1 + c2 * f2 + c3 * f3;
		}
	}
}

/*
 * 閉領域塗りつぶし関数
 */
void fill(int x, int y) {
	int cl;
	cl = img[x][y];
	img[x][y] = Color;

	//上を走査
	if ((y - 1) >= 0) {
		if (img[x][y - 1] == cl) {
			fill(x, y - 1);
		}
	}

	//右を走査
	if ((x + 1) < c.width) {
		if (img[x + 1][y] == cl) {
			fill(x + 1, y);
		}
	}

	//下を走査
	if ((y + 1) < c.height) {
		if (img[x][y + 1] == cl) {
			fill(x, y + 1);
		}
	}

	//左を走査
	if ((x - 1) >= 0) {
		if (img[x - 1][y] == cl) {
			fill(x - 1, y);
		}
	}
}

