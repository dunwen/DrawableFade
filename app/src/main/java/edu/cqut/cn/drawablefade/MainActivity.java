package edu.cqut.cn.drawablefade;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.dundunwen.MyDrawable;
import com.dundunwen.MyImageView;

public class MainActivity extends AppCompatActivity {

    MyImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        iv = (MyImageView) findViewById(R.id.iv);
        initDrawable();

    }

    private void initDrawable() {
//        Drawable drawable0 = getResources().getDrawable(R.drawable.image);
//        Drawable drawable1 = getResources().getDrawable(R.drawable.image1);

//        Bitmap bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.image);
//        Bitmap bitmap1 =BitmapFactory.decodeResource(getResources(),R.drawable.image1);

//        Bitmap[] drawables = new Bitmap[]{bitmap0,bitmap1};
//        MyDrawable layerDrawable = new MyDrawable(drawables,this);

        MyImageView.Builder builder = new MyImageView.Builder();
        builder.setResId(new int[]{R.drawable.image,R.drawable.image1,R.drawable.head});
        iv.setConfig(builder);
        iv.setListener(new MyImageView.ItemClickListener() {
            @Override
            public void onClick(int position, int resId) {
                Log.i("MAINACTIVITY", "onClick: 点击了position为"+position+"的图片");
            }
        });
        iv.startAnimation();

    }
}
