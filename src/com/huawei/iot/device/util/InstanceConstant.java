package com.huawei.iot.device.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.huawei.iot.device.db.Constants;

public class InstanceConstant {

	public static Map<String, Object> InstanceFromDevice(Map<String, Object> deviceMap, Map<String, Object> dtMap){
		Map<String, Object> mapResult = new HashMap<String, Object>();

		mapResult.put(Constants.DEVICE_ID, deviceMap.get(Constants.DEVICE_ID));
		mapResult.put(Constants.DEVICE_NAME, deviceMap.get(Constants.DEVICE_NAME));
		mapResult.put(Constants.DEVICE_TYPE_ID, deviceMap.get(Constants.DEVICE_TYPE_ID));
		mapResult.put(Constants.MASTER_KEY, deviceMap.get(Constants.MASTER_KEY));
		mapResult.put(Constants.SIM_CARD_ID, deviceMap.get(Constants.SIM_CARD_ID));
		mapResult.put(Constants.DEVICE_TYPE_NAME, dtMap.get(Constants.DEVICE_TYPE_NAME));
		if(deviceMap.containsKey(Constants.BUSLINE_NUM)){
			mapResult.put(Constants.BUSLINE_NUM, deviceMap.get(Constants.BUSLINE_NUM));
		}
		if(deviceMap.containsKey(Constants.REMAIN_TIME)){
			mapResult.put(Constants.REMAIN_TIME, deviceMap.get(Constants.REMAIN_TIME));
		}
		if(deviceMap.containsKey(Constants.POSITION_ID)){
			mapResult.put(Constants.POSITION_ID, deviceMap.get(Constants.POSITION_ID));
		}
		
		//设备添加时，当前时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String now = sdf.format(new Date());
		mapResult.put(Constants.FIRST_CONNECT_TIME, now);
		mapResult.put(Constants.LAST_CONNECT_TIME, now);
		
		//data stream id: deviceTypeID_*_deviceID_*_DataStreamID_*
		String dsIDPrefix = new String(Constants.DEVICE_TYPE_ID+"_"+deviceMap.get(Constants.DEVICE_TYPE_ID)
						+ "_" + Constants.DEVICE_ID + "_" + deviceMap.get(Constants.DEVICE_ID)
						+ "_" + Constants.DATA_STREAM_ID);
		
		//设备添加时，状态online
		mapResult.put(Constants.DEVICE_STATUS, Constants.DEVICE_STATUS_ONLINE);
		
		//设备操作同步到设备实例中
		if(dtMap.containsKey(Constants.OPERATION)){
			mapResult.put(Constants.OPERATION, dtMap.get(Constants.OPERATION));	
		}
		if(dtMap.containsKey(Constants.PLATFORM)){
			mapResult.put(Constants.PLATFORM, dtMap.get(Constants.PLATFORM));
		}
		//从设备类型处获取数据
		Map<String, Object> dataStreams = new HashMap<String,Object>();
		Map<String, Object> dtStreams = (Map<String, Object>) dtMap.get(Constants.DATA_STREAMS);
		Map<String, Object> dataStream = new HashMap<String, Object>();
		for(int i=0;i<dtStreams.size();i++){
			Map<String, Object> data = (Map<String, Object>) dtStreams.get(Constants.DATA_STREAM + (i+1));
			//添加设备dataStreamID
			data.put(Constants.DATA_STREAM_ID, new String(dsIDPrefix + "_" + (i+1)));
			dataStream.put(Constants.DATA_STREAM + (i+1), data);
		}
		mapResult.put(Constants.DATA_STREAMS, dataStream);
		
		return mapResult;
	}
}
