package com.example.satelliteimage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.satelliteimage.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    ImageView camera, gallery;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int GALLERY_PERM_CODE = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camera = findViewById(R.id.camera);
        camera.setOnClickListener(view -> cameraPermissions());
        gallery = findViewById(R.id.gallery);
        gallery.setOnClickListener(view -> galleryPermissions());

    }
    private void cameraPermissions(){
        if (ContextCompat.checkSelfPermission(this , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this , new String[] {Manifest.permission.CAMERA} , CAMERA_PERM_CODE);
        }else {
            openCamera();
        }
    }
    private void galleryPermissions(){
        if (ContextCompat.checkSelfPermission(this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this , new String[] {Manifest.permission.READ_EXTERNAL_STORAGE} , GALLERY_PERM_CODE);
        }else {
            openGallery();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERM_CODE){
                openCamera();
        }
        else if(requestCode == GALLERY_PERM_CODE){
                openGallery();
        }
    }


    private void openCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        launchCamera.launch(cameraIntent);
    }

    private void openGallery(){
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        launchGallery.launch(Intent.createChooser(galleryIntent, "Select Picture"));

    }

    ActivityResultLauncher<Intent> launchCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    Bitmap bitmap = Bitmap.createScaledBitmap(image, 224, 224, false);
                    Intent intent = new Intent(MainActivity.this , Classification.class);
                    try {
                        intent.putExtra("array" , Classification(bitmap).getFloatArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra("Bitmap", bitmap);
                    startActivity(intent);
                    }
        });


    ActivityResultLauncher<Intent> launchGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Uri imageUri = data.getData();
                    Intent intent = new Intent(MainActivity.this , Classification.class);
                    Bitmap photo = null;
                    try {
                        photo = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Bitmap bitmap = Bitmap.createScaledBitmap(photo, 224, 224, true);

                    try {
                        intent.putExtra("array" , Classification(bitmap).getFloatArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra("Bitmap", bitmap);
                    startActivity(intent);
                }
            });


    private TensorBuffer Classification(Bitmap bitmap) throws IOException {
            Model model = Model.newInstance(getApplicationContext());
            ByteBuffer input = ByteBuffer.allocateDirect(224 * 224 * 3 * 4).order(ByteOrder.nativeOrder());
            input.order(ByteOrder.nativeOrder());
            input.rewind();
            for (int y = 0; y < 224; y++) {
                for (int x = 0; x < 224; x++) {
                    int px = bitmap.getPixel(x, y);
                    int r = Color.red(px);
                    int g = Color.green(px);
                    int b = Color.blue(px);

                    float rf = (r) / 255.0f;
                    float gf = (g) / 255.0f;
                    float bf = (b) / 255.0f;
                    input.putFloat(rf);
                    input.putFloat(gf);
                    input.putFloat(bf);
                }}
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(input);
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            model.close();
        return outputFeature0;
    }

}