package com.moaskkyo.server.domain.exam.domain;


public enum ExamType {

    중간("중간고사"),
    기말("기말고사");

    private final String title;

    ExamType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
