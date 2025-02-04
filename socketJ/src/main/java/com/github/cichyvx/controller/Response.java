package com.github.cichyvx.controller;

public interface Response<T> {

    int getStatus();
    T getData();
    String getMessage();

    String
            OK_MSG = "Ok",
            Not_Found_MSG = "Not Found",
            Server_Error_MSG = "Server Error";

    int
            OK_CODE = 200,
            Not_Found_CODE = 404,
            Server_Error_CODE = 500;

}
