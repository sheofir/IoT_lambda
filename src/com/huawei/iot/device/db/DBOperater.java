package com.huawei.iot.device.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.dynamodbv2.util.Tables;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class DBOperater {

	static AmazonDynamoDBClient dynamoDB;

	private static void init(Context context) throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [default] credential
		 * profile by reading from the credentials file located at
		 * (C:\\Users\\sylar\\.aws\\credentials).
		 */
		/*
		 * 去除鉴权 AWSCredentials credentials = null; try { credentials = new
		 * ProfileCredentialsProvider("default").getCredentials(); } catch
		 * (Exception e) { throw new AmazonClientException(
		 * "Cannot load the credentials from the credential profiles file. " +
		 * "Please make sure that your credentials file is at the correct " +
		 * "location (C:\\Users\\sylar\\.aws\\credentials), and is in valid format."
		 * , e); }
		 */
		dynamoDB = new AmazonDynamoDBClient();
		// 使用Tokyo服务器
		Region tokyo = Region.getRegion(Regions.AP_SOUTHEAST_1);
		dynamoDB.setRegion(tokyo);
		context.getLogger().log("exit initting region function");
	}

	/**
	 * 
	 * @param tableName
	 *            : 表名
	 * @param condition
	 *            : 包含conditionKey和conditionValue
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static ScanResult scanItems(String tableName, JSONObject condition, Context context) throws Exception {
		init(context);
		try {
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// 没有对应数据表
				return null;
			}

			ScanRequest scanRequest = new ScanRequest(tableName);
			if (null != condition) {
				// Scan items for movies with a year attribute greater than 1985
				HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
				Condition con = new Condition().withComparisonOperator(ComparisonOperator.EQ.toString())
						.withAttributeValueList(new AttributeValue().withS(condition.getString(Constants.CONDITION_VALUE)));
				scanFilter.put(condition.getString(Constants.CONDITION_KEY), con);
				context.getLogger().log("scan filter : " + scanFilter.toString());
				scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
			}
			ScanResult scanResult = dynamoDB.scan(scanRequest);
			context.getLogger().log("scanResult: " + scanResult);
			return scanResult;
		} catch (AmazonServiceException ase) {
			context.getLogger().log("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			context.getLogger().log("Error Message:    " + ase);
		} catch (AmazonClientException ace) {
			context.getLogger()
					.log("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with AWS, "
							+ "such as not being able to access the network.");
		}
		return null;
	}

	public static UpdateItemResult updateOneItem(String tableName, Map<String, Object> json, Map<String,Object> datas, Context context)
			throws Exception {
		init(context);
		try {
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// 没有对应数据表
				return null;
			}

			UpdateItemRequest updateItemRequest = new UpdateItemRequest();
			updateItemRequest.withTableName(tableName)
							.addKeyEntry((String) json.get(Constants.CONDITION_KEY), new AttributeValue((String) json.get(Constants.CONDITION_VALUE)))
							.addAttributeUpdatesEntry(Constants.STATUS, new AttributeValueUpdate(new AttributeValue((String) datas.get(Constants.STATUS)), AttributeAction.PUT));
			UpdateItemResult updateItemResult = dynamoDB.updateItem(updateItemRequest);
			context.getLogger().log("updateItemResult: " + updateItemResult);
			return updateItemResult;
		} catch (AmazonServiceException ase) {
			context.getLogger().log("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			context.getLogger().log("Error Message:    " + ase);
		} catch (AmazonClientException ace) {
			context.getLogger()
					.log("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with AWS, "
							+ "such as not being able to access the network.");
		}
		return null;
	}
	
	public static UpdateItemResult updateDIItem(String tableName, Map<String, Object> keyMap, Map<String,Object> datas, Context context)
			throws Exception {
		init(context);
		try {
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// 没有对应数据表
				return null;
			}

			UpdateItemRequest updateItemRequest = new UpdateItemRequest();
			updateItemRequest.withTableName(tableName)
							.addKeyEntry((String) keyMap.get(Constants.CONDITION_KEY), new AttributeValue((String) keyMap.get(Constants.CONDITION_VALUE)))
							.addAttributeUpdatesEntry((String) datas.get(Constants.CONDITION_KEY), 
									new AttributeValueUpdate(new AttributeValue((String) datas.get(Constants.CONDITION_VALUE)), AttributeAction.PUT));
			UpdateItemResult updateItemResult = dynamoDB.updateItem(updateItemRequest);
			context.getLogger().log("updateItemResult: " + updateItemResult);
			return updateItemResult;
		} catch (AmazonServiceException ase) {
			context.getLogger().log("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			context.getLogger().log("Error Message:    " + ase);
		} catch (AmazonClientException ace) {
			context.getLogger()
					.log("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with AWS, "
							+ "such as not being able to access the network.");
		}
		return null;
	}
	
	public static UpdateItemResult updateConnectTime(String tableName, Map<String, Object> json, Map<String,Object> timeMap, Context context)
			throws Exception {
		init(context);
		try {
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// 没有对应数据表
				return null;
			}

			UpdateItemRequest updateItemRequest = new UpdateItemRequest();
			updateItemRequest.withTableName(tableName)
							.addKeyEntry((String) json.get(Constants.CONDITION_KEY), new AttributeValue((String) json.get(Constants.CONDITION_VALUE)))
							.addAttributeUpdatesEntry(Constants.LAST_CONNECT_TIME, new AttributeValueUpdate(new AttributeValue((String) timeMap.get(Constants.LAST_CONNECT_TIME)), AttributeAction.PUT))
			.addAttributeUpdatesEntry(Constants.REMAIN_TIME, new AttributeValueUpdate(new AttributeValue((String) timeMap.get(Constants.REMAIN_TIME)), AttributeAction.PUT))
			.addAttributeUpdatesEntry(Constants.POSITION_ID, new AttributeValueUpdate(new AttributeValue((String) timeMap.get(Constants.POSITION_ID)), AttributeAction.PUT));

			UpdateItemResult updateItemResult = dynamoDB.updateItem(updateItemRequest);
			context.getLogger().log("updateItemResult: " + updateItemResult);
			return updateItemResult;
		} catch (AmazonServiceException ase) {
			context.getLogger().log("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			context.getLogger().log("Error Message:    " + ase);
		} catch (AmazonClientException ace) {
			context.getLogger()
					.log("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with AWS, "
							+ "such as not being able to access the network.");
		}
		return null;
	}
	
	public static PutItemResult addOneItem(String tableName, Map<String, Object> json, Context context)
			throws Exception {
		init(context);
		try {
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// 没有对应数据表
				return null;
			}

			// add an item
			Map<String, AttributeValue> item = null;
			if (Constants.TBL_DEVICE_TYPE.equals(tableName)) {
				item = newDeviceTypeItem(json, Constants.operate_add_item, context);
			} else if (Constants.TBL_DTID.equals(tableName)) {
				item = newDTIDItem(json, Constants.operate_add_item, context);
			} else if (Constants.TBL_DEVICE_INSTANCE.equals(tableName)){
				item = newInstanceItem(json,  Constants.operate_add_item, context);
			} else if(Constants.TBL_DIID.equals(tableName)){
				item = newDIIDItem(json, Constants.operate_add_item, context);
			}else if(Constants.TBL_DATA_STREAM.equals(tableName)){
				context.getLogger().log("addOneItem, db map: " + json);
				item = newStreamItem(json, Constants.operate_add_item, context);
			}
			context.getLogger().log("item: " + item);
			PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
			PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
			context.getLogger().log("putItemResult: " + putItemResult);
			return putItemResult;
		} catch (AmazonServiceException ase) {
			context.getLogger().log("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			context.getLogger().log("Error Message:    " + ase);
		} catch (AmazonClientException ace) {
			context.getLogger()
					.log("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with AWS, "
							+ "such as not being able to access the network.");
		}
		return null;
	}

	private static Map<String, AttributeValue> newStreamItem(Map<String, Object> json, int operateAddItem,
			Context context) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		if (Constants.operate_add_item == operateAddItem) {
			item.put(Constants.DATA_STREAM_ID, new AttributeValue((String) json.get(Constants.DATA_STREAM_ID)));
			item.put(Constants.DATA_STREAM_VALUE, new AttributeValue((String) json.get(Constants.DATA_STREAM_VALUE)));
			if(json.containsKey(Constants.DATA_CREATE_TIME)){
				item.put(Constants.DATA_CREATE_TIME, new AttributeValue((String) json.get(Constants.DATA_CREATE_TIME)));
			}
		}
		return item;
	}

	private static Map<String, AttributeValue> newDIIDItem(Map<String, Object> json, int operateAddItem,
			Context context) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		if (Constants.operate_add_item == operateAddItem) {
			item.put(Constants.DIID_ID, new AttributeValue((String) json.get(Constants.DIID_ID)));
		}
		return item;
	}

	private static Map<String, AttributeValue> newInstanceItem(Map<String, Object> map, int operate,
			Context context) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		if (operate == Constants.operate_add_item) {
			context.getLogger().log("from input : type id: " + map.get(Constants.DEVICE_TYPE_ID) + "MASTER_KEY: "
					+ map.get(Constants.MASTER_KEY) + "DEVICE_ID: " + map.get(Constants.DEVICE_ID)
					+ "DEVICE_NAME: " + map.get(Constants.DEVICE_NAME) + "STATUS: "
					+ map.get(Constants.DEVICE_STATUS) + "FIRST_CONNECT_TIME: " + map.get(Constants.FIRST_CONNECT_TIME)
					+ "LAST_CONNECT_TIME: " + map.get(Constants.LAST_CONNECT_TIME));
			item.put(Constants.DEVICE_ID, new AttributeValue((String) map.get(Constants.DEVICE_ID)));
			item.put(Constants.DEVICE_NAME, new AttributeValue((String) map.get(Constants.DEVICE_NAME)));
			item.put(Constants.DEVICE_TYPE_ID,
					new AttributeValue((String) map.get(Constants.DEVICE_TYPE_ID)));
			item.put(Constants.DEVICE_TYPE_NAME,
					new AttributeValue((String) map.get(Constants.DEVICE_TYPE_NAME)));
			item.put(Constants.MASTER_KEY, new AttributeValue((String) map.get(Constants.MASTER_KEY)));
			item.put(Constants.SIM_CARD_ID, new AttributeValue((String) map.get(Constants.SIM_CARD_ID)));
			item.put(Constants.FIRST_CONNECT_TIME, new AttributeValue((String) map.get(Constants.FIRST_CONNECT_TIME)));
			item.put(Constants.LAST_CONNECT_TIME, new AttributeValue((String) map.get(Constants.LAST_CONNECT_TIME)));
			item.put(Constants.DEVICE_STATUS, new AttributeValue((String) map.get(Constants.DEVICE_STATUS)));
			item.put(Constants.PLATFORM, new AttributeValue((String) map.get(Constants.PLATFORM)));
			if(map.containsKey(Constants.BUSLINE_NUM)){
				item.put(Constants.BUSLINE_NUM, new AttributeValue((String) map.get(Constants.BUSLINE_NUM)));
			}
			if(map.containsKey(Constants.REMAIN_TIME)){
				item.put(Constants.REMAIN_TIME, new AttributeValue((String) map.get(Constants.REMAIN_TIME)));
			}
			if(map.containsKey(Constants.POSITION_ID)){
				item.put(Constants.POSITION_ID, new AttributeValue((String) map.get(Constants.POSITION_ID)));
			}
			if(map.containsKey(Constants.OPERATION)){
				//添加操作
				Map<String, AttributeValue> operateMap = new HashMap<String, AttributeValue>();
				Map<String, Object> opsMap = (Map<String, Object>) map.get(Constants.OPERATION);
				context.getLogger().log("\nopsMap : " + opsMap.toString());
				operateMap.put(Constants.OPERATION_RESTART, new AttributeValue((String) opsMap.get(Constants.OPERATION_RESTART)));
				operateMap.put(Constants.OPERATION_POWEROFF, new AttributeValue((String) opsMap.get(Constants.OPERATION_POWEROFF)));
				operateMap.put(Constants.OPERATION_FOTA, new AttributeValue((String) opsMap.get(Constants.OPERATION_FOTA)));
				operateMap.put(Constants.OPERATION_DIAGNOSIS, new AttributeValue((String) opsMap.get(Constants.OPERATION_DIAGNOSIS)));
				operateMap.put(Constants.OPERATION_CONFIG, new AttributeValue((String) opsMap.get(Constants.OPERATION_CONFIG)));
				context.getLogger().log("\noperateMap : " + operateMap.toString());
				AttributeValue operAttrs = new AttributeValue();
				operAttrs.setM(operateMap);
				item.put(Constants.OPERATION, operAttrs);
			}
			if (map.containsKey(Constants.DATA_STREAMS)) {
				Map<String, Object> joMap = (Map<String, Object>) map.get(Constants.DATA_STREAMS);
				context.getLogger().log("datastreams joMap : " + joMap.toString());
				Map<String, AttributeValue> streams = new HashMap<String, AttributeValue>();
				for (int j = 1; j < joMap.size() + 1; j++) {
					AttributeValue av = new AttributeValue();
					String dataStreamDisplayName = (String) ((Map<String, Object>) joMap
							.get(Constants.DATA_STREAM + (j + ""))).get(Constants.DATA_STREAM_DISPLAY_NAME);
					av.addMEntry(Constants.DATA_STREAM_DISPLAY_NAME, new AttributeValue(dataStreamDisplayName));
					String dataStreamName = (String) ((Map<String, Object>) joMap.get(Constants.DATA_STREAM + (j + "")))
							.get(Constants.DATA_STREAM_NAME);
					av.addMEntry(Constants.DATA_STREAM_NAME, new AttributeValue(dataStreamName));
					String dataStreamType = (String) ((Map<String, Object>) joMap.get(Constants.DATA_STREAM + (j + "")))
							.get(Constants.DATA_STREAM_TYPE);
					av.addMEntry(Constants.DATA_STREAM_TYPE, new AttributeValue(dataStreamType));
					String dataStreamUnit = (String) ((Map<String, Object>) joMap.get(Constants.DATA_STREAM + (j + "")))
							.get(Constants.DATA_STREAM_UNIT);
					av.addMEntry(Constants.DATA_STREAM_UNIT, new AttributeValue(dataStreamUnit));
					String dataStreamId = (String) ((Map<String, Object>) joMap.get(Constants.DATA_STREAM + (j + "")))
							.get(Constants.DATA_STREAM_ID);
					av.addMEntry(Constants.DATA_STREAM_ID, new AttributeValue(dataStreamId));
					streams.put(Constants.DATA_STREAM + (j + ""), av);
				}
				AttributeValue dataAttrs = new AttributeValue();
				dataAttrs.setM(streams);
				item.put(Constants.DATA_STREAMS, dataAttrs);
			}
		}
		context.getLogger().log("newInstanceItems items : " + item.toString());
		return item;
	}

	private static Map<String, AttributeValue> newDTIDItem(Map<String, Object> json, int operateAddItem,
			Context context) {
		// TODO Auto-generated method stub
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		if (Constants.operate_add_item == operateAddItem) {
			item.put(Constants.DTID_ID, new AttributeValue((String) json.get(Constants.DTID_ID)));
			item.put(Constants.DTID_MASTER_KEY, new AttributeValue((String) json.get(Constants.DTID_MASTER_KEY)));
		}
		return item;
	}

	public static void batchDelItem(String tableName, List<String> listDel, Context context) throws Exception{
		init(context);
		
		if (Tables.doesTableExist(dynamoDB, tableName)) {
			System.out.println("Table " + tableName + " is already ACTIVE");
		} else {
			// 没有对应数据表
			throw new Exception("NO valid Table");
		}
		if (null == listDel) {
			throw new Exception("Input data is invalid");
		}
		for(String s: listDel){
			Map<String, AttributeValue> argDel = new HashMap<String, AttributeValue>();
			AttributeValue avDel = new AttributeValue(s);
			if (Constants.TBL_DEVICE_TYPE.equals(tableName)) {
				argDel.put(Constants.DEVICE_TYPE_ID, avDel);
			}else if(Constants.TBL_DEVICE_INSTANCE.equals(tableName)){
				argDel.put(Constants.DEVICE_ID, avDel);
			}else if(Constants.TBL_DIID.equals(tableName)){
				argDel.put(Constants.DIID_ID, avDel);
			}else if(Constants.TBL_DTID.equals(tableName)){
				argDel.put(Constants.DTID_ID, avDel);
				argDel.put(Constants.DTID_MASTER_KEY, avDel);
			}
			DeleteItemRequest delItemReq = new DeleteItemRequest(tableName, argDel);
			DeleteItemResult delItemResult = dynamoDB.deleteItem(delItemReq);
			context.getLogger().log("delItemResult: " + delItemResult);
		}
		
	}
	
	public static DeleteItemResult delOneItem(String tableName, String primaryId, Context context)
			throws Exception {
		init(context);
		try {
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// 没有对应数据表
				return null;
			}
			if (null == primaryId) {
				return null;
			}
			Map<String, AttributeValue> argDel = new HashMap<String, AttributeValue>();
			AttributeValue avDel = new AttributeValue(primaryId);
			//AttributeValue avDelKey = new AttributeValue(masterKey);
			if (Constants.TBL_DEVICE_TYPE.equals(tableName)) {
				argDel.put(Constants.DEVICE_TYPE_ID, avDel);
				//argDel.put(Constants.MASTER_KEY, avDelKey);
			}

			DeleteItemRequest delItemReq = new DeleteItemRequest(tableName, argDel);
			DeleteItemResult delItemResult = dynamoDB.deleteItem(delItemReq);
			context.getLogger().log("delItemResult: " + delItemResult);
			return delItemResult;
		} catch (AmazonServiceException ase) {
			context.getLogger().log("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			context.getLogger().log("Error Message:    " + ase);
		} catch (AmazonClientException ace) {
			context.getLogger()
					.log("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with AWS, "
							+ "such as not being able to access the network.");
		}
		return null;
	}

	public static ScanResult getOneItem(String tableName, String primaryId, Context context) throws Exception {
		init(context);
		try {
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// 没有对应数据表
				return null;
			}

			ScanRequest scanRequest = new ScanRequest(tableName);
			if (null != primaryId) {
				// Scan items for movies with a year attribute greater than 1985
				HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
				if (Constants.TBL_DEVICE_TYPE.equals(tableName)) {
					Condition con = new Condition().withComparisonOperator(ComparisonOperator.EQ.toString())
							.withAttributeValueList(new AttributeValue(primaryId));
					scanFilter.put(Constants.DEVICE_TYPE_ID, con);
				}else if(Constants.TBL_DEVICE_INSTANCE.equals(tableName)){
					Condition con = new Condition().withComparisonOperator(ComparisonOperator.EQ.toString())
							.withAttributeValueList(new AttributeValue(primaryId));
					scanFilter.put(Constants.DEVICE_ID, con);
				}else if(Constants.TBL_DATA_STREAM.equals(tableName)){
					Condition con = new Condition().withComparisonOperator(ComparisonOperator.BEGINS_WITH.toString())
							.withAttributeValueList(new AttributeValue(primaryId));
					scanFilter.put(Constants.DATA_STREAM_ID, con);
				}

				scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
			}

			ScanResult scanResult = dynamoDB.scan(scanRequest);
			context.getLogger().log("getItemResult: " + scanResult);
			return scanResult;
		} catch (AmazonServiceException ase) {
			context.getLogger().log("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			context.getLogger().log("Error Message:    " + ase);
		} catch (AmazonClientException ace) {
			context.getLogger()
					.log("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with AWS, "
							+ "such as not being able to access the network.");
		}
		return null;
	}
	
	public static ScanResult getStreamFromInstance(String tableName, String createTime, Context context) throws Exception {
		init(context);
		try {
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// 没有对应数据表
				return null;
			}

			ScanRequest scanRequest = new ScanRequest(tableName);
			if (null != createTime) {
				// Scan items for movies with a year attribute greater than 1985
				HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
				if(Constants.TBL_DATA_STREAM.equals(tableName)){
					Condition con = new Condition().withComparisonOperator(ComparisonOperator.EQ.toString())
							.withAttributeValueList(new AttributeValue(createTime));
					scanFilter.put(Constants.DATA_STREAM_ID, con);
				}

				scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
			}

			ScanResult scanResult = dynamoDB.scan(scanRequest);
			context.getLogger().log("getItemResult: " + scanResult);
			return scanResult;
		} catch (AmazonServiceException ase) {
			context.getLogger().log("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			context.getLogger().log("Error Message:    " + ase);
		} catch (AmazonClientException ace) {
			context.getLogger()
					.log("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with AWS, "
							+ "such as not being able to access the network.");
		}
		return null;
	}

	/**
	 * 
	 * @param json
	 *            传入JSON字串
	 * @param operate
	 *            操作类型：get 0; add 1; del 2; update 3;
	 * @return
	 */
	private static Map<String, AttributeValue> newDeviceTypeItem(Map<String, Object> json, int operate,
			Context context) {
		context.getLogger().log("json = " + json);
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		if (operate == Constants.operate_add_item) {
			context.getLogger().log("from input : type id: " + json.get(Constants.DEVICE_TYPE_ID) + "MASTER_KEY: "
					+ json.get(Constants.MASTER_KEY) + "MANAGEMENT_PROTOCOL: " + json.get(Constants.MANAGEMENT_PROTOCOL)
					+ "SERVICE_PROTOCOL: " + json.get(Constants.SERVICE_PROTOCOL) + "DEVICE_TYPE_NAME: "
					+ json.get(Constants.DEVICE_TYPE_NAME) + "DESCRIPTION: " + json.get(Constants.DESCRIPTION)
					+ "PLATFORM: " + json.get(Constants.PLATFORM) + "STATUS: " + json.get(Constants.STATUS));
			item.put(Constants.DEVICE_TYPE_ID, new AttributeValue((String) json.get(Constants.DEVICE_TYPE_ID)));
			item.put(Constants.MASTER_KEY, new AttributeValue((String) json.get(Constants.MASTER_KEY)));
			item.put(Constants.MANAGEMENT_PROTOCOL,
					new AttributeValue((String) json.get(Constants.MANAGEMENT_PROTOCOL)));
			item.put(Constants.SERVICE_PROTOCOL, new AttributeValue((String) json.get(Constants.SERVICE_PROTOCOL)));
			item.put(Constants.DEVICE_TYPE_NAME, new AttributeValue((String) json.get(Constants.DEVICE_TYPE_NAME)));
			item.put(Constants.DESCRIPTION, new AttributeValue((String) json.get(Constants.DESCRIPTION)));
			item.put(Constants.PLATFORM, new AttributeValue((String) json.get(Constants.PLATFORM)));
			item.put(Constants.STATUS, new AttributeValue((String) json.get(Constants.STATUS)));
			//添加操作
			Map<String, AttributeValue> operateMap = new HashMap<String, AttributeValue>();
			Map<String, Object> opsMap = (Map<String, Object>) json.get(Constants.OPERATION);
			context.getLogger().log("\nopsMap : " + opsMap.toString());
			operateMap.put(Constants.OPERATION_RESTART, new AttributeValue((String) opsMap.get(Constants.OPERATION_RESTART)));
			operateMap.put(Constants.OPERATION_POWEROFF, new AttributeValue((String) opsMap.get(Constants.OPERATION_POWEROFF)));
			operateMap.put(Constants.OPERATION_FOTA, new AttributeValue((String) opsMap.get(Constants.OPERATION_FOTA)));
			operateMap.put(Constants.OPERATION_DIAGNOSIS, new AttributeValue((String) opsMap.get(Constants.OPERATION_DIAGNOSIS)));
			operateMap.put(Constants.OPERATION_CONFIG, new AttributeValue((String) opsMap.get(Constants.OPERATION_CONFIG)));
			context.getLogger().log("\noperateMap : " + operateMap.toString());
			AttributeValue operAttrs = new AttributeValue();
			operAttrs.setM(operateMap);
			item.put(Constants.OPERATION, operAttrs);
			if (json.containsKey(Constants.DATA_STREAMS)) {
				Map<String, Object> joMap = (Map<String, Object>) json.get(Constants.DATA_STREAMS);
				context.getLogger().log("datastreams joMap : " + joMap.toString());
				Map<String, AttributeValue> streams = new HashMap<String, AttributeValue>();
				for (int j = 1; j < joMap.size() + 1; j++) {
					AttributeValue av = new AttributeValue();
					String dataStreamDisplayName = (String) ((Map<String, Object>) joMap
							.get(Constants.DATA_STREAM + (j + ""))).get(Constants.DATA_STREAM_DISPLAY_NAME);
					av.addMEntry(Constants.DATA_STREAM_DISPLAY_NAME, new AttributeValue(dataStreamDisplayName));
					String dataStreamName = (String) ((Map<String, Object>) joMap.get(Constants.DATA_STREAM + (j + "")))
							.get(Constants.DATA_STREAM_NAME);
					av.addMEntry(Constants.DATA_STREAM_NAME, new AttributeValue(dataStreamName));
					String dataStreamType = (String) ((Map<String, Object>) joMap.get(Constants.DATA_STREAM + (j + "")))
							.get(Constants.DATA_STREAM_TYPE);
					av.addMEntry(Constants.DATA_STREAM_TYPE, new AttributeValue(dataStreamType));
					String dataStreamUnit = (String) ((Map<String, Object>) joMap.get(Constants.DATA_STREAM + (j + "")))
							.get(Constants.DATA_STREAM_UNIT);
					av.addMEntry(Constants.DATA_STREAM_UNIT, new AttributeValue(dataStreamUnit));
					streams.put(Constants.DATA_STREAM + (j + ""), av);
				}
				AttributeValue dataAttrs = new AttributeValue();
				dataAttrs.setM(streams);
				item.put(Constants.DATA_STREAMS, dataAttrs);
			}
		}

		return item;
	}
}
