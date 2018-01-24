package com.huawei.bean;

import java.util.List;

public class SiteLevelRule {
	public String column;
	public String attribute;
	public String KeyValue;
	public List<String> determinants;
	
	public String getColumn() {
		return column;
	}
	public String getAttribute() {
		return attribute;
	}
	public String getKeyValue() {
		return KeyValue;
	}
	public List<String> getDeterminants() {
		return determinants;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public void setKeyValue(String keyValue) {
		this.KeyValue = keyValue;
	}
	public void setDeterminants(List<String> determinants) {
		this.determinants = determinants;
	}
	
	
}
