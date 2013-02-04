#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <stdbool.h>

#define i_printf(...) __android_log_print(ANDROID_LOG_INFO, "hj", __VA_ARGS__)

#define MAXSIZE 40000000
#define MAX_BRUSH_WIDTH 100
#define MAX_BRUSH_HEIGHT 100
#define MAX_LAYER_SIZE 5
#define MAX_BRUSH_NUM 2

void mymemset(int*  dst, int c, int n);
void mymemset2(int*  dst, int c, int n);
void brush_draw(int x, int y, int flag);
void applyEdit(int flag);
void initEditLayer(int flag);
int distance(int x1, int x2, int y1, int y2);
void setColor(int color, int flag);
void Bezier(int x1, int y1, int x2, int y2, int x3, int y3, int flag);
void Bicubic(int sx, int sy, double by, int flag);
double First_Neighborhood(double d);
double Second_Neighborhood(double d);
void Bilinear(int x, int y);
void scanLine(int lx, int rx, int y, unsigned int col);
void fill(int x, int y, unsigned int paintCol);
void setBrush(jchar brush_img[], int flag);
int Normal_Draw(int src, int dest);
int Eraser_Draw(int src, int dest);
void setBrushSize(int size, int flag);
int get_alpha(int c);
int grayscale(int c);
int max(int a, int b);
int min(int a, int b);
void blendBuff(int x, int y, int flag);
int Blend_Layer(int mode, int src, int dest, int layer_num);
void recomposition(int flag);
void initLayer(int current);
void startDraw(int x, int y, int flag);
void draw(int x, int y, int flag);
int swap_rgb(int c);
int alphablend(int src, int dst, int alpha);

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
	char **map_img;
	int width;
	int height;
	int frequency;
};

struct Brush {
	int **brush_img;
	int width;
	int height;
	int color;
	int Mode;
};

struct LayerData {
	int **img;
	int alpha;
	int mode;
};

static clock_t start,end;

static struct Display disp;
static struct Canvas c;
static struct Layers layers;
static struct DrawPoints *dp;
static struct FillPoint fp;
static struct BrushMap *brushmap;
static struct Brush *brush;
static struct LayerData *layerdata;
static int Size;
static int theta;
static int init_flag = 0;

static int ***EditLayer;
static int *BuffImg;
static double scale;

struct BufStr {
	int sx; /* 領域右端のX座標 */
	int sy; /* 領域のY座標 */
};
struct BufStr buff[MAXSIZE]; /* シード登録用バッファ */
struct BufStr *sIdx, *eIdx; /* buffの先頭・末尾ポインタ */

void  mymemset(int*  dst, int color, int size){
	__asm__ volatile (
		"loopBegin:\n\t"
		"STR %[color],[%[dst]], #+4\n\t"
		"SUBS	%[size], %[size], #1\n\t"
		"BNE loopBegin\n\t"
		: [dst] "+r" (dst)
		: [color] "r" (color), [size] "r" (size)
		:
	);
}

void  mymemset2(int*  dst, int color, int size){
	__asm__ volatile (
		"loopBegin2:\n\t"
		"STR %[color],[%[dst]], #-4\n\t"
		"SUBS	%[size], %[size], #1\n\t"
		"BNE loopBegin2\n\t"
		: [dst] "+r" (dst)
		: [color] "r" (color), [size] "r" (size)
		:
	);
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setCanvasSize(
		JNIEnv* env, jobject obj, jint jx, jint jy) {
	i_printf("setCanvasSize\n");
	if (c.flag) {
		c.flag = JNI_FALSE;
		c.height = jx;
		c.width = jy;
		return JNI_TRUE;
	} else {
		return JNI_FALSE;
	}
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_deleteEditLayer(
		JNIEnv* env, jobject obj) {
	i_printf("deleteEditLayer\n");

	//編集レイヤの初期化
	initEditLayer(0);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_addLayer(
		JNIEnv* env, jobject obj) {
	i_printf("addLayer\n");
	int i;
	struct LayerData temp_layerdata;

	if (layers.layer_max + 1 > MAX_LAYER_SIZE) {
		return JNI_FALSE;
	} else {
		//退避
		temp_layerdata = layerdata[layers.layer_max];
		for (i = layers.layer_max - 1; i > layers.current_layer; i--) {
			layerdata[i + 1] = layerdata[i];
		}
		layerdata[layers.current_layer + 1] = temp_layerdata;

		layers.layer_max++;
		layers.current_layer++;

		i_printf("Current Layer is %d", layers.current_layer);
		return JNI_TRUE;
	}
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_deleteLayer(
		JNIEnv* env, jobject obj) {
	int i;
	struct LayerData temp_layerdata;
	i_printf("deleteLayer\n");

	initLayer(layers.current_layer);

	//退避
	temp_layerdata = layerdata[layers.current_layer];
	for (i = layers.current_layer; i < layers.layer_max; i++) {
		layerdata[i] = layerdata[i + 1];
	}
	layerdata[layers.layer_max - 1] = temp_layerdata;

	layers.layer_max--;
	if (layers.current_layer == 0) {
		layers.current_layer = 0;
	} else {
		layers.current_layer--;
	}

	i_printf("Current Layer is %d", layers.current_layer);

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_selectLayer(
		JNIEnv* env, jobject obj, jint num) {
	i_printf("selectLayer\n");
	layers.current_layer = num;
	i_printf("Current Layer is %d", layers.current_layer);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setLayerMode(
		JNIEnv* env, jobject obj, jint mode) {
	i_printf("setLayerMode\n");
	//可視不可視もここで設定
	layerdata[layers.current_layer].mode = mode;
	i_printf("mode is %d", layerdata[layers.current_layer].mode);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setLayerAlpha(
		JNIEnv* env, jobject obj, jint progress) {
	i_printf("setLayerAlpha\n");
	i_printf("set alpha = %d", progress);
	layerdata[layers.current_layer].alpha = progress;
	return JNI_TRUE;
}

JNIEXPORT jint JNICALL Java_com_katout_paint_draw_NativeFunction_getLayerNum(
		JNIEnv* env, jobject obj) {
	i_printf("getLayerNum\n");
	return layers.layer_max;
}

JNIEXPORT jint JNICALL Java_com_katout_paint_draw_NativeFunction_getCurrentNum(
		JNIEnv* env, jobject obj) {
	i_printf("getCurrentNum\n");
	return layers.current_layer;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_getLayersData(
		JNIEnv* env, jobject obj, jintArray jmode, jintArray jalpha) {
	i_printf("getLayersData\n");
	jint* mode = (*env)->GetIntArrayElements(env, jmode, 0);
	jint* alpha = (*env)->GetIntArrayElements(env, jalpha, 0);
	int i;

	for (i = 0; i < layers.layer_max; i++) {
		mode[i] = layerdata[i].mode;
		alpha[i] = layerdata[i].alpha;
	}

	(*env)->ReleaseIntArrayElements(env, jmode, mode, 0);
	(*env)->ReleaseIntArrayElements(env, jalpha, alpha, 0);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_Recomposition(
		JNIEnv* env, jobject obj) {
	i_printf("Recomposition\n");
	recomposition(0);
	return JNI_TRUE;
}

/*
 * getPreview(int レイヤー番号,int[] Preview格納用配列,int Preview幅,int Preview高さ)
 * レイヤー番号 == -1 で全体Preview
 */
JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_getPreview(
		JNIEnv* env, jobject obj, jint layer_num, jobject bitmap) {
	i_printf("getPreview:%d",layer_num);

	int i, j;
	int x, y;
	int s;
	AndroidBitmapInfo info;
	int* pixels;
	int ret;

	int black_padding;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		i_printf("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return JNI_FALSE;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		i_printf("Bitmap format is not RGBA_8888 !");
		return JNI_FALSE;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
		i_printf("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	for (i = 0; i < info.width; i++) {
		for (j = 0; j < info.height; j++) {
			pixels[j * info.width + i] = 0xFF000000;
		}
	}

	if ((double) c.height / info.height < (double) c.width / info.width) {
		i_printf("Preview width apply\n");
		//浮動小数点演算対策
		s = info.width * 512 / c.width;
		black_padding = (info.height - info.width * (double) c.height / c.width)
				/ 2;

		//imgの二次元配列を一次元配列に変換し代入
		for (i = 0; i < info.width; i++) {
			//拡縮元座標の算出
			x = i * 512 / s;
			for (j = black_padding; j < info.height - black_padding - 1; j++) {
				//拡縮元座標の算出
				y = (j - black_padding) * 512 / s;
				if (layer_num == -1) {
					int temp = BuffImg[y * c.width + x];
					pixels[j * info.width + i] =temp;
				} else {
					pixels[j * info.width + i] = layerdata[layer_num].img[x][y];
				}
			}
		}
	} else {
		i_printf("Preview height apply\n");
		//浮動小数点演算対策
		s = info.height * 512 / c.height;
		black_padding = (info.width - info.height * (double) c.width / c.height)
				/ 2;

		i_printf("s = %d, black_padding = %d\n", s, black_padding);
		//imgの二次元配列を一次元配列に変換し代入
		for (i = black_padding; i < info.width - black_padding - 1; i++) {
			//拡縮元座標の算出
			x = (i - black_padding) * 512 / s;
			for (j = 0; j < info.height; j++) {
				//拡縮元座標の算出
				y = j * 512 / s;
				if (layer_num == -1) {
					pixels[j * info.width + i] = BuffImg[y * c.width + x];
				} else {
					pixels[j * info.width + i] = layerdata[layer_num].img[x][y];
				}
			}
		}
	}
	AndroidBitmap_unlockPixels(env, bitmap);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_Replace(
		JNIEnv* env, jobject obj, jint num, jint move) {
	i_printf("Replace\n");
//TODO レイヤーの順序変更
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setMask(
		JNIEnv* env, jobject obj, jint num, jboolean truth) {
	i_printf("setMask\n");
//TODO マスクレイヤーの作成
	return JNI_TRUE;
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

	brushmap[0].frequency = jfrequency;

//ブラシマップサイズの定義
	brushmap[0].width = jw;
	brushmap[0].height = jh;

//新規ブラシマップの適用
	setBrush(colors, 0);

//サイズ変更の適用
	setBrushSize(Size, 0);

	(*env)->ReleaseCharArrayElements(env, color, colors, 0);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_getBrushRawSize(
		JNIEnv* env, jobject obj, jintArray wh) {
	i_printf("getBrushRawSize");
	jint* array = (*env)->GetIntArrayElements(env, wh, 0);

	array[0] = brushmap[0].width;
	array[1] = brushmap[0].height;
	array[2] = brushmap[0].frequency;

	(*env)->ReleaseIntArrayElements(env, wh, array, 0);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_getBrushRawMap(
		JNIEnv* env, jobject obj, jcharArray map) {
	i_printf("getBrushRawMap");
	jchar* Map = (*env)->GetCharArrayElements(env, map, 0);
	int i, j;

	for (i = 0; i < brushmap[0].width; i++) {
		for (j = 0; j < brushmap[0].height; j++) {
			Map[j * brushmap[0].width + i] = brushmap[0].map_img[i][j];
		}
	}

	(*env)->ReleaseCharArrayElements(env, map, Map, 0);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setColor(
		JNIEnv* env, jobject obj, jint jcolor) {
	i_printf("setColor\n");
	setColor(jcolor, 0);
	setBrushSize(Size, 0);
	i_printf("end_setColor");
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setBrushSize(
		JNIEnv* env, jobject obj, jint jsize) {
//jsize = 1~500
//使用時に√x * 3.0 で補正;
	i_printf("setBrushSize JNI\n");
	setBrushSize(jsize, 0);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_startDraw(
		JNIEnv* env, jobject obj, jint jx, jint jy) {
	i_printf("startDraw\n");

	startDraw(jx, jy, 0);

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_endDraw(
		JNIEnv* env, jobject obj) {
	i_printf("endDraw\n");
	int i, j;

	//EditLayerをimg配列に適用する
	applyEdit(0);

	//EditLayerの初期化
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			EditLayer[0][i][j] = 0x00000000;
		}
	}

//initEditLayer();
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_draw(
		JNIEnv* env, jobject obj, jint jx, jint jy) {
	i_printf("draw\n");

	draw(jx, jy, 0);

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_Bucket(
		JNIEnv* env, jobject obj, jint jx, jint jy, jint t) {
//fill(jx, jy, t);
	return JNI_TRUE;
}

JNIEXPORT jint JNICALL Java_com_katout_paint_draw_NativeFunction_getCanvasHeight(
		JNIEnv* env, jobject obj) {
	i_printf("getCanvasHeight\n");
	return c.height;
}

JNIEXPORT jint JNICALL Java_com_katout_paint_draw_NativeFunction_getCanvasWidth(
		JNIEnv* env, jobject obj) {
	i_printf("getCanvasWidth\n");
	return c.width;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_getRawdata(
		JNIEnv* env, jobject obj, jintArray color) {
	i_printf("getRawdata\n");
	int i, j, k, l;
	int pixel = 0x00FFFFFF;
	jint* colors = (*env)->GetIntArrayElements(env, color, 0);

	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			for (k = 0; k < layers.layer_max; k++) {
				pixel = Blend_Layer(layerdata[k].mode, layerdata[k].img[i][j],
						pixel, k);
			}
			colors[j * c.width + i] = swap_rgb(pixel);
		}
	}

	(*env)->ReleaseIntArrayElements(env, color, colors, 0);
	return JNI_TRUE;
}

// TODO getBitmap
JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_getBitmap(
		JNIEnv* env, jobject obj, jobject bitmap) {
	int  i, j;
	int  x, y,ym;
	int  s;
	AndroidBitmapInfo info;
	int* pixels;
	int ret;
	static int init;
	int x_s,y_s,x_f,y_f, temp_x, temp_y;
	temp_x = disp.x;
	temp_y = disp.y;
	//i_printf( "getBitmap start");

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		i_printf("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return JNI_FALSE;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		i_printf("Bitmap format is not RGBA_8888 !");
		return JNI_FALSE;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
		i_printf("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}


	//画面サイズの代入
	disp.width = info.width;
	disp.height = info.height;
	 start = clock();
	//浮動小数点演算対策
	s = scale * 512;

	//上方向黒埋め
	y_s = 0;
	y =(temp_y<<9) / s;
	if(y < 0){
		for(y_s = 1;y_s < disp.height; y_s++){
			y = ((y_s + temp_y) <<9) / s;
			if(y >=0){
				break;
			}
		}
		//y_s--;
		//i_printf("y_s = %d", y_s);
		mymemset(pixels, 0xFF000000, y_s*disp.width);
		//i_printf("上方向\n");
	}

	//下方向黒埋め
	y_f = disp.height - 1;
	y = ((y_f + temp_y) << 9) / s;
	if (y >= c.height) {
		while(y_f--){
			y = ((y_f + temp_y) <<9) / s;
			if(y < c.height){
				break;
			}
		}
		//y_f++;
		//i_printf("y_f = %d", y_f);
		mymemset2(pixels + disp.height*disp.width, 0xFF000000, (disp.height - y_f)*disp.width);
		//i_printf("下方向\n");
	}


	//左方向黒埋め
	x_s = 0;
	x = (temp_x << 9) / s;
	if (x < 0) {
		for (x_s = 1; x_s < disp.width; x_s++) {
			x = ((x_s + temp_x) <<9) / s;
			if (x >= 0) {
				break;
			}
		}
		//x_s--;
		//i_printf("x_s = %d", x_s);
		for(j = y_s; j < y_f; j++){
			mymemset(pixels + j * disp.width, 0xFF000000, x_s);
		}
		//i_printf("左方向\n");
	}

	//右方向黒埋め
	x_f = disp.width - 1;
	x = ((x_f + temp_x) <<9) / s;
	if (x >= c.width) {
		while (x_f--) {
			x = ((x_f + temp_x) <<9) / s;
			if (x < c.width) {
				break;
			}
		}
		//x_f++;
		//i_printf("x_f = %d", x_f);
		for (j = y_s; j < y_f; j++) {
			mymemset(pixels + j * disp.width + x_f, 0xFF000000, disp.width - x_f);
		}
		//i_printf("右方向\n");
	}

	//i_printf("pixel埋め開始\n");
	y = (temp_y<<9) / s;
	ym = y * c.width;
	pixels+=y_s*disp.width + x_s;
	//imgの二次元配列を一次元配列に変換し代入
	for (i = x_s,j = y_s; j < y_f; i++) {
		if (i >= x_f) {
			i = x_s-1;
			j++;
			//拡縮元座標の算出
			y = ((j + temp_y) <<9) / s;
			ym = y * c.width;
			pixels += x_s + (disp.width - x_f) ;
			continue;
		}

		//拡縮元座標の算出
		x = ((i + temp_x) <<9) / s;

		*(pixels++) = BuffImg[ym + x];
	}
	end = clock();
	//i_printf("getbitmap end:%.3f\n",(double)(end-start)/CLOCKS_PER_SEC);
	AndroidBitmap_unlockPixels(env, bitmap);
	return JNI_TRUE;
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

	return JNI_TRUE;
}

JNIEXPORT jboolean
JNICALL Java_com_katout_paint_draw_NativeFunction_setPositionD(JNIEnv* env,
		jobject obj) {
	i_printf("setPositionD\n");
	disp.x = 0;
	disp.y = 0;

	return JNI_TRUE;
}

JNIEXPORT jboolean
JNICALL Java_com_katout_paint_draw_NativeFunction_setRadian(JNIEnv* env,
		jobject obj, jdouble rad) {
	i_printf("setRadian\n");
	return JNI_TRUE;
}

JNIEXPORT jboolean
JNICALL Java_com_katout_paint_draw_NativeFunction_setScale(JNIEnv* env,
		jobject obj, jdouble jscale) {
	i_printf("setScale\n");
	scale = jscale;
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_init(
		JNIEnv* env, jobject obj, jint x, jint y, jint jflag, jintArray jmap) {
	i_printf("init\n");
	//flag == 1 のときmap利用
	//初回フラグinit_flag
	//既に初期化されていればfalse,初期化されていなければtrue

	if (init_flag == 1) {
		return JNI_FALSE;
	} else {
		jint* map;
		int i, j, k;
		if (jflag == 1) {
			map = (*env)->GetIntArrayElements(env, jmap, 0);
		}

		c.width = x;
		c.height = y;
		disp.x = 0;
		disp.y = 0;
		scale = 1.0;

		//layerdataの確保と初期化
		layerdata = (struct LayerData*) malloc(
				sizeof(struct LayerData) * MAX_LAYER_SIZE);
		for (i = 0; i < MAX_LAYER_SIZE; i++) {
			layerdata[i].img = (int **) malloc(sizeof(int*) * c.width);
			for (j = 0; j < c.width; j++) {
				layerdata[i].img[j] = (int*) malloc(sizeof(int) * c.height);
			}
		}
		for (i = 0; i < MAX_LAYER_SIZE; i++) {
			for (j = 0; j < c.width; j++) {
				mymemset(layerdata[i].img[j], 0x00FFFFFF,c.height);
			}
		}
		for (i = 0; i < c.width; i++) {
			for (j = 0; j < c.height; j++) {
				if (jflag == 0) {
					layerdata[0].img[i][j] = 0xFFFFFFFF;
				} else {
					layerdata[0].img[i][j] = swap_rgb(map[j * c.width + i]);
				}
			}
		}

		//レイヤーモード配列確保と初期化
		for (i = 0; i < MAX_LAYER_SIZE; i++) {
			layerdata[i].mode = 0;
			layerdata[i].alpha = 255;
		}

		//EditLayerの確保と初期化
		EditLayer = (int***) malloc(sizeof(int **) * MAX_BRUSH_NUM);
		for (i = 0; i < MAX_BRUSH_NUM; i++) {
			EditLayer[i] = (int **) malloc(sizeof(int*) * c.width);
			for (j = 0; j < c.width; j++) {
				EditLayer[i][j] = (int*) malloc(sizeof(int) * c.height);
			}
		}
		for (i = 0; i < MAX_BRUSH_NUM; i++) {
			for (j = 0; j < c.width; j++) {
				mymemset(EditLayer[i][j], 0x00000000,c.height);
			}
		}
		//バッファ配列の確保と初期化
		BuffImg = (int *) malloc(sizeof(int) * c.width*c.height);
		mymemset(BuffImg,0xffffffff,c.width*c.height);

		//brushmapの確保と初期化
		brushmap = (struct BrushMap*) malloc(
				sizeof(struct BrushMap) * MAX_BRUSH_NUM);
		for (i = 0; i < MAX_BRUSH_NUM; i++) {
			brushmap[i].map_img = (char **) malloc(
					sizeof(char*) * MAX_BRUSH_WIDTH);
			for (j = 0; j < MAX_BRUSH_WIDTH; j++) {
				brushmap[i].map_img[j] = (char*) malloc(
						sizeof(char) * MAX_BRUSH_HEIGHT);
			}
		}
		for (i = 0; i < MAX_BRUSH_NUM; i++) {
			for (j = 0; j < MAX_BRUSH_WIDTH; j++) {
				for (k = 0; k < MAX_BRUSH_HEIGHT; k++) {
					brushmap[i].map_img[j][k] = 255;
				}
			}
		}
		for (i = 0; i < MAX_BRUSH_NUM; i++) {
			brushmap[i].width = 10;
			brushmap[i].height = 10;
			brushmap[i].frequency = 30;
		}

		//brushの確保と初期化
		brush = (struct Brush*) malloc(sizeof(struct Brush) * 2);
		for (i = 0; i < MAX_BRUSH_NUM; i++) {
			brush[i].brush_img = (int **) malloc(
					sizeof(int*) * MAX_BRUSH_WIDTH * 20);
			for (j = 0; j < MAX_BRUSH_WIDTH * 20; j++) {
				brush[i].brush_img[j] = (int*) malloc(
						sizeof(int) * MAX_BRUSH_HEIGHT * 20);
			}
		}
		for (i = 0; i < MAX_BRUSH_NUM; i++) {
			brush[i].width = 10;
			brush[i].height = 10;
			brush[i].color = 0xFFFFFFFF;
			brush[i].Mode = 0;
		}

		for (i = 0; i < MAX_BRUSH_NUM; i++) {
			for (j = 0; j < brush[i].width; j++) {
				for (k = 0; k < brush[i].height; k++) {
					brush[i].brush_img[j][k] = brushmap[i].map_img[j][k];
				}
			}
		}

		//DrawPoints配列の確保と初期化
		dp = (struct DrawPoints*) malloc(
				sizeof(struct DrawPoints) * MAX_BRUSH_NUM);
		for (i = 0; i < MAX_BRUSH_NUM; i++) {
			dp[i].flag = 0;
			dp[i].x = 0;
			dp[i].x2 = 0;
			dp[i].y = 0;
			dp[i].y2 = 0;
		}

		layers.layer_max = 1;
		layers.current_layer = 0;
		Size = 16;

		init_flag = 1;

		if (jflag == 1) {
			recomposition(0);
			(*env)->ReleaseIntArrayElements(env, jmap, map, 0);
		}
		return JNI_TRUE;
	}
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_destructor(
		JNIEnv* env, jobject obj) {
	i_printf("destructor\n");

	int i, j;

//img配列の開放
	for (i = 0; i < MAX_LAYER_SIZE; i++) {
		for (j = 0; j < c.width; j++) {
			free(layerdata[i].img[j]);
		}
		free(layerdata[i].img);
	}
	free(layerdata);

	i_printf("free(img)\n");

//編集レイヤー配列の開放
	for (i = 0; i < MAX_BRUSH_NUM; i++) {
		for (j = 0; j < c.width; j++) {
			free(EditLayer[i][j]);
		}
		free(EditLayer[i]);
	}
	free(EditLayer);

	i_printf("free(EditLayer)\n");

//バッファ配列の開放
	free(BuffImg);

	i_printf("free(BuffImg)\n");

//brushmap配列の開放
	for (i = 0; i < MAX_BRUSH_NUM; i++) {
		for (j = 0; j < MAX_BRUSH_WIDTH; j++) {
			free(brushmap[i].map_img[j]);
		}
		free(brushmap[i].map_img);
	}
	free(brushmap);

	i_printf("free(brush_map)\n");

//brush配列の開放
	for (i = 0; i < MAX_BRUSH_NUM; i++) {
		for (j = 0; j < MAX_BRUSH_WIDTH * 20; j++) {
			free(brush[i].brush_img[j]);
		}
		free(brush[i].brush_img);
	}
	free(brush);

	i_printf("free(brush)\n");

	init_flag = 0;

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_katout_paint_draw_NativeFunction_setMode(
		JNIEnv* env, jobject obj, jint num) {
	i_printf("setMode Mode = %d\n", num);
	brush[0].Mode = num;
	return JNI_TRUE;
}

/*
 * 通信共同描画用関数
 * joint(レイヤ番号,ブラシマップ,ブラシマップ幅,ブラシマップ高,ブラシ頻度,ブラシサイズ,色,座標(x,y交互),座標配列サイズ,描画モード)
 */
JNIEXPORT jboolean
JNICALL Java_com_katout_paint_draw_NativeFunction_joint(JNIEnv* env,
		jobject obj, jint jlayer_num, jcharArray jcolor, jint jw, jint jh,
		jint jfrequency, jint jsize, jint color, jintArray jpoint,
		jint jpoint_size, int jmode) {
	i_printf("joint\n");
	int i, j;

	jchar* colors = (*env)->GetCharArrayElements(env, jcolor, 0);
	jint* points = (*env)->GetIntArrayElements(env, jpoint, 0);

	brushmap[1].frequency = jfrequency;

//ブラシマップサイズの定義
	brushmap[1].width = jw;
	brushmap[1].height = jh;

	brush[1].Mode = jmode;

//新規ブラシマップの適用
	setBrush(colors, 1);

//色指定
	setColor(color, 1);

//サイズ変更の適用
	setBrushSize(jsize, 1);

	i_printf("end setBrushSize\n");

	i_printf("points[0] = %d, points[1] = %d\n", points[0], points[1]);

//始点描画
	startDraw(points[0], points[1], 1);

	i_printf("end startDraw\n");

	for (i = 2; i < jpoint_size; i += 2) {
		draw(points[i], points[i + 1], 1);
	}

	i_printf("joint_draw_end\n");

//EditLayerをimg配列に適用する
	applyEdit(1);

//EditLayerの初期化
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			EditLayer[1][i][j] = 0x00000000;
		}
	}

	(*env)->ReleaseCharArrayElements(env, jcolor, colors, 0);
	(*env)->ReleaseIntArrayElements(env, jpoint, points, 0);

	return JNI_TRUE;
}

/*
 * ブラシによる描画関数
 */
void brush_draw(int x, int y, int flag) {
	int i, j;
	int bwidth, bheight;

	bwidth = brush[flag].width / 2;
	bheight = brush[flag].height / 2;

//描画
//i_printf( "width = %d,height = %d,x = %d,y = %d", c.width, c.height, x, y);
	for (i = -bwidth; i < bwidth; i++) {
		for (j = -bheight; j < bheight; j++) {
			if (((x + i) > 0) && ((x + i) < c.width) && ((y + j) > 0)
					&& ((y + j) < c.height)) {
				if (get_alpha(EditLayer[flag][x + i][y + j])
						< get_alpha(
								brush[flag].brush_img[i + bwidth][j + bheight])) {
					EditLayer[flag][x + i][y + j] = brush[flag].brush_img[i
							+ bwidth][j + bheight];
					blendBuff(x + i, y + j, flag);
				}
			}
		}
	}
}

/*
 * Buff配列に合成する関数
 */
void blendBuff(int x, int y, int flag) {
	int i;
	int Pixel = 0xFFDDDDDD;
	int Edit;

	if ((x / 30 + y / 30) % 2 == 0) {
		Pixel = 0xFFFFFFFF;
	}

	if (brush[flag].Mode == 1) {
		Edit = Eraser_Draw(EditLayer[flag][x][y],
				layerdata[layers.current_layer].img[x][y]);
	} else if (brush[flag].Mode == 0) {
		Edit = Normal_Draw(EditLayer[flag][x][y],
				layerdata[layers.current_layer].img[x][y]);
	}
	for (i = 0; i < layers.layer_max; i++) {
		if (i == layers.current_layer) {
			Pixel = Blend_Layer(layerdata[layers.current_layer].mode, Edit,
					Pixel, i);
		} else {
			Pixel = Blend_Layer(layerdata[i].mode, layerdata[i].img[x][y],
					Pixel, i);
		}
	}
	BuffImg[y * c.width + x] = Pixel;
}

/*
 * 再合成関数
 */
void recomposition(int flag) {
	int i, j;
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			blendBuff(i, j, flag);
		}
	}
}

/*
 * アルファ値抽出関数
 */
int get_alpha(int c) {
	return ((c & 0xFF000000) >> 24);
}

int swap_rgb(int c){
	int a,r,g,b;
	a= get_alpha(c);
	r = ((c & 0x00FF0000) >> 16);
	g = ((c & 0x0000FF00) >> 8);
	b = (c & 0x000000FF);
	return (a << 24) | (b<< 16) | (g << 8) | r;
}

/*
 * EditLayerの変更をimg配列に適用する関数
 */
void applyEdit(int flag) {
	int i, j;
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			if (brush[flag].Mode == 0) {
				if (EditLayer[flag][i][j] != 0x00000000) {
					layerdata[layers.current_layer].img[i][j] = Normal_Draw(
							EditLayer[flag][i][j],
							layerdata[layers.current_layer].img[i][j]);
				}
			} else if (brush[flag].Mode == 1) {
				if (EditLayer[flag][i][j] != 0x00000000) {
					layerdata[layers.current_layer].img[i][j] = Eraser_Draw(
							EditLayer[flag][i][j],
							layerdata[layers.current_layer].img[i][j]);
				}
			}
		}
	}
}

/*
 * EditLayer初期化関数
 */
void initEditLayer(int flag) {
	int i, j;
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			if (EditLayer[flag][i][j] != 0x00000000) {
				EditLayer[flag][i][j] = 0x00000000;
				blendBuff(i, j, flag);
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
void setColor(int color, int flag) {
	brush[flag].color = color;
}

/*
 * 3点Bezier曲線を描画する関数
 */
void Bezier(int x1, int y1, int x2, int y2, int x3, int y3, int flag) {
	int i;
	int x, y;
	double t;

//frequencyが大きいほど緻密に描画する
	for (i = 0; i <= brushmap[flag].frequency; i++) {
		t = (double) i / brushmap[flag].frequency;
		x = (1 - t) * (1 - t) * x1 + 2 * t * (1 - t) * x2 + t * t * x3;
		y = (1 - t) * (1 - t) * y1 + 2 * t * (1 - t) * y2 + t * t * y3;
		brush_draw(x, y, flag);
	}
}

/*
 * バイキュービック法による拡縮関数
 * 始点x,y、倍率
 */
void Bicubic(int sx, int sy, double by, int flag) {
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

	a = get_alpha(brush[flag].color);
	rgb = swap_rgb(brush[flag].color & 0x00FFFFFF);

	nscale = 1.0 / by;

//バイキュービック法
	for (y = sy; y < sy + by * brushmap[flag].width; y++) {
		for (x = sx; x < sx + by * brushmap[flag].height; x++) {
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
					if ((x0 < 0) || (x0 > (brushmap[flag].height - 1))) {
						x0 = ix;
					}
					y0 = tmpy;
					if ((y0 < 0) || (y0 > (brushmap[flag].width - 1))) {
						y0 = iy;
					}
					//重みを乗じて16ピクセル分を足し合わせていく
					data = data + (double) brushmap[flag].map_img[y0][x0] * w;
				}
			}
			if (data > 255.0) {
				data = 255.0;
			} else if (data < 0.0) {
				data = 0.0;
			}
			if ((y >= 0) && (y < by * brushmap[flag].width) && (x >= 0)
					&& (x < by * brushmap[flag].height)) {
				brush[flag].brush_img[y - sy][x - sx] = ((a * (int) data) / 255
						<< 24) | rgb;
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
				c0 = layerdata[layers.current_layer].img[y0][x0];
				c1 = layerdata[layers.current_layer].img[y0][x1];
				c2 = layerdata[layers.current_layer].img[y1][x0];
				c3 = layerdata[layers.current_layer].img[y1][x1];
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
void setBrush(jchar brush_img[], int flag) {
	int i, j;
//新規ブラシマップを適用
	for (i = 0; i < brushmap[flag].width; i++) {
		for (j = 0; j < brushmap[flag].height; j++) {
			brushmap[flag].map_img[i][j] = brush_img[i * brushmap[flag].height
					+ j];
			//i_printf("brush_map[%d][%d] = %d\n", i, j, brush_map[i][j]);
		}
	}
}

/*
 * ブラシサイズ変更関数
 */
void setBrushSize(int size, int flag) {
	i_printf("setBrushSize");
	int magni_size;
	double Magnification;
	Size = size;
	magni_size = sqrt(Size) * 3.0;

	Magnification = (double) magni_size / (double) brushmap[flag].width;

	Bicubic(0, 0, Magnification, flag);
	brush[flag].width = brushmap[flag].width * Magnification;
	brush[flag].height = brushmap[flag].height * Magnification;
}

/*
 * 始点描画用関数
 * flag = 1 は共同描画用
 */
void startDraw(int x, int y, int flag) {
	//始点の保持
	dp[flag].x = x;
	dp[flag].y = y;

	//始点の描画
	brush_draw(dp[flag].x, dp[flag].y, flag);

	//次点フラグをオフ（次のdraw呼出ポイントを2点目とする）
	dp[flag].flag = 0;
}

/*
 * 次点以降描画用関数
 * flag = 1 は共同描画用
 */
void draw(int x, int y, int flag) {
	//3点目である場合
	if (dp[flag].flag == 1) {
		//3点Bezier曲線描画
		Bezier(dp[flag].x, dp[flag].y, dp[flag].x2, dp[flag].y2, x, y, flag);

		//2点目を次回の1点目にする
		dp[flag].x = dp[flag].x2;
		dp[flag].y = dp[flag].y2;
		//3点目を次回の2点目にする
		dp[flag].x2 = x;
		dp[flag].y2 = y;
	} else {
		//2点目である場合
		dp[flag].x2 = x;
		dp[flag].y2 = y;
	}
	dp[flag].flag = 1;
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

void initLayer(int current) {
	int i, j;
	for (i = 0; i < c.width; i++) {
		for (j = 0; j < c.height; j++) {
			layerdata[current].img[i][j] = 0x00000000;
		}
	}
}

/*
 * レイヤー合成関数
 */
int Blend_Layer(int mode, int src, int dest, int layer_num) {
	int src_a, src_r, src_g, src_b;
	int dest_a, dest_r, dest_g, dest_b;
	int a, r, g, b;
	int result;

	//上側
	src_a = (src & 0xFF000000) >> 24;
	src_r = (src & 0x00FF0000) >> 16;
	src_g = (src & 0x0000FF00) >> 8;
	src_b = (src & 0x000000FF);
	src_a = src_a * layerdata[layer_num].alpha / 255;

	//下側
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
		dest_r = (src_r * dest_r) / 255;
		r = alphablend(src_r, dest_r, src_a);
		if (r > 255) {
			r = 255;
		}
		dest_g = (src_g * dest_g) / 255;
		g = alphablend(src_g, dest_g, src_a);
		if (g > 255) {
			g = 255;
		}
		dest_b = (src_b * dest_b) / 255;
		b = alphablend(src_b, dest_b, src_a);
		if (b > 255) {
			b = 255;
		}
		a = src_a + dest_a;
		if (a > 255) {
			a = 255;
		}
		result = (a << 24) | (r<< 16) | (g << 8) | b;
		break;
	case 2: //スクリーン
		dest_r = 255 - (255 - dest_r) * (255 - src_r) / 255;
		r = alphablend(dest_r, src_r, src_a);
		if(r > 255){
			r = 255;
		}
		dest_g = 255 - (255 - dest_g) * (255 - src_g) / 255;
		g = alphablend(dest_g, src_g, src_a);
		if (g > 255) {
			g = 255;
		}
		dest_b = 255 - (255 - dest_b) * (255 - src_b) / 255;
		b = alphablend(dest_b, src_b, src_a);
		if (b > 255) {
			b = 255;
		}
		a = src_a + dest_a;
		if (a > 255) {
			a = 255;
		}
		result = (a << 24) | (r << 16) | (g << 8) | b;
		break;
	case 3: //オーバレイ
		if (dest_r < 128) {
			r = 2 * dest_r * src_r / 255;
		} else {
			r = 255 - 2 * (255 - dest_r) * (255 - src_r) / 255;
		}
		if (r > 255) {
			r = 255;
		}
		if (dest_g < 128) {
			g = 2 * dest_g * src_g / 255;
		} else {
			g = 255 - 2 * (255 - dest_g) * (255 - src_g) / 255;
		}
		if (g > 255) {
			g = 255;
		}
		if (dest_b < 128) {
			b = 2 * dest_b * src_b / 255;
		} else {
			b = 255 - 2 * (255 - dest_b) * (255 - src_b) / 255;
		}
		if (b > 255) {
			b = 255;
		}
		a = src_a + dest_a;
		if (a > 255) {
			a = 255;
		}
		result = (a << 24) | (r<< 16) | (g << 8) | b;
		break;
	case 4: //ソフトライト
		break;
	case 5: //ハードライト
		if (src_r > 128) {

		} else {

		}
		if (r > 255) {
			r = 255;
		}
		a = src_a + dest_a;
		if (a > 255) {
			a = 255;
		}
		result = (a << 24) | (r << 16) | (g << 8) | b;
		break;
	case 6: //覆い焼きカラー
		r = (256 * dest_r) / ((255 - src_r) - dest_r);
		if (r > 255) {
			r = 255;
		}
		g = (256 * dest_g) / ((255 - src_g) - dest_g);
		if (g > 255) {
			g = 255;
		}
		b = (256 * dest_b) / ((255 - src_b) - dest_b);
		if (b > 255) {
			b = 255;
		}
		a = src_a + dest_a;
		if (a > 255) {
			a = 255;
		}
		result = (a << 24) | (r << 16) | (g << 8) | b;
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
		result = dest;
		break;
	}
	return result;
}

/*
 * アルファブレンド関数
 */
int alphablend(int src, int dst, int alpha) {
	return ((255 - alpha) * dst + alpha * src) / 255;
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
