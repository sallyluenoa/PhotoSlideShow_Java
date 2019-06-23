package com.example.photoslideshow.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtils {

    /**
     * 指定された画像ファイルを読み込んでBitmapを生成する.
     */
    public static Bitmap getBitmap(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    /**
     * 画像の元サイズから期待されるサイズに近づけるための、最も効率的な縮小率を求める.
     * Ex. org(1080,1920), exp(500,1000) の場合、最も期待サイズに近い値は (540,960) なので 0.5 を返す.
     */
    public static float getEffectiveScale(long orgMin, long orgMax, long expMin, long expMax) {
        // 最大値と最小値が逆になっていたら正しく修正する.
        if (orgMin > orgMax) {
            long tmp = orgMin;
            orgMin = orgMax;
            orgMax = tmp;
        }
        if (expMin > expMax) {
            long tmp = expMin;
            expMin = expMax;
            expMax = tmp;
        }

        // 期待サイズより元サイズが小さい場合は縮小しない.
        if (orgMin < expMin && orgMax < expMax) return 1.0f;

        // 元サイズの半分のサイズで再帰処理をして、最小スケール値を求める.
        float minScale = getEffectiveScale(orgMin/2, orgMax/2, expMin, expMax) * 0.5f;

        // 評価値を元に、期待サイズに最も近い効率の良いスケール値を判断する.
        // 縦横それぞれの期待サイズとの差の絶対値をとり、その和を評価値とする.
        // 比較対象: 元サイズ、元サイズに0.75かけたもの、元サイズに最小スケール値(0.5以下)をかけたもの
        long orgEvl = Math.abs(orgMin - expMin) + Math.abs(orgMax - expMax);
        long quaEvl = Math.abs(orgMin*3/4 - expMin) + Math.abs(orgMax*3/4 - expMax);
        long sclEvl = Math.abs((long)(orgMin * minScale) - expMin) + Math.abs((long)(orgMax * minScale) - expMax);
        long evaluation = Math.min(orgEvl, Math.min(quaEvl, sclEvl));

        if (evaluation == orgEvl) return 1.0f;
        else if (evaluation == quaEvl) return 0.75f;
        else return minScale;
    }

    /**
     * 画像ファイルの縦横サイズを取得する.
     * 結果をlong型の配列で返し、第1要素にwidth, 第2要素にheightを格納する.
     * オプション指定することで、Bitmapにメモリ展開せずに効率的にサイズ取得が可能.
     */
    public static long[] getLengths(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        return new long[] { (long)options.outWidth, (long)options.outHeight };
    }
}
