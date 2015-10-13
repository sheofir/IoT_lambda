package com.huawei.iot.device.stream.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.RestResult;
import com.huawei.iot.device.util.TransferDI2Result;

public class UploadDatas implements RequestHandler<Object, RestResult> {

	@Override
	public RestResult handleRequest(Object input, Context context) {
		LambdaLogger logger = context.getLogger();
        logger.log("log data from Lambda logger\n");
        
        RestResult result = new RestResult();
        result.setCode(RestResult.FAILED);
		result.setDescription("failed to add datas");
		try {
			//从输入中拿到设备数据
			Map<String, Object> inputMap = (Map<String, Object>) input;
			logger.log("input map: " + inputMap.toString() +"\n");
			
			//取得设备id,设备类型id
			String deviceID = (String) inputMap.get(Constants.DEVICE_ID);
			String deviceTypeID = (String) inputMap.get(Constants.DEVICE_TYPE_ID);
			
			//判断设备状态
			//设备处于poweroff或者restart状态等非online状态，返回
			String state = getDeviceState(deviceID, context);
			if(!Constants.DEVICE_STATUS_ONLINE.equals(state)){
				if(Constants.DEVICE_STATUS_OFFLINE.equals(state)
						|| Constants.DEVICE_STATUS_ENABLE.equals(state)){
					//如果离线状态， 将被更新为online
					updateState(deviceID, context, Constants.DEVICE_STATUS_ONLINE);
					
				}else if(Constants.DEVICE_STATUS_DISABLE.equals(state)){
					//data里返回“disable"
					result.setData(state);
					result.setDescription("The device is now disable");
					return result;
				}
				else{
					//如果非离线状态，将更新服务状态为online
					//此时可能状态为：restart, poweroff, fota etc.
					updateState(deviceID, context, Constants.DEVICE_STATUS_OFFLINE);
					result.setData(state);
					result.setDescription("The device is not online");
					return result;
				}
				
			}

			//开始上报数据，更新数据库
			//1. 构造数据id
			//data stream id: deviceTypeID_*_deviceID_*_DataStreamID_*
			String dsIDPrefix = new String(Constants.DEVICE_TYPE_ID+"_"+deviceTypeID
							+ "_" + Constants.DEVICE_ID + "_" + deviceID
							+ "_" + Constants.DATA_STREAM_ID);
			//2. 当前时间作为最后连接时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String now = sdf.format(new Date());
			
			//3. 构造最后连接时间和数据id
			String lastConnectTime = now;
			
			
			//4. 更新设备实例表：最后连接时间
			Map<String, Object> cdt = new HashMap<String,Object>();
			Map<String, Object> timeMap = new HashMap<String,Object>();
			cdt.put(Constants.CONDITION_KEY, Constants.DEVICE_ID);
			cdt.put(Constants.CONDITION_VALUE, deviceID);
			
			timeMap.put(Constants.LAST_CONNECT_TIME, lastConnectTime);
			timeMap.put(Constants.REMAIN_TIME, inputMap.get(Constants.REMAIN_TIME));
			timeMap.put(Constants.POSITION_ID, inputMap.get(Constants.POSITION_ID));
			UpdateItemResult uir = DBOperater.updateConnectTime(Constants.TBL_DEVICE_INSTANCE, cdt, timeMap, context);
			logger.log("Upload datas from instance, update item connect time: " + uir);
			
			//5. 新增数据表数据
			Map<String, String> dataMap = (Map<String, String>) inputMap.get(Constants.DATA_STREAM);
			Set<String> keySet = dataMap.keySet();
			Map<String, Object> dbMap = new HashMap<String, Object>();
			String streamID = null;
			String streamValue = null;
			PutItemResult pir = null;
			for(String key: keySet){
				if(Constants.LONGITUDE.equals(key)){
					streamID = dsIDPrefix + "_" + 1 + "_CreateTime_" + now;
				}else if(Constants.LATITUDE.equals(key)){
					streamID = dsIDPrefix + "_" + 2 + "_CreateTime_" + now;
				}else if(Constants.ALTITUDE.equals(key)){
					streamID = dsIDPrefix + "_" + 3 + "_CreateTime_" + now;
				}else if(Constants.SPEED.equals(key)){
					streamID = dsIDPrefix + "_" + 4 + "_CreateTime_" + now;
				}
				streamValue = dataMap.get(key);
				dbMap.put(Constants.DATA_STREAM_ID, streamID);
				dbMap.put(Constants.DATA_STREAM_VALUE, streamValue);
				dbMap.put(Constants.DATA_CREATE_TIME, now);
				
				pir = DBOperater.addOneItem(Constants.TBL_DATA_STREAM, dbMap, context);
				logger.log("UploadDatas put datastreams pir result : " + pir);
				
				//4.2 更新实时数据
				Map<String, Object> cdtCurrentValue = new HashMap<String,Object>();
				cdtCurrentValue.put(Constants.CONDITION_KEY, key);
				cdtCurrentValue.put(Constants.CONDITION_VALUE, streamValue);
				UpdateItemResult uirCurrentValue = DBOperater.updateDIItem(Constants.TBL_DEVICE_INSTANCE, cdt, cdtCurrentValue, context);
				logger.log("UploadDatas update current value result : " + uirCurrentValue);
			}
			
			ScanResult scanR = DBOperater.getOneItem(Constants.TBL_DEVICE_INSTANCE, deviceID, context);
			Map<String,AttributeValue> item = scanR.getItems().get(0);
			Map<String, Object> mapResult = TransferDI2Result.converFromDeviceInstance(item, context);
			result.setData(mapResult);
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to get datas");
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log("occur an exception e: " + e + "\n");
			return result;
		}
	}

	/**
	 * 将指定deviceID的设备实例状态恢复到online/offline
	 * 具体场景
	 * 1. 设备重启或掉电等操作时，状态被更新成offline
	 * 2. 设备重新连接上是，如果此时状态为offline，将被更新为online
	 * @param deviceID
	 * @param context
	 * @throws Exception
	 */
	private void updateState(String deviceID, Context context, String state) throws Exception {

		Map<String, Object> cdt = new HashMap<String,Object>();
		Map<String, Object> datas = new HashMap<String,Object>();
		cdt.put(Constants.CONDITION_KEY, Constants.DEVICE_ID);
		cdt.put(Constants.CONDITION_VALUE, deviceID);
		
		datas.put(Constants.DEVICE_STATUS, state);
		
		UpdateItemResult updateR = DBOperater.updateOneItem(Constants.TBL_DEVICE_INSTANCE, cdt, datas, context);
		context.getLogger().log("updateState， update item result : " + updateR);
	}

	private String getDeviceState(String deviceID, Context context) throws Exception {

		ScanResult scanR = DBOperater.getOneItem(Constants.TBL_DEVICE_INSTANCE, deviceID, context);
		context.getLogger().log("getDeviceState scan result: " + scanR);
		Map<String, AttributeValue> map = scanR.getItems().get(0);
		String state = map.get(Constants.DEVICE_STATUS).getS();
		context.getLogger().log("getDeviceState state: " + state);
		return state;
	}

}
