#include <jni.h>
//#include <android/log.h>
//#include <android/bitmap.h>
#include <stdlib.h>
#include <math.h>
#include <stdbool.h>

void brush_draw(int x, int y);
void init();
int distance(int x1, int x2, int y1, int y2);

/*
 * レイヤ周り
 */

struct Canvas {
	bool flag;
	int height;
	int width;
};

struct Layers {
	int layer_num;
};

struct Layer {

};

struct DrawPoints {
	int sx, sy; //始点
	int ex, ey; //終点
};

static struct Canvas c;
static struct Laler layers;
static struct DrawPoints dp;
static int Color;
static int Size;
static int theta;

//テスト用
static int brush[5][5];
static int height = 600;
static int width = 800;
static int img[600][800]; // = 0x00000000
static int interval = 10;

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_setCanvasSize(
		JNIEnv* env, jobject obj, jint jx, jint jy) {
	if (c.flag) {
		c.flag = false;
		c.height = jx;
		c.width = jy;
		return true;
	} else {
		return false;
	}
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_deleteEditLayer(
		JNIEnv* env, jobject obj) {

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_addLayer(
		JNIEnv* env, jobject obj, jint num) {

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_deleteLayer(
		JNIEnv* env, jobject obj, jint num) {

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_EditLayer(
		JNIEnv* env, jobject obj, jint num) {

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_setLayerMode(
		JNIEnv* env, jobject obj, jint num, jint mode) {

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_Replace(
		JNIEnv* env, jobject obj, jint num, jint move) {

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_setVisible(
		JNIEnv* env, jobject obj, jint num, jboolean truth) {

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_setMask(
		JNIEnv* env, jobject obj, jint num, jboolean truth) {

}

/*
 * 描画周り
 */

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_Brush_setBrush(
		JNIEnv* env, jobject obj, jobject brush) {

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_setColor(
		JNIEnv* env, jobject obj, jint jcolor) {
	Color = jcolor;
	return true;

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_setBrushSize(
		JNIEnv* env, jobject obj, jint jsize) {
	if (jsize > 0) {
		Size = jsize;
		return true;
	} else {
		return false;
	}
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_startDraw(
		JNIEnv* env, jobject obj, jint jx, jint jy) {
	int i, j;
	init();
	//始点の保持
	dp.sx = jx;
	dp.sy = jy;
	//初期描画
	brush_draw(dp.sx, dp.sy);
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_draw(
		JNIEnv* env, jobject obj, jint jx, jint jy) {
	int i, j;
	int dst;
	double cos_t, sin_t;
	//終点の保持
	dp.ex = jx;
	dp.ey = jy;
	dst = distance(dp.sx, dp.ex, dp.sy, dp.ey);
	theta = atan2(dp.ey - dp.sy, dp.ex - dp.sx);
	cos_t = cos(theta);
	sin_t = sin(theta);

	for (i = 0; i < dst; i += interval) {
		dp.sx = interval * cos_t;
		dp.sy = interval * sin_t;
		brush_draw(dp.sx, dp.sy);
	}
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_getBitmap(
		JNIEnv* env, jobject obj) {

}

/*
 * 移動周り
 */
JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_setPosition(
		JNIEnv* env, jobject obj, jint jx, jint jy) {

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_setRadian(
		JNIEnv* env, jobject obj, jdouble rad) {

}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_MainActivity_setScale(
		JNIEnv* env, jobject obj, jdouble scale) {

}

void brush_draw(int x, int y) {
	int i, j;
	//描画
	for (i = 0; i < 5; i++) {
		if (((x + i - 2) < 0) || ((x + i - 2) >= 800)) {
			continue;
		}
		for (j = 0; j < 5; j++) {
			if (((y + j - 2) < 0) || ((y + i - 2) >= 800)) {
				continue;
			}
			img[x + i][y + j] = brush[i][j];
		}
	}
}

int distance(int x1, int x2, int y1, int y2) {
	int root = (int) sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	return root;
}

void init() {
	int i, j;
	for (i = 0; i < 5; i++) {
		for (j = 0; j < 5; j++) {
			brush[i][j] = 0xFF000000;
		}
	}
}
