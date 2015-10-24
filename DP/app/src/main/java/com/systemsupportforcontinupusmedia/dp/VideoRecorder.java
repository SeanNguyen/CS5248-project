package com.systemsupportforcontinupusmedia.dp;
import java.io.File;
import java.io.IOException;

import com.systemsupportforcontinupusmedia.dp.R;


import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Chronometer;
import android.widget.Toast;
public class VideoRecorder extends Activity {
	private SurfaceView surfaceview=null;
	private SurfaceHolder surfaceholder=null;
	private Camera camera=null;
	private boolean inPreview=false;
	private boolean cameraConfigured=false;
	boolean startedRecording=false;
	boolean stoppedRecording=false;
	public MediaRecorder mediaRecorder;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_recorder);
        surfaceview=(SurfaceView)findViewById(R.id.preview);
        surfaceholder=surfaceview.getHolder();
        surfaceholder.addCallback(surfaceCallback);
    }
    /*starts the timer for the recorder*/
    public void startTimer() {
    	runOnUiThread(new Runnable() {
			public void run() {
				try {
					Chronometer txtCurrentTime = (Chronometer) findViewById(R.id.timer);
					txtCurrentTime.setBase(SystemClock.elapsedRealtime());
					txtCurrentTime.setText("00:00");
					txtCurrentTime.start();
				} catch (Exception e) {
					
				}
			}

		});
	}
   @Override
	public boolean onPrepareOptionsMenu(Menu menu) 
	{
		super.onPrepareOptionsMenu(menu);
		menu.clear(); 
		menu.add(0, 0, 0, "Start Recording"); 
		menu.add(1, 1, 0, "Stop Recording");
		menu.setGroupVisible(0, false);
		menu.setGroupVisible(1, false);
		if(startedRecording==false)
			menu.setGroupVisible(0, true);
		else if(startedRecording==true&&stoppedRecording==false)
			menu.setGroupVisible(1, true);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
		case 0:
			//start the recorder
			startedRecording=true;
			startRecording();
			break;
		case 1: 
			stoppedRecording=true;
			stopRecording();
			Intent intent = new Intent(VideoRecorder.this, SegmenterUploader.class);
     	    startActivity(intent);
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
    @SuppressWarnings("unused")
	private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    // A safe way to get an instance of the Camera object
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
        }
        return c; // returns null if camera is unavailable
    }
    @Override
    public void onResume() {
      super.onResume();
      
      camera=Camera.open();
      startPreview();
    }
    @Override
    public void onPause() {
      if (inPreview) {
        camera.stopPreview();
      }
      camera.release();
      camera=null;
      inPreview=false;
      super.onPause();
    }
    private Camera.Size getBestPreviewSize(int width, int height,Camera.Parameters parameters) 
    {
      Camera.Size result=null;
       for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
        if (size.width<=width && size.height<=height) {
          if (result==null) {
            result=size;
          }
          else {
            int resultArea=result.width*result.height;
            int newArea=size.width*size.height;
            
            if (newArea>resultArea) {
              result=size;
            }
          }
        }
      }
      return(result);
    }
    private void initPreview(int width, int height) {
      if (camera!=null && surfaceholder.getSurface()!=null) {
        try {
          camera.setPreviewDisplay(surfaceholder);
        }
        catch (Throwable t) {
          Log.e("PreviewDemo-surfaceCallback",
                "Exception in setPreviewDisplay()", t);
          Toast
            .makeText(VideoRecorder.this, t.getMessage(), Toast.LENGTH_LONG)
            .show();
        }
        if (!cameraConfigured) {
          Camera.Parameters parameters=camera.getParameters();
          Camera.Size size=getBestPreviewSize(width, height,parameters);
          if (size!=null) {
            parameters.setPreviewSize(size.width, size.height);
            camera.setParameters(parameters);
            cameraConfigured=true;
          }
        }
      }
    }
    private void startPreview() {
      if (cameraConfigured && camera!=null) {
        camera.startPreview();
        inPreview=true;
      }
    }
    SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
      public void surfaceCreated(SurfaceHolder holder) {
      }
      public void surfaceChanged(SurfaceHolder holder,
                                 int format, int width,
                                 int height) {
        initPreview(width, height);
        startPreview();
      }
      public void surfaceDestroyed(SurfaceHolder holder) {
        // no-op
      }
    };
    @SuppressLint({ "SdCardPath", "SdCardPath" })
	public boolean startRecording() {
		String TAG = null;
		try {
			camera.unlock();
			mediaRecorder = new MediaRecorder();
			mediaRecorder.setCamera(camera);
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mediaRecorder.setAudioEncodingBitRate(160000);
			mediaRecorder.setAudioChannels(2);
			mediaRecorder.setAudioSamplingRate(48000);
			mediaRecorder.setVideoSize(720, 480);
			mediaRecorder.setVideoEncodingBitRate(3145728);
			File dir = new File("/sdcard/Client/Input/file.mp4").getParentFile();
			if (!dir.exists() && !dir.mkdirs()) {
				throw new IOException("Directory doesn't exists");
			} else {
			}
			mediaRecorder.setOutputFile("/sdcard/Client/Input/file.mp4");
			try {
			mediaRecorder.setVideoFrameRate(24);
			} catch (Exception e) {
				finish();
			}
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			mediaRecorder.setPreviewDisplay(surfaceholder.getSurface());
			mediaRecorder.setMaxFileSize(900000000);
			mediaRecorder.prepare();
			startTimer();
			mediaRecorder.start();
			return true;
		} catch (IllegalStateException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
    public void stopRecording() {
		try {
			mediaRecorder.stop();
			mediaRecorder.release();
			mediaRecorder = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		camera.lock();
	}
}
    

