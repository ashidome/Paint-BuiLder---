#include <jni.h>
#include <android/log.h>
//#include <android/bitmap.h>
#include <stdlib.h>
#include <math.h>
#include <stdbool.h>

#define i_printf(...) __android_log_print(ANDROID_LOG_INFO, "hj", __VA_ARGS__)

#define MAXSIZE 40000000
#define MAX_BRUSH_WIDTH 100
#define MAX_BRUSH_HEIGHT 100
#define MAX_LAYER_SIZE 5

void brush_draw(int x, int y);
void applyEdit();
void initEditLayer();
int distance(int x1, int x2, int y1, int y2);
void setColor(int color);
void Bezier(int x1, int y1, int x2, int y2, int x3, int y3);
void Bicubic(int sx, int sy, double by);
double First_Neighborhood(double d);
double Second_Neighborhood(double d);
void Bilinear(int x, int y);
void scanLine(int lx, int rx, int y, unsigned int col);
void fill(int x, int y, unsigned int paintCol);
void setBrush(jchar brush_img[]);
int Normal_Draw(int src, int dest);
int Eraser_Draw(int src, int dest);
void setBrushSize(int size);
int get_alpha(int c);
int grayscale(int c);
int max(int a, int b);
int min(int a, int b);
void blendBuff(int x, int y);
int Blend_Layer(int mode, int src, int dest);

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
	int layer_max;
	int current_layer;
};

struct DrawPoints {
	int x, y; //始点
	int x2, y2; //次点
	int flag;
};

struct FillPoint {
	int *x;
	int *y;
	int count;
};

struct BrushMap {
	int width;
	int height;
};

static struct Display disp;
static struct Canvas c;
static struct Layers layers;
static struct DrawPoints dp;
static struct FillPoint fp;
static struct BrushMap brushmap;
static int Color;
static int Size;
static int theta;
static int Mode;

//テスト用
static int **brush;
static char **brush_map;
static int bx;
static int by;
static int ***img;
static int **EditLayer;
static int **BuffImg;
static int frequency = 30;
static double scale;

struct BufStr {
	int sx; /* 領域右端のX座標 */
	int sy; /* 領域のY座標 */
};
struct BufStr buff[MAXSIZE]; /* シード登録用バッファ */
struct BufStr *sIdx, *eIdx; /* buffの先頭・末尾ポインタ */

/*
 * レイヤーモード列挙
 */
enum layer_mode {
	NORMAL, MUL, SCREEN, OVERLAY
};

enum layer_mode lmode;

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

	//編集レイヤの初期化
	initEditLayer();
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_addLayer(
		JNIEnv* env, jobject obj) {
	i_printf("addLayer\n");
	int i;

	if (layers.layer_max + 1 > MAX_LAYER_SIZE) {
		return false;
	} else {
		layers.layer_max++;
		layers.current_layer++;

		for (i = layers.layer_max - 1; i >= layers.current_layer; i--) {
			img[i + 1] = img[i];
		}
		return true;
	}
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_deleteLayer(
		JNIEnv* env, jobject obj, jint num) {
	i_printf("deleteLayer\n");
	int i;

	for (i = layers.current_layer; i < layers.layer_max; i++) {
		img[i] = img[i + 1];
	}
	layers.layer_max--;
	if (layers.current_layer == 0) {
		layers.current_layer = 0;
	} else {
		layers.current_layer--;
	}
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_selectLayer(
		JNIEnv* env, jobject obj, jint num) {
	i_printf("EditLayer\n");
	layers.current_layer = num;
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
JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setBrush(
		JNIEnv* env, jobject obj, jcharArray color, jint jw, jint jh,
		jint jfrequency) {
	int i, j;
	i_printf("setBrush\n");
	jchar* colors = (*env)->GetCharArrayElements(env, color, 0);

	frequency = jfrequency;

	//ブラシマップサイズの定義
	brushmap.width = jw;
	brushmap.height = jh;

	//新規ブラシマップの適用
	setBrush(colors);

	//サイズ変更の適用
	setBrushSize(Size);

	(*env)->ReleaseCharArrayElements(env, color, colors, 0);
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setColor(
		JNIEnv* env, jobject obj, jint jcolor) {
	i_printf("setColor\n");
	setColor(jcolor);
	setBrushSize(Size);
	i_printf("end_setColor");
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setBrushSize(
		JNIEnv* env, jobject obj, jint jsize) {
	//jsize = 1~500
	//使用時に√x / 20 0.05 ~ 25;
	i_printf("setBrushSize\n");
	setBrushSize(jsize);
	return true;
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

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_endDraw(
		JNIEnv* env, jobject obj) {
	i_printf("endDraw\n");

	//EditLayerをimg配列に適用する
	applyEdit();

	//EditLayerの初期化
	initEditLayer();
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

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_Bucket(
		JNIEnv* env, jobject obj, jint jx, jint jy, jint t) {
	//fill(jx, jy, t);
	return true;
}

JNIEXPORT jint JNICALL Java_com_katout_paint_draw_NativeFunction_getCanvasHeight(
		JNIEnv* env, jobject obj) {
	return c.height;
}

JNIEXPORT jint JNICALL Java_com_katout_paint_draw_NativeFunction_getCanvasWidth(
		JNIEnv* env, jobject obj) {
	return c.width;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_getRawdata(
		JNIEnv* env, jobject obj, jintArray color) {
	int i, j;
	jint* colors = (*env)->GetIntArrayElements(env, color, 0);

	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			colors[j * c.width + i] = img[layers.current_layer][i][j];
		}
	}

	(*env)->ReleaseIntArrayElements(env, color, colors, 0);
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
				colors[j * disp.width + i] = BuffImg[x][y];
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
	int i, j, k;
	c.width = x;
	c.height = y;
	disp.x = 0;
	disp.y = 0;
	scale = 1.0;

	//img配列の確保と初期化
	img = (int ***) malloc(sizeof(int*) * MAX_LAYER_SIZE);
	for (i = 0; i < MAX_LAYER_SIZE; i++) {
		img[i] = (int **) malloc(sizeof(int*) * c.width);
		for (j = 0; j < c.width; j++) {
			img[i][j] = (int*) malloc(sizeof(int) * c.height);
		}
	}
	for (i = 0; i < MAX_LAYER_SIZE; i++) {
		for (j = 0; j < c.width; j++) {
			for (k = 0; k < c.height; k++) {
				img[i][j][k] = 0x00000000;
			}
		}
	}
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			img[0][i][j] = 0xFFFFFFFF;
		}
	}

	//編集レイヤー配列の確保と初期化
	EditLayer = (int **) malloc(sizeof(int*) * c.width);
	for (i = 0; i < c.width; i++) {
		EditLayer[i] = (int*) malloc(sizeof(int) * c.height);
	}
	//EditLayerの初期化
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			EditLayer[i][j] = 0x00000000;
		}
	}
	//バッファ配列の確保と初期化
	BuffImg = (int **) malloc(sizeof(int*) * c.width);
	for (i = 0; i < c.width; i++) {
		BuffImg[i] = (int*) malloc(sizeof(int) * c.height);
	}
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			BuffImg[i][j] = 0xFFFFFFFF;
		}
	}

	//brush_map配列の確保と初期化
	brush_map = (char **) malloc(sizeof(char*) * MAX_BRUSH_WIDTH);
	for (i = 0; i < MAX_BRUSH_WIDTH; i++) {
		brush_map[i] = (char*) malloc(sizeof(char) * MAX_BRUSH_HEIGHT);
	}
	for (i = 0; i < MAX_BRUSH_WIDTH; i++) {
		for (j = 0; j < MAX_BRUSH_HEIGHT; j++) {
			brush_map[i][j] = 255;
		}
	}

	brush = (int **) malloc(sizeof(int*) * MAX_BRUSH_WIDTH * 20);
	for (i = 0; i < MAX_BRUSH_WIDTH * 20; i++) {
		brush[i] = (int*) malloc(sizeof(int) * MAX_BRUSH_HEIGHT * 20);
	}
	brushmap.width = 10;
	bx = brushmap.width;

	brushmap.height = 10;
	by = brushmap.height;

	for (i = 0; i < bx; i++) {
		for (j = 0; j < by; j++) {
			brush[i][j] = brush_map[i][j];
		}
	}
	layers.layer_max = 1;
	layers.current_layer = 0;
	Size = 16;
	Color = 0xFFFFFFFF;
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_destructor(
		JNIEnv* env, jobject obj) {
	i_printf("destructor\n");

	int i, j;

	//img配列の開放
	for (i = 0; i < MAX_LAYER_SIZE; i++) {
		for (j = 0; j < c.width; j++) {
			free(img[i][j]);
		}
		free(img[i]);
	}
	free(img);

	//編集レイヤー配列の開放
	for (i = 0; i < c.width; i++) {
		free(EditLayer[i]);
	}
	free(EditLayer);

	//brush_map配列の開放
	for (i = 0; i < MAX_BRUSH_WIDTH; i++) {
		free(brush_map[i]);
	}
	free(brush_map);

	//brush配列の開放
	for (i = 0; i < MAX_BRUSH_WIDTH * 20; i++) {
		free(brush[i]);
	}
	free(brush);

	return true;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setMode(
		JNIEnv* env, jobject obj, jint num) {
	i_printf("setMode Mode = %d\n", num);
	Mode = num;
	return true;
}
/*
 * ブラシによる描画関数
 */
void brush_draw(int x, int y) {
	int i, j;
	//描画
	//i_printf( "width = %d,height = %d,x = %d,y = %d", c.width, c.height, x, y);
	for (i = 0; i < bx; i++) {
		for (j = 0; j < by; j++) {
			if (((x + i) > 0) && ((x + i) < c.width) && ((y + j) > 0)
					&& ((y + j) < c.height)) {
				if (get_alpha(EditLayer[x + i][y + j])
						< get_alpha(brush[i][j])) {
					EditLayer[x + i][y + j] = brush[i][j];
					blendBuff(x + i, y + j);
				}
			}
		}
	}
}

/*
 * Buff配列に合成する関数
 */
void blendBuff(int x, int y) {
	int i;
	int Pixel = 0xFFFFFFFF;
	int flag = 0;
	//EditLayerのため
	for (i = 0; i < layers.layer_max + 1; i++) {
		if (flag == 0 && i > layers.current_layer) {
			if (Mode == 0) {
				Pixel = Normal_Draw(EditLayer[x][y], Pixel);
			} else if (Mode == 1) {
				Pixel = Eraser_Draw(EditLayer[x][y], Pixel);
			}
			flag = 1;
		} else {
			/*
			 * TODO
			 * レイヤー合成モード実装
			 */
			//Pixel = Normal_Draw(img[i][x][y], Pixel);
			Pixel = Blend_Layer(0, img[i][x][y], Pixel);
		}
	}
	BuffImg[x][y] = Pixel;
}

/*
 * アルファ値抽出関数
 */
int get_alpha(int c) {
	return ((c & 0xFF000000) >> 24);
}

/*
 * EditLayerの変更をimg配列に適用する関数
 */
void applyEdit() {
	int i, j;
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			if (Mode == 0) {
				if (EditLayer[i][j] != 0x00000000) {
					img[layers.current_layer][i][j] = Normal_Draw(EditLayer[i][j],
							img[layers.current_layer][i][j]);
				}
			} else if (Mode == 1) {
				if (EditLayer[i][j] != 0x00000000) {
					img[layers.current_layer][i][j] = Eraser_Draw(EditLayer[i][j],
							img[layers.current_layer][i][j]);
				}
			}
		}
	}
}

/*
 * EditLayer初期化関数
 */
void initEditLayer() {
	int i, j;
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			if (EditLayer[i][j] != 0x00000000) {
				EditLayer[i][j] = 0x00000000;
				blendBuff(i, j);
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
	Color = color;
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
 * 始点x,y、倍率
 */
void Bicubic(int sx, int sy, double by) {
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

	int a, rgb;

	a = get_alpha(Color);
	rgb = (Color & 0x00FFFFFF);
	i_printf("a = %d, rgb = %x", a, rgb);

	nscale = 1.0 / by;

	//バイキュービック法
	for (y = sy; y < sy + by * brushmap.width; y++) {
		for (x = sx; x < sx + by * brushmap.height; x++) {
			//拡大縮小比率から変換先ピクセルに対応する変換元の座標を計算する
			xx = nscale * (double) x;	//変換元の比率
			ix = (int) xx;	//変換元座標

			yy = nscale * (double) y;
			iy = (int) yy;

			data = 0.0;

			//対象となるいちの周囲４＊４ピクセルの値を重みつけして足し合わせる
			for (tmpy = iy - 1; tmpy <= iy + 2; tmpy++) {
				for (tmpx = ix - 1; tmpx <= ix + 2; tmpx++) {
					dx = xx - (double) tmpx;
					if (dx < 0) {
						dx *= -1;
					}
					dy = yy - (double) tmpy;
					if (dy < 0) {
						dy *= -1;
					}

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

					//縦横が変換元のピクセルからはみ出ないように位置を矯正
					x0 = tmpx;
					if ((x0 < 0) || (x0 > (brushmap.height - 1))) {
						x0 = ix;
					}
					y0 = tmpy;
					if ((y0 < 0) || (y0 > (brushmap.width - 1))) {
						y0 = iy;
					}
					//重みを乗じて16ピクセル分を足し合わせていく
					data = data + (double) brush_map[y0][x0] * w;
				}
			}
			if (data > 255.0) {
				data = 255.0;
			} else if (data < 0.0) {
				data = 0.0;
			}
			if ((y >= 0) && (y < by * brushmap.width) && (x >= 0)
					&& (x < by * brushmap.height)) {
				brush[y - sy][x - sx] = ((a * (int) data) / 255 << 24) | rgb;
			}
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
				c0 = img[layers.current_layer][y0][x0];
				c1 = img[layers.current_layer][y0][x1];
				c2 = img[layers.current_layer][y1][x0];
				c3 = img[layers.current_layer][y1][x1];
			} else {
				c0 = 0;
				c1 = 0;
				c2 = 0;
				c3 = 0;
			}
			//imgs[i - y][j - x] = c0 * f0 + c1 * f1 + c2 * f2 + c3 * f3;
		}
	}
}
//
///*
// * 線分からシードを探索してバッファに登録する関数
// *
// * int lx,rx:線分のX座標の範囲
// * int y:線分のY座標
// * unsigned int col:領域色
// */
//void scanLine(int lx, int rx, int y, unsigned int col) {
//	while (lx <= rx) {
//		//非領域色を飛ばす
//		for (; lx <= rx; lx++) {
//			if (img[lx][y] == col) {
//				break;
//			}
//		}
//		if (img[lx][y] != col) {
//			break;
//		}
//
//		//領域色を飛ばす
//		for (; lx <= rx; lx++) {
//			if (img[lx][y] != col) {
//				break;
//			}
//		}
//
//		eIdx->sx = lx - 1;
//		eIdx->sy = y;
//		if (++eIdx == &buff[MAXSIZE]) {
//			eIdx = buff;
//		}
//	}
//}
//
///*
// * 塗りつぶし関数
// *
// * int x,y:開始座標
// * unsigned int paintCol : 描画色
// */
//void fill(int x, int y, unsigned int paintCol) {
//	int lx, rx;
//	int ly;
//	int i;
//	unsigned int col = img[x][y];
//	if (col == paintCol) {
//		return;
//	}
//	sIdx = buff;
//	sIdx = buff + 1;
//	sIdx->sx = x;
//	sIdx->sy = y;
//
//	do {
//		lx = rx = sIdx->sx;
//		ly = sIdx->sy;
//		if (++sIdx == &buff[MAXSIZE]) {
//			sIdx = buff;
//		}
//
//		//処理済みのシードなら無視
//		if (img[lx][ly] != col) {
//			continue;
//		}
//
//		//右方向の境界を走査
//		while (rx < c.width) {
//			if (img[rx + 1][ly] != col) {
//				break;
//			}
//			rx++;
//		}
//
//		//左方向の境界を走査
//		while (lx > 0) {
//			if (img[lx - 1][ly] != col) {
//				break;
//			}
//			lx--;
//		}
//
//		//lx-rxの線分を描画
//		for (i = lx; i <= rx; i++) {
//			img[i][ly] = paintCol;
//		}
//
//		//真上のスキャンラインを走査
//		if (ly - 1 >= 0) {
//			scanLine(lx, rx, ly - 1, col);
//		}
//
//		//真下のスキャンラインを走査
//		if (ly + 1 <= c.height) {
//			scanLine(lx, rx, ly + 1, col);
//		}
//	} while (sIdx != eIdx);
//}

/*
 * 新規ブラシマップセット関数
 */
void setBrush(jchar brush_img[]) {
	int i, j;
//新規ブラシマップを適用
	for (i = 0; i < brushmap.width; i++) {
		for (j = 0; j < brushmap.height; j++) {
			brush_map[i][j] = brush_img[i * brushmap.height + j];
			//i_printf("brush_map[%d][%d] = %d\n", i, j, brush_map[i][j]);
		}
	}
}

/*
 * ブラシサイズ変更関数
 */
void setBrushSize(int size) {
	i_printf("setBrushSize");
	double Magnification;
	Size = size;
	Magnification = sqrt(Size) / 4.0;

	Bicubic(0, 0, Magnification);
	bx = brushmap.width * Magnification;
	by = brushmap.height * Magnification;
}

/*
 * グレースケール値を返す関数
 */
int grayscale(int c) {
	int a, r, g, b;
	int gray;
	int MAX, MIN;

//a = (c & 0xFF000000) >> 24;
	r = (c & 0x00FF0000) >> 16;
	g = (c & 0x0000FF00) >> 8;
	b = (c & 0x000000FF);

	MAX = max(r, max(g, b));
	MIN = min(r, min(g, b));

	gray = (MAX + MIN) / 2;

//gray = (77 * r + 150 * g + 29 * b) >> 8;

	return (255 - gray);
}

/*
 * レイヤー合成関数
 */
int Blend_Layer(int mode, int src, int dest) {
	int src_a, src_r, src_g, src_b;
	int dest_a, dest_r, dest_g, dest_b;
	int a, r, g, b;
	int result;

	src_a = (src & 0xFF000000) >> 24;
	src_r = (src & 0x00FF0000) >> 16;
	src_g = (src & 0x0000FF00) >> 8;
	src_b = (src & 0x000000FF);

	dest_a = (dest & 0xFF000000) >> 24;
	dest_r = (dest & 0x00FF0000) >> 16;
	dest_g = (dest & 0x0000FF00) >> 8;
	dest_b = (dest & 0x000000FF);

	switch (mode) {
	case 0: //通常
		r = (src_r * src_a + dest_r * (255 - src_a)) / 255;
		if (r > 255) {
			r = 255;
		}
		g = (src_g * src_a + dest_g * (255 - src_a)) / 255;
		if (g > 255) {
			g = 255;
		}
		b = (src_b * src_a + dest_b * (255 - src_a)) / 255;
		if (b > 255) {
			b = 255;
		}
		a = src_a + dest_a;
		if (a > 255) {
			a = 255;
		}

		result = (a << 24) | (r << 16) | (g << 8) | b;
		break;
	case 1: //乗算
		break;
	case 2: //スクリーン
		break;
	case 3: //オーバレイ
		break;
	case 4: //ソフトライト
		break;
	case 5: //ハードライト
		break;
	case 6: //覆い焼きカラー
		break;
	case 7: //焼きこみカラー
		break;
	case 8: //比較（暗）
		break;
	case 9: //比較（明）
		break;
	case 10: //差の絶対値
		break;
	case 11: //除外
		break;
	case 12: //インビジブル
		break;
	}
	return result;
}

/*
 * 通常描画関数
 */
int Normal_Draw(int src, int dest) {
	int src_a, src_r, src_g, src_b;
	int dest_a, dest_r, dest_g, dest_b;
	int a, r, g, b;
	int result;

	src_a = (src & 0xFF000000) >> 24;
	src_r = (src & 0x00FF0000) >> 16;
	src_g = (src & 0x0000FF00) >> 8;
	src_b = (src & 0x000000FF);

	dest_a = (dest & 0xFF000000) >> 24;
	dest_r = (dest & 0x00FF0000) >> 16;
	dest_g = (dest & 0x0000FF00) >> 8;
	dest_b = (dest & 0x000000FF);

	r = (src_r * src_a + dest_r * (255 - src_a)) / 255;
	if (r > 255) {
		r = 255;
	}
	g = (src_g * src_a + dest_g * (255 - src_a)) / 255;
	if (g > 255) {
		g = 255;
	}
	b = (src_b * src_a + dest_b * (255 - src_a)) / 255;
	if (b > 255) {
		b = 255;
	}
	a = src_a + dest_a;
	if (a > 255) {
		a = 255;
	}

	result = (a << 24) | (r << 16) | (g << 8) | b;

	return result;
}

/*
 * 消しゴム関数
 */
int Eraser_Draw(int src, int dest) {
	int src_a;
	int dest_a, dest_r, dest_g, dest_b;
	int a;
	int result;

	src_a = (src & 0xFF000000) >> 24;

	dest_a = (dest & 0xFF000000) >> 24;
	dest_r = (dest & 0x00FF0000) >> 16;
	dest_g = (dest & 0x0000FF00) >> 8;
	dest_b = (dest & 0x000000FF);

	a = dest_a - src_a;
	if (a < 0) {
		a = 0;
	}

	result = (a << 24) | (dest_r << 16) | (dest_g << 8) | dest_b;

	return result;
}

int max(int a, int b) {
	if (a > b) {
		return a;
	} else {
		return b;
	}
}

int min(int a, int b) {
	if (a < b) {
		return a;
	} else {
		return b;
	}
}

