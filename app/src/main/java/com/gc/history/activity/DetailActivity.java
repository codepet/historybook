package com.gc.history.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gc.history.R;
import com.gc.history.app.BaseApplication;
import com.gc.history.entity.HistoryDetail;
import com.gc.history.entity.HistoryDetailResult;
import com.gc.history.entity.PicDetail;
import com.gc.history.util.ConnUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DetailActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView dateText;
    private TextView contentText;
    private LinearLayout mImagesLayout;
    private FloatingActionButton mRefreshButton;
    private String eId;
    private String date;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        initData();
        initView();
        fetchData();
    }

    private void initData() {
        eId = getIntent().getStringExtra("e_id");
        date = getIntent().getStringExtra("date");
    }

    private void initView() {
        titleText = (TextView) findViewById(R.id.id_detail_title);
        dateText = (TextView) findViewById(R.id.id_detail_date);
        contentText = (TextView) findViewById(R.id.id_detail_content);
        mImagesLayout = (LinearLayout) findViewById(R.id.id_detail_images);
        mRefreshButton = (FloatingActionButton) findViewById(R.id.id_fab_refresh);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });
    }

    private void fetchData() {
        if (!ConnUtil.isNetConnected(this)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    titleText.setText("获取数据失败，请检查网络");
                    Snackbar.make(contentText, "获取数据失败，请检查网络", Snackbar.LENGTH_SHORT).show();
                }
            }, 2000);
            return;
        }
        BaseApplication.getService().queryHistory(eId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<HistoryDetailResult, HistoryDetail>() {
                    @Override
                    public HistoryDetail call(HistoryDetailResult historyDetailResult) {
                        if (historyDetailResult == null) {
                            return null;
                        }
                        return historyDetailResult.getResult().get(0);
                    }
                })
                .subscribe(new Subscriber<HistoryDetail>() {
                               @Override
                               public void onCompleted() {
                                   Snackbar.make(contentText, "刷新成功", Snackbar.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onError(Throwable e) {
                                   Snackbar.make(contentText, "刷新失败", Snackbar.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onNext(HistoryDetail historyDetail) {
                                   if (historyDetail == null) {
                                       return;
                                   }
                                   titleText.setText(historyDetail.getTitle());
                                   contentText.setText(historyDetail.getContent());
                                   dateText.setText("时间：" + date);
                                   if (Integer.valueOf(historyDetail.getPicNo()) > 0) {
                                       mImagesLayout.removeAllViews();
                                       List<PicDetail> picDetail = historyDetail.getPicUrl();
                                       for (int i = 0; i < picDetail.size(); i++) {
                                           View view = LayoutInflater.from(DetailActivity.this)
                                                   .inflate(R.layout.item_history_detail,
                                                           new LinearLayout(DetailActivity.this),
                                                           false);
                                           ImageView image = (ImageView) view.findViewById(R.id.id_detail_image);
                                           TextView title = (TextView) view.findViewById(R.id.id_detail_image_text);
                                           title.setText(picDetail.get(i).getPic_title());
                                           Picasso.with(DetailActivity.this).load(picDetail.get(i).getUrl())
                                                   .into(image);
                                           mImagesLayout.addView(view);
                                       }
                                   }
                               }
                           }

                );
    }

}
