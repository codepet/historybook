package com.gc.history.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CalendarView;

import com.gc.history.R;
import com.gc.history.adapter.BaseRecyclerAdapter;
import com.gc.history.adapter.HistoryAdapter;
import com.gc.history.app.BaseApplication;
import com.gc.history.entity.History;
import com.gc.history.entity.HistoryResult;
import com.gc.history.util.ConnUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mHistoryItemView;  // 事件列表
    private List<History> mHistories;  // 事件集合
    private HistoryAdapter mAdapter;  // 列表适配器
    private FloatingActionButton mCalendarButton;  // 日历选择按钮
    private int year;  // 当前年
    private int month;  // 查询的月份
    private int day;  // 查询的某一月的某一天

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        fetchData(month + "/" + day);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        year = Calendar.getInstance().get(Calendar.YEAR);  // 当前年
        month = Calendar.getInstance().get(Calendar.MONTH) + 1;  // 当前月
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);  // 当前具体日期
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mCalendarButton = (FloatingActionButton) findViewById(R.id.id_fab_calendar);
        mCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendarPickerDialog();  // 弹出日历选择对话框
            }
        });
        mHistoryItemView = (RecyclerView) findViewById(R.id.id_history_list);
        mHistoryItemView.setLayoutManager(new LinearLayoutManager(this));
        mHistories = new ArrayList<>();
        mAdapter = new HistoryAdapter(this, mHistories);
        mHistoryItemView.setAdapter(mAdapter);
        mAdapter.setOnItemListener(new BaseRecyclerAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onClick(View view, int postion) {
                // 点击跳转至详情页面
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("e_id", mHistories.get(postion).getE_id());
                intent.putExtra("date", mHistories.get(postion).getDate());
                startActivity(intent);
            }
        });
    }

    /**
     * 根据日期获取数据
     *
     * @param date 日期，格式为：月/日
     */
    private void fetchData(String date) {
        if (!ConnUtil.isNetConnected(this)) {  // 判断网络连接
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(mHistoryItemView, getString(R.string.data_error), Snackbar.LENGTH_SHORT).show();
                }
            }, 2000);
            return;
        }
        BaseApplication.getService().getHistories(date)
                .subscribeOn(Schedulers.io())  // 请求于io线程
                .observeOn(AndroidSchedulers.mainThread())  // 于UI线程处理
                .map(new Func1<HistoryResult, List<History>>() {
                    @Override
                    public List<History> call(HistoryResult historyResult) {
                        if (historyResult == null) {  // 判空，否则异常时将抛出空指针错误
                            return null;
                        }
                        return historyResult.getResult();
                    }
                })
                .subscribe(new Subscriber<List<History>>() {
                    @Override
                    public void onCompleted() {  // 请求完成回调
                        Snackbar.make(mHistoryItemView, getString(R.string.refresh_success), Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {  // 请求错误回调
                        Snackbar.make(mHistoryItemView, getString(R.string.refresh_fail), Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<History> histories) {  // 请求结果处理过程
                        if (histories == null) {
                            return;
                        }
                        mHistories.clear();  // 添加时先移除，否则刷新时列表会重复
                        mHistories.addAll(histories);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * 弹出日历选择对话框
     */
    private void showCalendarPickerDialog() {
        final Dialog dialog = new Dialog(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_date_picker, null);
        final CalendarView calendarView = (CalendarView) view.findViewById(R.id.id_calendar_view);
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
            calendarView.setMaxDate(df.parse(year + "1231235959").getTime());  // 设置最大日期为当前年的12月31日
            calendarView.setMinDate(df.parse(year + "0101000000").getTime());  // 设置最小日期为当前年的1月1日
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                MainActivity.this.month = month + 1;
                MainActivity.this.day = dayOfMonth;
                String date = (month + 1) + "/" + dayOfMonth;
                fetchData(date);  // 获取数据
                dialog.dismiss();  // 对话框消失
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);  // 对话框无标题，须在setContentView前调用
        dialog.setContentView(view);
        dialog.show();
    }

}
