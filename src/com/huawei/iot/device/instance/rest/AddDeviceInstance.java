package com.huawei.iot.device.instance.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.InstanceConstant;
import com.huawei.iot.device.util.MD5Util;
import com.huawei.iot.device.util.RestResult;
import com.huawei.iot.device.util.TransferDI2Result;
import com.huawei.iot.device.util.TransferDT2Result;

public class AddDeviceInstance implements RequestHandler<Object, RestResult> {

	@Override
	public RestResult handleRequest(Object input, Context context) {
		LambdaLogger logger = context.getLogger();
        logger.log("log data from Lambda logger\n");
        
        RestResult result = new RestResult();
        result.setCode(RestResult.FAILED);
		result.setDescription("failed to add datas");
		try {
			//从输入中拿到设备数据
			Map<String, Object> map = (Map<String, Object>) input;
			
			//从数据库中拿到设备类型数据
			String deviceTypeID = (String) map.get(Constants.DEVICE_TYPE_ID);
			//如果不存在该设备类型，返回failed
			if(!hasDeviceType(deviceTypeID, context)){
				result.setData(new String(Constants.NO_TYPE));
				result.setDescription("The device type isn't added.");
				return result;
			}
			
			//如果设备已经加入，返回成功，开始上传数据
			String simID = (String) map.get(Constants.SIM_CARD_ID);
			String temDeviceID = hasInstance(simID, context);
			if(null != temDeviceID){
				result.setCode(RestResult.SUCCESS);
				result.setData(new String(Constants.ALREADY_EXIST));
				result.setDescription(temDeviceID);
				return result;
			}
			
			map.put(Constants.DEVICE_ID, getNewDTID(context));
			logger.log("input map: " + map.toString() +"\n");


			Map<String, Object> dtMap = getDeviceType(deviceTypeID, context);
			logger.log("dtMap: " + dtMap.toString() +"\n");
			
			//整合需要的数据
			Map<String, Object> instanceDatas = InstanceConstant.InstanceFromDevice(map, dtMap);
			logger.log("AddDeviceInstance instanceDatas 1 : " + instanceDatas + "\n");
			//添加设备实例到数据库
			ScanResult scanR = add2DB(instanceDatas, context);
			//查询该添加数据，并转化成前台需要格式
			Map<String, Object> mapResult = TransferDI2Result.converFromDeviceInstance(scanR.getItems().get(0), context);
			result.setData(mapResult);
			logger.log("result : " + result + "\n");
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to add device");
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log("occur an exception e: " + e + "\n");
			return result;
		}
	}
	
	private boolean hasDeviceType(String deviceTypeID, Context context) throws Exception {
		String tbl = Constants.TBL_DEVICE_TYPE;
		
		ScanResult result = DBOperater.getOneItem(tbl, deviceTypeID, context);
		context.getLogger().log("result of device type result: " + result);
		if(result.getCount() != 0){
			return true;
		}
		return false;
	}

	/**
	 * 判断设备实例中已经添加过该设备,如果有返回deviceID
	 * @return true : for has this instance; false : no this instance
	 * @throws Exception 
	 */
	private String hasInstance(String simID, Context context) throws Exception{
		ScanResult scanR = DBOperater.scanItems(Constants.TBL_DEVICE_INSTANCE, null, context);
		
		List<Map<String, Object>> listResult = new ArrayList();
		List<Map<String,AttributeValue>> listItems = scanR.getItems();
		context.getLogger().log("hasInstance, listItems: " + listItems);
		for(Map<String,AttributeValue> m: listItems){
			if(simID.equals(m.get(Constants.SIM_CARD_ID).getS())){
				return m.get(Constants.DEVICE_ID).getS();
			}
		}
		return null;
	}
	
	private String getNewDTID(Context context){
		String newID = null;
		try {
			//取得diid表中最大值
			List<Map<String, AttributeValue>> diidList = DBOperater.scanItems(Constants.TBL_DIID, null, context).getItems();
			int maxID = 0;
			if(diidList.size()>0){
				for(Map<String, AttributeValue> map: diidList){
					String id = map.get(Constants.DIID_ID).getS();
					if(Integer.valueOf(id) >= maxID)
					maxID = Integer.valueOf(id);
				}
			}
			
			context.getLogger().log("DIID_ID maxID :" + maxID);
			newID = (maxID +1) + "";
			
			Map<String,Object> map = new HashMap<String,Object>();
			map.put(Constants.DIID_ID, newID);
			
			PutItemResult pir = DBOperater.addOneItem(Constants.TBL_DIID, map, context);
			context.getLogger().log("insert new DIID_ID item, putItemResult : "+ pir);
			return MD5Util.string2MD5(newID);
		} catch (Exception e) {
			context.getLogger().log("getNew dtid exception, e: " + e);
		}
		
		return newID;
	}
	
	private Map<String, Object> getDeviceType(String deviceTYpeID, Context context) throws Exception{
		ScanResult sr = DBOperater.getOneItem(Constants.TBL_DEVICE_TYPE, deviceTYpeID, context);
		Map<String, Object> dtMap = TransferDT2Result.converFromDeviceType(sr.getItems().get(0), context);
		return dtMap;
	}
	
	private ScanResult add2DB(Map<String, Object> map, Context context) throws Exception{
		PutItemResult pir = DBOperater.addOneItem(Constants.TBL_DEVICE_INSTANCE, map, context);
		ScanResult sr = DBOperater.getOneItem(Constants.TBL_DEVICE_INSTANCE, (String) map.get(Constants.DEVICE_ID), context);
		return sr;
	}
}
