package com.huawei.iot.device.stream.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.RestResult;

public class GetStreamFromIns implements RequestHandler<Object, RestResult>{

	@Override
	public RestResult handleRequest(Object input, Context context) {
		RestResult result = new RestResult();
		result.setCode(RestResult.FAILED);
		result.setDescription("failed to get datas");
		try {
			Map<String, Object> inputMap = (Map<String, Object>) input;
			context.getLogger().log("GetBusPosition, input map : " + inputMap);
			//1.通过设备ID取得数据ID
			ScanResult scanR4DeviceInstance = DBOperater.getOneItem(Constants.TBL_DEVICE_INSTANCE, (String) inputMap.get(Constants.DEVICE_ID), context);
			Map<String,AttributeValue> diItem = scanR4DeviceInstance.getItems().get(0);
			String dsID = diItem.get(Constants.DATA_STREAMS).getM().get(Constants.DATA_STREAM+1).getM().get(Constants.DATA_STREAM_ID).getS();
			context.getLogger().log("GetBusPosition, dsID : " + dsID);
			dsID = dsID.substring(0, dsID.length()-1);
			//根据dataID取得
			Map<String,Object> datas = new TreeMap<String, Object>();
			ScanResult sr = DBOperater.getOneItem(Constants.TBL_DATA_STREAM, dsID, context);
			context.getLogger().log("sr: " + sr);
			List<Map<String, AttributeValue>> list = sr.getItems();
			int count = sr.getCount();
			context.getLogger().log("sr count: " + count + ",list: "+ list);
			for(int i=0;i<count;i++){
				
				String createTime = list.get(i).get(Constants.DATA_CREATE_TIME).getS();
				context.getLogger().log("createTime: " + createTime);
				String lonDataKey = dsID+"1_CreateTime_"+createTime;
				String latDataKey = dsID+"2_CreateTime_"+createTime;
				String altDataKey = dsID+"3_CreateTime_"+createTime;
				String speDataKey = dsID+"4_CreateTime_"+createTime;
				String lon = "";
				String lat = "";
				String alt = "";
				String spe = "";
				//判断是否已经存入datas
				if(!datas.containsKey(createTime)){
					Map<String, String> resultMap = new HashMap<String,String>();
					//数据没有存入datas
					if(lonDataKey.equals(list.get(i).get(Constants.DATA_STREAM_ID).getS())){
						lon = list.get(i).get(Constants.DATA_STREAM_VALUE).getS();
					}else if(latDataKey.equals(list.get(i).get(Constants.DATA_STREAM_ID).getS())){
						lat = list.get(i).get(Constants.DATA_STREAM_VALUE).getS();
					}else if(altDataKey.equals(list.get(i).get(Constants.DATA_STREAM_ID).getS())){
						alt = list.get(i).get(Constants.DATA_STREAM_VALUE).getS();
					}else if(speDataKey.equals(list.get(i).get(Constants.DATA_STREAM_ID).getS())){
						spe = list.get(i).get(Constants.DATA_STREAM_VALUE).getS();
					}
					//填充数据
					resultMap.put(Constants.LONGITUDE, lon);
					resultMap.put(Constants.LATITUDE, lat);
					resultMap.put(Constants.ALTITUDE, alt);
					resultMap.put(Constants.SPEED, spe);
					datas.put(createTime, resultMap);
				}else{
					//数据已经存入datas,将该数据 取出
					Map<String, String> existMap = (Map<String, String>) datas.get(createTime);
					if(lonDataKey.equals(list.get(i).get(Constants.DATA_STREAM_ID).getS())){
						lon = list.get(i).get(Constants.DATA_STREAM_VALUE).getS();
						existMap.put(Constants.LONGITUDE, lon);
					}else if(latDataKey.equals(list.get(i).get(Constants.DATA_STREAM_ID).getS())){
						lat = list.get(i).get(Constants.DATA_STREAM_VALUE).getS();
						existMap.put(Constants.LATITUDE, lat);
					}else if(altDataKey.equals(list.get(i).get(Constants.DATA_STREAM_ID).getS())){
						alt = list.get(i).get(Constants.DATA_STREAM_VALUE).getS();
						existMap.put(Constants.ALTITUDE, alt);
					}else if(speDataKey.equals(list.get(i).get(Constants.DATA_STREAM_ID).getS())){
						spe = list.get(i).get(Constants.DATA_STREAM_VALUE).getS();
						existMap.put(Constants.SPEED, spe);
					}
					datas.put(createTime, existMap);
				}
				
			}
			//排序

			context.getLogger().log("GetStreamFromInstance resultMap : " + datas);
			result.setData(datas);
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
