package maratische.android.sharediscountapps;

import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import java.io.File;

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
    }
}
