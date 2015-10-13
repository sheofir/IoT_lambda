package com.huawei.iot.device.instance.rest;

import java.util.ArrayList;
import java.util.HashMap;
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

public class QtyInstanceEnabled implements RequestHandler<S3Event, RestResult> {

	@Override
	public RestResult handleRequest(S3Event input, Context context) {
		
		RestResult result = new RestResult();
        result.setCode(RestResult.FAILED);
		result.setDescription("failed to get Qty");
		try {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			ScanResult scanR = DBOperater.scanItems(Constants.TBL_DEVICE_INSTANCE, null, context);
			List<Map<String,AttributeValue>> listItems = scanR.getItems();
			//º∆À„ ˝¡ø
			int enabledDevices = 0;
			int allDevices = scanR.getCount();
			for(Map<String, AttributeValue> map : listItems){
				if(map.containsKey(Constants.DEVICE_STATUS) 
						&& !Constants.DEVICE_STATUS_DISABLE.equals(map.get(Constants.DEVICE_STATUS).getS())){
					enabledDevices ++;
				}
			}
			resultMap.put(Constants.QTY_INSTANCE, allDevices + "");
			resultMap.put(Constants.QTY_INSTANCE_ENABLE, enabledDevices + "");
			result.setData(resultMap);
			context.getLogger().log("result : " + result + "\n");
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to get Qty");
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			context.getLogger().log("occur an exception e: " + e + "\n");
		}
		return result;
	}

}
