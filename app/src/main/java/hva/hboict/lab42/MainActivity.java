package hva.hboict.lab42;

import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
    ImageView bgImage;
    ArrayList<Bubble> bubbles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bgImage = findViewById(R.id.bg_bubble);

        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setContentView(R.layout.activity_main);

        animateBackground();
    }

    private void animateBackground() {
        generateBubbles();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                // Get the display size
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

                // Create bitmap with a canvas size of the screen
                Bitmap bitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.heightPixels, Bitmap.Config.ARGB_4444);
                Canvas canvas = new Canvas(bitmap);

                // Paint????
                Paint paint = new Paint();
                paint.setColor(Color.rgb(253, 182, 91));
                paint.setStyle(Paint.Style.FILL);
                paint.setAntiAlias(true);
                paint.setDither(true);

                for(Bubble bubble : MainActivity.this.bubbles) {
                    canvas.drawCircle(bubble.x, bubble.y, 70F, paint);
                }

                runOnUiThread(() -> bgImage.setBackground(new BitmapDrawable(getResources(), bitmap)));
                updateBubblePositions();
            }
        }, 0, 10);
    }

    void generateBubbles() {
        int bubbleCount = 5;

        for(int i = 0; i < bubbleCount; i++) {
            // Get the display size
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

            int spacer = displayMetrics.widthPixels / bubbleCount * i;
            float offset = ThreadLocalRandom.current().nextInt(spacer - 50, spacer + 150);

            this.bubbles.add(generateBubble(offset));
        }
    }

    private Bubble generateBubble(float offsetLeft) {
        // Get the display size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

        return new Bubble(offsetLeft, displayMetrics.heightPixels + ThreadLocalRandom.current().nextInt(100, 500), (float) Math.random() * 0.3f);
    }

    private void updateBubblePositions() {
        for(int i = 0; i < this.bubbles.size(); i++) {
            Bubble bubble = this.bubbles.get(i);
            bubble.y -= bubble.speed;

            if(bubble.y < -50)
                this.bubbles.set(i, generateBubble(bubble.x));
        }
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
}