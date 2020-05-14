package com.example.toosie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.toosie.Activity.CompletedTracks.CompletedTracksActivity;
import com.example.toosie.Activity.Main.MainActivity1;


public class MainActivity extends AppCompatActivity {

    private Button tooth;
    private Button running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main);

        tooth = findViewById(R.id.blue);
        tooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openblue();
            }
        });

        running = findViewById(R.id.run);
        running.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openrun();
            }
        });

    }

    public void openblue(){
        Intent intent1 = new Intent(this, Dmain.class);
        startActivity(intent1);
    }

    public void openrun(){
        Intent intent2 = new Intent(this, MainActivity1.class);
        startActivity(intent2);
    }
}
