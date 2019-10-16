package com.example.pictureloader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    private ImageView ivTest;

    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initIvTest();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1){
            if(ContextCompat.checkSelfPermission(getApplication(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                loadBitmapFromFile();
            }
        }
    }

    public void resize(View view) {
        Bitmap test;
        //Rect rect = new Rect(0,0,test.getWidth(),test.getHeight());
        test = resizeBitmapFromFile("/storage/131B-1A02/IMG_20191011_135824.jpg",2);
        showBitmap(test);
    }

    public void gray(View view) {
        Bitmap test = BitmapFactory.decodeFile("/storage/131B-1A02/IMG_20191011_135824.jpg");
        test = bitmapToGray(test);
        showBitmap(test);
    }

    public void round(View view) {
        Bitmap test = BitmapFactory.decodeFile("/storage/131B-1A02/IMG_20191011_135824.jpg");
        test = roundBitmap(test,test.getWidth()/2,test.getHeight()/2);
        showBitmap(test);
    }

    public void heart(View view) {
        Bitmap test = BitmapFactory.decodeFile("/storage/131B-1A02/IMG_20191011_135824.jpg");
        test = smileBitmap(test);
        showBitmap(test);
    }

    public void request(View view) {
        loadBitmapFromHttp("https://ws1.sinaimg.cn/large/0065oQSqly1g0ajj4h6ndj30sg11xdmj.jpg");
    }

    public void reset(View view) {
        loadBitmapFromFile();
    }

    private void initIvTest(){
        ivTest = findViewById(R.id.iv_test);
        checkPermission();
    }

    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            loadBitmapFromFile();
        }
    }

    private void loadBitmapFromHttp(final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL path = new URL(url);
                    connection = (HttpURLConnection)path.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(6000);
                    connection.setReadTimeout(6000);

                    InputStream in = connection.getInputStream();
                    final Bitmap gank = BitmapFactory.decodeStream(in);
                    Log.d("loadBitmap:",""+(gank==null));
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(gank!=null)
                            showBitmap(gank);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(connection!=null)
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void loadBitmapFromFile(){
        Bitmap test = BitmapFactory.decodeFile("/storage/131B-1A02/IMG_20191011_135824.jpg");
        //Rect rect = new Rect(0,0,test.getWidth(),test.getHeight());
        showBitmap(test);
    }

    private void showBitmap(Bitmap bitmap){
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)ivTest.getLayoutParams();
        params.width = bitmap.getWidth();
        params.height = bitmap.getHeight();
        ivTest.setImageBitmap(bitmap);
    }

    private Bitmap resizeBitmapFromFile(String path , int scaleFactor){
        final  BitmapFactory.Options options= new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        int width = options.outWidth;
        int height = options.outHeight;
        int requireWidth = width / scaleFactor;
        int requireHeight = height / scaleFactor;
        int inSampleSize = 1;

        if (height>requireHeight ||  width> requireWidth){
            final int halfHeight=height/2;
            final int halfWidth=width/2;
            //计算最大的采样率，采样率为2的指数
            while ((halfWidth/inSampleSize)>=requireWidth && (halfHeight/inSampleSize)>=requireHeight){
                inSampleSize = inSampleSize << 1;
            }
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }

    private Bitmap bitmapToGray(Bitmap bitmap){
        Bitmap result = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        int width = result.getWidth();
        int height = result.getHeight();

        // 保存所有的像素的数组，图片宽×高
        int[] pixels = new int[width * height];
        result.getPixels(pixels,0,width,0,0,width,height);
        for(int i =0;i < pixels.length;i++){
            int a = (pixels[i] & 0xff000000)>>24; //透明通道
            int red  = (pixels[i] & 0x00ff0000) >> 16; //红色通道
            int green = (pixels[i] & 0x0000ff00) >> 8; //绿色通道
            int blue = pixels[i] & 0x000000ff; //蓝色通道
            int average = (red + green + blue )/3; // 转换成为灰度图像需要平均三个通道
            pixels[i] = (a << 24) | (average << 16) | (average << 8) | average;
        }
        result.setPixels(pixels,0,width,0,0,width,height);
        return result;
    }

    private Bitmap roundBitmap(Bitmap bitmap,float rx,float ry){
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        final Paint paint = new Paint();
        Canvas canvas = new Canvas(result);
        paint.setAntiAlias(true);
        RectF rectF = new RectF(0,0,result.getWidth(),result.getHeight());
        canvas.drawRoundRect(rectF,rx,ry,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,0,0,paint);
        return result;
    }

    private Bitmap smileBitmap(Bitmap bitmap){
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        final Paint paint = new Paint();
        Canvas canvas = new Canvas(result);
        paint.setAntiAlias(true);
        RectF rectF = new RectF(0,0,bitmap.getWidth()/2,bitmap.getHeight()/2);
        canvas.drawArc(rectF,180,360,false,paint);
        RectF rectF1 = new RectF(bitmap.getWidth()/2,0,bitmap.getWidth(),bitmap.getHeight()/2);
        canvas.drawArc(rectF1,180,360,false,paint);
        //RectF rectF2 = new RectF(0,0,bitmap.getWidth(),bitmap.getHeight());
        RectF rectF2 = new RectF(0,-bitmap.getHeight()/4,bitmap.getWidth(),bitmap.getHeight()*3/4);
        canvas.drawArc(rectF2,0,180,false,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,0,0,paint);
        return result;
    }


    private void compressBitmap(Bitmap source,String path){
        int requireWidth = source.getWidth()/2;
        int requireHeight = 0;
        Bitmap result = Bitmap.createBitmap(source,0,0,requireWidth,requireHeight);
        File file = new File(path);
        try {
            FileOutputStream out = new FileOutputStream(path);
            result.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
