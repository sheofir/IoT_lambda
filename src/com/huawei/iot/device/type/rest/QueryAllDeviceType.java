package com.huawei.iot.device.type.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.util.json.JSONObject;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.RestResult;
import com.huawei.iot.device.util.TransferDT2Result;

public class QueryAllDeviceType implements RequestHandler<S3Event, RestResult> {

	
	@Override
	public RestResult handleRequest(S3Event input, Context context) {
		LambdaLogger logger = context.getLogger();
        logger.log("log data from Lambda logger\n");
        
        RestResult result = new RestResult();
		result.setCode(RestResult.FAILED);
		result.setDescription("failed to get datas");
		
		try {
			List<Map<String, Object>> listResult = new ArrayList();
			List<Map<String,AttributeValue>> listItems = testGetAllItems(context);
			
			for(Map<String,AttributeValue> m: listItems){
				listResult.add(TransferDT2Result.converFromDeviceType(m, context));
			}
			result.setData(listResult);
			logger.log("result : " + result.getData().toString() + "\n");
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to get datas");
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log("occur an exception e: " + e + "\n");
		}
        return result;
        
	}
	
	private List<Map<String, AttributeValue>> testGetAllItems(Context context) throws Exception{
		
		String tbl = Constants.TBL_DEVICE_TYPE;
		ScanResult result = DBOperater.scanItems(tbl, null, context);
		context.getLogger().log("testGetAllItems result: " + result);
		context.getLogger().log("result.getItems() result: " + result.getItems());
		context.getLogger().log("result.getItems().get(0) result: " + result.getItems().get(0));
		return result.getItems();
	}
}
