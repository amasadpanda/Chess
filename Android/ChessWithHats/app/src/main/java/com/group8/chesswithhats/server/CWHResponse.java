package com.group8.chesswithhats.server;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/*
 * @author Philip Rodriguez
 */
public class CWHResponse {

    @Expose
    private String message;

    @Expose
    boolean isSuccess;

    public CWHResponse(String message, boolean isSuccess)
    {
        this.message = message;
        this.isSuccess = isSuccess;
    }

    public String toString()
    {
        return "CWHRequest:\n\tisSuccess = " + isSuccess + "\n\tmessage = " + message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getJSON()
    {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }
}
