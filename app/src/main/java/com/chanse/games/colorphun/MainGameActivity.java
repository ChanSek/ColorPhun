package com.chanse.games.colorphun;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chanse.games.ChanseGames;
import com.chanse.games.common.ChanseConstants;
import com.chanse.games.model.Game;

import java.util.LinkedHashMap;

public abstract class MainGameActivity extends Activity implements View.OnClickListener {

    protected static final String ACTION_EASY = "com.chanse.COLORPHUN_EASY";
    protected static final String ACTION_HARD = "com.chanse.COLORPHUN_HARD";

    protected TextView pointsTextView, levelTextView;
    protected ProgressBar timerProgress;
    protected AnimatorSet pointAnim, levelAnim;

    protected int level, points;
    protected long score;
    protected boolean gameStart = false;
    protected Runnable runnable;
    protected int timer;

    enum GameMode { EASY, HARD }
    protected GameMode gameMode;
    protected String intentAction;

    protected int POINT_INCREMENT;
    protected int SCORE_INCREMENT;
    protected int TIMER_BUMP;
    protected static int TIMER_DELTA = -1;
    protected static final int START_TIMER = 200;
    protected static final int FPS = 100;
    protected static final int LEVEL = 25;

    protected Handler handler;
    protected Bundle mExtra;
    protected Game mFromGame;
    protected long mTargetScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        ChanseGames.onGameStart(this);
        Intent intent = getIntent();
        mExtra = intent.getBundleExtra(ChanseConstants.KEY_EXTRA);
        if (mExtra != null) {
            mFromGame = mExtra.getParcelable(ChanseConstants.KEY_EXTRA_GAME);
            if (mFromGame != null) {
                mTargetScore = mFromGame.getScore();
            }
        }
    }

    protected void setupProgressView() {
        timerProgress = (ProgressBar) findViewById(R.id.progress_bar);
        pointsTextView = (TextView) findViewById(R.id.points_value);
        levelTextView = (TextView) findViewById(R.id.level_value);
        TextView pointsLabel = (TextView) findViewById(R.id.points_label);
        TextView levelsLabel = (TextView) findViewById(R.id.level_label);

        // setting up fonts
        Typeface avenir_black = Typeface.createFromAsset(getAssets(), "fonts/avenir_black.ttf");
        Typeface avenir_book = Typeface.createFromAsset(getAssets(), "fonts/avenir_book.ttf");
        pointsTextView.setTypeface(avenir_black);
        levelTextView.setTypeface(avenir_black);
        pointsLabel.setTypeface(avenir_book);
        levelsLabel.setTypeface(avenir_book);

        // setting up animations
        pointAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.points_animations);
        pointAnim.setTarget(pointsTextView);
        levelAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.level_animations);
        levelAnim.setTarget(levelTextView);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        endGame();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gameStart = false;
    }

    protected void setupGameLoop() {
        runnable = new Runnable() {
            @Override
            public void run() {
                while (timer > 0 && gameStart) {
                    synchronized (this) {
                        try {
                            wait(FPS);
                        } catch (InterruptedException e) {
                            Log.i("THREAD ERROR", e.getMessage());
                        }
                        timer = timer + TIMER_DELTA;
                        if (TIMER_DELTA > 0) {
                            TIMER_DELTA = -TIMER_DELTA / TIMER_BUMP;
                        }
                    }
                    handler.post(() -> timerProgress.setProgress(timer));
                }
                if (gameStart) {
                    handler.post(() -> endGame());
                }
            }
        };
    }

    protected void resetGame() {
        gameStart = false;
        level = 1;
        points = 0;
        score = 0;

        // update view
        pointsTextView.setText(String.valueOf(points));
        levelTextView.setText(String.valueOf(level));
        timerProgress.setProgress(0);
    }

    protected void startGame() {
        gameStart = true;

        Toast.makeText(this, R.string.game_help, Toast.LENGTH_SHORT).show();
        setColorsOnButtons();

        // start timer
        timer = START_TIMER;
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void startIntentSenderForResult(IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    protected void endGame() {
        gameStart = false;
        Game game = new Game();
        game.setPackageName(getPackageName());
        game.setScore(score);
        LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
        properties.put(getString(R.string.points_text), points);
        properties.put(getString(R.string.level_text), level);
        game.setProperties(properties);
        game.setIntentAction(intentAction);

        // Determine whether it's a challenge or a normal game
        if (mFromGame != null) ChanseGames.onChallengeOver(this, game, mExtra); // It's a challenge
        else ChanseGames.onGameOver(this, game);                                // It's a game
        int highScore = saveAndGetHighScore();
        launchGameOver(highScore);
    }

    private int saveAndGetHighScore() {
        SharedPreferences preferences = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        int highScore = preferences.getInt("HIGHSCORE", 0);
        if (points > highScore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("HIGHSCORE", points);
            editor.apply();
            highScore = points;
        }
        return highScore;
    }

    private void launchGameOver(int highScore) {
        // Send data to another activity
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("points", points);
        intent.putExtra("level", level);
        intent.putExtra("best", highScore);
        intent.putExtra("newScore", highScore == points);
        intent.putExtra("gameMode", gameMode.name());
        startActivity(intent);
        finish();
    }

    // called on correct guess
    public void updatePoints() {
        points = points + POINT_INCREMENT;
        score = score + SCORE_INCREMENT;
        TIMER_DELTA = -TIMER_BUMP * TIMER_DELTA; // give a timer bump
        pointsTextView.setText(String.valueOf(points));
        pointAnim.start();

        if (points > level * LEVEL) {
            incrementLevel();
            levelTextView.setText(String.valueOf(level));
            levelAnim.start();
        }
    }

    // called when user goes to next level
    public void incrementLevel() {
        level += 1;
        updateScoreIncrement();
        TIMER_DELTA = level;
    }

    // ABSTRACT METHODS
    abstract protected void setColorsOnButtons();

    abstract protected void calculatePoints(View view);

    abstract protected void updateScoreIncrement();
}