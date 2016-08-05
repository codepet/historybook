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

    private TextView mTitleText;  // 标题文本
    private TextView mDateText;  // 日期文本
    private TextView mContentText;  // 内容文本
    private LinearLayout mImagesLayout;  // 图片布局
    private FloatingActionButton mRefreshButton;  // 刷新按钮
    private String eId;  // 事件id
    private String date;  // 事件发生的时间

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        initData();
        initView();
        fetchData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        eId = getIntent().getStringExtra("e_id");
        date = getIntent().getStringExtra("date");
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mTitleText = (TextView) findViewById(R.id.id_detail_title);
        mDateText = (TextView) findViewById(R.id.id_detail_date);
        mContentText = (TextView) findViewById(R.id.id_detail_content);
        mImagesLayout = (LinearLayout) findViewById(R.id.id_detail_images);
        mRefreshButton = (FloatingActionButton) findViewById(R.id.id_fab_refresh);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });
    }

    /**
     * 获取数据
     */
    private void fetchData() {
        if (!ConnUtil.isNetConnected(this)) {  // 判断网络连接
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTitleText.setText(getString(R.string.data_error));
                    Snackbar.make(mContentText, getString(R.string.data_error), Snackbar.LENGTH_SHORT).show();
                }
            }, 2000);
            return;
        }
        BaseApplication.getService().queryHistory(eId)
                .subscribeOn(Schedulers.io())  // 请求于io线程
                .observeOn(AndroidSchedulers.mainThread())  // 于UI线程处理
                .map(new Func1<HistoryDetailResult, HistoryDetail>() {
                    @Override
                    public HistoryDetail call(HistoryDetailResult historyDetailResult) {
                        if (historyDetailResult == null) {  // 判空，否则异常时将抛出空指针错误
                            return null;
                        }
                        return historyDetailResult.getResult().get(0);
                    }
                })
                .subscribe(new Subscriber<HistoryDetail>() {
                               @Override
                               public void onCompleted() {  // 请求完成回调
                                   Snackbar.make(mContentText, getString(R.string.refresh_success), Snackbar.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onError(Throwable e) {  // 请求错误回调
                                   mTitleText.setText(getString(R.string.data_error));
                                   Snackbar.make(mContentText, getString(R.string.refresh_fail), Snackbar.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onNext(HistoryDetail historyDetail) {  // 请求结果处理过程
                                   if (historyDetail == null) {
                                       return;
                                   }
                                   mTitleText.setText(historyDetail.getTitle());
                                   mContentText.setText(historyDetail.getContent());
                                   mDateText.setText(date);
                                   if (Integer.valueOf(historyDetail.getPicNo()) > 0) {
                                       mImagesLayout.removeAllViews();  // 添加时先移除View，否则刷新时图片会重复
                                       List<PicDetail> picDetail = historyDetail.getPicUrl();
                                       for (int i = 0; i < picDetail.size(); i++) {
                                           View view = LayoutInflater.from(DetailActivity.this)
                                                   .inflate(R.layout.item_history_detail,
                                                           new LinearLayout(DetailActivity.this),
                                                           false);
                                           ImageView image = (ImageView) view.findViewById(R.id.id_detail_image);
                                           TextView title = (TextView) view.findViewById(R.id.id_detail_image_text);
                                           title.setText(picDetail.get(i).getPic_title());
                                           Picasso.with(DetailActivity.this)
                                                   .load(picDetail.get(i).getUrl())  // 从网络地址加载图片
                                                   .placeholder(R.mipmap.ic_loading)  // 占位图，即加载时显示图片
                                                   .error(R.mipmap.ic_error)  // 错误时显示图片
                                                   .into(image);
                                           mImagesLayout.addView(view);
                                       }
                                   }
                               }
                           }

                );
    }

}
