package com.wangxingxing.mymtcnndemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SELECT_IMAGE = 1;

    private TextView infoResult;
    private ImageView imageView;
    private Bitmap mSelectedImage = null;

    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private int[] mSamplePics = {
            R.drawable.pic_sample_1,
            R.drawable.pic_sample_2,
            R.drawable.pic_sample_3,
            R.drawable.pic_sample_4,
            R.drawable.pic_sample_5,
            R.drawable.pic_sample_6,
            R.drawable.pic_sample_7,
    };
    private int mCurrentIndex = 0;

    private int minFaceSize = 5;
    private int testTimeCount = 1;
    private int threadsNumber = 2;
    private boolean maxFaceSettings = false;

    private List<Uri> mSelectedUriList;
    private long firstPressedTime = 0;

    private MTCNN mtcnn = new MTCNN();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            initMTCNN();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LogUtils.i("NCNN初始化完成");
        initView();

        checkPermission();
    }

    private void initView() {
        infoResult = findViewById(R.id.infoResult);
        imageView = findViewById(R.id.imageView);
    }

    private void checkPermission() {
        if (!PermissionUtils.isGranted(permissions)) {
            PermissionUtils.permission(permissions).request();
        }
    }

    private void initMTCNN() throws IOException {
        byte[] det1_param = null;
        byte[] det1_bin = null;
        byte[] det2_param = null;
        byte[] det2_bin = null;
        byte[] det3_param = null;
        byte[] det3_bin = null;

        //det1
        {
            //用io流读取二进制文件，最后存入到byte[]数组中
            InputStream assetsInputStream = getAssets().open("det1.param.bin");// param：  网络结构文件
            int available = assetsInputStream.available();
            det1_param = new byte[available];
            int byteCode = assetsInputStream.read(det1_param);
            assetsInputStream.close();
        }
        {
            //用io流读取二进制文件，最后存入到byte上，转换为int型
            InputStream assetsInputStream = getAssets().open("det1.bin");//bin：   model文件
            int available = assetsInputStream.available();
            det1_bin = new byte[available];
            int byteCode = assetsInputStream.read(det1_bin);
            assetsInputStream.close();
        }

        //det2
        {
            //用io流读取二进制文件，最后存入到byte[]数组中
            InputStream assetsInputStream = getAssets().open("det2.param.bin");// param：  网络结构文件
            int available = assetsInputStream.available();
            det2_param = new byte[available];
            int byteCode = assetsInputStream.read(det2_param);
            assetsInputStream.close();
        }
        {
            //用io流读取二进制文件，最后存入到byte上，转换为int型
            InputStream assetsInputStream = getAssets().open("det2.bin");//bin：   model文件
            int available = assetsInputStream.available();
            det2_bin = new byte[available];
            int byteCode = assetsInputStream.read(det2_bin);
            assetsInputStream.close();
        }

        //det3
        {
            //用io流读取二进制文件，最后存入到byte[]数组中
            InputStream assetsInputStream = getAssets().open("det3.param.bin");// param：  网络结构文件
            int available = assetsInputStream.available();
            det3_param = new byte[available];
            int byteCode = assetsInputStream.read(det3_param);
            assetsInputStream.close();
        }
        {
            //用io流读取二进制文件，最后存入到byte上，转换为int型
            InputStream assetsInputStream = getAssets().open("det3.bin");//bin：   model文件
            int available = assetsInputStream.available();
            det3_bin = new byte[available];
            int byteCode = assetsInputStream.read(det3_bin);
            assetsInputStream.close();
        }

        mtcnn.FaceDetectionModelInit(det1_param,det1_bin,det2_param,det2_bin,det3_param,det3_bin);
    }

    public void selectSampleImg(View view) {
        mSelectedImage = BitmapFactory.decodeResource(getResources(), mSamplePics[mCurrentIndex]);
        imageView.setImageBitmap(mSelectedImage);
        mCurrentIndex++;
        if (mCurrentIndex >= mSamplePics.length) {
            mCurrentIndex = 0;
        }
    }

    public void faceDetect(View view) {
        if (mSelectedImage == null) {
            ToastUtils.showShort("请选择图片");
            return;
        }

        mtcnn.SetMinFaceSize(minFaceSize);
        mtcnn.SetTimeCount(testTimeCount);
        mtcnn.SetThreadsNumber(threadsNumber);

        //检测流程
        int width = mSelectedImage.getWidth();
        int height = mSelectedImage.getHeight();
        byte[] imageDate = getPixelsRGBA(mSelectedImage);

        long timeDetectFace = System.currentTimeMillis();
        int faceInfo[] = null;
        if (!maxFaceSettings) {
            faceInfo = mtcnn.FaceDetect(imageDate, width, height, 4);
            LogUtils.i("faceDetect: 检测所有人脸");
        } else {
            faceInfo = mtcnn.MaxFaceDetect(imageDate, width, height, 4);
            LogUtils.i( "faceDetect: 检测最大人脸");
        }
        timeDetectFace = System.currentTimeMillis() - timeDetectFace;
        LogUtils.i("faceDetect: 人脸平均检测时间：" + timeDetectFace / testTimeCount);

        if (faceInfo.length > 1) {
            int faceNum = faceInfo[0];
            infoResult.setText("图宽："+width+"高："+height+"人脸平均检测时间："+timeDetectFace/testTimeCount+" 数目：" + faceNum);
            LogUtils.i("图宽："+width+"高："+height+" 人脸数目：" + faceNum );

            Bitmap drawBitmap = mSelectedImage.copy(Bitmap.Config.ARGB_8888, true);
            for (int i = 0; i < faceNum; i++) {
                int left, top, right, bottom;
                Canvas canvas = new Canvas(drawBitmap);
                Paint paint = new Paint();
                left = faceInfo[1 + 14 * i];
                top = faceInfo[2 + 14 * i];
                right = faceInfo[3 + 14 * i];
                bottom = faceInfo[4 + 14 * i];
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE); //不填充
                paint.setStrokeWidth(2); //线的宽度
                canvas.drawRect(left, top, right, bottom, paint);
                //画特征点
                canvas.drawPoints(new float[]{faceInfo[5+14*i],faceInfo[10+14*i],
                        faceInfo[6+14*i],faceInfo[11+14*i],
                        faceInfo[7+14*i],faceInfo[12+14*i],
                        faceInfo[8+14*i],faceInfo[13+14*i],
                        faceInfo[9+14*i],faceInfo[14+14*i]}, paint);//画多个点
            }
            imageView.setImageBitmap(drawBitmap);
        } else {
            infoResult.setText("未检测到人脸");
        }
    }

    //提取像素点
    private byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }

    public void selectImg(View view) {
        checkPermission();

        Matisse.from(MainActivity.this)
                .choose(MimeType.ofAll())
                .countable(true)
                .maxSelectable(9)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(RC_SELECT_IMAGE);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstPressedTime < 2000) {
            super.onBackPressed();
        } else {
            ToastUtils.showShort("再按一次返回键退出");
            firstPressedTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SELECT_IMAGE && resultCode == RESULT_OK) {
            mSelectedUriList = Matisse.obtainResult(data);
            LogUtils.i("mSelected: " + mSelectedUriList);

            String path = UriUtils.uri2File(mSelectedUriList.get(0)).getAbsolutePath();
            mSelectedImage = BitmapFactory.decodeFile(path);
            imageView.setImageBitmap(mSelectedImage);
        }
    }
}
