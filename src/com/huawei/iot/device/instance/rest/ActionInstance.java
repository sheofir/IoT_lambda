package com.huawei.iot.device.instance.rest;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.RestResult;

public class ActionInstance implements RequestHandler<Object, RestResult> {

	@Override
	public RestResult handleRequest(Object input, Context context) {
		RestResult result = new RestResult();
		result.setCode(RestResult.FAILED);
		result.setDescription("failed to do this action");
		try {
			Map<String, Object> inputMap = (Map<String, Object>) input;
			context.getLogger().log("input map: " + inputMap.toString() +"\n");
			String deviceId = (String) inputMap.get(Constants.DEVICE_ID);
			String operate = (String) inputMap.get(Constants.DEVICE_ACTION);
			
			Map<String, Object> cdt = new HashMap<String,Object>();
			Map<String, Object> datas = new HashMap<String,Object>();
			cdt.put(Constants.CONDITION_KEY, Constants.DEVICE_ID);
			cdt.put(Constants.CONDITION_VALUE, deviceId);
			
			datas.put(Constants.DEVICE_STATUS, operate);
			
			UpdateItemResult updateR = DBOperater.updateOneItem(Constants.TBL_DEVICE_INSTANCE, cdt, datas, context);
			result.setData(updateR);
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to get datas");
			context.getLogger().log("action result : " + result);
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			context.getLogger().log("occur an exception e: " + e + "\n");
		}
		
		return result;
	}

}
