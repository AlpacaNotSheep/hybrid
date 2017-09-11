package com.uzai.app.hybrid.core;

import org.json.JSONException;
import org.json.JSONObject;

public class ExceptionJson {
    private final static int EXCEPTION_CODE_NO = 200;
    private final static int EXCEPTION_CODE_SYSTEM = -1;
    private final static int EXCEPTION_CODE_CUSTOM = -2;

    private final static int EXCEPTION_CODE_DISCONNECTION = -3;

    public String NoExceptionJson() {

        return getExceptionJson(EXCEPTION_CODE_NO + "", "");
    }

    public String SystemExceptionJson(String error) {

        return getExceptionJson(EXCEPTION_CODE_SYSTEM + "", error);
    }

    public String CustomExceptionJson(String error) {

        return getExceptionJson(EXCEPTION_CODE_CUSTOM + "", error);
    }

    public String DisconnectionExceptionJson(String error) {

        return getExceptionJson(EXCEPTION_CODE_DISCONNECTION + "", error);
    }

    protected String getExceptionJson(String returnCode, String error) {
        JSONObject exceptionObj = new JSONObject();
        try {
            exceptionObj.put("ReturnCode", returnCode);
            exceptionObj.put("Message", error);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return exceptionObj.toString();//返回整合了错误的Json串
    }
}

