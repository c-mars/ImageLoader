package c.mars.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static final String IMAGE_PATH="IMAGE";
    private String path;

    @Bind(R.id.image)
    ImageView imageView;

    @OnClick(R.id.button) void load(){

//      load image from URL to files directory and display it later in image view
        final String URL = "http://lorempixel.com/400/400/business/";
        Picasso.with(this).load(URL)

//               outcomment policies to use default picasso cache
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)

                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                        //create a file for bitmap data
                        File f = new File(getFilesDir(), "image");
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            f.createNewFile();

//                            save file path to preferences
                            path = f.getPath();
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                                    .edit()
                                    .putString(IMAGE_PATH, path)
                                    .commit();

                            //convert bitmap to byte array
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                            byte[] bitmapdata = bos.toByteArray();

                            //write the bytes into file
                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    display local file (just the same will be done for loading image path from database field)
                                    loadLocalImage();
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        Timber.e("error");
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        Timber.e("preparing");
                    }
                });
    }

    private void loadLocalImage(){
//        load local image (path) to imageView
        File localImageFile = new File(path);

        if (localImageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(localImageFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());

//        read path from preferences (if file was saved, it can be restored to image or stored to database)
        path= PreferenceManager.getDefaultSharedPreferences(this).getString(IMAGE_PATH, null);
        Timber.d(path);

        if(path == null) {
            load();
        } else {
            loadLocalImage();
        }
    }
}
