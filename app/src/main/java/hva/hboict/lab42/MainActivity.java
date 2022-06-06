package hva.hboict.lab42;

// Resource: https://github.com/softbankrobotics-labs/pepper-interaction-sample/blob/master/app/src/main/java/com/softbankrobotics/interactionsample/statemachine/InteractionStateMachine.java

import androidx.appcompat.app.AppCompatActivity;
import hva.hboict.lab42.utils.HumanEngager;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.EngageHumanBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.EngageHuman;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
    private QiContext qiContext;
    private HumanEngager humanEngager = null;

    ImageView bgImage;
    ArrayList<Bubble> bubbles = new ArrayList<>();
    ArrayList<SpeechBubble> speechBubbles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setContentView(R.layout.activity_main);

        this.speechBubbles.add(new SpeechBubble("Welkom", "Welkom, leuk dat je er bent!"));
        this.speechBubbles.add(new SpeechBubble("Goedendag", "Welkom bij de opening van LAB42."));
        this.speechBubbles.add(new SpeechBubble("Welkom", "Leuk om je te zien in dit gloednieuwe gebouw."));
        this.speechBubbles.add(new SpeechBubble("Hey!", "Welkom bij de opening van LAB42."));
        this.speechBubbles.add(new SpeechBubble("Hoi, welkom.", "Welkom bij de opening van LAB42."));

        bgImage = findViewById(R.id.bg_bubble);
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

    private void sayRandomSentence() {
        int index = (int)(Math.random() * this.speechBubbles.size());

        SpeechBubble speechBubble = this.speechBubbles.get(index);

        runOnUiThread(() -> ((TextView)findViewById(R.id.robot_text_sentence)).setText(speechBubble.displayText));
        this.makeSay(speechBubble.speechText, false);
    }

    public Say makeSay(String text, boolean disableMovement) {
        SayBuilder sayBuilder = SayBuilder.with(this.qiContext);

        text = "\\vol=80\\" + text;

        sayBuilder = sayBuilder.withPhrase(new Phrase(text));
        if(disableMovement) {
            sayBuilder = sayBuilder.withBodyLanguageOption(BodyLanguageOption.DISABLED);
        }
        sayBuilder = sayBuilder.withLocale(new Locale(Language.DUTCH, Region.NETHERLANDS));

        if(this.qiContext != null)
            return sayBuilder.build();

        String finalText = text;
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), finalText, Toast.LENGTH_SHORT).show());

        return null;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        this.humanEngager = new HumanEngager(this.qiContext, 5*1000);

        this.humanEngager.onInteracting = isInteracting -> {
            System.out.println("Human engaged something");
            System.out.println(isInteracting);
        };
        this.humanEngager.start();
    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
}