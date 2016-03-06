package com.tiger.tigeruploader;


import android.content.Context;

import org.json.JSONObject;

public class ServerResponse {

    private static final int RESPONSE_NO_ERROR = 0;

    private static final int RESPONSE_DUPLICATED = 1;

    private static final int RESPONSE_NOT_AN_IMAGE = 10;

    private static final int RESPONSE_WRONG_EXTENSION = 20;

    private static final int RESPONSE_UNDELETABLE_DUPLICATE = 30;

    private static final int RESPONSE_UPLOAD_ERROR = 40;

    public enum RESPONSE_TYPE{OK, DUPLICATED, NOT_AN_IMAGE, WRONG_EXTENSION, UNDELETABLE_DUPLICATE, UPLOAD_ERROR, UNKNOWN_CODE, UNPARSABLE_RESPONSE}

    private RESPONSE_TYPE responseType;

    private String message;

    public ServerResponse(String jsonMessage, Context currentContex){
        try{
            JSONObject parsedResponse = new JSONObject(jsonMessage);
            int responseCode = parsedResponse.getInt("ResponseCode");
            message = parsedResponse.getString("ResponseMessage");
            switch (responseCode){
                case RESPONSE_NO_ERROR:
                    responseType = RESPONSE_TYPE.OK;
                    break;
                case RESPONSE_DUPLICATED:
                    responseType = RESPONSE_TYPE.DUPLICATED;
                    break;
                case RESPONSE_NOT_AN_IMAGE:
                    responseType = RESPONSE_TYPE.NOT_AN_IMAGE;
                    break;
                case RESPONSE_WRONG_EXTENSION:
                    responseType = RESPONSE_TYPE.WRONG_EXTENSION;
                    break;
                case RESPONSE_UNDELETABLE_DUPLICATE:
                    responseType = RESPONSE_TYPE.UNDELETABLE_DUPLICATE;
                    break;
                case RESPONSE_UPLOAD_ERROR:
                    responseType = RESPONSE_TYPE.UPLOAD_ERROR;
                    break;
                default:
                    responseType = RESPONSE_TYPE.UNKNOWN_CODE;
                    break;
            }
        } catch (Exception ex){
            responseType = RESPONSE_TYPE.UNPARSABLE_RESPONSE;
            message = currentContex.getString(R.string.errorInvalidJSON);
            TigerApplication.ShowException(ex);
        }
    }

    public RESPONSE_TYPE getResponseType(){
        return responseType;
    }

    public String getResponseMessage(){
        return message;
    }

    public boolean isMessageLink(){
        return getResponseType().equals(RESPONSE_TYPE.OK) || getResponseType().equals(RESPONSE_TYPE.DUPLICATED);
    }

}
