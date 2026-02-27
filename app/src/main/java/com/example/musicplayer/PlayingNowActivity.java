package com.example.musicplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PlayingNowActivity extends AppCompatActivity {
    private MediaPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateTimeTask;
    protected ImageView backButton;
    protected ImageView pauseButton;
    protected ImageView beforeButton;
    protected ImageView nextButton;
    protected ImageView imgBeforeSong;
    protected ImageView imgNextSong;
    protected ImageView imgPresentSong;
    protected ImageView likeButton;
    protected ImageView volumeButton;
    protected ImageView shuffleButton;
    protected ImageView repeatButton;
    protected TextView nameSong;
    protected TextView authorSong;
    protected TextView presentTime;
    protected TextView totalTime;
    protected FrameLayout timeTick;
    protected View timeLine;
    private boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playing_now);

        bindViews();
        setEvents();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.playing_now_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    void bindViews() {
        backButton = findViewById(R.id.back_button);
        pauseButton = findViewById(R.id.pause_button);
        beforeButton = findViewById(R.id.before_button);
        nextButton = findViewById(R.id.next_button);
        imgBeforeSong = findViewById(R.id.img_before_song);
        imgNextSong = findViewById(R.id.img_next_song);
        imgPresentSong = findViewById(R.id.img_present_song);
        likeButton = findViewById(R.id.like_button);
        volumeButton = findViewById(R.id.volume);
        shuffleButton = findViewById(R.id.shuffle);
        repeatButton = findViewById(R.id.repeat);
        nameSong = findViewById(R.id.name_song);
        authorSong = findViewById(R.id.author_song);
        presentTime = findViewById(R.id.play_time_present);
        totalTime = findViewById(R.id.time_song);
        timeTick = findViewById(R.id.circle_song);
        timeLine = findViewById(R.id.time_line);
    }
    void setEvents() {
        setupMediaPlayer();
        pauseButton.setOnClickListener(v -> togglePlayPause());
        repeatButton.setOnClickListener(v -> resetSong());
        likeButton.setOnClickListener(v -> setUpLike());
        timeTick.setOnTouchListener(this::onTimeTickTouch);
    }
    private void setUpLike() {
        // thiết lập nút yêu thích
        isLiked = !isLiked;
        if (isLiked) {
            likeButton.setImageResource(R.drawable.ic_liked_heart);
        } else {
            likeButton.setImageResource(R.drawable.ic_heart);
        }
    }
    private void setupMediaPlayer() {
        // thiết lập bài nhạc để bắt đầu phát
        player = MediaPlayer.create(this, R.raw.duyen_troi_lay_2);
        if (player != null) {
            player.start();
            pauseButton.setImageResource(R.drawable.ic_pause);
            startUpdateTime();
            // đặt lại nhạc sau khi phát xong và chỉnh nút pause
            player.setOnCompletionListener(mp -> {
                pauseButton.setImageResource(R.drawable.ic_play);
                mp.seekTo(0);
                stopUpdateTime();
            });
        }

        updateSongInfo();
    }
    private void updateSongInfo() {
        // tính và hiển thị thời gian mà bài hát cần để phát hết
        if (player != null) {
            int duration = player.getDuration();
            int minutes = (duration / 1000) / 60;
            int seconds = (duration / 1000) % 60;
            totalTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        }
    }
    private void togglePlayPause() {
        // kiểm tra nếu player đang phát thì tạm dừng, ngược lại thì tiếp tục phát
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
                pauseButton.setImageResource(R.drawable.ic_play);
                stopUpdateTime();
            }else {
                player.start();
                pauseButton.setImageResource(R.drawable.ic_pause);
                startUpdateTime();
            }
        }
    }
    private void startUpdateTime() {
        // cập nhật thời gian hiện tại mà bài đã chạy
        updateTimeTask = () -> {
            if (player != null && player.isPlaying()) {
                int currentPosition = player.getCurrentPosition();
                int duration = player.getDuration();

                // cập nhật thời gian đã chạy
                int minutes = (currentPosition / 1000) / 60;
                int seconds = (currentPosition / 1000) % 60;
                presentTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

                // cập nhật thanh thời gian
                updateTimeLine(currentPosition, duration);

                handler.postDelayed(updateTimeTask, 1000);
            }
        };
        handler.post(updateTimeTask);
    }
    private void stopUpdateTime() {
        // ngừng cập nhật thời gian bài hát đang chạy
        if (updateTimeTask != null) {
            handler.removeCallbacks(updateTimeTask);
        }
    }
    private void updateTimeLine(int currentPosition, int duration) {
        // cập nhật thanh thời gian
        if (timeLine.getWidth() == 0) return;
        // tính tỉ lệ phần trăm đã chạy
        float progress = (float) currentPosition / duration;
        // lấy marginStart
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) timeLine.getLayoutParams();
        int marginStart = layoutParams.getMarginStart();
        int timeLineWidth = timeLine.getWidth();
        // tính vị trí mới và cập nhật
        float newX = marginStart + (progress * timeLineWidth) - (timeTick.getWidth()) / 2f;

        newX = Math.max(marginStart - (timeTick.getWidth()) / 2f, Math.min(newX, marginStart + timeLineWidth - (timeTick.getWidth()) / 2f));

        timeTick.setX(newX);
    }
    private void resetSong() {
        // đặt lại bài hát về đầu và tiếp tục phát nếu đang tạm dừng, đồng thời cập nhật thời gian hiện tại về 00:00
        if (player != null) {
            player.seekTo(0);
            if (!player.isPlaying()) {
                player.start();
                pauseButton.setImageResource(R.drawable.ic_pause);
                startUpdateTime();
            }
            // thiết lập lại thời gian đã phát
            presentTime.setText(getString(R.string.time_present));
            // thiết lập lại thanh thời gian
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) timeLine.getLayoutParams();
            int marginStart = layoutParams.getMarginStart();
            timeTick.setX(marginStart - (timeTick.getWidth()) / 2f);
        }
    }
    private boolean onTimeTickTouch(View v, MotionEvent event) {
        if (player == null || timeLine.getWidth() == 0) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                stopUpdateTime();

                float touchX = event.getRawX();
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) timeLine.getLayoutParams();
                int marginStart = layoutParams.getMarginStart();
                int timeLineWidth = timeLine.getWidth();

                int[] timeLineLocation = new int[2];
                timeLine.getLocationOnScreen(timeLineLocation);
                float relativeX = touchX - timeLineLocation[0];

                float progress = relativeX / timeLineWidth;
                progress = Math.max(0, Math.min(progress, 1));

                int newPosition = (int) (progress * player.getDuration());

                float newX = marginStart + (progress * timeLineWidth) - (timeTick.getWidth()) / 2f;
                timeTick.setX(newX);

                int minutes = (newPosition / 1000) / 60;
                int seconds = (newPosition / 1000) % 60;
                presentTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

                return true;
            case MotionEvent.ACTION_UP:
                float finalTouchX = event.getRawX();
                int finalTimeLineWidth = timeLine.getWidth();

                int[] finalTimeLineLocation = new int[2];
                timeLine.getLocationOnScreen(finalTimeLineLocation);
                float finalRelativeX = finalTouchX - finalTimeLineLocation[0];

                float finalProgress = finalRelativeX / finalTimeLineWidth;
                finalProgress = Math.max(0, Math.min(finalProgress, 1));
                int finalPosition = (int) (finalProgress * player.getDuration());

                player.seekTo(finalPosition);

                if (player.isPlaying()) {
                    startUpdateTime();
                }

                return true;
        }

        return false;
    }
    // giải phóng tài nguyên của MediaPlayer khi activity bị hủy để tránh rò rỉ bộ nhớ
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopUpdateTime();
        if (player != null) {
            player.release();
            player = null;
        }
    }
    // tạm dừng phát nhạc khi activity bị ẩn / bị ghi đè / thoát focus và chạy tiếp được khi vào lại
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && player.isPlaying()) {
            player.pause();
            pauseButton.setImageResource(R.drawable.ic_play);
        }
        stopUpdateTime();
    }
}