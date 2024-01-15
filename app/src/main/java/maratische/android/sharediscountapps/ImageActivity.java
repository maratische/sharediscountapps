package maratische.android.sharediscountapps;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.io.File;
import java.io.IOException;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView imageView = findViewById(R.id.imageView);
        var key = getIntent().getStringExtra("key");

        var myDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        var file = new File(myDir, key + ".jpg");

        Glide.with(this)
                .load(file.getAbsolutePath())
                .into(imageView);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ImageDecoder.Source source = ImageDecoder.createSource(getApplicationContext().getContentResolver(),
                            Uri.fromFile(file));
                    Bitmap bitmap2 = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true);
                    String result = decodeQRCode(bitmap2);
                    ImageActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result != null && result.length() > 0) {
                                Toast.makeText(ImageActivity.this, "OK " + result, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ImageActivity.this, "ERROR " + result, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    ;
                }
            }
        }).start();
    }

    private String decodeQRCode(Bitmap bitmap) {

        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
//            Collection<BarcodeFormat> formats = new ArrayList<>();
//            formats.add(BarcodeFormat.PDF_417);
//            formats.add(BarcodeFormat.CODABAR);
//            Map<DecodeHintType, Collection<BarcodeFormat>> hints = new HashMap<>();
//            hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
//            hints.put(DecodeHintType.TRY_HARDER, formats);
//            com.google.zxing.Result result = new MultiFormatReader().decode(binaryBitmap, hints);
            com.google.zxing.Result result = new MultiFormatReader().decode(binaryBitmap);
            return result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
