package com.huawei.iot.device.type.rest;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.huawei.iot.device.type.service.DeviceTpyeTemplate;

public class GetDeviceTemplate implements RequestHandler<S3Event, Object> {

	@Override
	public Object handleRequest(S3Event input, Context context) {
		// TODO Auto-generated method stub
		Map<String, Object> templateMap = DeviceTpyeTemplate.getTemplateFromS3();
		
		context.getLogger().log("templateMap from s3 : "+ templateMap);
		return templateMap;
	}

}
