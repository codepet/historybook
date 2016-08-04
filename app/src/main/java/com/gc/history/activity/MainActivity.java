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
import android.support.v7.widget.Toolbar;
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

    private RecyclerView mHistoryItemView;
    private List<History> mHistories;
    private HistoryAdapter mAdapter;
    private FloatingActionButton mCalendarButton;
    private Toolbar mToolbar;
    private int year;
    private int month;
    private int day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        year = Calendar.getInstance().get(Calendar.YEAR);
        month = Calendar.getInstance().get(Calendar.MONTH);
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        mToolbar = (Toolbar) findViewById(R.id.id_toolbar);
        setSupportActionBar(mToolbar);
        initHistoryItemView();
        fetchData((month + 1) + "/" + day);
    }

    private void initHistoryItemView() {
        mCalendarButton = (FloatingActionButton) findViewById(R.id.id_fab_calendar);
        mCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendarPickerDialog();
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
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("e_id", mHistories.get(postion).getE_id());
                intent.putExtra("date", mHistories.get(postion).getDate());
                startActivity(intent);
            }
        });
    }

    private void fetchData(String date) {
        if (!ConnUtil.isNetConnected(this)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(mHistoryItemView, "获取数据失败，请检查网络", Snackbar.LENGTH_SHORT).show();
                }
            }, 2000);
            return;
        }
        BaseApplication.getService().getHistories(date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<HistoryResult, List<History>>() {
                    @Override
                    public List<History> call(HistoryResult historyResult) {
                        if (historyResult == null) {
                            return null;
                        }
                        return historyResult.getResult();
                    }
                })
                .subscribe(new Subscriber<List<History>>() {
                    @Override
                    public void onCompleted() {
                        Snackbar.make(mHistoryItemView, "刷新成功", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Snackbar.make(mHistoryItemView, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<History> histories) {
                        if (histories == null) {
                            Snackbar.make(mHistoryItemView, "刷新失败", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        mHistories.clear();
                        mHistories.addAll(histories);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void showCalendarPickerDialog() {
        final Dialog dialog = new Dialog(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_date_picker, null);
        final CalendarView calendarView = (CalendarView) view.findViewById(R.id.id_calendar_view);
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
            calendarView.setMaxDate(df.parse(year + "1231235959").getTime());
            calendarView.setMinDate(df.parse(year + "0101000000").getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                MainActivity.this.month = month + 1;
                MainActivity.this.day = dayOfMonth;
                String today = (month + 1) + "月" + dayOfMonth + "日";
                String date = (month + 1) + "/" + dayOfMonth;
                fetchData(date);
                dialog.dismiss();
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.show();
    }

}
