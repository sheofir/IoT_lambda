package com.huawei.iot.device.instance.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.huawei.iot.device.util.RestResult;

public class GetPositionTest implements RequestHandler<S3Event, String> {

	@Override
	public String handleRequest(S3Event input, Context context) {
		// TODO Auto-generated method stub
		String result = "<html><head>this is test head</head><body> This is test body !</body></html>";
		
		return result;
	}
	
	private static final double EARTH_RADIUS = 6378.137;//µØÇò°ë¾¶
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
