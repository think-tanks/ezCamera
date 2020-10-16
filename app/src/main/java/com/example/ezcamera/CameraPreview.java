package com.example.ezcamera;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.hardware.Camera;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private static int cameraID = CAMERA_FACING_BACK;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Uri outputMediaFileUri;
    private String outputMediaFileType;


    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ph");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir()) {
                Log.d(TAG, "failed to create dir");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
            outputMediaFileType = "image/*";
        }
        else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
            outputMediaFileType = "video/*";
        }
        else {
            return null;
        }

        Log.d(TAG, "" + mediaFile);
        outputMediaFileUri = Uri.fromFile(mediaFile);

        return mediaFile;
    }

    public Uri getOutputMediaFileUri() {
        return outputMediaFileUri;
    }

    public String getOutputMediaFileType() {
        return outputMediaFileType;
    }

    public void takePicture() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Log.d(TAG, "onPictureTaken ...");
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "err creat media files");
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(bytes);
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mCamera.startPreview();
            }

        });
    }


    public CameraPreview(Context context) {
        super(context);
        Log.d(TAG, "CameraPreview ...");

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public Camera getCameraInstance() {
        Log.d("CameraPreview", "getCameraInstance ...");

        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mCamera;
    }


    public int getDisplayOrientation() {
        Log.d(TAG, "getDisplayOrientation ...");

        Display display =  ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;

        switch(rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(CAMERA_FACING_BACK, camInfo);

        int result = 0;
        if (cameraID == CAMERA_FACING_FRONT) {
            result = (camInfo.orientation - degrees + 360 + 180) % 360;
        }
        else if (cameraID == CAMERA_FACING_BACK) {
            result = (camInfo.orientation - degrees + 360) % 360;
        }
        Log.d(TAG, "display " + degrees + ", cam orientation "  + camInfo.orientation + ", result " + result);

        return result;
    }

    private void adjustDisplayRatio(int rotation) {
        Log.d(TAG, "adjustDisplayRatio ...");
        ViewGroup parent = (ViewGroup) getParent();
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);

        int width = rect.width();
        int height = rect.height();

        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        int previewWidth;
        int previewHeight;

        if (rotation == 90 || rotation == 270) {
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        }
        else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }


//        Log.d(TAG, "width " + width + ", height " +  height + ", previewWidth " + previewWidth + ", previewHeight " + previewHeight);
//        Log.d(TAG,"width/height " + (float)width/height + ", previewWidth/previewHeight " + (float)previewWidth/previewHeight);
        if ((float)width/height > (float)previewWidth/previewHeight) {
//            final int scaledChildWidth = previewWidth * height / previewHeight;
//            layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
//            layout(0, 0, scaledChildWidth, height);

            final int scaledH = previewHeight * width / previewWidth;
            layout(0, 0, width, scaledH);

            Log.d(TAG, "width " + width + ", height " +  height + ", previewWidth " + previewWidth +
                    ", previewHeight " + previewHeight + ", scaledH " + scaledH);
        }
        else {
//            final int scaledChildHeight = previewHeight * width / previewWidth;
//            layout(0,(height - scaledChildHeight) / 2, width, (width + scaledChildHeight) / 2);
//            layout(0,0, width, scaledChildHeight);

            final int scaledW = previewWidth * height / previewHeight;
            layout(0, 0, scaledW, height);

            Log.d(TAG, "width " + width + ", height " +  height + ", previewWidth " + previewWidth +
                    ", previewHeight " + previewHeight + ", scaledW " + scaledW);
        }

    }



    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated ...");

        mCamera = getCameraInstance();

        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        int rotation = getDisplayOrientation();
//        adjustDisplayRatio(rotation);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "surfaceChanged ...");

//        try {
//            mCamera.stopPreview();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        int rotation = getDisplayOrientation();
        mCamera.setDisplayOrientation(rotation);    // 设置预览方向

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(rotation);           // 设置拍照方向
        mCamera.setParameters(parameters);

//        adjustDisplayRatio(rotation);

//        try {
//            mCamera.setPreviewDisplay(surfaceHolder);
//            mCamera.startPreview();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed ...");

        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}
