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
static int frequency = 30;
static double scale;

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

/*
 JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_draw(
 JNIEnv* env, jobject obj, jint jx, jint jy) {
 i_printf("draw\n");
 int i, j;
 int dst;
 double cos_t, sin_t;
 dst = distance(dp.x, jx, dp.y, jy);
 //i_printf("dst = %d\n", dst);
 theta = atan2(jy - dp.y, jx - dp.x);
 //i_printf("theta = %d\n", theta);
 cos_t = cos(theta);
 sin_t = sin(theta);
 //始点、終点間の補間
 for (i = 0; i < dst / interval; i++) {
 dp.x += interval * cos_t;
 dp.y += interval * sin_t;
 brush_draw(dp.x, dp.y);
 //i_printf("put(%d,%d)\n", dp.x, dp.y);
 }
 return true;
 }
 */

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

	//imgの二次元配列を一次元配列に変換し代入
	for (i = 0; i < disp.width; i++) {
		for (j = 0; j < disp.height; j++) {
			if (((i + disp.x) < c.width) && ((j + disp.y) < c.height)
					&& ((i + disp.x) > 0) && ((j + disp.y) > 0)) {
				colors[j * disp.width + i] = img[i + disp.x][j + disp.y];
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
	disp.x = -(jx + disp.width);
	disp.y = -(jy + disp.height);
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
	//i_printf("width = %d,height = %d,x = %d,y = %d", c.width, c.height, x, y);
	for (i = 0; i < bx; i++) {
		for (j = 0; j < by; j++) {
			if (((x + i - bx / 2) > 0) && ((x + i - (bx + 10) / 2) < c.width)
					&& ((y + j - by / 2) > 0)
					&& ((y + j - (by + 10) / 2) < c.height)) {
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
void Bicubic(){
	int x, y;
	int ix, iy;
	double wx, wy;
	int x0, y0;
	double xx, yy;
	double data;
	int tmpx, tmpy;

	for(y = 0; y < c.width; y++){
		for(x = 0; x < c.height; y++){
			//拡縮比率より変換先ピクセルに対応する元配列の座標を計算
			xx = scale * (double)x;
			ix = (int)xx;

			yy = scale * (double)y;
			iy = (int)yy;

			data = 0.0;

			//対象ピクセルの周囲4*4ピクセルの値を重み付けし足し合わせる
			for(tmpy = iy-1;tmpy <= iy+2; tmpy++){
				for(tmpx = ix-1;tmpx <= ix+2; tmpx++){

				}
			}
		}
	}
}
