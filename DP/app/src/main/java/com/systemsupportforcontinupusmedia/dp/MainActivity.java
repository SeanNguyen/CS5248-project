package com.systemsupportforcontinupusmedia.dp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    private Button playerbtn;//button to start player
    private Button recorderbtn;//button to record
    private Button exitbtn;//button to quit

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.recorderbtn = (Button) this.findViewById(R.id.recorder);
        recorderbtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, VideoRecorder.class); //passes the intent to VideoRecorder
                startActivity(intent);
            }
        });

        this.exitbtn = (Button) this.findViewById(R.id.exit);
        exitbtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                MainActivity.this.finish(); //quits the app


            }
        });
        this.playerbtn = (Button) this.findViewById(R.id.player);
        playerbtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, DashPlayer.class);	//passes the intent to DashPlayer
                startActivity(intent);
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // getMenuInflater().inflate(R.layout.activity_main, menu);
        return true;
    }


}
