package com.gc.history.entity;

import java.util.List;

public class HistoryDetail {

    private String e_id;
    private String title;
    private String content;
    private String picNo;
    private List<PicDetail> picUrl;

    public String getE_id() {
        return e_id;
    }

    public void setE_id(String e_id) {
        this.e_id = e_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPicNo() {
        return picNo;
    }

    public void setPicNo(String picNo) {
        this.picNo = picNo;
    }

    public List<PicDetail> getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(List<PicDetail> picUrl) {
        this.picUrl = picUrl;
    }

    @Override
    public String toString() {
        return "HistoryDetailActivity{" +
                "e_id='" + e_id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", picNo='" + picNo + '\'' +
                ", picUrl=" + picUrl +
                '}';
    }
}
