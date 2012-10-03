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
	return true;
}

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

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_getBitmap(
		JNIEnv* env, jobject obj, jintArray color, jint jw, jint jh) {
	int i, j;
	int flag = 0;
	jint* colors = (*env)->GetIntArrayElements(env, color, 0);
	i_printf("getBitmap\n");

	//画面サイズの代入
	disp.width = jw;
	disp.height = jh;
	//i_printf( "disp.width = %d, disp.height = %d", disp.width, disp.height);

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
		return false;
	}
}

/*
 * 移動周り
 */
JNIEXPORT jboolean
JNICALL Java_com_katout_paint_draw_NativeFunction_setPosition(JNIEnv* env,
		jobject obj, jint jx, jint jy) {
	i_printf("setPosition\n");
	disp.x = jx;
	disp.y = jy;

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
	setColor(0xFF000000);
	return true;
}

void brush_draw(int x, int y) {
	int i, j;
	//描画
	for (i = 0; i < bx; i++) {
		if (((x + i - bx / 2) < 0) || ((x + i - bx / 2) >= c.width)) {
			continue;
		}
		for (j = 0; j < by; j++) {
			if (((y + j - by / 2) < 0) || ((y + j - by / 2) >= c.height)) {
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

void setColor(int color) {
	int a, rgb;
	int i, j;
	Color = color;
	a = (Color & 0xFF000000) >> 24;
	rgb = (Color & 0x00FFFFFF);
	for (i = 0; i < bx; i++) {
		for (j = 0; j < by; j++) {
			brush[i][j] = (a * brush_map[i][j] / 255 << 24) | rgb;
		}
	}
}
