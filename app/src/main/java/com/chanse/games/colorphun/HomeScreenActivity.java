package com.chanse.games.colorphun;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeScreenActivity extends Activity {

    private Button playGameButton;
    private ImageView logoView;
    private TextView taglineTextView1, taglineTextView2, taglineTextView3;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        taglineTextView1 = (TextView) findViewById(R.id.tagline_text);
        taglineTextView2 = (TextView) findViewById(R.id.tagline_text2);
        taglineTextView3 = (TextView) findViewById(R.id.tagline_text3);
        playGameButton = (Button) findViewById(R.id.play_game_btn);
        logoView = (ImageView) findViewById(R.id.logo);

        // setting the typeface
        Typeface avenir_book = Typeface.createFromAsset(getAssets(), "fonts/avenir_book.ttf");
        taglineTextView1.setTypeface(avenir_book);
        taglineTextView2.setTypeface(avenir_book);
        taglineTextView3.setTypeface(avenir_book);

        // get user's preference for sign-in
        sharedPreferences = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            taglineTextView1.setVisibility(View.INVISIBLE);
            taglineTextView2.setVisibility(View.INVISIBLE);
            taglineTextView3.setVisibility(View.INVISIBLE);

            ValueAnimator bounceAnim = getBounceAnimator();
            ValueAnimator fadeAnim = getFadeAnimator();
            bounceAnim.setDuration(800);
            fadeAnim.setDuration(1000);

            bounceAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    taglineTextView1.setVisibility(View.VISIBLE);
                    taglineTextView2.setVisibility(View.VISIBLE);
                    taglineTextView3.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            AnimatorSet homeAnimation = new AnimatorSet();
            homeAnimation.playSequentially(bounceAnim, fadeAnim);
            homeAnimation.start();
        }
    }

    ValueAnimator getFadeAnimator() {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.addUpdateListener(valueAnimator -> {
            taglineTextView1.setAlpha(valueAnimator.getAnimatedFraction());
            taglineTextView2.setAlpha(valueAnimator.getAnimatedFraction());
            taglineTextView3.setAlpha(valueAnimator.getAnimatedFraction());
        });
        return anim;
    }

    ValueAnimator getBounceAnimator() {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setInterpolator(new BounceInterpolator());
        anim.addUpdateListener(valueAnimator -> {
            logoView.setScaleX(valueAnimator.getAnimatedFraction());
            logoView.setScaleY(valueAnimator.getAnimatedFraction());
            logoView.setAlpha(valueAnimator.getAnimatedFraction());

            playGameButton.setScaleX(valueAnimator.getAnimatedFraction());
            playGameButton.setScaleY(valueAnimator.getAnimatedFraction());
            playGameButton.setAlpha(valueAnimator.getAnimatedFraction());
        });
        return anim;
    }

    public void playGame(View view) {
        startActivity(new Intent(this, EasyGameActivity.class));
        //startActivity(new Intent(this, HardGameActivity.class));
    }
}
