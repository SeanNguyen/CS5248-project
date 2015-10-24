package com.systemsupportforcontinupusmedia.dp;

import android.app.Activity;

public class SegmenterUploader extends Activity {
}

/*import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox;
import edu.nus.MyDashServiceT12.R;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

@SuppressLint({ "SdCardPath", "SdCardPath" })
public class SegmenterUploader extends Activity {
	private Button splitbtn;//button to start player
	private Button uploadbtn;//button to record
	private Button showuploaded;
	private Button exit;
	ProgressDialog progressbar; 
	static File file =null;
	static float audio_timescale;
	static float audio_tts;
	static long audio_count;
	static boolean scd;
	String directory = "/sdcard/Client/Result/";
	//static Track audiosample;
	int flag=0;
	int number_of_streamlets;
	Handler myHandler;
	Handler myHandler2;
	Handler myHandler3;
	private ProgressDialog progDailog2;
	AlertDialog.Builder builder;
	private String[] values;
	private ArrayAdapter<CharSequence> AdapterStreamlets;
	int currentStreamlet=0;
    
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploader);
        
        Calendar rightNow = Calendar.getInstance();
    	int hours = rightNow.get(Calendar.HOUR_OF_DAY);
    	int minutes = rightNow.get(Calendar.MINUTE);
    	int seconds = rightNow.get(Calendar.SECOND);
    	SimpleDateFormat formatter= new SimpleDateFormat("MMM-dd");
    	String date1=formatter.format(rightNow.getTime());
    	String date=date1+"/";
    	String curTime = hours+"h"+"_"+minutes+"m"+"_"+seconds+"s";
    	directory+= date;
        
    	scd = (new File(directory+curTime)).mkdirs();
    	directory="/sdcard/Client/Result/"+date+curTime;
    	
        this.exit = (Button) this.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View view){
        		SegmenterUploader.this.finish();
        	}});
    	
        this.splitbtn = (Button) this.findViewById(R.id.splitter);
        splitbtn.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View view){ 
        		final ProgressDialog progressbar=ProgressDialog.show(SegmenterUploader.this,"May the force be with you","Streamlets on the fly...",true);
        		progressbar.setCancelable(true);
        		final Handler threadHandler = new Handler();
        		new Thread(new Runnable(){
        			public void run(){
        				try{
        					long sta1=System.currentTimeMillis();
        					streamletsplitting();
        					long sta2=System.currentTimeMillis();
        					progressbar.dismiss();
        					progressbar.cancel();
        					}catch(Exception e){}
        				threadHandler.post(new Runnable() {
        					public void run() {
        						alertbox("Completed...","File is split");
        					}
        				});
        			}
        		}).start();
    }});
        
        this.showuploaded = (Button) this.findViewById(R.id.showupload);
        showuploaded.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View view){ 
        		values = new String[1000];
        		for (int ct = 0; ct < 1000; ct++) {
        			values[ct] = new String();}
        		new Thread(new Runnable() {
        			public void run() {
        				showStreamlets();
        				runOnUiThread(new Runnable() {
        						public void run() {
        						inflateList();
        						}
        				});
        			}
        		}).start();
    }});
        
        
         this.uploadbtn = (Button) this.findViewById(R.id.uploader);
        uploadbtn.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View view){
             		progDailog2 = ProgressDialog.show(SegmenterUploader.this, "Uploader ",
        				"Uploading....May the force be with you", true, false);
        		myHandler2=new Handler()
        		{
        			@Override
        			public void handleMessage(Message msg) {
        				switch (msg.what) {
        				case 0:
        					progDailog2.setMessage("" + (String) msg.obj);
        					break;
        				case 1:
        					progDailog2.cancel();
        					finish();
        					break;
        				case 2:
        					progDailog2.cancel();
        					break;
        				}
        				super.handleMessage(msg);
        			}
        		};
        		final Handler threadHandler2 = new Handler();
        		new Thread() {
        			public void run() {
        				Looper.prepare();
        				try {
        					long sta3=System.currentTimeMillis();
        										
        					uploadfiles();
        					long sta4=System.currentTimeMillis();
        					if(currentStreamlet==number_of_streamlets)
        					{
        						long sta5=System.currentTimeMillis();
								
        						calltranscodefunction();
        						long sta6=System.currentTimeMillis();
            					}
        					progDailog2.dismiss();
        					} catch (Exception e) {
        				SegmenterUploader.this.finish();
        				}
        				threadHandler2.post(new Runnable() {
        					public void run() {
        						checkforresumeupload();
        					}
        				});
        			}
        		}.start();
        		
         	}
        });
        }
    public void onDestroy(){
    	super.onDestroy();
    	if(progDailog2!=null)
    	if(progDailog2.isShowing()){
    		progDailog2.cancel();
    	}

    	}
   /*After network uncertainities, while uploading the streamlets,  checks if any streamlets is or are left out*/
   /* private void checkforresumeupload() {
		builder = new AlertDialog.Builder(this);
		if (0 == flag) {
			builder.setMessage("Completed Successfully   Press Exit ")
					.setCancelable(false)
					.setPositiveButton("Exit",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									//UploaderActivity.this.finish();
									SegmenterUploader.this.recreate();
								}
							})
					.setNegativeButton("Back",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent intent = new Intent(
											SegmenterUploader.this,
											MainActivity.class);
									startActivity(intent);
									finish();
								}
							});
		} else if (flag==1) {
			
			File file = new File(String.format(directory+"/uploaderror.txt"));
			FileReader fstream = null;
			try {
				fstream = new FileReader(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			BufferedReader in = new BufferedReader(fstream);
			String s = "";
			String k = "";
			try {
				while ((s = in.readLine()) != null) {
					k += s + "\n";
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			builder.setMessage("Not Completed \n" + k)
					.setCancelable(false)
					.setPositiveButton("Upload",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									progDialog();

									new Thread() {

										public void run() {

											Looper.prepare();
											File file = new File(String
													.format(directory+"/uploaderror.txt"));
											FileReader fstream = null;
											try {
												fstream = new FileReader(file);
											} catch (FileNotFoundException e1) {
												e1.printStackTrace();
											}
											String s = "";
											BufferedReader in = new BufferedReader(
													fstream);

											try {

												while ((s = in.readLine()) != null) {

													resumeupload(s);
													if(currentStreamlet==number_of_streamlets)/*check to verify whether streamlets uploaded is fully done before invoking transcode*/
													/*{calltranscodefunction();}
													//write transcode script here
													}
												Message msg = new Message();
												msg.what = 1;
												msg.obj = "Uploading Error "
														+ file;

												myHandler3.sendMessage(msg);
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}.start();
								}

							})
					.setNegativeButton("Exit",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									SegmenterUploader.this.finish();
								}
							});

		}
		builder.show();
    }
    /*Shows streamlets that were last uploaded*/
/*
public void showStreamlets() {
		
		HttpClient httpclient3 = new DefaultHttpClient();
		System.out.println("httpclient1" + httpclient3);
		try {
			HttpPost httppost3 = new HttpPost("http://137.132.82.164/~a0118982/videos/display_uploaded_videos.php");
			HttpResponse response3 = null;
			response3 = httpclient3.execute(httppost3);
			HttpEntity resEntity3 = response3.getEntity();
			if (resEntity3 != null) {
				}
			String hash = EntityUtils.toString(resEntity3);
			String x = hash;
			String extracted;
			String[] s = x.split("\n");
			int first, last;
			int j = 0;
			for (int i = 0; i < s.length; i++) {
					extracted = s[i];
					values[j] = extracted;
					j++;
					System.out.println("values "+values[i]);
				}

			if (resEntity3 != null) {
				if (resEntity3.isStreaming()) {
					InputStream instream = resEntity3.getContent();
					if (instream != null) {
						instream.close();
					}
				}
			}

		} catch (Exception e) {
			
				System.out.println("PlayVideoActivity :Exception "
						+ e.getMessage());
			
		} finally {
			try {
				httpclient3.getConnectionManager().shutdown();
			} catch (Exception ignore) {
			}
		}
	}
    /*inflates the list with last uploaded streamlets*/

/*public void inflateList()
{
	ListView listView = (ListView) findViewById(R.id.listView1);
	AdapterStreamlets = new ArrayAdapter<CharSequence>(this,android.R.layout.simple_list_item_1);
	listView.setAdapter(AdapterStreamlets);
	for (int i = 0; i < values.length; i++) {
		AdapterStreamlets.add(values[i]);
	}
}
    
    public void serverFolderCreator() {
		HttpClient httpclient1 = new DefaultHttpClient();
		try {
			HttpPost httppost1 = new HttpPost("http://137.132.82.164/~a0118982/videos/folderexample.php");
			HttpResponse response1 = null;
			response1 = httpclient1.execute(httppost1);
			HttpEntity resEntity1 = response1.getEntity();
			if (resEntity1 != null) 
			{
				System.out.println("MP4SplitterActivity- Response content length: "+ resEntity1.getContentLength());
			}
			if (resEntity1 != null) 
			{
				System.out.println(EntityUtils.toString(resEntity1));
			}
			if (resEntity1 != null) 
			{
				if (resEntity1.isStreaming()) {
					InputStream instream = resEntity1.getContent();
					if (instream != null) {
						instream.close();
					}
				}
			}

		} 
		catch (Exception e) {
			System.out.println("MP4SplitterActivity :error"+ e.getMessage());
		} finally {
			try {
				httpclient1.getConnectionManager().shutdown();
			} catch (Exception ignore) {
			}
		}
    }
    /*calls server script to begin transcoding */
  /*  public void calltranscodefunction(){
		HttpClient httpclient1 = new DefaultHttpClient();
		try {
			HttpPost httppost1 = new HttpPost("http://137.132.82.164/~a0118982/videos/transcode.php");
			HttpResponse response1 = null;
			response1 = httpclient1.execute(httppost1);
			HttpEntity resEntity1 = response1.getEntity();
			if (resEntity1 != null) 
			{
				System.out.println("Transcode-PHP.- Response content length: "+ resEntity1.getContentLength());
			}
			if (resEntity1 != null) 
			{
				System.out.println(EntityUtils.toString(resEntity1));
			}
			if (resEntity1 != null) 
			{
				if (resEntity1.isStreaming()) {
					InputStream instream = resEntity1.getContent();
					if (instream != null) {
						instream.close();
					}
				}
			}

		} 
		catch (Exception e) {
			System.out.println("Transcode-PHP.:error"+ e.getMessage());
		} finally {
			try {
				httpclient1.getConnectionManager().shutdown();
			} catch (Exception ignore) {
			}
		}
	}
    
    
    private void progDialog() {
		progDailog2 = ProgressDialog.show(SegmenterUploader.this, "MP4 Uploader ",
				"Uploading....Still may the force be with you", true, false);
		myHandler3 = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					progDailog2.setMessage("" + (String) msg.obj);
					break;
				case 1:
					progDailog2.dismiss();
					break;
				case 2:
					progDailog2.cancel();
					break;
				}
				super.handleMessage(msg);
			}

		};

	}
    
    
    
  protected void alertbox(String title, String mymessage)
    {
    new AlertDialog.Builder(this)
       .setMessage(mymessage)
       .setTitle(title)
       .setCancelable(true)
       .setNeutralButton(android.R.string.ok,
          new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton){}
          })
       .show();
    }
  /*called after network uncertainities to upload the left out streamlets*/
  /*public void resumeupload(String FileName) {
		
		flag = 0;
		System.out.println("UploaderActivity" + file);
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		File file = new File(FileName);
		Message msg = new Message();
		msg.what = 0;
		msg.obj = "Uploading File " + file;
		myHandler2.sendMessage(msg);
		try {
			httppost = new HttpPost("http://pilatus.d1.comp.nus.edu.sg/~team10/video_repo/car-20120827-89_dash.mpd");
			FileBody bin = new FileBody(file);
			StringBody comment = null;
			comment = new StringBody("A binary file of some kind");
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("userfile", bin);
			reqEntity.addPart("comment", comment);
			httppost.setEntity(reqEntity);
			HttpResponse response = null;
			response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				System.out.println("ResumeUpload-getContentLength"+ resEntity.getContentLength());
			}
			if (resEntity != null) {
				System.out.println(EntityUtils.toString(resEntity));
			}
			if (resEntity != null) {
				if (resEntity.isStreaming()) {
					InputStream instream = resEntity.getContent();
					if (instream != null) {
						instream.close();
					}
				}
			}
		} catch (Exception e) {
			flag = 1;
			msg = new Message();
			msg.what = 0;
			msg.obj = "Error in Uploading -" + FileName;
			myHandler2.sendMessage(msg);
			System.out.println("resumeupload-Error" + e);
			System.out.println("resumeupload-Error" + e.getMessage());
			httppost.abort();
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception ignore) {
			}

		} finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception ignore) {
			}
		}
		currentStreamlet++;
  }
  
  /*Uploads video streamlets
   * 1. Calls server script to upload a streamlet 
   * 2. Uses Httppost method to upload the streamlets
   * 3. Writes the left out files in case of network failure, which is later picked up by other function to resume the upload
   * */
  /*private void uploadfiles()
  {
	  int i=0;
	  String s=null;
	  System.out.println("Number of files"+number_of_streamlets);
	  Message msg = new Message();
	  for(i = 0;i<number_of_streamlets;i++)
	  {		  
		  HttpClient httpclient = new DefaultHttpClient();
		  HttpPost httppost=null;
	  try
	  {		  //Message msg = new Message();
			httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		    httppost = new HttpPost("http://137.132.82.164/~a0118982/videos/testup.php");
		    File file = new File(directory+"/Streamlet"+i+".mp4");
		    s=directory+"/Streamlet"+i+".mp4";
		    MultipartEntity mpEntity = new MultipartEntity();
		    ContentBody cbFile = new FileBody(file, "image/jpeg");
		    mpEntity.addPart("userfile", cbFile);
		    httppost.setEntity(mpEntity);
		    System.out.println("upload files" + httppost.getRequestLine());
		    HttpResponse response = httpclient.execute(httppost);
		    HttpEntity resEntity = response.getEntity();
		    System.out.println(response.getStatusLine());
		    if (resEntity != null) {
		      currentStreamlet++;
		  	  System.out.println("CurrentStreamlet insideuplaod files "+currentStreamlet);
		      System.out.println(EntityUtils.toString(resEntity));
		    }
		    if (resEntity != null) {
		      resEntity.consumeContent();
		    }
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
		  msg = new Message();
			msg.what = 0;
			msg.obj = "Error in Uploading" + s;
			myHandler2.sendMessage(msg);
			file = new File(String.format(directory+"/uploaderror.txt"));
			try {
				FileWriter fstream = new FileWriter(file, true);
				BufferedWriter out = new BufferedWriter(fstream);
				flag = 1;
				out.write(s);
				out.write("\n");
				out.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			httppost.abort();
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception ignore) {
			}
	  }
	  finally {
			httppost.abort();
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception ignore) {
			}
		}
	  }
	}
  /* Splits the recorded video
   * 1. Find sync samples for the tracks 
   * 2. Use the streamlet splitting algorithm to identify the sync samples to be cut
   * 3. Fixed minimum streamlet length to be 2.75 seconds
   * 4. Calculate the audio sync sample from the video time
   * 5. Enter the splitting phase where all the streamlets are divided based upon calculated values
   * 6. Call a script in the server to create a folder for uploading process
   * */
   /* @SuppressLint("SdCardPath")
	@SuppressWarnings("resource")
	private  void streamletsplitting() throws FileNotFoundException, IOException
{
    int i=0;
	TreeMap<Double,Long> sorted_video_times=new TreeMap<Double,Long>();
	List<Double> video_times= new ArrayList<Double>();
    Map<Double,Long> video_times_sync_samples =new HashMap<Double,Long>();
	TreeMap<Double,Double> video_times_to_be_cut=new TreeMap<Double,Double>();
    Movie mainmovie=MovieCreator.build(new FileInputStream("/sdcard/Client/Input/file.mp4").getChannel());
    List<Track> tracks = mainmovie.getTracks();
    for (i = 0; i < 1; i++) {
       for (Track t : tracks) {
            String type = t.getMediaHeaderBox().getType();
                if (type.equals("vmhd")) {
                long[] syncSamples = t.getSyncSamples();
                for(int k=0;k<t.getSyncSamples().length;k++)
                {
                	double video_time=0;
                	long video_sample=1;
                	long temp1=syncSamples[k];
                	for(i=0;i<t.getDecodingTimeEntries().size();i++)
                	{
                		TimeToSampleBox.Entry entry = t.getDecodingTimeEntries().get(i);
                		for(int j=0;j<entry.getCount();j++)
                		{
                			if(video_sample!=temp1)
                			{
                				video_time+=(double)entry.getDelta()/(double)t.getTrackMetaData().getTimescale();
                				video_sample++;
                			}
                			else if(video_sample==temp1)
                			{
                				break;
                			}
                		}
                	}
                	video_times.add((double) video_time);
                	video_times_sync_samples.put(video_time,(long)syncSamples[k]);
                	sorted_video_times.put(video_time,(long)syncSamples[k]);
                }
            }
            if (type.equals("smhd")) {
               	audiosample=t;
                audio_timescale = t.getTrackMetaData().getTimescale();
                audio_tts = t.getDecodingTimeEntries().get(0).getDelta();
                audio_count = t.getDecodingTimeEntries().get(0).getCount();
            }

        }

    }
    mainmovie.setTracks(new LinkedList<Track>());
    List<Double> synsam =new ArrayList<Double>();
    double previous = 0.0;
    double temp0=0;
    for (Double sync : video_times) {
        Double delta = sync - previous;
       //System.out.println("Common sync point: " + (double)sync + " delta: " + (double)delta );
        if((delta>=3.000)&&(temp0==0)){synsam.add(sync);}
        else if((delta>=3.000)&&(temp0!=0)){synsam.add(sync);temp0=0;}
        else if((delta<3.000)&&(temp0<3.000))
        {
        	temp0+=delta;
        	if(temp0>=2.750){synsam.add(sync);temp0=0;}
        }
        previous = sync;
        video_times_to_be_cut.put(sync, delta);
    }
    List<Integer> list= new ArrayList<Integer>();
    list.add(0);
    for(i=0;i<synsam.size();i++){
    for(Entry<Double, Long> m:sorted_video_times.entrySet()){
          if(m.getKey().equals(synsam.get(i))==true)
        {
           	list.add((int) (m.getValue()-1));}
    }
    }
    List<Integer> audio_times_to_be_cut= new ArrayList<Integer>();
    audio_times_to_be_cut.add(0);
    for(int k=0;k<synsam.size();k++){
    	double audio_time=0;
		int audio_sample=0;
		double temp=synsam.get(k);
    for (i = 0; i < audiosample.getDecodingTimeEntries().size(); i++) 
	{
		TimeToSampleBox.Entry entry = audiosample.getDecodingTimeEntries().get(i);
		for (int j = 0; j < entry.getCount(); j++) {
			if(audio_time<=temp){audio_time+=(double)entry.getDelta()/(double)audiosample.getTrackMetaData().getTimescale();audio_sample++;}
        	else if(audio_time>temp){
        		break;
        	}
		}
	}
    audio_times_to_be_cut.add(audio_sample-1);
    }
    mainmovie.setTracks(new LinkedList<Track>());
    for (i = 0;i<list.size()-1;i++){
    	mainmovie.setTracks(new LinkedList<Track>());
    try{
    	mainmovie.addTrack(new CroppedTrack(tracks.get(0),list.get(i), list.get(i+1)));
    	mainmovie.addTrack(new CroppedTrack(tracks.get(1),audio_times_to_be_cut.get(i), audio_times_to_be_cut.get(i+1)));
    }catch(Exception e){e.printStackTrace();}
    	IsoFile out = new DefaultMp4Builder().build(mainmovie);
    	String finm=directory+"/Streamlet"+i+".mp4";
    	file = new File(finm);
		FileOutputStream fos = new FileOutputStream(file);
		out.getBox(fos.getChannel());
		fos.close();
    }
//    System.out.println("<----------------List of Video SyncSamples to be cut---count: "+list.size()+"------->"+list);
//    System.out.println("<----------------List of Audio SyncSamples to be cut---count: "+audio_times_to_be_cut.size()+"------->"+audio_times_to_be_cut);
//    System.out.println("<----------------List of times to be cut---count: "+synsam.size()+"------->"+synsam);
//    System.out.println("Number of files"+i);
    number_of_streamlets=i;
    temp0=0;
    double temp1=0;
    serverFolderCreator();
    temp0=temp1;
  } 
	
}*/
   
    

