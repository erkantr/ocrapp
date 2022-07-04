package tr.edu.medipol.ocrapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    EditText mResultEt;
    EditText cevaptext;
    ImageView mPreviewIv;
    FloatingActionButton fabImage;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String cameraPermission[];
    String storagePermission[];
    String cevaplar = "";
    int soru_sayisi = 0;
    String test;

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultEt = findViewById(R.id.resultEt);
        mPreviewIv = findViewById(R.id.imageIv);
        fabImage = findViewById(R.id.fab);
        cevaptext = findViewById(R.id.cevaptext);
        this.setTitle("");
        fabImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageImportDialog();
                cevaptext.setText("");
            }
        });

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        switch (keyCode) {
            case KeyEvent.KEYCODE_A:
            {

                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

   // abcçdefghıijklmnoöprsştuüvyzqwx
   // jkglşfdisxcvbmçüğerwqaunzıopöty

    // abcdefghijklmnoprstuvyzqwx
    // jkglfdisxcvbmherwqaunzopty



    private void showImageImportDialog() {
        String[] items = {" Kamera", " Galeri"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Resim Seç");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else {
                        pickCamera();
                    }
                }
                if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    }
                    else {
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this, "İzin reddedildi", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this, "İzin reddedildi", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to Text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                mPreviewIv.setImageURI(resultUri);

                BitmapDrawable bitmapDrawable = (BitmapDrawable)mPreviewIv.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> items = recognizer.detect(frame);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    TextBlock myItem = items.valueAt(i);
                    sb.append(myItem.getValue());
                    sb.append("\n");
                }
                test = sb.toString();
                if (test.contains("20")){

                    cevaplar = cevaplar +"20-";
                    soru_sayisi = 20;
                    if (test.indexOf("A",test.indexOf("20")) < 0){
                        cevaplar = cevaplar + "A";
                    }
                    if (test.indexOf("B",test.indexOf("20")) < 0){
                        cevaplar = cevaplar + "B";
                    }
                    if (test.indexOf("C",test.indexOf("20")) < 0){
                        cevaplar = cevaplar + "C";
                    }
                    if (test.indexOf("D",test.indexOf("20")) < 0){
                        cevaplar = cevaplar + "D";
                    }
                    if (test.indexOf("E",test.indexOf("20")) < 0){
                        cevaplar = cevaplar + "E";
                    }
                }

                if (test.contains("19")){

                    if (soru_sayisi == 20){
                        cevaplar = cevaplar +"\n19-";
                        soru_sayisi = 19;
                        if (test.contains("A") && test.indexOf("A)",test.indexOf("19.")) < test.indexOf("20.") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B)",test.indexOf("19.")) < test.indexOf("20.") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C)",test.indexOf("19.")) < test.indexOf("20.") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D)") && test.indexOf("D)",test.indexOf("19.")) < test.indexOf("20.") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E)") && test.indexOf("E)",test.indexOf("19.")) < test.indexOf("20.") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 19;
                        cevaplar = cevaplar +"19-";
                        if (test.indexOf("A",test.indexOf("19")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("19")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("19")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("19")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("19")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("18")){

                    if (soru_sayisi == 19){
                        cevaplar = cevaplar +"\n18-";
                        soru_sayisi = 18;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("18")) < test.indexOf("19") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("18")) < test.indexOf("19") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("18")) < test.indexOf("19") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("18")) < test.indexOf("19") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("18")) < test.indexOf("19") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 18;
                        cevaplar = cevaplar +"18-";
                        if (test.indexOf("A",test.indexOf("18")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("18")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("18")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("18")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("18")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("17")){

                    if (soru_sayisi == 18){
                        cevaplar = cevaplar +"\n17-";
                        soru_sayisi = 17;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("17")) < test.indexOf("18") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("17")) < test.indexOf("18") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("17")) < test.indexOf("18") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("17")) < test.indexOf("18") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("9")) < test.indexOf("10") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 17;
                        cevaplar = cevaplar + "17-";
                        if (test.indexOf("A",test.indexOf("17")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("17")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("17")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("17")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("17")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("16")){

                    if (soru_sayisi == 17){
                        cevaplar = cevaplar +"\n16-";
                        soru_sayisi = 16;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("16")) < test.indexOf("17") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("16")) < test.indexOf("17") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("16")) < test.indexOf("17") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("16")) < test.indexOf("17") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("16")) < test.indexOf("17") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 16;
                        cevaplar = cevaplar +"16-";
                        if (test.indexOf("A",test.indexOf("16")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("16")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("16")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("16")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("16")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("15")){

                    if (soru_sayisi == 16){
                        cevaplar = cevaplar +"\n15-";
                        soru_sayisi = 15;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("15")) < test.indexOf("16") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("15")) < test.indexOf("16") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("15")) < test.indexOf("16") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("15")) < test.indexOf("16") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("15")) < test.indexOf("16") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 15;
                        cevaplar = cevaplar +"15-";
                        if (test.indexOf("A",test.indexOf("15")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("15")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("15")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("15")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("15")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("14.")){

                    if (soru_sayisi == 15){
                        cevaplar = cevaplar +"\n14-";
                        soru_sayisi = 14;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("14")) < test.indexOf("15") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("14")) < test.indexOf("15") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("14")) < test.indexOf("15") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("14")) < test.indexOf("15") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("14")) < test.indexOf("15") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 14;
                        cevaplar = cevaplar + "14-";
                        if (test.indexOf("A",test.indexOf("14")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("14")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("14")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("14")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("14")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("13")){

                    if (soru_sayisi == 14){
                        cevaplar = cevaplar +"\n13-";
                        soru_sayisi = 13;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("13")) < test.indexOf("14") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("13")) < test.indexOf("14") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("13")) < test.indexOf("14") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("13")) < test.indexOf("14") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("13")) < test.indexOf("14") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 13;
                        cevaplar = cevaplar +"13-";
                        if (test.indexOf("A",test.indexOf("13")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("13")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("13")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("13")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("13")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("12")){

                    if (soru_sayisi == 13){
                        cevaplar = cevaplar +"\n12-";
                        soru_sayisi = 12;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("12")) < test.indexOf("13") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("12")) < test.indexOf("13") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("12")) < test.indexOf("13") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("12")) < test.indexOf("13") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("12")) < test.indexOf("13") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 12;
                        cevaplar = cevaplar +"12-";
                        if (test.indexOf("A",test.indexOf("12")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("12")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("12")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("12")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("12")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("11")){

                    if (soru_sayisi == 12){
                        cevaplar = cevaplar +"\n11-";
                        soru_sayisi = 11;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("11")) < test.indexOf("12") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("11")) < test.indexOf("12") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("11")) < test.indexOf("12") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("11")) < test.indexOf("12") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("11")) < test.indexOf("12") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 11;
                        cevaplar = cevaplar + "11-";
                        if (test.indexOf("A",test.indexOf("11")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("11")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("11")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("11")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("11")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("10")){

                    if (soru_sayisi == 11){
                        cevaplar = cevaplar + "\n10-";
                        soru_sayisi = 10;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("10")) < test.indexOf("11") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("10")) < test.indexOf("11") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("10")) < test.indexOf("11") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("10")) < test.indexOf("11") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("10")) < test.indexOf("11") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 10;
                        cevaplar = cevaplar +"9-";
                        if (test.indexOf("A",test.indexOf("10")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("10")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("10")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("10")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("10")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("9")){

                    if (soru_sayisi == 10){
                        cevaplar = cevaplar +"\n9-";
                        soru_sayisi = 9;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("9")) < test.indexOf("10") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("9")) < test.indexOf("10") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("9")) < test.indexOf("10") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("9")) < test.indexOf("10") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("9")) < test.indexOf("10") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 9;
                        cevaplar = cevaplar +"9-";
                        if (test.indexOf("A",test.indexOf("9")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("9")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("9")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("9")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("9")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("8")){

                    if (soru_sayisi == 9){
                        cevaplar = cevaplar +"\n8-";
                        soru_sayisi = 8;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("8")) < test.indexOf("9") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("8")) < test.indexOf("9") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("8")) < test.indexOf("9") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("8")) < test.indexOf("9") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("8")) < test.indexOf("9") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 8;
                        cevaplar = cevaplar +"8-";
                        if (test.indexOf("A",test.indexOf("8")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("8")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("8")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("8")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("8")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("7")){

                    if (soru_sayisi == 8){
                        cevaplar = cevaplar + "\n7-";
                        soru_sayisi = 7;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("7")) < test.indexOf("8") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("7")) < test.indexOf("8") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("7")) < test.indexOf("8") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("7")) < test.indexOf("8") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("7")) < test.indexOf("8") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 7;
                        cevaplar = cevaplar +"7-";
                        if (test.indexOf("A",test.indexOf("7")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("7")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("7")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("7")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("7")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("6")){

                    if (soru_sayisi == 7){
                        cevaplar = cevaplar +"\n6-";
                        soru_sayisi = 6;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("6")) < test.indexOf("7") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("6")) < test.indexOf("7") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("6")) < test.indexOf("7") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("6")) < test.indexOf("7") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("6")) < test.indexOf("7") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 6;
                        cevaplar = cevaplar +"6-";
                        if (test.indexOf("A",test.indexOf("6")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("6")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("6")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("6")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("6")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("5")){

                    if (soru_sayisi == 6){
                        cevaplar = cevaplar +"\n5-";
                        soru_sayisi = 6;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("5")) < test.indexOf("6") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("5")) < test.indexOf("6") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("5")) < test.indexOf("6") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("5")) < test.indexOf("6") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("5")) < test.indexOf("6") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 5;
                        cevaplar = cevaplar +"5-";
                        if (test.indexOf("A",test.indexOf("5")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("5")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("5")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("5")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("5")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("4.")){

                    if (soru_sayisi == 5){
                        cevaplar = cevaplar +"\n4-";
                        soru_sayisi = 4;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("4")) < test.indexOf("5") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("4")) < test.indexOf("5") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("4")) < test.indexOf("5") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("4")) < test.indexOf("5") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("4")) < test.indexOf("5") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 4;
                        cevaplar = cevaplar +"4-";
                        if (test.indexOf("A",test.indexOf("4")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("4")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("4")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("4")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("4")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("3")){

                    if (soru_sayisi == 4){
                        cevaplar = cevaplar +"\n3-";
                        soru_sayisi = 3;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("3")) < test.indexOf("4") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("3")) < test.indexOf("4") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("3")) < test.indexOf("4") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("3")) < test.indexOf("4") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("3")) < test.indexOf("4") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 3;
                        cevaplar = cevaplar +"3-";
                        if (test.indexOf("A",test.indexOf("3")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("3")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("3")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("3")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("3")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("2")){

                    if (soru_sayisi == 3){
                        cevaplar = cevaplar +"\n2-";
                        soru_sayisi = 2;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("2")) < test.indexOf("3") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("2")) < test.indexOf("3") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("2")) < test.indexOf("3") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("2")) < test.indexOf("3") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("2")) < test.indexOf("3") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 2;
                        cevaplar = cevaplar +"2-";
                        if (test.indexOf("A",test.indexOf("2")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("2")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("2")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("2")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("2")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }

                if (test.contains("1")){

                    if (soru_sayisi == 2){
                        cevaplar = cevaplar +"\n1-";
                        soru_sayisi = 1;
                        //System.out.println(test.("A)"));
                        if (test.contains("A") && test.indexOf("A",test.indexOf("1")) < test.indexOf("2") ){
                            System.out.println("Cevap A değil");
                        } else {
                            cevaplar = cevaplar + "A";
                        }
                        if (test.contains("B") && test.indexOf("B",test.indexOf("1")) < test.indexOf("2") ){
                            System.out.println("Cevap B değil");
                        } else {
                            cevaplar = cevaplar + "B";
                        }
                        if (test.contains("C") && test.indexOf("C",test.indexOf("1")) < test.indexOf("2") ){
                            System.out.println("Cevap C değil");
                        } else {
                            cevaplar = cevaplar + "C";
                        }
                        if (test.contains("D") && test.indexOf("D",test.indexOf("1")) < test.indexOf("2") ){
                            System.out.println("Cevap D değil");
                        } else {
                            cevaplar = cevaplar + "D";
                        }
                        if (test.contains("E") && test.indexOf("E",test.indexOf("1")) < test.indexOf("2") ){
                            System.out.println("Cevap E değil");
                        } else {
                            cevaplar = cevaplar + "E";
                        }
                    } else {
                        soru_sayisi = 1;
                        cevaplar = cevaplar + "1-";
                        if (test.indexOf("A",test.indexOf("1")) < 0){
                            cevaplar = cevaplar + "A";
                        }
                        if (test.indexOf("B",test.indexOf("1")) < 0){
                            cevaplar = cevaplar + "B";
                        }
                        if (test.indexOf("C",test.indexOf("1")) < 0){
                            cevaplar = cevaplar + "C";
                        }
                        if (test.indexOf("D",test.indexOf("1")) < 0){
                            cevaplar = cevaplar + "D";
                        }
                        if (test.indexOf("E",test.indexOf("1")) < 0){
                            cevaplar = cevaplar + "E";
                        }
                    }
                }
                cevaptext.setText(cevaplar);
            }
        }
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }
}