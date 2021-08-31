package com.example.satelliteimage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

public class Classification extends AppCompatActivity {
    ImageView imageView;
    TextView result;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classification);
        setTitle("Classification");
        imageView = findViewById(R.id.image);
        Intent intent = getIntent();
        Bitmap image = intent.getParcelableExtra("Bitmap");
        float[] array= intent.getFloatArrayExtra("array");
        imageView.setImageBitmap(image);
        result = findViewById(R.id.result);
        result.setGravity(Gravity.CENTER);
        if(array[0] > array[1] && array[0] > array[2] && array[0] > array[3])
            result.setText("Cloud" + "\t\t\t\t" + array[0]*100 +"%");
        else if(array[1] > array[0] && array[1] > array[2] && array[1] > array[3])
            result.setText("Desert" + "\t\t\t\t" + array[1]*100 +"%");
        else if(array[2] > array[1] && array[2] > array[0] && array[2] > array[3])
            result.setText("Green area" + "\t\t\t\t" + array[2]*100 + "%");
        else
            result.setText("Water" + "\t\t\t\t" + array[3]*100 + "%");
//        result.setText(array[0] + " \n" +array[1] + "\n" +array[2] + "\n" +array[3]);
    }
}
