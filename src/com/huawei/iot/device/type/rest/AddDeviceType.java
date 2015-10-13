package com.huawei.iot.device.type.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.type.service.DeviceTpyeTemplate;
import com.huawei.iot.device.util.MD5Util;
import com.huawei.iot.device.util.RestResult;

public class AddDeviceType implements RequestHandler<Object, RestResult> {

	private static final String templateKey = "TemplatePath";
	private static final String templatePath = "https://gyuk1oqrw9.execute-api.eu-west-1.amazonaws.com/rest/templatepath";
	@Override
	public RestResult handleRequest(Object input, Context context) {
		LambdaLogger logger = context.getLogger();
        logger.log("log data from Lambda logger\n");
        
        RestResult result = new RestResult();
        result.setCode(RestResult.FAILED);
		result.setDescription("failed to add datas");
		try {
			Map<String, String> map = (Map<String, String>) input;
			logger.log("input map: " + map.toString() +"\n");
			String  tPath = map.get(templateKey);
			if(!templatePath.equals(tPath)){
				return result;
			}
			String newID = getNewDTID(context);
			logger.log("newID : " + newID + "\n");
			ScanResult scanR = addItem(newID, input, context);
			result.setData(scanR);
			logger.log("result : " + result + "\n");
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to get datas");
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log("occur an exception e: " + e + "\n");
		}
		return result;
        
	}
	
	private String getNewDTID(Context context){
		String newID = null;
		try {
			//取得diid表中最大值
			List<Map<String, AttributeValue>> dtidList = DBOperater.scanItems(Constants.TBL_DTID, null, context).getItems();
			int maxID = 0;
			if(dtidList.size() > 0){
				for(Map<String, AttributeValue> map: dtidList){
					String id = map.get(Constants.DTID_ID).getS();
					if(Integer.valueOf(id) >= maxID)
					maxID = Integer.valueOf(id);
				}
			}
			
			context.getLogger().log("DTID maxID :" + maxID);
			newID = (maxID +1) + "";
			
			Map<String,Object> map = new HashMap<String,Object>();
			map.put(Constants.DTID_ID, newID);
			map.put(Constants.DTID_MASTER_KEY, newID);
			
			PutItemResult pir = DBOperater.addOneItem(Constants.TBL_DTID, map, context);
			context.getLogger().log("insert new DTID item, putItemResult : "+ pir);
			return MD5Util.string2MD5(newID);
		} catch (Exception e) {
			context.getLogger().log("getNew dtid exception, e: " + e);
		}
		
		return newID;
	}
	
	private ScanResult addItem(String newDTID, Object obj, Context context) throws Exception{
		String tbl = Constants.TBL_DEVICE_TYPE;
		Map<String, Object> map = DeviceTpyeTemplate.getTemplateFromS3();
		map.put(Constants.DEVICE_TYPE_ID, newDTID);
		map.put(Constants.MASTER_KEY, newDTID);
		map.put(Constants.STATUS, Constants.STATUS_ENABLE);
		/*Map<String, Object> map = new HashMap<String, Object>();
		context.getLogger().log("obj json : " + map.toString());
		map.put(Constants.DEVICE_TYPE_ID, newDTID);
		map.put(Constants.MASTER_KEY, newDTID);
		map.put(Constants.DEVICE_TYPE_NAME, "AR511GW-1");
		map.put(Constants.DESCRIPTION, "Vehicle mobile terminal gateway, up 2G/3G/4G, down WIFI");
		map.put(Constants.PLATFORM, "Android5.0");
		map.put(Constants.SERVICE_PROTOCOL, "CoAP");
		map.put(Constants.MANAGEMENT_PROTOCOL, "LWM2M");
		map.put(Constants.STATUS, Constants.STATUS_ENABLE);
		
		Map<String,Object> datas = new HashMap<String,Object>();
		Map<String,Object> data1 = new HashMap<String,Object>();
		Map<String,Object> data2 = new HashMap<String,Object>();
		Map<String,Object> data3 = new HashMap<String,Object>();
		Map<String,Object> data4 = new HashMap<String,Object>();
		data1.put(Constants.DATA_STREAM_NAME, "Longitude");
		data1.put(Constants.DATA_STREAM_DISPLAY_NAME, "Longitude");
		data1.put(Constants.DATA_STREAM_TYPE, "Numeric");
		data1.put(Constants.DATA_STREAM_UNIT, "E/W");
		
		data2.put(Constants.DATA_STREAM_NAME, "Latitude");
		data2.put(Constants.DATA_STREAM_DISPLAY_NAME, "Latitude");
		data2.put(Constants.DATA_STREAM_TYPE, "Numeric");
		data2.put(Constants.DATA_STREAM_UNIT, "N/S");
		
		data3.put(Constants.DATA_STREAM_NAME, "Altitude");
		data3.put(Constants.DATA_STREAM_DISPLAY_NAME, "Altitude");
		data3.put(Constants.DATA_STREAM_TYPE, "Numeric");
		data3.put(Constants.DATA_STREAM_UNIT, "m");
		
		data4.put(Constants.DATA_STREAM_NAME, "Speed");
		data4.put(Constants.DATA_STREAM_DISPLAY_NAME, "Speed");
		data4.put(Constants.DATA_STREAM_TYPE, "Numeric");
		data4.put(Constants.DATA_STREAM_UNIT, "km/h");
		
		datas.put(Constants.DATA_STREAM + "1", data1);
		datas.put(Constants.DATA_STREAM + "2", data2);
		datas.put(Constants.DATA_STREAM + "3", data3);
		datas.put(Constants.DATA_STREAM + "4", data4);
		map.put(Constants.DATA_STREAMS, datas);*/
		
		context.getLogger().log("new map : " + map.toString());
		
		DBOperater.addOneItem(tbl, map, context);
		
		return DBOperater.getOneItem(tbl, (String) map.get(Constants.DEVICE_TYPE_ID), context);
	}
	
	//for text add item
/*	private String testAddItem(Object obj, Context context) throws Exception{
		
		String tbl = Constants.TBL_DEVICE_TYPE;
		
		JSONObject json = new JSONObject(obj.toString());
		context.getLogger().log(json.toString());
		
		DBOperater.addOneItem(tbl, json, context).toString();
		
		return DBOperater.getOneItem(tbl, json.getString(Constants.DEVICE_TYPE_ID), context).toString();
	}*/
}
