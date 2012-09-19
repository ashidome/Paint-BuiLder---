#include <jni.h>
#include <android/log.h>
//#include <android/bitmap.h>
#include <stdlib.h>
#include <math.h>
#include <stdbool.h>

#define i_printf(...) __android_log_print(ANDROID_LOG_INFO, "hj", __VA_ARGS__)

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
static int flag = 0;

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
	Color = jcolor;
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
	if (flag == 0) {
		init();
		flag++;
	}
	//始点の保持
	dp.sx = jx;
	dp.sy = jy;
	//初期描画
	brush_draw(dp.sx, dp.sy);
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_draw(
		JNIEnv* env, jobject obj, jint jx, jint jy) {
	i_printf("draw\n");
	int i, j;
	int dst;
	double cos_t, sin_t;

	//終点の保持
	dp.ex = jx;
	dp.ey = jy;
	dst = distance(dp.sx, dp.ex, dp.sy, dp.ey);
	//theta = atan2(dp.sy - dp.ey, dp.sx - dp.ex);
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

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_getBitmap(
		JNIEnv* env, jobject obj, jintArray color, jint jw, jint jh) {
	i_printf("getBitmap\n");
	int i, j;
	jint* colors = (*env)->GetIntArrayElements(env, color, 0);
	i_printf("getBitmap1\n");
	for (i = 0; i < jw; i++) {
		for (j = 0; j < jh; j++) {
			colors[i * jw + j] = img[i][j];
		}
	}
	i_printf("getBitmap2\n");
	(*env)->ReleaseIntArrayElements(env, color, colors, 0);
	return true;
}

/*
 * 移動周り
 */
JNIEXPORT jboolean
JNICALL Java_com_katout_paint_draw_NativeFunction_setPosition(JNIEnv* env,
		jobject obj, jint jx, jint jy) {
	i_printf("setPosition\n");
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
		jobject obj, jdouble scale) {
	i_printf("setScale\n");
	return true;
}

JNIEXPORT jboolean
JNICALL Java_com_katout_paint_draw_NativeFunction_init(JNIEnv* env, jobject obj,
		jint x, jint y) {
	i_printf("init\n");
	c.width = x;
	c.height = y;
	return true;
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
	for (i = 0; i < height; i++) {
		for (j = 0; j < width; j++) {
			img[i][j] = 0xFFFFFFFF;
		}
	}
}
