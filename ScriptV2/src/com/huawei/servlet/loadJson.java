package com.huawei.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Servlet implementation class loadJson
 */
@WebServlet("/loadJsonServlet")
public class loadJson extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public loadJson() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String path = this.getClass().getClassLoader().getResource("").getPath();
		path = URLDecoder.decode(path,"UTF-8");
		String[] pathArr = path.split("/WEB-INF/classes/");
		File rootDir = new File(pathArr[0] + "/resource");
		String excelNames = "{\"excels\":[";
		String excelname = "";
		boolean isExcelExists = false;
		
		for(File excel : rootDir.listFiles()){
			if (excel.getName().endsWith(".xlsx")) {
				isExcelExists = true;
				excelname = "{\"name\": \"" + excel.getName()+"\",";
				try {
					Workbook wb = WorkbookFactory.create(excel);
					int sheetNum = wb.getNumberOfSheets();
					excelname = excelname.concat("\"sheets\":[");
					for (int i=0; i<sheetNum; i++) {
						String sheetName = wb.getSheetAt(i).getSheetName();
						if (sheetName.contains("MML"))
								excelname = excelname.concat("\"" + sheetName + "\",");
					}
					excelname = excelname.substring(0, excelname.length()-1).concat("]},");
					wb.close();
				} catch (EncryptedDocumentException e) {
					e.printStackTrace();
				} catch (InvalidFormatException e) {
					e.printStackTrace();
				}
				System.out.println(excelname);
				excelNames = excelNames.concat(excelname);
			}
		}
		if (isExcelExists)
			excelNames = excelNames.substring(0,excelNames.length()-1).concat("],\"sheetNames\":");
		else 
			excelNames = excelNames.substring(0,excelNames.length()).concat("],\"sheetNames\":");
		
		String fileString = "";
		//add sheetName Attribute from sheetNames.json as String
		for(File json : rootDir.listFiles()){
			if (json.getName().endsWith("sheetNames.json")) {
				fileString = new String(Files.readAllBytes(Paths.get(json.toURI())));
			}
		}
		excelNames = excelNames.concat(fileString + "}");
		
		System.out.println(excelNames);
		response.getWriter().write(excelNames);
	}

}
