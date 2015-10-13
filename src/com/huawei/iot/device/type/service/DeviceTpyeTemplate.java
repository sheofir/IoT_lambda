package com.huawei.iot.device.type.service;

import java.io.InputStream;
import java.util.Map;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.huawei.iot.device.util.DataTypeXmlParser;

public class DeviceTpyeTemplate {

	private static final String bucketName = "devicetype-bucket";
	private static final String key = "DeviceTypeTemplate_1.xml";
	
	//拿到S3上模板文件，转成Map
	public static Map<String,Object> getTemplateFromS3(){
		return DataTypeXmlParser.parserXmlFromInputStream(getObjectFromS3());
	}
	
	private static InputStream getObjectFromS3(){

        AmazonS3 s3 = new AmazonS3Client();
        Region usWest2 = Region.getRegion(Regions.AP_NORTHEAST_1);
        s3.setRegion(usWest2);
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
        return object.getObjectContent();
	}
}
