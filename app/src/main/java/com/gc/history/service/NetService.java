package com.gc.history.service;


import com.gc.history.entity.HistoryDetailResult;
import com.gc.history.entity.HistoryResult;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface NetService {

    /**
     * 历史上的今天接口
     *
     * @param date 日期，格式为8/18
     * @return
     */
    @GET("/todayOnhistory/queryEvent.php?key=6d80429976e176018e69641c46d5b73b")
    Observable<HistoryResult> getHistories(@Query("date") String date);

    /**
     * 历史上的今天的详情接口
     *
     * @param e_id
     * @return
     */
    @GET("/todayOnhistory/queryDetail.php?key=6d80429976e176018e69641c46d5b73b")
    Observable<HistoryDetailResult> queryHistory(@Query("e_id") String e_id);


}
