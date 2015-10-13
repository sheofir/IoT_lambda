package com.huawei.iot.device.util;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.huawei.iot.device.db.Constants;

public class TransferDI2Result {

	public static Map<String, Object> converFromDeviceInstance(Map<String, AttributeValue> map, Context context) {
		
		Map<String, Object> mapResult = new HashMap<String, Object>();
		mapResult.put(Constants.DEVICE_ID, map.get(Constants.DEVICE_ID).getS());
		
		context.getLogger().log("converFromDeviceInstance db map : " + map);
		mapResult.put(Constants.DEVICE_NAME, map.get(Constants.DEVICE_NAME).getS());
		mapResult.put(Constants.DEVICE_TYPE_ID, map.get(Constants.DEVICE_TYPE_ID).getS());
		mapResult.put(Constants.DEVICE_TYPE_NAME, map.get(Constants.DEVICE_TYPE_NAME).getS());
		mapResult.put(Constants.SIM_CARD_ID, map.get(Constants.SIM_CARD_ID).getS());
		mapResult.put(Constants.FIRST_CONNECT_TIME, map.get(Constants.FIRST_CONNECT_TIME).getS());
		mapResult.put(Constants.LAST_CONNECT_TIME, map.get(Constants.LAST_CONNECT_TIME).getS());
		mapResult.put(Constants.DEVICE_STATUS, map.get(Constants.DEVICE_STATUS).getS());
		mapResult.put(Constants.MASTER_KEY, map.get(Constants.MASTER_KEY).getS());
		if(map.containsKey(Constants.PLATFORM)){
			mapResult.put(Constants.PLATFORM, map.get(Constants.PLATFORM).getS());
		}
		if(map.containsKey(Constants.BUSLINE_NUM)){
			mapResult.put(Constants.BUSLINE_NUM, map.get(Constants.BUSLINE_NUM).getS());
		}
		if(map.containsKey(Constants.REMAIN_TIME)){
			mapResult.put(Constants.REMAIN_TIME, map.get(Constants.REMAIN_TIME).getS());
		}
		if(map.containsKey(Constants.POSITION_ID)){
			mapResult.put(Constants.POSITION_ID, map.get(Constants.POSITION_ID).getS());
		}
		
		context.getLogger().log("converFromDeviceInstance STATUS : " + map.get(Constants.DEVICE_STATUS));
		
		//½âÎö²Ù×÷
		if(map.containsKey(Constants.OPERATION)){
			Map<String, Object> operMap = new HashMap<String, Object>();
			Map<String, AttributeValue> opMap = (Map<String, AttributeValue>) map.get(Constants.OPERATION).getM();
			operMap.put(Constants.OPERATION_RESTART, opMap.get(Constants.OPERATION_RESTART).getS());
			operMap.put(Constants.OPERATION_POWEROFF, opMap.get(Constants.OPERATION_POWEROFF).getS());
			operMap.put(Constants.OPERATION_FOTA, opMap.get(Constants.OPERATION_FOTA).getS());
			operMap.put(Constants.OPERATION_DIAGNOSIS, opMap.get(Constants.OPERATION_DIAGNOSIS).getS());
			operMap.put(Constants.OPERATION_CONFIG, opMap.get(Constants.OPERATION_CONFIG).getS());
			
			mapResult.put(Constants.OPERATION, operMap);
		}
		Map<String, Object> datastreams = new HashMap<String, Object>();

		if (map.containsKey(Constants.DATA_STREAMS)) {
			Map<String, AttributeValue> joMap = map.get(Constants.DATA_STREAMS).getM();
			context.getLogger().log("Transfer2Result joMap : " + joMap.toString());
			for (int i = 1; i < joMap.size() + 1; i++) {
				Map<String, Object> datastream = new HashMap<String, Object>();
				AttributeValue dataStreamDisplayName = joMap.get(Constants.DATA_STREAM + (i + "")).getM().get(Constants.DATA_STREAM_DISPLAY_NAME);
				AttributeValue dataStreamName = joMap.get(Constants.DATA_STREAM + (i + "")).getM().get(Constants.DATA_STREAM_NAME);
				AttributeValue dataStreamType = joMap.get(Constants.DATA_STREAM + (i + "")).getM().get(Constants.DATA_STREAM_TYPE);
				AttributeValue dataStreamUnit = joMap.get(Constants.DATA_STREAM + (i + "")).getM().get(Constants.DATA_STREAM_UNIT);
				AttributeValue dataStreamId = joMap.get(Constants.DATA_STREAM + (i + "")).getM().get(Constants.DATA_STREAM_ID);
				
				datastream.put(Constants.DATA_STREAM_NAME, dataStreamName.getS());
				datastream.put(Constants.DATA_STREAM_DISPLAY_NAME, dataStreamDisplayName.getS());
				datastream.put(Constants.DATA_STREAM_TYPE, dataStreamType.getS());
				datastream.put(Constants.DATA_STREAM_UNIT, dataStreamUnit.getS());
				datastream.put(Constants.DATA_STREAM_ID, dataStreamId.getS());
				datastreams.put(Constants.DATA_STREAM + (i + ""), datastream);
			}


			mapResult.put(Constants.DATA_STREAMS, datastreams);
		}
		context.getLogger().log("Transfer2Result mapResult : " + mapResult.toString());
		return mapResult;

	}
}
