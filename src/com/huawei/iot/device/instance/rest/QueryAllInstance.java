package com.huawei.iot.device.instance.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.RestResult;
import com.huawei.iot.device.util.TransferDI2Result;

public class QueryAllInstance implements RequestHandler<S3Event, RestResult> {

	@Override
	public RestResult handleRequest(S3Event input, Context context) {

		RestResult result = new RestResult();
		result.setCode(RestResult.FAILED);
		result.setDescription("failed to get datas");
		
		//get datas from db
		try {
			ScanResult scanR = DBOperater.scanItems(Constants.TBL_DEVICE_INSTANCE, null, context);
			List<Map<String, Object>> listResult = new ArrayList();
			List<Map<String,AttributeValue>> listItems = scanR.getItems();
			
			for(Map<String,AttributeValue> m: listItems){
				listResult.add(TransferDI2Result.converFromDeviceInstance(m, context));
			}
			context.getLogger().log("QueryAllInstance scan result : " + listResult);
			result.setData(listResult);
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to get datas");
		} catch (Exception e) {
			context.getLogger().log("Occur exception when scan result : " + e);
		}
		
		return result;
	}

}
