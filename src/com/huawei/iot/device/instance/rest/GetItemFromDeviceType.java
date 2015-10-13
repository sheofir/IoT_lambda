package com.huawei.iot.device.instance.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.json.JSONObject;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.RestResult;
import com.huawei.iot.device.util.TransferDI2Result;

public class GetItemFromDeviceType implements RequestHandler<Object, RestResult> {

	@Override
	public RestResult handleRequest(Object input, Context context) {

		RestResult result = new RestResult();
		result.setCode(RestResult.FAILED);
		result.setDescription("failed to get datas");
		
		//get datas from db
		try {
			Map<String, Object> inputMap = (Map<String, Object>) input;
			context.getLogger().log("GetItemFromDeviceType Item, input map : " + inputMap);
			if(null != inputMap && null == inputMap.get(Constants.DEVICE_TYPE_ID)){
				return result;
			}
			ScanResult scanR = DBOperater.scanItems(Constants.TBL_DEVICE_INSTANCE, null, context);
			
			List<Map<String, Object>> listResult = new ArrayList();
			List<Map<String,AttributeValue>> listItems = scanR.getItems();
			
			for(Map<String,AttributeValue> m: listItems){
				if(inputMap.get(Constants.DEVICE_TYPE_ID).equals(m.get(Constants.DEVICE_TYPE_ID).getS())){
					listResult.add(TransferDI2Result.converFromDeviceInstance(m, context));
				}
			}
			context.getLogger().log("GetItemFromDeviceType result : " + listResult);
			result.setData(listResult);
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to get datas");
		} catch (Exception e) {
			context.getLogger().log("Occur exception when scan result : " + e);
		}
		
		return result;
	}

}
