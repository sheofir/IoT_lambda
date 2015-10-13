package com.huawei.iot.device.util;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.huawei.iot.device.db.Constants;

public class TransferDT2Result {

	public static Map<String, Object> converFromDeviceType(Map<String, AttributeValue> map, Context context) {
		
		Map<String, Object> mapResult = new HashMap<String, Object>();
		context.getLogger().log("converFromDeviceType map : " + map);
		mapResult.put(Constants.DEVICE_TYPE_ID, map.get(Constants.DEVICE_TYPE_ID).getS());
		mapResult.put(Constants.MASTER_KEY, map.get(Constants.MASTER_KEY).getS());
		mapResult.put(Constants.DESCRIPTION, map.get(Constants.DESCRIPTION).getS());
		mapResult.put(Constants.PLATFORM, map.get(Constants.PLATFORM).getS());
		mapResult.put(Constants.MANAGEMENT_PROTOCOL, map.get(Constants.MANAGEMENT_PROTOCOL).getS());
		mapResult.put(Constants.SERVICE_PROTOCOL, map.get(Constants.SERVICE_PROTOCOL).getS());
		mapResult.put(Constants.STATUS, map.get(Constants.STATUS).getS());
		mapResult.put(Constants.DEVICE_TYPE_ID, map.get(Constants.DEVICE_TYPE_ID).getS());
		mapResult.put(Constants.DEVICE_TYPE_NAME, map.get(Constants.DEVICE_TYPE_NAME).getS());

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
			Map<String, AttributeValue> joMap = map.get(Constants.DATA_STREAMS).getM();// (Map<String,
																						// Object>)
																						// json.getJSONArray(Constants.DATA_STREAMS).get(0);
			context.getLogger().log("Transfer2Result joMap : " + joMap.toString());
			for (int i = 1; i < joMap.size() + 1; i++) {
				Map<String, Object> datastream = new HashMap<String, Object>();
				AttributeValue dataStreamDisplayName = joMap.get(Constants.DATA_STREAM + (i + "")).getM().get(Constants.DATA_STREAM_DISPLAY_NAME);
				AttributeValue dataStreamName = joMap.get(Constants.DATA_STREAM + (i + "")).getM().get(Constants.DATA_STREAM_NAME);
				AttributeValue dataStreamType = joMap.get(Constants.DATA_STREAM + (i + "")).getM().get(Constants.DATA_STREAM_TYPE);
				AttributeValue dataStreamUnit = joMap.get(Constants.DATA_STREAM + (i + "")).getM().get(Constants.DATA_STREAM_UNIT);
				
				datastream.put(Constants.DATA_STREAM_NAME, dataStreamName.getS());
				datastream.put(Constants.DATA_STREAM_DISPLAY_NAME, dataStreamDisplayName.getS());
				datastream.put(Constants.DATA_STREAM_TYPE, dataStreamType.getS());
				datastream.put(Constants.DATA_STREAM_UNIT, dataStreamUnit.getS());
				datastreams.put(Constants.DATA_STREAM + (i + ""), datastream);
			}

			context.getLogger().log("Transfer2Result datastream : " + datastreams.toString());
			mapResult.put(Constants.DATA_STREAMS, datastreams);
		}

		return mapResult;

	}
}
