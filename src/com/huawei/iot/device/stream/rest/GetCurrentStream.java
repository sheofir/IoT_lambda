package com.huawei.iot.device.stream.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.RestResult;

import aj.org.objectweb.asm.Attribute;

public class GetCurrentStream implements RequestHandler<Object, RestResult>{

	@Override
	public RestResult handleRequest(Object input, Context context) {
		RestResult result = new RestResult();
		result.setCode(RestResult.FAILED);
		result.setDescription("failed to get datas");
		try {
			Map<String, Object> inputMap = (Map<String, Object>) input;
			context.getLogger().log("GetCurrentStream, input map : " + inputMap);
			//如果没有指定设备ID，去取最新设备
			List<Map<String, AttributeValue>> diidList = DBOperater.scanItems(Constants.TBL_DIID, null, context).getItems();
			int maxID = 0;
			for(Map<String, AttributeValue> map: diidList){
				String id = map.get(Constants.DIID_ID).getS();
				if(Integer.valueOf(id) >= maxID)
				maxID = Integer.valueOf(id);
			}
			String deviceID = String.valueOf(maxID);
			if(inputMap.containsKey(Constants.DEVICE_ID) && (null != inputMap.get(Constants.DEVICE_ID)
					&& !"".equals(inputMap.get(Constants.DEVICE_ID)))){
				deviceID = (String) inputMap.get(Constants.DEVICE_ID);
			}
			//1.通过设备ID取得数据
			ScanResult scanR4DeviceInstance = DBOperater.getOneItem(Constants.TBL_DEVICE_INSTANCE, deviceID, context);
			Map<String,AttributeValue> diItem = scanR4DeviceInstance.getItems().get(0);
			//2.取得该条设备实例的数据流
			if(null == diItem.get(Constants.LONGITUDE)){
				result.setDescription("No current datas");
				return result;
			}
			Map<String, String> resultMap = new HashMap<String,String>();
			String lon = diItem.get(Constants.LONGITUDE).getS();
			String lat = diItem.get(Constants.LATITUDE).getS();
			String alt = diItem.get(Constants.ALTITUDE).getS();
			String spe = diItem.get(Constants.SPEED).getS();
			resultMap.put(Constants.LONGITUDE, lon);
			resultMap.put(Constants.LATITUDE, lat);
			resultMap.put(Constants.ALTITUDE, alt);
			resultMap.put(Constants.SPEED, spe);
			
			context.getLogger().log("GetCurrentStream resultMap : " + resultMap);
			result.setData(resultMap);
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to get datas");
		} catch (Exception e) {
			context.getLogger().log("Occur exception when scan result : " + e);
		}
		
		return result;
	}
	
	private List<Map<String, AttributeValue>> getOrderList(List<Map<String, AttributeValue>> list){
		List<Map<String, AttributeValue>> orderedList = new ArrayList<Map<String, AttributeValue>>();
		for(Map<String, AttributeValue> map: list){
			
		}
		return list;
	}

}
