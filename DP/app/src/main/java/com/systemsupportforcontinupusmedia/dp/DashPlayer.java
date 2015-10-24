package com.systemsupportforcontinupusmedia.dp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DashPlayer extends Activity{
    int numberofstreamlets;
    int countstreamlets;
    int videoplaystreamlets;
    VideoView videoView;
    private String[] values;
    private ArrayAdapter<CharSequence> AdapterPlaylists;
    public String dropDownText;
    private Spinner spinnerController;
    private Button play,gauge;
    TextView tv;
    String parsedstreamletscp[][];
    float contentlength2,avgContentlength2;
    float bandwidth,avgBandwidth;
    long difference,avgDifference;
    int qualityValue[][];
    int threshold1;
    int threshold2;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        threshold1=1000;threshold2=5500;

        videoView = (VideoView)this.findViewById(R.id.videoView);	/*video output*/
        tv = (TextView) findViewById(R.id.editText1);	/*textview to print the currently played streamlet*/
        values = new String[1000];
        for (int ct = 0; ct < 1000; ct++) {
            values[ct] = new String();}
        selectPlaylist();	/*invokes a thread to retrieve playlist values from server and update accordingly the spinner controller drop down list*/

        this.play = (Button) this.findViewById(R.id.button2);
        this.play.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
                startDownload2(); /*This thread asynchronously downloads the streamlets*/
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        if(videoplaystreamlets<=numberofstreamlets)
                        {
                            videoView.start();
                        }
                    }
                });
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @SuppressLint("SdCardPath")
                    public void onCompletion(MediaPlayer mp) {
                        //mp.reset();

                        if(videoplaystreamlets!=numberofstreamlets)
                        {	//displaytext();
                            videoView.setVideoURI(Uri.parse("storage/emulated/0/Pictures/Streamlet" + videoplaystreamlets + ".mp4"));
                            videoView.start();
                            videoplaystreamlets++;}
                    }
                });
                Handler myHandler = new Handler();
                myHandler.postDelayed(mMyRunnable, 10000); /* A Start-up Delay for Video Playback is set here*/
            }
        });
        this.gauge = (Button) this.findViewById(R.id.button1);
        this.gauge.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int y=0;
                while(y<3){
                    for(int x=0;x<numberofstreamlets;x++)
                    {
                        startDownload(parsedstreamletscp[y][x]);

                    }
                    y++;}
            }
        });
    }
    private Runnable mMyRunnable = new Runnable()
    {
        @SuppressLint("SdCardPath")
        public void run()
        {
            videoplaystreamlets=0;
            String sdcardPath = "storage/emulated/0/Pictures/Streamlet"+videoplaystreamlets+".mp4";
                videoView.setVideoURI(Uri.parse("storage/emulated/0/Pictures/Streamlet" + videoplaystreamlets + ".mp4"));
                videoView.start();
                //  displaytext();
                videoplaystreamlets++;

            }

    };
    /*called when the gauge button is pressed, downloads the streamlets belonging to the playlist*/
    private void startDownload(String urli) {
        new DownloadFileAsync().execute(urli);
    }
    /*called when the play button is pressed, downloads the streamlets of the playlist*/
    private void startDownload2() {
        new DownloadFileAsync2().execute();
    }
    /*prints the video quality of the current streamlet being played*/
    public void displaytext()
    {
        if(qualityValue[videoplaystreamlets][0]==0){tv.setText("LOW_Q Streamlet_"+videoplaystreamlets+" Currrent Bandwidth:"+qualityValue[videoplaystreamlets][1]);}
        else if(qualityValue[videoplaystreamlets][0]==1){tv.setText("MID_Q Streamlet_"+videoplaystreamlets+" Current Bandwidth:"+qualityValue[videoplaystreamlets][1]);}
        else if(qualityValue[videoplaystreamlets][0]==2){tv.setText("High_Q Streamlet_"+videoplaystreamlets+" Current Bandwidth:"+qualityValue[videoplaystreamlets][1]);}
    }
    /* Class file
     * parameter - URL of the playlist
     * will downloads the streamlets belonging to the particular playlist*/
    class DownloadFileAsync extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL(aurl[0]);
                System.out.println("DownloadFileAsync URL"+aurl[0]);
                long startTime=System.currentTimeMillis();
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lengthOfFile = conexion.getContentLength();
                Log.d("Player_ASYNC", "Length of file: " + lengthOfFile);
                InputStream input = new BufferedInputStream(url.openStream());
                long endTime=System.currentTimeMillis();
                OutputStream output = new FileOutputStream("storage/emulated/0/Pictures/Streamlet"+countstreamlets+".mp4");
                byte data[] = new byte[1024];
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                contentlength2=lengthOfFile/1024;
                difference =endTime-startTime;
                bandwidth =(contentlength2*1000)/difference;
                if((countstreamlets)%numberofstreamlets==0){countstreamlets=0;avgBandwidth=0;avgContentlength2=0;avgDifference=0;}
                if(avgBandwidth>0)
                {avgBandwidth=(avgBandwidth+bandwidth)/2;
                    avgDifference=(avgDifference+difference)/2;
                    avgContentlength2=(avgContentlength2+contentlength2)/2;
                }
                else{avgBandwidth=0;avgBandwidth=bandwidth;
                    avgDifference=0;avgDifference=difference;
                    avgContentlength2=0;avgContentlength2=contentlength2;}
                countstreamlets++;
            } catch (Exception e) {}
            return null;
        }
    }
    /*Class file
     * parameter - URL of the playlist
     * downloads the selected .MPD file*/
    class DownloadMPDFileAsync extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url;
                url = new URL("http://pilatus.d1.comp.nus.edu.sg/~team10/"+params[0]);
                System.out.println("Value "+url);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                File folder = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "Client");
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }
                if (success) {
                    // Do something on success
                } else {
                    // Do something else on failure
                }
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(folder.getPath()+File.separator + "new1.mpd");
                byte data[] = new byte[1024];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(new File(folder.getPath()+
                        File.separator + "new1.mpd"));
                doc.getDocumentElement ().normalize ();
                NodeList nodes = doc.getElementsByTagName("SegmentURL");
                int SegmentList = nodes.getLength();
                int stream =SegmentList/3;
                numberofstreamlets=stream;
                String parsedstreamlets[][]=new String[1000][stream];
                parsedstreamletscp=new String[1000][numberofstreamlets];
                int j = 0;
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item( i );
                    String streamleturl=node.getAttribute("media");
                    parsedstreamlets[j][i%stream] = streamleturl;
                    parsedstreamletscp[j][i%stream] = streamleturl;
                    if(i % stream == (stream-1)){
                        j = j+1;
                    }
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

    }
    /*Class file
     * parameter - URL of the playlist
     * downloads the streamlets belonging to the particular playlist
     * rate adaptation (bandwidth) algo written here
     * downloads the first 2 streamlets
     * calculates bandwidth, values & decides the video quality of successive streamlets*/
    class DownloadFileAsync2 extends AsyncTask<String, String, String> {
        int ctr1,ctr2;
        @Override
        protected String doInBackground(String... aurl) {
            int count,initialBufferEstimate;
            countstreamlets=0;
            avgBandwidth=0;
            avgContentlength2=0;
            avgDifference=0;
            initialBufferEstimate=3; /*sets the minimum number of streamlets to buffer*/
            qualityValue=new int[numberofstreamlets][2];
            URL url;
            try {

                for(ctr2=0;ctr2<initialBufferEstimate;ctr2++)
                {

                    count=0;
if (ctr2 == 0) {
    url = new URL("http://pilatus.d1.comp.nus.edu.sg/~team10/video_repo/BigBuckBunny_320x180/BigBuckBunny_320x180_original_init.mp4" );
}
                    else {
    url = new URL("http://pilatus.d1.comp.nus.edu.sg/~team10/video_repo/BigBuckBunny_320x180/" + parsedstreamletscp[ctr1][ctr2]);
}
                    long startTime=System.currentTimeMillis();
                    URLConnection conexion = url.openConnection();
                    conexion.connect();
                    System.out.println("Url " + url.toString());
                    int lengthOfFile = conexion.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream());
                    long endTime=System.currentTimeMillis();
                    OutputStream output = new FileOutputStream("storage/emulated/0/Pictures/Streamlet"+countstreamlets+".mp4");
                    byte data[] = new byte[1024];
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                    System.out.println("Value data "+data);
                    output.flush();
                    output.close();
                    input.close();
                    System.out.println("data value byte"+data);
                    contentlength2=lengthOfFile/1024;
                    difference =endTime-startTime;
                    bandwidth =(contentlength2*1000)/difference;
                    countstreamlets++;
                    if(avgBandwidth>0)
                    {avgBandwidth=(avgBandwidth+bandwidth)/2;
                        avgDifference=(avgDifference+difference)/2;
                        avgContentlength2=(avgContentlength2+contentlength2)/2;
                    }
                    else{avgBandwidth=0;avgBandwidth=bandwidth;
                        avgDifference=0;avgDifference=difference;
                        avgContentlength2=0;avgContentlength2=contentlength2;}
                    qualityValue[ctr2][0]=ctr1;
                    qualityValue[ctr2][1]=(int) bandwidth;
                }
                for(ctr2=initialBufferEstimate;ctr2<numberofstreamlets;ctr2++)
                {
                    count=0;
                    if (ctr2 == 0) {
                        url = new URL("http://pilatus.d1.comp.nus.edu.sg/~team10/video_repo/BigBuckBunny_320x180/BigBuckBunny_320x180_original_init.mp4" );
                    }
                    else {
                        url = new URL("http://pilatus.d1.comp.nus.edu.sg/~team10/video_repo/BigBuckBunny_320x180/" + parsedstreamletscp[ctr1][ctr2]);
                    }
                    long startTime=System.currentTimeMillis();
                    URLConnection conexion = url.openConnection();
                    conexion.connect();
                    int lengthOfFile = conexion.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream());
                    long endTime=System.currentTimeMillis();
                    OutputStream output = new FileOutputStream("storage/emulated/0/Pictures/Streamlet"+countstreamlets+".mp4");
                    byte data[] = new byte[1024];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();

                    contentlength2=lengthOfFile/1024;
                    difference =endTime-startTime;
                    bandwidth =(contentlength2*1000)/difference;
                    countstreamlets++;
                    qualityValue[ctr2][0]=ctr1;
                    qualityValue[ctr2][1]=(int) bandwidth;
                    if(avgBandwidth>0)
                    {avgBandwidth=(avgBandwidth+bandwidth)/2;
                        avgDifference=(avgDifference+difference)/2;
                        avgContentlength2=(avgContentlength2+contentlength2)/2;
                    }
                    else{avgBandwidth=0;avgBandwidth=bandwidth;
                        avgDifference=0;avgDifference=difference;
                        avgContentlength2=0;avgContentlength2=contentlength2;}
						/*Statically coded the bandwidth*/
                    if((avgBandwidth>=threshold1)&&(avgBandwidth<threshold2)){ctr1=1;}
                    else if(avgBandwidth>threshold2){ctr1=2;}
                    else if(avgBandwidth<threshold1){ctr1=0;}
                }
                for(int i=0;i<numberofstreamlets;i++)
                {
                    System.out.println("QualityValue"+qualityValue[i]);
                }
            } catch (Exception e) {}
            return null;

        }
    }
    /*Class - displays the selected playlist*/
    public class MyOnItemSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            countstreamlets=0;
            Toast.makeText(parent.getContext(),"Playlist Selected is "+parent.getItemAtPosition(pos).toString(),Toast.LENGTH_LONG).show();
            dropDownText = parent.getItemAtPosition(pos).toString();
            new DownloadMPDFileAsync().execute(dropDownText);
        }
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
    /*Invokes server script to retrieve the .MPD playlists
    * Updates the value of strings which contain the MPD playlist URLs*/
    public void showMPDPlaylist() {
        HttpClient httpclient3 = new DefaultHttpClient();
        System.out.println("httpclient1" + httpclient3);
        try {
            //WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            //WifiManager.WifiLock wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");
            //wifiLock.acquire();
            HttpPost httppost3 = new HttpPost("http://pilatus.d1.comp.nus.edu.sg/~team10/mpdList.php");
            HttpResponse response3 = null;
            response3 = httpclient3.execute(httppost3);
            HttpEntity resEntity3 = response3.getEntity();
            if (resEntity3 != null) {
                System.out.println("DashPlayer- Reply - Content Length: "+ resEntity3.getContentLength());
            }
            String wholeString = EntityUtils.toString(resEntity3);
            //wholeString = "\n"+","+wholeString;
            wholeString = wholeString.replace("\"","");
            wholeString = wholeString.replaceAll("[\\[\\]]","");
            String tempString;
            String[] s = wholeString.split(",");
            int j = 0;
            for (int i = 0; i < s.length; i++) {
                tempString = s[i];
                values[j] = tempString;
                j++;
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
            System.out.println("DashPlayer :Exception "
                    + e.getMessage());
        } finally {
            try {
                httpclient3.getConnectionManager().shutdown();
            } catch (Exception ignore) {
            }
        }
    }
    /*invoked by onCreate to run on UI thread that updates playlists to the client*/
    public void selectPlaylist() {
        new Thread(new Runnable() {
            public void run() {
                showMPDPlaylist();	/*calls the server script to return MPD playlist files*/
                runOnUiThread(new Runnable() {
                    public void run() {
                        inflateSpinnerList();	/*updates the playlist into the spinner controller*/	}
                });
            }
        }).start();
    }

    public void inflateSpinnerList() {
        spinnerController = (Spinner) findViewById(R.id.spinner1);
        AdapterPlaylists = new ArrayAdapter<CharSequence>(this,android.R.layout.simple_spinner_item);
        AdapterPlaylists.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerController.setOnItemSelectedListener(new MyOnItemSelectedListener());
        spinnerController.setAdapter(AdapterPlaylists);
        for (int i = 0; i < values.length; i++) {
            AdapterPlaylists.add(values[i]);
        }
    }
}

