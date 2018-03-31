package com.example.phili.rabbitmqtestproject;

/**
 * Created by phili on 3/27/2018.
 */

public interface RequestCompletionListener {
    void requestCompleted(Request.Response response);
}
