package com.srb.oss.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

import com.srb.common.result.Result;
import com.srb.oss.service.FileService;
import com.srb.oss.utils.OssProperties;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(InputStream inputStream, String module, String fileName) {
        // 填写Bucket名称，例如examplebucket。
        String bucketName = OssProperties.BUCKET_NAME;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(
                OssProperties.ENDPOINT,
                OssProperties.KEY_ID,
                OssProperties.KEY_SECRET);

        //判断oss是否存在Bucket
        if (!ossClient.doesBucketExist(OssProperties.BUCKET_NAME)){
            ossClient.createBucket(OssProperties.BUCKET_NAME);
            ossClient.setBucketAcl(OssProperties.BUCKET_NAME,CannedAccessControlList.PublicRead);
        }

        //上传文件路径
        //文件目录结构 “avatar/2023/3/9/uuid.jpg”
        //构建目录路劲
        String time = new DateTime().toString("/yyyy/MM/dd/");

        //得到文件的扩展名
        fileName = UUID.randomUUID().toString() + fileName.substring(fileName.lastIndexOf("."));

        //得到文件名
        String key=module+time+fileName;
       // ossClient.putObject(OssProperties.BUCKET_NAME, key, inputStream);

        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(OssProperties.BUCKET_NAME, key, inputStream);
            // 设置该属性可以返回response。如果不设置，则返回的response为空。
            putObjectRequest.setProcess("true");
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
       }
        // 关闭OSSClient。
        //ossClient.shutdown();
        //阿里云文件绝对路径
        return "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/" + key;
    }

    @Override
    public void remove(String url) {
        // 填写Bucket名称，例如examplebucket。
        String bucketName = OssProperties.BUCKET_NAME;
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(
                OssProperties.ENDPOINT,
                OssProperties.KEY_ID,
                OssProperties.KEY_SECRET);

        //判断oss是否存在Bucket
        if (!ossClient.doesBucketExist(OssProperties.BUCKET_NAME)){
            ossClient.createBucket(OssProperties.BUCKET_NAME);
            ossClient.setBucketAcl(OssProperties.BUCKET_NAME,CannedAccessControlList.PublicRead);
        }
        String host = "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/";
        String objectName=url.substring(host.length());
        boolean found = ossClient.doesObjectExist(bucketName, objectName);
        if (found) {
            ossClient.deleteObject(OssProperties.BUCKET_NAME,objectName);
        }
        ossClient.shutdown();
    }
}
