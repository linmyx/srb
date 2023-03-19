package com.srb.oss.service;

import java.io.InputStream;

public interface FileService {

    String upload(InputStream inputStream,String module,String fileName);

    void remove(String url);
}
