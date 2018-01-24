package com.huawei.bean;

import java.util.List;

public class CellLevelRule {
	
	public String getColumn() {
		return column;
	}
	public String getAttribute() {
		return attribute;
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
	public void setDeterminants(List<String> determinants) {
		this.determinants = determinants;
	}
	
	public String column;
	public String attribute;
	public List<String> determinants;
	
}
