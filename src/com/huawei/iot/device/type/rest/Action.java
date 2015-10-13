package com.huawei.iot.device.type.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.RestResult;
import com.huawei.iot.device.util.TransferDI2Result;

public class Action implements RequestHandler<Object, RestResult> {

	@Override
	public RestResult handleRequest(Object input, Context context) {
		
		RestResult result = new RestResult();
		result.setCode(RestResult.FAILED);
		result.setDescription("failed to do this action");
		try {
			Map<String, Object> map = (Map<String, Object>) input;
			context.getLogger().log("input map: " + map.toString() +"\n");
			String deviceTypeID = (String) map.get(Constants.DEVICE_TYPE_ID);
			String status = (String) map.get(Constants.STATUS);
			
			Map<String, Object> cdt = new HashMap<String,Object>();
			Map<String, Object> datas = new HashMap<String,Object>();
			cdt.put(Constants.CONDITION_KEY, Constants.DEVICE_TYPE_ID);
			cdt.put(Constants.CONDITION_VALUE, deviceTypeID);
			
			datas.put(Constants.STATUS, status);
			//更新设备类型数据库
			UpdateItemResult updateR = DBOperater.updateOneItem(Constants.TBL_DEVICE_TYPE, cdt, datas, context);
			//更新该设备类型关联的设备实例的状态
			updateDeviceInstanceState(deviceTypeID, status, context);
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

	private void updateDeviceInstanceState(String deviceTypeID, String status, Context context) throws Exception {
		// TODO Auto-generated method stub
		
		ScanResult scanR = DBOperater.scanItems(Constants.TBL_DEVICE_INSTANCE, null, context);
		
		List<Map<String, Object>> listResult = new ArrayList();
		List<Map<String,AttributeValue>> listItems = scanR.getItems();
		
		for(Map<String,AttributeValue> m: listItems){
			//先找到所有关联设备实例
			if(deviceTypeID.equals(m.get(Constants.DEVICE_TYPE_ID).getS())){
				//再去更新这些实例的状态
				String deviceId = m.get(Constants.DEVICE_ID).getS();
				
				Map<String, Object> cdt = new HashMap<String,Object>();
				Map<String, Object> datas = new HashMap<String,Object>();
				cdt.put(Constants.CONDITION_KEY, Constants.DEVICE_ID);
				cdt.put(Constants.CONDITION_VALUE, deviceId);
				
				datas.put(Constants.DEVICE_STATUS, status);
				//更新设备实例状态数据库
				UpdateItemResult updateR = DBOperater.updateOneItem(Constants.TBL_DEVICE_INSTANCE, cdt, datas, context);
				context.getLogger().log("updateDeviceInstanceState update item result: " + updateR + "\n");
			}
		}
	}

}
