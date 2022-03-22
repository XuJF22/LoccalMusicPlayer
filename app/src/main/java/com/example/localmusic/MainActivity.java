package com.example.localmusic;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  ImageView nextIv, playIv, lastIv, modeIv, iconIv;
  TextView singerTv, songTv;
  RecyclerView musicRv;
  RelativeLayout relativeLayout;

  List<LocalMusicBean> mDatas;
  private LocalMusicAdapter adapter;

  private static final int MODE_LOOP = 1;
  private static final int MODE_RANDOM = 2;
  private static final int MODE_FOCUS = 3;

  private int musicMode;

  // 记录当前正在播放音乐的位置
  private int currentPlayPosition = -1;
  // 暂停时进度条位置
  private int currentPausePositionInSong = 0;
  MediaPlayer mediaPlayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Window window = getWindow();
      window.clearFlags(
          WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
              | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
      window
          .getDecorView()
          .setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(Color.TRANSPARENT);
      window.setNavigationBarColor(Color.TRANSPARENT);
    }
    setContentView(R.layout.activity_main);
    initView();
    mediaPlayer = new MediaPlayer();
    mDatas = new ArrayList<>();
    adapter = new LocalMusicAdapter(this, mDatas);
    musicRv.setAdapter(adapter);

    musicMode = MODE_LOOP;

    // 设置布局管理器
    LinearLayoutManager linearLayoutManager =
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    musicRv.setLayoutManager(linearLayoutManager);

    // 加载本地数据源
    loadLocalMusicData();
    setEventListener();
    mediaPlayer.setOnCompletionListener(
        new MediaPlayer.OnCompletionListener() {
          @Override
          public void onCompletion(MediaPlayer mediaPlayer) {
            continueMode();
          }
        });
  }

  private void setEventListener() {
    // 设置每一项监听事件
    adapter.setOnItemClickListener(
        new LocalMusicAdapter.OnItemClickListener() {
          @Override
          public void onItemClick(View view, int position) {
            currentPlayPosition = position;
            LocalMusicBean musicBean = mDatas.get(position);
            playMusicInMusicBean(musicBean);
          }
        });
  }

  private void playMusicInMusicBean(LocalMusicBean musicBean) {
    singerTv.setText(musicBean.getSinger());
    songTv.setText(musicBean.getSong());
    stopMusic();
    // 重置多媒体播放器
    mediaPlayer.reset();
    try {
      mediaPlayer.setDataSource(musicBean.getPath());
      playMusic();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void playMusic() {
    // 播放音乐
    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
      if (currentPausePositionInSong == 0) {
        try {
          mediaPlayer.prepare();
          mediaPlayer.start();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        mediaPlayer.seekTo(currentPausePositionInSong);
        mediaPlayer.start();
      }

      playIv.setImageResource(R.mipmap.icon_pause);
    }
  }

  private void stopMusic() {
    /*停止音乐播放*/
    if (mediaPlayer != null) {
      currentPausePositionInSong = 0;
      mediaPlayer.pause();
      mediaPlayer.seekTo(0);
      mediaPlayer.stop();
      playIv.setImageResource(R.mipmap.icon_play);
    }
  }

  private void pauseMusic() {
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      currentPausePositionInSong = mediaPlayer.getCurrentPosition();
      mediaPlayer.pause();
      playIv.setImageResource(R.mipmap.icon_play);
    }
  }

  private void continueMode() {
    if (musicMode == MODE_LOOP) {
      nextMusic();
    } else if (musicMode == MODE_RANDOM) {
      Random random = new Random();
      int randomNumber = random.nextInt(mDatas.size() - 1);
      currentPlayPosition = randomNumber;
      LocalMusicBean lastBean = mDatas.get(currentPlayPosition);
      playMusicInMusicBean(lastBean);
    } else {
      LocalMusicBean nowBean = mDatas.get(currentPlayPosition);
      playMusicInMusicBean(nowBean);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    stopMusic();
  }

  private void loadLocalMusicData() {
    // 加载本地存储当中的mp3文件到集合当中
    // 1.获取ContentResolver对象
    ContentResolver resolver = getContentResolver();
    // 2.获取本地音乐存储的Uri地址
    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    // 3.开始查询地址
    Cursor cursor = resolver.query(uri, null, null, null, null);
    // 4.遍历Cursor
    int id = 0;
    while (cursor.moveToNext()) {
      String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
      String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
      String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
      id++;
      String sid = String.valueOf(id);
      String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
      long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
      SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
      String time = sdf.format(new Date(duration));

      if (time.equals("00:00") || song.startsWith("录音") || song.startsWith("20200329")) {
        id--;
      } else {
        // 将一行当中的数据封装到对象中
        LocalMusicBean bean = new LocalMusicBean(sid, song, singer, album, time, path);
        mDatas.add(bean);
      }
    }
    adapter.notifyDataSetChanged();
  }

  private void initView() {
    nextIv = findViewById(R.id.local_music_bottom_iv_next);
    playIv = findViewById(R.id.local_music_bottom_iv_play);
    lastIv = findViewById(R.id.local_music_bottom_iv_last);
    modeIv = findViewById(R.id.local_music_bottom_iv_mode);
    songTv = findViewById(R.id.local_music_bottom_tv_song);
    iconIv = findViewById(R.id.local_music_bottom_iv_icon);
    singerTv = findViewById(R.id.local_music_bottom_tv_singer);
    musicRv = findViewById(R.id.local_music_rv);
    relativeLayout = findViewById(R.id.main_layout);

    nextIv.setOnClickListener(this);
    playIv.setOnClickListener(this);
    lastIv.setOnClickListener(this);
    modeIv.setOnClickListener(this);
    iconIv.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.local_music_bottom_iv_last:
        if (musicMode == MODE_RANDOM) {
          stopMusic();
          Random random = new Random();
          int randomNumber = random.nextInt(mDatas.size() - 1);
          currentPlayPosition = randomNumber;
          playMusicInMusicBean(mDatas.get(currentPlayPosition));
          return;
        }
        if (currentPlayPosition == 0) {
          currentPlayPosition = mDatas.size() - 1;
        } else {
          currentPlayPosition = currentPlayPosition - 1;
        }
        LocalMusicBean lastBean = mDatas.get(currentPlayPosition);
        playMusicInMusicBean(lastBean);
        break;
      case R.id.local_music_bottom_iv_next:
        if (musicMode == MODE_RANDOM) {
          stopMusic();
          Random random = new Random();
          int randomNumber = random.nextInt(mDatas.size() - 1);
          currentPlayPosition = randomNumber;
          playMusicInMusicBean(mDatas.get(currentPlayPosition));
          return;
        }
        nextMusic();
        break;
      case R.id.local_music_bottom_iv_play:
        if (currentPlayPosition == -1) {
          Toast.makeText(this, "请选择想要播放的音乐", Toast.LENGTH_SHORT).show();
          return;
        }
        if (mediaPlayer.isPlaying()) {
          pauseMusic();
        } else {
          playMusic();
        }
        break;
      case R.id.local_music_bottom_iv_mode:
        if (musicMode == MODE_LOOP) {
          modeIv.setImageResource(R.mipmap.radom);
          musicMode = MODE_RANDOM;
        } else if (musicMode == MODE_RANDOM) {
          modeIv.setImageResource(R.mipmap.focus);
          musicMode = MODE_FOCUS;
        } else {
          modeIv.setImageResource(R.mipmap.loop);
          musicMode = MODE_LOOP;
        }
        break;
      case R.id.local_music_bottom_iv_icon:
        int[] backgroundMap = {
          R.mipmap.bg0,
          R.mipmap.bg1,
          R.mipmap.bg2,
          R.mipmap.bg3,
          R.mipmap.bg4,
          R.mipmap.bg5,
          R.mipmap.bg6,
          R.mipmap.bg7,
          R.mipmap.bg8,
          R.mipmap.bg9,
          R.mipmap.bg10,
          R.mipmap.bg11,
          R.mipmap.bg12,
          R.mipmap.bg13,
          R.mipmap.bg14,
          R.mipmap.bg15,
        };
        Random random = new Random();
        int randomPic = random.nextInt(15);
        relativeLayout.setBackground(getResources().getDrawable(backgroundMap[randomPic]));
        break;
      default:
    }
  }

  private void nextMusic() {
    if (currentPlayPosition == mDatas.size() - 1) {
      currentPlayPosition = 0;
    } else {
      currentPlayPosition = currentPlayPosition + 1;
    }
    LocalMusicBean nextBean = mDatas.get(currentPlayPosition);
    playMusicInMusicBean(nextBean);
  }
}
