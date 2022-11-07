package ru.netology;

public class Request {
String method;
String path;
String body;

    public Request(String method,String path, String body) {
        this.method = method;
        this.path=path;
        this.body = body;
    }
}
