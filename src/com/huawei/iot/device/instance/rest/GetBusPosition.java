package com.huawei.iot.device.instance.rest;

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

public class GetBusPosition implements RequestHandler<Object, RestResult> {

	@Override
	public RestResult handleRequest(Object input, Context context) {
		// TODO Auto-generated method stub
		RestResult result = new RestResult();
		result.setCode(RestResult.FAILED);
		result.setDescription("failed to get datas");
		try {
			Map<String, Object> inputMap = (Map<String, Object>) input;
			
			//1.1 如果没有传入设备ID，使用最近连接的设备ID
			List<Map<String, AttributeValue>> diidList = DBOperater.scanItems(Constants.TBL_DIID, null, context).getItems();
			int maxID = 0;
			for(Map<String, AttributeValue> map: diidList){
				String id = map.get(Constants.DIID_ID).getS();
				if(Integer.valueOf(id) >= maxID)
				maxID = Integer.valueOf(id);
			}
			String deviceID = String.valueOf(maxID);
			context.getLogger().log("GetBusPosition, input map : " + inputMap+",deviceID: "+ deviceID);
			//1.2 如果传入设备ID， 使用传入的设备ID
			if(inputMap.containsKey(Constants.DEVICE_ID) && null!= inputMap.get(Constants.DEVICE_ID)){
				deviceID = (String) inputMap.get(Constants.DEVICE_ID);
			}
			context.getLogger().log("GetBusPosition, input map : " + inputMap+",deviceID: "+ deviceID);
			//2.通过设备ID取得当前设备值
			ScanResult scanR4DeviceInstance = DBOperater.getOneItem(Constants.TBL_DEVICE_INSTANCE, deviceID, context);
			Map<String,AttributeValue> diItem = scanR4DeviceInstance.getItems().get(0);
			
			String deviceName = diItem.get(Constants.DEVICE_NAME).getS();
			String lon = diItem.get(Constants.LONGITUDE).getS();
			String lat = diItem.get(Constants.LATITUDE).getS();
			String alt = diItem.get(Constants.ALTITUDE).getS();
			String spe = diItem.get(Constants.SPEED).getS();
			float speed  = Float.parseFloat(spe);
			//3. 返回的数据集
			Map<String, String> resultMap = new HashMap<String,String>();
			resultMap.put(Constants.LONGITUDE, lon);
			resultMap.put(Constants.LATITUDE, lat);
			resultMap.put(Constants.DEVICE_NAME, deviceName);

			context.getLogger().log("GetBusPosition, inputMap.get(Constants.LONGITUDE) : " + resultMap.get(Constants.LONGITUDE)
							+ ",inputMap.get(Constants.LATITUDE) : "+ resultMap.get(Constants.LATITUDE));
			
			//计算时间
			float   lonD   =   Float.parseFloat(lon);
			float   latD   =   Float.parseFloat(lat);
			double dis = getDistance(lonD, latD,
					Double.parseDouble(lon), Double.parseDouble(lat));
			context.getLogger().log("GetBusPosition, distance: " + dis);
			resultMap.put("Time", dis/speed+"");
			
			context.getLogger().log("Get data stream result : " + resultMap);
			result.setData(resultMap);
			result.setCode(RestResult.SUCCESS);
			result.setDescription("success to get datas");
		} catch (Exception e) {
			context.getLogger().log("Occur exception when scan result : " + e);
		}
		
		return result;
	}
	
	private static final double EARTH_RADIUS = 6378.137;//地球半径
	private double rad(double d)
	{
	   return d * Math.PI / 180.0;
	}

	private double getDistance(double lng1, double lat1,  double lng2, double lat2)
	{
	   double radLat1 = rad(lat1);
	   double radLat2 = rad(lat2);
	   double a = radLat1 - radLat2;
	   double b = rad(lng1) - rad(lng2);

	   double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
	    Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
	   s = s * EARTH_RADIUS;
	   s = Math.round(s * 10000) / 10000;
	   return s;
	}

}
