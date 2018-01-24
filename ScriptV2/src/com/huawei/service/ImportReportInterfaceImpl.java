package com.huawei.service;

import java.util.Date;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;


public class ImportReportInterfaceImpl implements ImportReportInterface {
	
	
	@Override
	public String returnCellValue(Cell cell) {
		String value = "";
		if (cell != null) {

			if (cell.getCellTypeEnum() == CellType.NUMERIC) {
				
				value = (cell != null && cell.toString().length() > 0 ? String
						.valueOf((int) Math.round(cell.getNumericCellValue()))
						: "");
				
				
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					java.util.Date utDate = cell.getDateCellValue();
					Date sqlDate = new Date(utDate.getTime());
					value = sqlDate.toString();// cell.getDateCellValue().toString();
				}
			} else if (cell.getCellTypeEnum() == CellType.STRING) {
				value = (cell != null && cell.toString().length() > 0 ? cell
						.toString() : "");
			} else if (cell.getCellTypeEnum() == CellType.FORMULA) {
				CellType fType = cell.getCachedFormulaResultTypeEnum();
				if (fType == CellType.NUMERIC) {
					
					//if got dot then represents a double type
					if(String.valueOf(cell.getNumericCellValue()).contains(".")) {
						value =  (cell != null && cell.toString().length() > 0 ? String
								.valueOf(cell.getNumericCellValue()) : "");
					}
					else {
						value = (cell != null && cell.toString().length() > 0 ? String
							.valueOf((int) Math.round(cell.getNumericCellValue()))
							: "");
					}
				} else {
//					System.out.println("fType=" + fType);
					value = (cell != null && cell.toString().length() > 0 ? cell
							.getStringCellValue() : "");
				}
			} else {
				value = (cell != null && cell.toString().length() > 0 ? cell
						.toString() : "");
			}
		} else {
			value = "";
		}
		return value;
	}

	@Override
	public int getTotalRowCount(int tableStartIndex, int xlsheetLastRowIndex,
			Sheet ws) {
		int rowCount = 0;
		for (int i = tableStartIndex; i<xlsheetLastRowIndex;i++) {
			Row row = ws.getRow(i);
			if (row != null) {
				Cell cell = row.getCell(0);
				if (cell == null) return rowCount;
				else if (cell.toString().compareTo("") == 0) {
					return rowCount;
				}
				rowCount++;
			}
			else {
				return rowCount;
			}
		}
		return rowCount;
	}


}