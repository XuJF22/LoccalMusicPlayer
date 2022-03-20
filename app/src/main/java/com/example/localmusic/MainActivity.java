package com.example.localmusic;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  ImageView nextIv, playIv, lastIv;
  TextView singerTv, songTv;
  RecyclerView musicRv;

  List<LocalMusicBean> mDatas;
  private LocalMusicAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
    mDatas = new ArrayList<>();
    adapter = new LocalMusicAdapter(this, mDatas);
    musicRv.setAdapter(adapter);

    // 设置布局管理器
    LinearLayoutManager linearLayoutManager =
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    musicRv.setLayoutManager(linearLayoutManager);

    // 加载本地数据源
    loadLocalMusicData();
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
      // 将一行当中的数据封装到对象中
      LocalMusicBean bean = new LocalMusicBean(sid, song, singer, album, time, path);
      mDatas.add(bean);
    }
    adapter.notifyDataSetChanged();
  }

  private void initView() {
    nextIv = findViewById(R.id.local_music_bottom_iv_next);
    playIv = findViewById(R.id.local_music_bottom_iv_play);
    lastIv = findViewById(R.id.local_music_bottom_iv_last);
    songTv = findViewById(R.id.local_music_bottom_tv_song);
    singerTv = findViewById(R.id.item_local_music_singer);
    musicRv = findViewById(R.id.local_music_rv);

    nextIv.setOnClickListener(this);
    playIv.setOnClickListener(this);
    lastIv.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.local_music_bottom_iv_last:
        break;
      case R.id.local_music_bottom_iv_next:
        break;
      case R.id.local_music_bottom_iv_play:
        break;
      default:
    }
  }
}
