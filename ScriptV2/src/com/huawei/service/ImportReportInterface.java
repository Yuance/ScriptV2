package com.huawei.service;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

public interface ImportReportInterface {

	public abstract String returnCellValue(Cell cell);

	public abstract int getTotalRowCount(int tableStartIndex,
			int xlsheetLastRowIndex, Sheet ws);

}