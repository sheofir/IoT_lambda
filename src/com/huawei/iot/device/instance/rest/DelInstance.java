package com.huawei.iot.device.instance.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.huawei.iot.device.db.Constants;
import com.huawei.iot.device.db.DBOperater;
import com.huawei.iot.device.util.MD5Util;
import com.huawei.iot.device.util.RestResult;

public class DelInstance implements RequestHandler<Object, RestResult> {


	@Override
	public RestResult handleRequest(Object input, Context context) {
		LambdaLogger logger = context.getLogger();
        logger.log("log data from Lambda logger\n");
        
        RestResult result = new RestResult();
		result.setCode(RestResult.FAILED);
		result.setDescription("failed to del datas");
		try {
			testDelItem(input, context);
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to del datas");
			logger.log("result : " + result.toString() + "\n");
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log("occur an exception e: " + e + "\n");
		}
        return result;
        
	}
	
	private void testDelItem(Object input, Context context) throws Exception{
		
		String tbl = Constants.TBL_DEVICE_INSTANCE;
		Map<String, Object> map = (Map<String, Object>) input;
		context.getLogger().log("primary key id: " + map.get(Constants.TBL_DEVICE_INSTANCE));
		List<Map<String,String>> lists = (List<Map<String, String>>) map.get(Constants.TBL_DEVICE_INSTANCE);
		List<String> diidDelList = new ArrayList<String>();
		List<String> list = new ArrayList<String>();
		for(Map<String,String> m: lists){
			list.add(m.get(Constants.DEVICE_ID));
			diidDelList.add(m.get(Constants.DEVICE_ID));
		}
		DBOperater.batchDelItem(tbl, list, context);
		//删除设备实例同时更新设备实例ID表
		List<Map<String, AttributeValue>> diidList = DBOperater.scanItems(Constants.TBL_DIID, null, context).getItems();
		List<String> delIdList = new ArrayList<String>();
		for(Map<String, AttributeValue> dtidMap: diidList){
			String id = dtidMap.get(Constants.DIID_ID).getS();
			for(String s: diidDelList){
				if(MD5Util.string2MD5(id).equals(s)){
					delIdList.add(id);
				}
			}
		}
		DBOperater.batchDelItem(Constants.TBL_DIID, delIdList, context);
	}
}
