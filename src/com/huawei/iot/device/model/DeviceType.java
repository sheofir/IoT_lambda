package com.huawei.iot.device.model;

import java.util.Map;

public class DeviceType {

	private String deviceTypeID;
	private String masterKey;
	private String deviceTypeName;
	private String status = "enable";//default
	private String description;
	private String platform = "Android";//default
	private String serviceProtocol;
	private String managementProtocol;
	private Map dataStream;
	private String createTime;
	public String getDeviceTypeID() {
		return deviceTypeID;
	}
	public void setDeviceTypeID(String deviceTypeID) {
		this.deviceTypeID = deviceTypeID;
	}
	public String getMasterKey() {
		return masterKey;
	}
	public void setMasterKey(String masterKey) {
		this.masterKey = masterKey;
	}
	public String getDeviceTypeName() {
		return deviceTypeName;
	}
	public void setDeviceTypeName(String deviceTypeName) {
		this.deviceTypeName = deviceTypeName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getServiceProtocol() {
		return serviceProtocol;
	}
	public void setServiceProtocol(String serviceProtocol) {
		this.serviceProtocol = serviceProtocol;
	}
	public String getManagementProtocol() {
		return managementProtocol;
	}
	public void setManagementProtocol(String managementProtocol) {
		this.managementProtocol = managementProtocol;
	}
	public Map getDataStream() {
		return dataStream;
	}
	public void setDataStream(Map dataStream) {
		this.dataStream = dataStream;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public DeviceType(String deviceTypeID, String masterKey, String deviceTypeName) {
		this.deviceTypeID = deviceTypeID;
		this.masterKey = masterKey;
		this.deviceTypeName = deviceTypeName;
	}
	@Override
	public String toString() {
		return "DeviceType [deviceTypeID=" + deviceTypeID + ", masterKey=" + masterKey + ", deviceTypeName="
				+ deviceTypeName + ", status=" + status + ", description=" + description + ", platform=" + platform
				+ ", dataStream=" + dataStream + ", createTime=" + createTime + "]";
	}
	
}
