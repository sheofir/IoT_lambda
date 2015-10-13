package com.huawei.iot.device.instance.rest;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.RestResult;
import com.huawei.iot.device.util.TransferDI2Result;

public class GetInstanceItem implements RequestHandler<Object, RestResult> {

	@Override
	public RestResult handleRequest(Object input, Context context) {

		RestResult result = new RestResult();
		result.setCode(RestResult.FAILED);
		result.setDescription("failed to get datas");
		
		//get datas from db
		try {
			Map<String, Object> inputMap = (Map<String, Object>) input;
			context.getLogger().log("Query deviceInstance Item, input map : " + inputMap);
			ScanResult scanR = DBOperater.getOneItem(Constants.TBL_DEVICE_INSTANCE, (String) inputMap.get(Constants.DEVICE_ID), context);
			
			Map<String,AttributeValue> item = scanR.getItems().get(0);
			Map<String, Object> mapResult = TransferDI2Result.converFromDeviceInstance(item, context);
			context.getLogger().log("GetInstanceItem result : " + mapResult);
			result.setData(mapResult);
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to get datas");
		} catch (Exception e) {
			context.getLogger().log("Occur exception when scan result : " + e);
		}
		
		return result;
	}

}
