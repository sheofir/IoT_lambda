package com.huawei.iot.device.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.huawei.iot.device.db.Constants;

public class DataTypeXmlParser {

	private static void init() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			System.out.println(e.getMessage());
		}
	}

	public static Map<String, Object> parserXmlFromInputStream(InputStream is){
		init();
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(is);
            
            NodeList nl = document.getElementsByTagName("devicetype"); 
            map.put(Constants.DEVICE_TYPE_NAME, document.getElementsByTagName("devicetypename").item(0).getFirstChild().getNodeValue().trim());
            map.put(Constants.DESCRIPTION, document.getElementsByTagName("description").item(0).getFirstChild().getNodeValue().trim());
            map.put(Constants.PLATFORM, document.getElementsByTagName("platform").item(0).getFirstChild().getNodeValue().trim());
            map.put(Constants.SERVICE_PROTOCOL, document.getElementsByTagName("serviceprotocol").item(0).getFirstChild().getNodeValue().trim());
            map.put(Constants.MANAGEMENT_PROTOCOL, document.getElementsByTagName("managementprotocol").item(0).getFirstChild().getNodeValue().trim());
            //添加操作功能
            Map<String, Object> operationMap = new HashMap<String, Object>();
            operationMap.put(Constants.OPERATION_RESTART,document.getElementsByTagName("restart").item(0).getFirstChild().getNodeValue().trim());
            operationMap.put(Constants.OPERATION_POWEROFF,document.getElementsByTagName("poweroff").item(0).getFirstChild().getNodeValue().trim());
            operationMap.put(Constants.OPERATION_FOTA,document.getElementsByTagName("fota").item(0).getFirstChild().getNodeValue().trim());
            operationMap.put(Constants.OPERATION_DIAGNOSIS,document.getElementsByTagName("diagnosis").item(0).getFirstChild().getNodeValue().trim());
            operationMap.put(Constants.OPERATION_CONFIG,document.getElementsByTagName("configuration").item(0).getFirstChild().getNodeValue().trim());
			map.put(Constants.OPERATION, operationMap);
			//data streams type define
            Map<String, Object> dataStreamsMap = new HashMap<String,Object>();
			NodeList dataStreams = document.getElementsByTagName("datastream");
			for(int i=0;i<dataStreams.getLength();i++){
				Map<String, String> dataStreamMap = new HashMap<String,String>();
				dataStreamMap.put(Constants.DATA_STREAM_NAME, document.getElementsByTagName("datastreamname").item(i).getFirstChild().getNodeValue().trim());
				dataStreamMap.put(Constants.DATA_STREAM_DISPLAY_NAME, document.getElementsByTagName("datastreamdisplayname").item(i).getFirstChild().getNodeValue().trim());
				dataStreamMap.put(Constants.DATA_STREAM_TYPE, document.getElementsByTagName("datastreamtype").item(i).getFirstChild().getNodeValue().trim());
				dataStreamMap.put(Constants.DATA_STREAM_UNIT, document.getElementsByTagName("datastreamunit").item(i).getFirstChild().getNodeValue().trim());
				dataStreamsMap.put(Constants.DATA_STREAM + (i+1), dataStreamMap);
			}
			map.put(Constants.DATA_STREAMS, dataStreamsMap);
            System.out.print("done");
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
        } catch (SAXException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
		return map;
	}
	
}
