package com.huawei.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import com.huawei.bean.*;
import com.huawei.utils.ResourceUtil;


public class ScriptBuilderInterfaceImpl implements ScriptBuilderInterface {
	public static void main(String[] args) {
		System.out.println("Start:");
		ScriptBuilderInterface scriptBuilder = new ScriptBuilderInterfaceImpl();
		scriptBuilder.startThread();
		System.out.println("End.");
	}
	

	public  void startThread() {
		System.out.println("Start startThread() of ScriptBulilderInterfaceImpl()...");
		String path= "";
		try {
			path = this.getClass().getClassLoader().getResource("").getPath();
			path = URLDecoder.decode(path,"UTF-8");
			String[] pathArr = path.split("/WEB-INF/classes");
			path = pathArr[0];
			System.out.println(path);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
//		processImportExcel("D:/ZHANG YUANCE/JavaHome/WorkSpace/ScriptGeneratorTesting");
//		processImportExcel(path);
		processImportJson(path);
		
		System.out.println("\nEnd of startThread() of ScriptBuliderInterfaceImpl()...");
	}
	
	public String processImportJson(String sourceDirectory) {
			
			//output a log file in directory
			File LogFolder = new File(sourceDirectory+"/Log");
			if (!LogFolder.exists()) LogFolder.mkdirs();
			
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			
			File logFile = new File(LogFolder + "/"+ sdf.format(cal.getTime()) + ".txt");
			BufferedWriter log = null;
			try {
				log = new BufferedWriter(new FileWriter(logFile));
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			
			//get the following properties by file/IO
			JsonReader jsonReader = null;
			File rootDir = null;
			String fileString = "";
			SheetNames sheetNames = null;
			CellGroup cellGroupJson = null;
			try {
				rootDir = new File(sourceDirectory + ResourceUtil.getCommonProperty("file.templateInput"));
				System.out.println(rootDir);
				log.write("Template Input Directory: " + rootDir);
				
				for (File file: rootDir.listFiles()){
	
					if (file.getName().equalsIgnoreCase("sheetNames.json")){
						jsonReader = new JsonReader(new FileReader(file));
						sheetNames = new Gson().fromJson(jsonReader, SheetNames.class);
						jsonReader.close();
					}
					
					if (file.getName().equalsIgnoreCase("cellGroup.json")) {
						jsonReader = new JsonReader(new FileReader(file));
						cellGroupJson = new Gson().fromJson(jsonReader, CellGroup.class);
						jsonReader.close();
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			//get SheetName and CellGroup Info from local json files  
			final String siteSheetName = sheetNames.siteSheetName;
			final List<String> cellSheetNames = sheetNames.cellSheetNames;
	//		final List<String> cellSheetNames = new ArrayList<String>(Arrays.asList("L18 cell MML(eran 12.1)","L26 cell MML(eran12.1)"));
	
			int cellGroupLen = cellGroupJson.group.size();
			final List<String>[] cellGroup = (List<String>[])new List[cellGroupLen];
			for(int i=0; i<cellGroupLen; i++) {
				cellGroup[Integer.parseInt(cellGroupJson.group.get(i).matchTo)] = cellGroupJson.group.get(i).cellGroup;
			}
			
			
			ImportReportInterfaceImpl importInterfaceImpl = new ImportReportInterfaceImpl();
			String returnMsg = "";
			
			File file = null;
			
			BufferedWriter output = null;
			
			try {
				
				ArrayList<String>[] cellProperties = null;
				ArrayList<String>[] siteProperties = null;
				ArrayList<String>[] cellsInfo = null;
				ArrayList<String>[][] sitesInfo = null;
				
				int rowNum = 0;
				rootDir = new File(sourceDirectory + ResourceUtil.getCommonProperty("file.ruleInput"));
				System.out.println("\nStart to read properties from Rule file...");
				for (File fileA : rootDir.listFiles()) {
					
					//read properties fileA siteLevelRule.json
					if (fileA.getName().endsWith("json") && fileA.getName().startsWith("siteLevelRule")) {
						
						System.out.println("Processing on::" + fileA.getName());
						
						jsonReader = new JsonReader(new FileReader(fileA));
						SiteSetting data = new Gson().fromJson(jsonReader, SiteSetting.class);
						jsonReader.close();
						
						rowNum = data.setting.size();
						System.out.println("===== No. of rows = " + rowNum);
						List<SiteLevelRule> list = data.setting;
						siteProperties = readSiteProperties(list);
					}
					
					if (fileA.getName().endsWith("json") && fileA.getName().startsWith("cellLevelRule")){
						
						System.out.println("Processing on::" + fileA.getName());
						
						jsonReader = new JsonReader(new FileReader(fileA));
						CellSetting data = new Gson().fromJson(jsonReader, CellSetting.class);
						jsonReader.close();
							
						rowNum = data.setting.size();
						System.out.println("====No. of rows = " + rowNum);
						List<CellLevelRule> list = data.setting;
						cellProperties = readCellProperties(list);
					}
					
	//					//read properties fileA LTERule
	//				if (fileA.getName().endsWith("xlsx") && (fileA.getName().startsWith(ResourceUtil.getCommonProperty("file.RuleName")))) {
	//					System.out.println("Processing on::" + fileA.getName());
	//					
	//					Workbook wb = WorkbookFactory.create(fileA);
	////						System.out.println("Number of sheets::" + wb.getNumberOfSheets());
	//					//get the first sheet.
	//					String sheetName = wb.getSheetName(0);
	//					System.out.println("===== Getting sheet: " + sheetName);
	//					Sheet ws = wb.getSheet(sheetName);
	//					
	//					rowNum = ws.getPhysicalNumberOfRows();
	//					System.out.println("===== No. of rows = " + rowNum);
	//				
	//					//read cell properties into an array of arrayLists
	//					cellProperties = readCellProperties(ws, rowNum);
	//					System.out.println("cellProperties length: " + cellProperties.length);
						//read site properties into an array of arrayLists
	//					ws = wb.getSheetAt(1);
	//					System.out.println("===== Getting sheet: " + ws.getSheetName());
	//	
	//					rowNum = ws.getPhysicalNumberOfRows();
	//					System.out.println("===== No. of rows = " + rowNum);
	//					siteProperties = readSiteProperties(ws, rowNum);
						
	//				}
				}
				
				System.out.println("\nStart to read data from cellinfo file...");
				log.write("\nStart to read data from RF Data Input File...\n");
					//read data in cell info list
				rootDir = new File(sourceDirectory + ResourceUtil.getCommonProperty("file.dataInput"));
				for (File excel : rootDir.listFiles()) {
					if (excel.getName().endsWith("xlsx")) {
						System.out.println("Processing on::" + excel.getName());
						log.write("\nProcessing on::" + excel.getName());
						Workbook wb = WorkbookFactory.create(excel);
	
						String sheetName = wb.getSheetName(0);
						System.out.println("===== Getting sheet: " + sheetName);
						Sheet ws = wb.getSheet(sheetName);
	
						rowNum = 0;
						int colNum = 0;
	
						System.out.println("ws.getPhysicalNumberOfRows(): " + ws.getPhysicalNumberOfRows());
						rowNum = importInterfaceImpl.getTotalRowCount(Integer.parseInt(ResourceUtil.getCommonProperty("file.scriptInputHeadersRownum")), ws.getPhysicalNumberOfRows(), ws);
						System.out.println("===== No. of rows = " + rowNum);
						
						colNum = ws.getRow(0).getLastCellNum();
						System.out.println("===== No. of columns = " + colNum);
						
	
						printArrayOfArrayList(cellProperties, log);
						
						cellsInfo = readCellInfo(ws, rowNum, cellProperties);
						sitesInfo = readSitesInfo(ws, rowNum, siteProperties);
						
		
						System.out.println("==========cellsInfo==========");
						log.write("\n=======RF Data Input=======\n");
						for (int i = 0; i<cellProperties.length;i++) {
							System.out.print(cellProperties[i].get(1) + "\t");
							log.write(cellProperties[i].get(1) + "\t");
						}
						System.out.println("");
						log.write("\n");
						printArrayOfArrayList(cellsInfo, log);
						
						for (int i = 0; i<sitesInfo.length; i++) {
							
							System.out.println("==========sitesInfo["+ i +"]=============");
							printArrayOfArrayList(sitesInfo[i]);
						}
//						sitesInfo = validateGroup(sitesInfo);
//						
//						System.out.println("==========AfterChanged=============");
//						printArrayOfArrayList(sitesInfo[0]);
					}
				}
				
				System.out.println("\nStart to read generator tool and get on output...");
				log.write("\nStart to read generator tool and get on output...");
				rootDir = new File(sourceDirectory + ResourceUtil.getCommonProperty("file.templateInput"));
				for (File excel : rootDir.listFiles()){
					//read Final OutputFile
					Sheet ws;
					if (excel.getName().equalsIgnoreCase(sheetNames.excelName)) {
						System.out.println("Processing on::" + excel.getName());
						log.write("\nProcessing on::" + excel.getName());
						Workbook wb = WorkbookFactory.create(excel);
						
						//get the first sheet.
						System.out.println("====== Get " + siteSheetName );
						log.write("\n====== Get " + siteSheetName);
						ws = wb.getSheet(siteSheetName);
						
						ArrayList<String>[] scriptName = getSiteName(cellsInfo, sitesInfo[0].length);
						printArrayOfArrayList(scriptName, log);
						
						int cellsInfoStart;
						int cellsInfoEnd = 0;
						//loop sites
						for (int count = 0; count < sitesInfo[0].length; count++) {
							
							//create file
							fileString = new String(sourceDirectory + "/Output/" + scriptName[count].get(0)+" cell ");
							for (int i = 1; i<scriptName[count].size(); i++) {
								if (i != scriptName[count].size() - 1)
									fileString = fileString.concat(scriptName[count].get(i) + ",");
								else fileString = fileString.concat(scriptName[count].get(i));
							}
							fileString = fileString.concat(".txt");
							file = new File(fileString);
							
							System.out.println("\n" + file + " created.");
							log.write("\n" + file + " created.");
							output = new BufferedWriter(new FileWriter(file));
							
							System.out.println("========= For site: " + scriptName[count].get(0));
							log.write("\n========= For site: " + scriptName[count].get(0));
							ws = wb.getSheet(siteSheetName);
							
							
							//output the first sheet contents, site script dead code(non-conditional)
							
							
							for (int i = Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptDeadcodeRowStart")); 
										i <= Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptDeadcodeRowEnd")); i++) {
								//Add Line
								output.write(importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptCol")))) + "\n");
							}
							
							System.out.println("\n===== Site-level dead code printed.\n");
							log.write("\n===== Site-level dead code printed.\n");
							
							//output the changeable site-level code
							System.out.println("===== Site-Level live code ");
							log.write("===== Site-Level live code ");
							
							boolean CAGroupFlag = false;
							int cellCount = 0;
							String[] requiredCellID;
							String requiredCellIDString = "";
							int predataLength = 0;
							
							int liveCodeStart = Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptLivecodeStart"));
							
							
							//loop the proprties, each property a sequence of lines
							for (int j = 0; j < siteProperties.length; j++) {
								int liveCodeEnd = liveCodeStart;
								predataLength = Integer.parseInt(siteProperties[j].get(1));
								
								liveCodeEnd = liveCodeEnd + siteProperties[j].size() - predataLength;
								output.write(importInterfaceImpl.returnCellValue(ws.getRow(liveCodeStart++).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptCol")))) + "\n");
								
	//							System.out.println("LivecodeStart: " + liveCodeStart);
	//							System.out.println("LivecodeEnd: " + liveCodeEnd);
							
								cellCount = 0;
								
								for (int i = liveCodeStart; i <= liveCodeEnd; i++) {
									
									requiredCellIDString = siteProperties[j].get(cellCount + predataLength);
									requiredCellID = siteProperties[j].get(cellCount + predataLength).split(",");
									
									System.out.println("siteproperties size: " + sitesInfo[j][count].size()+"  requiredCellID: " + requiredCellIDString);
									log.write("\nsiteproperties size: " + sitesInfo[j][count].size()+"  requiredCellID: " + requiredCellIDString);
									
									//print the site Value
//									System.out.print("siteValue: "+sitesInfo[j][count].get(requiredCellID-1));
//									log.write("\nsiteValue: "+sitesInfo[j][count].get(requiredCellID-1));
									
									System.out.print("siteValue: ");
									log.write("\nsiteValue: ");
									
									String currentValue="";
									CAGroupFlag = false;
									for(int cellNum=0; cellNum<requiredCellID.length; cellNum++) {
										
										if (cellNum == requiredCellID.length - 1) {
											currentValue = sitesInfo[j][count].get(Integer.parseInt(requiredCellID[cellNum])-1);
											if (currentValue.equalsIgnoreCase(""))
												System.out.println("Empty");
											else System.out.print(currentValue);
										}
										else {
											currentValue = sitesInfo[j][count].get(Integer.parseInt(requiredCellID[cellNum])-1);
											if (currentValue.equalsIgnoreCase(""))
												System.out.print("Empty,");
											else System.out.print(currentValue + ",");
										}
										
										if(currentValue.equalsIgnoreCase(siteProperties[j].get(3)))
											CAGroupFlag = true;
									}
									//if the required CellID equals to "yes"
									
								 	if (CAGroupFlag) {
										output.write(importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptCol")))) + "\n");
										System.out.print("\tPrint Line: " + cellCount + "\n\n");
										log.write("\n\tPrint Line: " + cellCount + "\n\n");
								 	}
								 	else {
								 		System.out.print("\tskip\n\n");
								 		log.write("\tskip\n\n");
								 	}
									cellCount++;
									
								}
								liveCodeStart += 2;
								
								//Empty Line between different site properties
								output.write("\n");
							}
					
							
							
							//output cell-level code
							
							int cellScriptCodeColPos = 0;
							Row rowInstance = null;
							
							if (count == 0) cellsInfoStart = 0;
							else cellsInfoStart = cellsInfoEnd;
							cellsInfoEnd = cellsInfoEnd + scriptName[count].size() - 1;
							
							//modify SiteIDIn Formular Cell
							rowInstance = ws.getRow(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptRefRow")));
							rowInstance.getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptSiteIDRef"))).setCellValue(cellsInfo[cellsInfoStart].get(0));
							rowInstance.getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptLatRef"))).setCellValue(cellsInfo[cellsInfoStart].get(2));
							rowInstance.getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptLongRef"))).setCellValue(cellsInfo[cellsInfoStart].get(3));
							
							XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
							//get the sheets specified in cellSheetNames(List<String>)
							Sheet[] sheets = new Sheet[cellSheetNames.size()];
						
							for (int i=0; i<cellSheetNames.size(); i++)
								sheets[i] = wb.getSheet(cellSheetNames.get(i));
							
							int checkStart = Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCheckStart"));
							
							System.out.println("===== Cell-level live/dead code");
							log.write("\n===== Cell-level live/dead code\n");
							//loop cells
							for (int i = cellsInfoStart; i < cellsInfoEnd; i++) {
								System.out.print("Cell " + cellsInfo[i].get(1));
								log.write("Cell " + cellsInfo[i].get(1));
								
								output.write("//Cell " + cellsInfo[i].get(1) + "\n\n");
								
								int groupNum = checkGroup(Integer.parseInt(cellsInfo[i].get(1)), cellGroup);
								if ( groupNum != -1) {
									ws = sheets[groupNum];
									System.out.println("  ==> belongs to group: " + cellSheetNames.get(groupNum));
									log.write("  ==> belongs to group: " + cellSheetNames.get(groupNum) + "\n");
									cellScriptCodeColPos = getCellScriptCodePos(Integer.parseInt(cellsInfo[i].get(1)),cellGroup);
									if (cellScriptCodeColPos == -1) throw new Exception();
								}
								else throw new Exception();
								
	//							// if it is L26
	//							if (checkGroup(Integer.parseInt(cellsInfo[i].get(1)))) {
	//								ws = ws26;
	//								System.out.println("  ==> belongs to L26");
	//								cellScriptCodeColPos = getCellScriptCodePos(Integer.parseInt(cellsInfo[i].get(1)));
	//							}
	//							else {
	//								ws = ws18;
	//								cellScriptCodeColPos = getCellScriptCodePos(Integer.parseInt(cellsInfo[i].get(1)));
	//								System.out.println("  ==> belongs to L18");
	//							}
	//							
								//use ws, cellsInfo, cellProperties to get output
								
								int row = checkStart;
								int propertiesCount = 0;
								int cellConditionCount = 0;
								
								//print all codes for a site
								//A record for return of writeDeadCode, 0 for rowNum, 1 for boolean value of whether end
								ArrayList<Integer> resultList;
								
								while (!importInterfaceImpl.returnCellValue(ws.getRow(row).getCell(0)).equalsIgnoreCase("end") && propertiesCount<(cellProperties.length - 4)) {
									
									//HardCoded 4 Cols before the Cells script
									cellConditionCount = cellProperties[propertiesCount + 4].size();
									
									for (int j = 2; j < cellConditionCount; j++) {
										
										if (cellsInfo[i].get(propertiesCount + 4).equalsIgnoreCase(cellProperties[propertiesCount + 4].get(j))) {
											
											do {
												//print until next *. reach
												row++;
												rowInstance = ws.getRow(row);
												if (rowInstance == null) {
													do{
														rowInstance = ws.getRow(++row);
													} while (rowInstance == null);
												}
												
												if (importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).equalsIgnoreCase("end")) 
													break;
												
												output.write(importInterfaceImpl.returnCellValue(
																rowInstance.getCell(cellScriptCodeColPos +
																			Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCodeCol")))) + "\n");
												
												
											} while(!importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).matches(".+[E,e][N,n][D,d]"));
											
											//start to write dead code until a question mark reach
											resultList = writeDeadCode(ws, row, cellScriptCodeColPos, output);
											row = resultList.get(0);
	
											if (resultList.get(1) == 1) break;
										}
										else {
											// skip the current part, skip until ....end meets.
											do {
												row++;
												rowInstance = ws.getRow(row);
												if (rowInstance == null) {
													do{
														rowInstance = ws.getRow(++row);
													} while (rowInstance == null);
												}
												if (importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).equalsIgnoreCase("end"))
													break;
											} while (!importInterfaceImpl.returnCellValue(
																	rowInstance.getCell(0)).matches(".+[E,e][N,n][D,d]"));
										
										
										//start to write dead code until a question mark reach
										resultList = writeDeadCode(ws, row, cellScriptCodeColPos, output);
										row = resultList.get(0);
	
										if (resultList.get(1) == 1) break;
										}
									}
									propertiesCount++;
									
									
						//double Checking method __ Archive		
								
	//								try {
	//									//skip the first four attributes of a cell, which is eNodeBID, CellID, Lat. and Lon.
	//									//if satisfied flag being checked, loop the positive part
	//									if (cellsInfo[i].get(propertiesCount + 4).equalsIgnoreCase(cellProperties[propertiesCount + 4].get(2))) {
	//										
	//										do {
	//											//print until next *. reach
	//											row++;
	//											rowInstance = ws.getRow(row);
	//											if (rowInstance == null) {
	//												do{
	//													rowInstance = ws.getRow(++row);
	//												} while (rowInstance == null);
	//											}
	//											
	//											outputStr = importInterfaceImpl.returnCellValue(
	//													ws.getRow(row).getCell(cellScriptCodeColPos + 
	//															Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCodeCol")))) + "\n";
	//											if (outputStr.contains(""))
	//											output.write(importInterfaceImpl.returnCellValue(
	//													ws.getRow(row).getCell(cellScriptCodeColPos + 
	//															Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCodeCol")))) + "\n");
	//											
	//											
	//										} while (!importInterfaceImpl.returnCellValue(
	//														rowInstance.getCell(0)).matches("\\d+\\..+"));
	//										
	//										
	//										//skip the negative part
	//										do {
	//											row++;
	//											rowInstance = ws.getRow(row);
	//											if (rowInstance == null) {
	//												do{
	//													rowInstance = ws.getRow(++row);
	//												} while (rowInstance == null);
	//											}
	//											if (importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).equalsIgnoreCase("end"))
	//												break;
	//										} while (!importInterfaceImpl.returnCellValue(
	//																rowInstance.getCell(0)).matches(".+[E,e]nd"));
	//										propertiesCount++;
	//										
	//										//Start to write dead code until a question mark reach
	//										resultList = writeDeadCode(ws, row, cellScriptCodeColPos, output);
	//										row = resultList.get(0);
	//										if (resultList.get(1) == 1) break;
	//									}
	//									else {
	//										//skip the positive part
	//										do {
	//											row++;
	//											rowInstance = ws.getRow(row);
	//											if (rowInstance == null) {
	//												do{
	//													rowInstance = ws.getRow(++row);
	//												} while (rowInstance == null);
	//											}
	//											
	//										} while (!importInterfaceImpl.returnCellValue(
	//														rowInstance.getCell(0)).matches("\\d+\\..+"));
	//										
	//										//loop the negative part
	//										do {
	//											//print until next *. reach
	//											row++;
	//											rowInstance = ws.getRow(row);
	//											if (rowInstance == null) {
	//												do{
	//													rowInstance = ws.getRow(++row);
	//												} while (rowInstance == null);
	//											}
	//											
	//											if (importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).equalsIgnoreCase("end")) 
	//												break;
	//											
	//											output.write(importInterfaceImpl.returnCellValue(
	//															rowInstance.getCell(cellScriptCodeColPos +
	//																		Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCodeCol")))) + "\n");
	//											
	//											
	//										} while(!importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).matches(".+[E,e]nd"));
	//										propertiesCount++;
	//									
	//										//Start to write dead code until a question mark reach
	//										resultList = writeDeadCode(ws, row, cellScriptCodeColPos, output);
	//										row = resultList.get(0);
	//										if (resultList.get(1) == 1) break;
	//									}
	//								} catch (Exception e) {
	//									System.out.println("RowInstance: " + (rowInstance == null) + " , row: " + row);
	//									e.printStackTrace();
	//								}
	
								}
								System.out.println("Cell level live Code printed for Cell " + cellsInfo[i].get(1));
								log.write("Cell level live Code printed for Cell " + cellsInfo[i].get(1) + "\n");
								
							}
							
							//IF CA feature is turned on
							if (ResourceUtil.getCommonProperty("file.MissingCAFunction").equalsIgnoreCase("on")) {
								//print the missing CA cell.  Remember to add constrains for checkGroup function when L2100 come.
								ArrayList<String> missingCA = getMissingCACell(scriptName[count],cellsInfo,cellsInfoStart,cellsInfoEnd, cellGroup);
		
								System.out.println("MissingCA: ");
								System.out.println(missingCA);
								log.write("\nMissingCA: " + missingCA);
								
								//print the CAscript
								if (!missingCA.isEmpty()) {
									output.write("//For existing CA Cells\n\n");
									
									for (int i = 0; i < missingCA.size(); i++) {
										int cellCol = 4;
										String cellnum = "";
										String lineWritten = "";
										
		//								//if its in L18
		//								if (!checkGroup(Integer.parseInt(missingCA.get(i))))
		//									ws = ws18;
		//								else ws = ws26;
										
										int groupNum = checkGroup(Integer.parseInt(missingCA.get(i)), cellGroup);
										if ( groupNum != -1) {
											ws = sheets[groupNum];
											System.out.println("  ==> belongs to group: " + cellSheetNames.get(groupNum));
											log.write("\n  ==> belongs to group: \" + cellSheetNames.get(groupNum)");
										}
										else throw new Exception();
										
										//print according to ws
										cellnum = missingCA.get(i);
										output.write("//Cell " + cellnum + "\n");
										
										int j = checkStart;
										rowInstance = ws.getRow(j);
										Cell cellInstance = rowInstance.getCell(0);
										
										//find until CA flag found   // ends with CA?
										while (!importInterfaceImpl.returnCellValue(cellInstance).matches(".+CA.?\\?.?")) {
											rowInstance = ws.getRow(++j);
											while (rowInstance == null) 
												rowInstance = ws.getRow(++j);
											cellInstance = rowInstance.getCell(0);
										} 
										j++;
										
										System.out.println("Start to output CAscript..");
										log.write("\nStart to output CAscript");
										//start to print until end meets
										cellCol = 4 + Integer.parseInt(cellnum);
										rowInstance = ws.getRow(j);
										while (rowInstance == null)
											rowInstance = ws.getRow(++j);
										cellInstance = rowInstance.getCell(0);
										
										while(!importInterfaceImpl.returnCellValue(cellInstance).matches(".+[E,e][N,n][D,d]")){
											
											lineWritten = importInterfaceImpl.returnCellValue(rowInstance.getCell(cellCol));
											output.write(lineWritten + "\n");
											
											rowInstance = ws.getRow(++j);
											while(rowInstance == null)
												rowInstance = ws.getRow(++j);
											
											cellInstance = rowInstance.getCell(0);
										}
									}
										
								}
								System.out.println("Finished printing the Existing CA Script.");
								log.write("\n\nFinished printing the Existing CA Script.");
							}
							
							//output file
							output.close();
						}
						
						wb.close();
					}
				}
		
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			//close the log write
			try {
				log.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return returnMsg;
		}
		
		
/*	public String processImportExcel(String sourceDirectory) {
		
		//output a log file in directory
		File LogFolder = new File(sourceDirectory+"/Log");
		if (!LogFolder.exists()) LogFolder.mkdirs();
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
		File logFile = new File(LogFolder + "/"+ sdf.format(cal.getTime()) + ".txt");
		BufferedWriter log = null;
		try {
			log = new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		
		//get the following properties by file/IO
		JsonReader jsonReader = null;
		File rootDir = null;
		String fileString = "";
		SheetNames sheetNames = null;
		CellGroup cellGroupJson = null;
		try {
			rootDir = new File(sourceDirectory + ResourceUtil.getCommonProperty("file.templateInput"));
			System.out.println(rootDir);
			log.write("Template Input Directory: " + rootDir);
			
			for (File file: rootDir.listFiles()){

				if (file.getName().equalsIgnoreCase("sheetNames.json")){
					jsonReader = new JsonReader(new FileReader(file));
					sheetNames = new Gson().fromJson(jsonReader, SheetNames.class);
					jsonReader.close();
				}
				
				if (file.getName().equalsIgnoreCase("cellGroup.json")) {
					jsonReader = new JsonReader(new FileReader(file));
					cellGroupJson = new Gson().fromJson(jsonReader, CellGroup.class);
					jsonReader.close();
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		//get SheetName and CellGroup Info from local json files
		final String siteSheetName = sheetNames.siteSheetName;
		final List<String> cellSheetNames = sheetNames.cellSheetNames;
//		final List<String> cellSheetNames = new ArrayList<String>(Arrays.asList("L18 cell MML(eran 12.1)","L26 cell MML(eran12.1)"));

		int cellGroupLen = cellGroupJson.group.size();
		final List<String>[] cellGroup = (List<String>[])new List[cellGroupLen];
		for(int i=0; i<cellGroupLen; i++) {
			cellGroup[Integer.parseInt(cellGroupJson.group.get(i).matchTo)] = cellGroupJson.group.get(i).cellGroup;
		}
		
		
		ImportReportInterfaceImpl importInterfaceImpl = new ImportReportInterfaceImpl();
		String returnMsg = "";
		
		File file = null;
		
		BufferedWriter output = null;
		
		try {
			
			ArrayList<String>[] cellProperties = null;
			ArrayList<String>[] siteProperties = null;
			ArrayList<String>[] cellsInfo = null;
			ArrayList<String>[][] sitesInfo = null;
			
			int rowNum = 0;
			rootDir = new File(sourceDirectory + ResourceUtil.getCommonProperty("file.ruleInput"));
			System.out.println("\nStart to read properties from Rule file...");
			for (File fileA : rootDir.listFiles()) {
				
				//read properties fileA siteLevelRule.json
				if (fileA.getName().endsWith("json") && fileA.getName().startsWith("siteLevelRule")) {
					
					System.out.println("Processing on::" + fileA.getName());
					
					jsonReader = new JsonReader(new FileReader(fileA));
					SiteSetting data = new Gson().fromJson(jsonReader, SiteSetting.class);
					jsonReader.close();
					
					rowNum = data.setting.size();
					System.out.println("===== No. of rows = " + rowNum);
					List<SiteLevelRule> list = data.setting;
					siteProperties = readSiteProperties(list);
				}
				
				if (fileA.getName().endsWith("json") && fileA.getName().startsWith("cellLevelRule")){
					
					System.out.println("Processing on::" + fileA.getName());
					
					jsonReader = new JsonReader(new FileReader(fileA));
					CellSetting data = new Gson().fromJson(jsonReader, CellSetting.class);
					jsonReader.close();
						
					rowNum = data.setting.size();
					System.out.println("====No. of rows = " + rowNum);
					List<CellLevelRule> list = data.setting;
					cellProperties = readCellProperties(list);
				}
				
//					//read properties fileA LTERule
//				if (fileA.getName().endsWith("xlsx") && (fileA.getName().startsWith(ResourceUtil.getCommonProperty("file.RuleName")))) {
//					System.out.println("Processing on::" + fileA.getName());
//					
//					Workbook wb = WorkbookFactory.create(fileA);
////						System.out.println("Number of sheets::" + wb.getNumberOfSheets());
//					//get the first sheet.
//					String sheetName = wb.getSheetName(0);
//					System.out.println("===== Getting sheet: " + sheetName);
//					Sheet ws = wb.getSheet(sheetName);
//					
//					rowNum = ws.getPhysicalNumberOfRows();
//					System.out.println("===== No. of rows = " + rowNum);
//				
//					//read cell properties into an array of arrayLists
//					cellProperties = readCellProperties(ws, rowNum);
//					System.out.println("cellProperties length: " + cellProperties.length);
					//read site properties into an array of arrayLists
//					ws = wb.getSheetAt(1);
//					System.out.println("===== Getting sheet: " + ws.getSheetName());
//	
//					rowNum = ws.getPhysicalNumberOfRows();
//					System.out.println("===== No. of rows = " + rowNum);
//					siteProperties = readSiteProperties(ws, rowNum);
					
//				}
			}
			
			System.out.println("\nStart to read data from cellinfo file...");
			log.write("\nStart to read data from RF Data Input File...\n");
				//read data in cell info list
			rootDir = new File(sourceDirectory + ResourceUtil.getCommonProperty("file.dataInput"));
			for (File excel : rootDir.listFiles()) {
				if (excel.getName().endsWith("xlsx")) {
					System.out.println("Processing on::" + excel.getName());
					log.write("\nProcessing on::" + excel.getName());
					Workbook wb = WorkbookFactory.create(excel);

					String sheetName = wb.getSheetName(0);
					System.out.println("===== Getting sheet: " + sheetName);
					Sheet ws = wb.getSheet(sheetName);

					rowNum = 0;
					int colNum = 0;

					System.out.println("ws.getPhysicalNumberOfRows(): " + ws.getPhysicalNumberOfRows());
					rowNum = importInterfaceImpl.getTotalRowCount(Integer.parseInt(ResourceUtil.getCommonProperty("file.scriptInputHeadersRownum")), ws.getPhysicalNumberOfRows(), ws);
					System.out.println("===== No. of rows = " + rowNum);
					
					colNum = ws.getRow(0).getLastCellNum();
					System.out.println("===== No. of columns = " + colNum);
					

					printArrayOfArrayList(cellProperties, log);
					
					cellsInfo = readCellInfo(ws, rowNum, cellProperties);
					sitesInfo = readSitesInfo(ws, rowNum, siteProperties);
					
	
					System.out.println("==========cellsInfo==========");
					log.write("\n=======RF Data Input=======\n");
					for (int i = 0; i<cellProperties.length;i++) {
						System.out.print(cellProperties[i].get(1) + "\t");
						log.write(cellProperties[i].get(1) + "\t");
					}
					System.out.println("");
					log.write("\n");
					printArrayOfArrayList(cellsInfo, log);
					
					System.out.println("==========sitesInfo[0]=============");
					printArrayOfArrayList(sitesInfo[0]);
					
					sitesInfo = validateGroup(sitesInfo);
					
					System.out.println("==========AfterChanged=============");
					printArrayOfArrayList(sitesInfo[0]);
				}
			}
			
			System.out.println("\nStart to read generator tool and get on output...");
			log.write("\nStart to read generator tool and get on output...");
			rootDir = new File(sourceDirectory + ResourceUtil.getCommonProperty("file.templateInput"));
			for (File excel : rootDir.listFiles()){
				//read Final OutputFile
				Sheet ws;
				if (excel.getName().equalsIgnoreCase(sheetNames.excelName)) {
					System.out.println("Processing on::" + excel.getName());
					log.write("\nProcessing on::" + excel.getName());
					Workbook wb = WorkbookFactory.create(excel);
					
					//get the first sheet.
					System.out.println("====== Get " + siteSheetName );
					log.write("\n====== Get " + siteSheetName);
					ws = wb.getSheet(siteSheetName);
					
					ArrayList<String>[] scriptName = getSiteName(cellsInfo, sitesInfo[0].length);
					printArrayOfArrayList(scriptName, log);
					
					int cellsInfoStart;
					int cellsInfoEnd = 0;
					//loop sites
					for (int count = 0; count < sitesInfo[0].length; count++) {
						
						//create file
						fileString = new String(sourceDirectory + "/Output/" + scriptName[count].get(0)+" cell ");
						for (int i = 1; i<scriptName[count].size(); i++) {
							if (i != scriptName[count].size() - 1)
								fileString = fileString.concat(scriptName[count].get(i) + ",");
							else fileString = fileString.concat(scriptName[count].get(i));
						}
						fileString = fileString.concat(".txt");
						file = new File(fileString);
						
						System.out.println("\n" + file + " created.");
						log.write("\n" + file + " created.");
						output = new BufferedWriter(new FileWriter(file));
						
						System.out.println("========= For site: " + scriptName[count].get(0));
						log.write("\n========= For site: " + scriptName[count].get(0));
						ws = wb.getSheet(siteSheetName);
						
						
						//output the first sheet contents, site script dead code(non-conditional)
						
						
						for (int i = Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptDeadcodeRowStart")); 
									i <= Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptDeadcodeRowEnd")); i++) {
							//Add Line
							output.write(importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptCol")))) + "\n");
						}
						
						System.out.println("\n===== Site-level dead code printed.\n");
						log.write("\n===== Site-level dead code printed.\n");
						
						//output the changeable site-level code
						System.out.println("===== Site-Level live code ");
						log.write("===== Site-Level live code ");
						int cellCount = 0;
						int requiredCellID = 0;
						int predataLength = 0;
						
						int liveCodeStart = Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptLivecodeStart"));
						int liveCodeEnd = liveCodeStart;
						
						//loop the proprties, each property a sequence of lines
						for (int j = 0; j < siteProperties.length; j++) {
							
							liveCodeEnd = liveCodeEnd + siteProperties[j].size() - Integer.parseInt(siteProperties[j].get(1));
							output.write(importInterfaceImpl.returnCellValue(ws.getRow(liveCodeStart++).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptCol")))) + "\n");
							
//							System.out.println("LivecodeStart: " + liveCodeStart);
//							System.out.println("LivecodeEnd: " + liveCodeEnd);
						
							cellCount = 0;
							predataLength = Integer.parseInt(siteProperties[j].get(1));
							
							for (int i = liveCodeStart; i <= liveCodeEnd; i++) {
								
								requiredCellID = Integer.parseInt(siteProperties[j].get(cellCount + predataLength));
								
								//skip the rest when no records is in sitesInfo
								
								if (requiredCellID-1 >= sitesInfo[j][count].size()) {
									liveCodeStart = liveCodeEnd + 1;
									break;
								}
								
								System.out.println("siteproperties size: " + sitesInfo[j][count].size()+"  requiredCellID: " + requiredCellID);
								log.write("\nsiteproperties size: " + sitesInfo[j][count].size()+"  requiredCellID: " + requiredCellID);
								System.out.print("siteValue: "+sitesInfo[j][count].get(requiredCellID-1));
								log.write("\nsiteValue: "+sitesInfo[j][count].get(requiredCellID-1));
								
								
							 	if (sitesInfo[j][count].get(requiredCellID - 1).equalsIgnoreCase(siteProperties[j].get(3))) {
									output.write(importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptCol")))) + "\n");
									System.out.print("\tPrint Line: " + cellCount + "\n\n");
									log.write("\n\tPrint Line: " + cellCount + "\n\n");
							 	}
							 	else {
							 		System.out.print("\tskip\n\n");
							 		log.write("\tskip\n\n");
							 	}
								cellCount++;
								
							}
							//Empty Line between different site properties
							output.write("\n");
						}
				
						
						
						//output cell-level code
						
						int cellScriptCodeColPos = 0;
						Row rowInstance = null;
						
						if (count == 0) cellsInfoStart = 0;
						else cellsInfoStart = cellsInfoEnd;
						cellsInfoEnd = cellsInfoEnd + scriptName[count].size() - 1;
						
						//modify SiteIDIn Formular Cell
						rowInstance = ws.getRow(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptRefRow")));
						rowInstance.getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptSiteIDRef"))).setCellValue(cellsInfo[cellsInfoStart].get(0));
						rowInstance.getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptLatRef"))).setCellValue(cellsInfo[cellsInfoStart].get(2));
						rowInstance.getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.siteScriptLongRef"))).setCellValue(cellsInfo[cellsInfoStart].get(3));
						
						XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
						//get the sheets specified in cellSheetNames(List<String>)
						Sheet[] sheets = new Sheet[cellSheetNames.size()];
					
						for (int i=0; i<cellSheetNames.size(); i++)
							sheets[i] = wb.getSheet(cellSheetNames.get(i));
						
						int checkStart = Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCheckStart"));
						
						System.out.println("===== Cell-level live/dead code");
						log.write("\n===== Cell-level live/dead code\n");
						//loop cells
						for (int i = cellsInfoStart; i < cellsInfoEnd; i++) {
							System.out.print("Cell " + cellsInfo[i].get(1));
							log.write("Cell " + cellsInfo[i].get(1));
							
							output.write("//Cell " + cellsInfo[i].get(1) + "\n\n");
							
							int groupNum = checkGroup(Integer.parseInt(cellsInfo[i].get(1)), cellGroup);
							if ( groupNum != -1) {
								ws = sheets[groupNum];
								System.out.println("  ==> belongs to group: " + cellSheetNames.get(groupNum));
								log.write("  ==> belongs to group: " + cellSheetNames.get(groupNum) + "\n");
								cellScriptCodeColPos = getCellScriptCodePos(Integer.parseInt(cellsInfo[i].get(1)));
							}
							else throw new Exception();
							
//							// if it is L26
//							if (checkGroup(Integer.parseInt(cellsInfo[i].get(1)))) {
//								ws = ws26;
//								System.out.println("  ==> belongs to L26");
//								cellScriptCodeColPos = getCellScriptCodePos(Integer.parseInt(cellsInfo[i].get(1)));
//							}
//							else {
//								ws = ws18;
//								cellScriptCodeColPos = getCellScriptCodePos(Integer.parseInt(cellsInfo[i].get(1)));
//								System.out.println("  ==> belongs to L18");
//							}
//							
							//use ws, cellsInfo, cellProperties to get output
							
							int row = checkStart;
							int propertiesCount = 0;
							int cellConditionCount = 0;
							
							//print all codes for a site
							//A record for return of writeDeadCode, 0 for rowNum, 1 for boolean value of whether end
							ArrayList<Integer> resultList;
							
							while (!importInterfaceImpl.returnCellValue(ws.getRow(row).getCell(0)).equalsIgnoreCase("end") && propertiesCount<(cellProperties.length - 4)) {
								
								//HardCoded 4 Cols before the Cells script
								cellConditionCount = cellProperties[propertiesCount + 4].size();
								
								for (int j = 2; j < cellConditionCount; j++) {
									
									if (cellsInfo[i].get(propertiesCount + 4).equalsIgnoreCase(cellProperties[propertiesCount + 4].get(j))) {
										
										do {
											//print until next *. reach
											row++;
											rowInstance = ws.getRow(row);
											if (rowInstance == null) {
												do{
													rowInstance = ws.getRow(++row);
												} while (rowInstance == null);
											}
											
											if (importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).equalsIgnoreCase("end")) 
												break;
											
											output.write(importInterfaceImpl.returnCellValue(
															rowInstance.getCell(cellScriptCodeColPos +
																		Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCodeCol")))) + "\n");
											
											
										} while(!importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).matches(".+[E,e][N,n][D,d]"));
										
										//start to write dead code until a question mark reach
										resultList = writeDeadCode(ws, row, cellScriptCodeColPos, output);
										row = resultList.get(0);

										if (resultList.get(1) == 1) break;
									}
									else {
										// skip the current part, skip until ....end meets.
										do {
											row++;
											rowInstance = ws.getRow(row);
											if (rowInstance == null) {
												do{
													rowInstance = ws.getRow(++row);
												} while (rowInstance == null);
											}
											if (importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).equalsIgnoreCase("end"))
												break;
										} while (!importInterfaceImpl.returnCellValue(
																rowInstance.getCell(0)).matches(".+[E,e][N,n][D,d]"));
									
									
									//start to write dead code until a question mark reach
									resultList = writeDeadCode(ws, row, cellScriptCodeColPos, output);
									row = resultList.get(0);

									if (resultList.get(1) == 1) break;
									}
								}
								propertiesCount++;
								
								
					//double Checking method __ Archive		
							
//								try {
//									//skip the first four attributes of a cell, which is eNodeBID, CellID, Lat. and Lon.
//									//if satisfied flag being checked, loop the positive part
//									if (cellsInfo[i].get(propertiesCount + 4).equalsIgnoreCase(cellProperties[propertiesCount + 4].get(2))) {
//										
//										do {
//											//print until next *. reach
//											row++;
//											rowInstance = ws.getRow(row);
//											if (rowInstance == null) {
//												do{
//													rowInstance = ws.getRow(++row);
//												} while (rowInstance == null);
//											}
//											
//											outputStr = importInterfaceImpl.returnCellValue(
//													ws.getRow(row).getCell(cellScriptCodeColPos + 
//															Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCodeCol")))) + "\n";
//											if (outputStr.contains(""))
//											output.write(importInterfaceImpl.returnCellValue(
//													ws.getRow(row).getCell(cellScriptCodeColPos + 
//															Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCodeCol")))) + "\n");
//											
//											
//										} while (!importInterfaceImpl.returnCellValue(
//														rowInstance.getCell(0)).matches("\\d+\\..+"));
//										
//										
//										//skip the negative part
//										do {
//											row++;
//											rowInstance = ws.getRow(row);
//											if (rowInstance == null) {
//												do{
//													rowInstance = ws.getRow(++row);
//												} while (rowInstance == null);
//											}
//											if (importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).equalsIgnoreCase("end"))
//												break;
//										} while (!importInterfaceImpl.returnCellValue(
//																rowInstance.getCell(0)).matches(".+[E,e]nd"));
//										propertiesCount++;
//										
//										//Start to write dead code until a question mark reach
//										resultList = writeDeadCode(ws, row, cellScriptCodeColPos, output);
//										row = resultList.get(0);
//										if (resultList.get(1) == 1) break;
//									}
//									else {
//										//skip the positive part
//										do {
//											row++;
//											rowInstance = ws.getRow(row);
//											if (rowInstance == null) {
//												do{
//													rowInstance = ws.getRow(++row);
//												} while (rowInstance == null);
//											}
//											
//										} while (!importInterfaceImpl.returnCellValue(
//														rowInstance.getCell(0)).matches("\\d+\\..+"));
//										
//										//loop the negative part
//										do {
//											//print until next *. reach
//											row++;
//											rowInstance = ws.getRow(row);
//											if (rowInstance == null) {
//												do{
//													rowInstance = ws.getRow(++row);
//												} while (rowInstance == null);
//											}
//											
//											if (importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).equalsIgnoreCase("end")) 
//												break;
//											
//											output.write(importInterfaceImpl.returnCellValue(
//															rowInstance.getCell(cellScriptCodeColPos +
//																		Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCodeCol")))) + "\n");
//											
//											
//										} while(!importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).matches(".+[E,e]nd"));
//										propertiesCount++;
//									
//										//Start to write dead code until a question mark reach
//										resultList = writeDeadCode(ws, row, cellScriptCodeColPos, output);
//										row = resultList.get(0);
//										if (resultList.get(1) == 1) break;
//									}
//								} catch (Exception e) {
//									System.out.println("RowInstance: " + (rowInstance == null) + " , row: " + row);
//									e.printStackTrace();
//								}

							}
							System.out.println("Cell level live Code printed for Cell " + cellsInfo[i].get(1));
							log.write("Cell level live Code printed for Cell " + cellsInfo[i].get(1) + "\n");
							
						}

						//print the missing CA cell.  Remember to add constrains for checkGroup function when L2100 come.
						ArrayList<String> missingCA = getMissingCACell(scriptName[count],cellsInfo,cellsInfoStart,cellsInfoEnd);

						System.out.println("MissingCA: ");
						System.out.println(missingCA);
						log.write("\nMissingCA: " + missingCA);
						
						//print the CAscript
						if (!missingCA.isEmpty()) {
							output.write("//For existing CA Cells\n\n");
							
							for (int i = 0; i < missingCA.size(); i++) {
								int cellCol = 4;
								String cellnum = "";
								String lineWritten = "";
								
//								//if its in L18
//								if (!checkGroup(Integer.parseInt(missingCA.get(i))))
//									ws = ws18;
//								else ws = ws26;
								
								int groupNum = checkGroup(Integer.parseInt(cellsInfo[i].get(1)), cellGroup);
								if ( groupNum != -1) {
									ws = sheets[groupNum];
									System.out.println("  ==> belongs to group: " + cellSheetNames.get(groupNum));
									log.write("\n  ==> belongs to group: \" + cellSheetNames.get(groupNum)");
								}
								else throw new Exception();
								
								//print according to ws
								cellnum = missingCA.get(i);
								output.write("//Cell " + cellnum + "\n");
								
								int j = checkStart;
								rowInstance = ws.getRow(j);
								Cell cellInstance = rowInstance.getCell(0);
								
								//find until CA flag found   // ends with CA?
								while (!importInterfaceImpl.returnCellValue(cellInstance).matches(".+CA.?\\?.?")) {
									rowInstance = ws.getRow(++j);
									while (rowInstance == null) 
										rowInstance = ws.getRow(++j);
									cellInstance = rowInstance.getCell(0);
								} 
								j++;
								
								System.out.println("Start to output CAscript..");
								log.write("\nStart to output CAscript");
								//start to print until end meets
								cellCol = 4 + Integer.parseInt(cellnum);
								rowInstance = ws.getRow(j);
								while (rowInstance == null)
									rowInstance = ws.getRow(++j);
								cellInstance = rowInstance.getCell(0);
								
								while(!importInterfaceImpl.returnCellValue(cellInstance).matches(".+[E,e][N,n][D,d]")){
									
									lineWritten = importInterfaceImpl.returnCellValue(rowInstance.getCell(cellCol));
									output.write(lineWritten + "\n");
									
									rowInstance = ws.getRow(++j);
									while(rowInstance == null)
										rowInstance = ws.getRow(++j);
									
									cellInstance = rowInstance.getCell(0);
								}
							}
								
						}
						
						System.out.println("Finished printing the Existing CA Script.");
						log.write("\n\nFinished printing the Existing CA Script.");
						
						//output file
						output.close();
					}
					
					wb.close();
				}
			}
	
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//close the log write
		try {
			log.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnMsg;
	}
	
*/	
	private ArrayList<String> getMissingCACell(ArrayList<String> scriptName, ArrayList<String>[] cellsInfo, int cellsInfoStart,int cellsInfoEnd, List<String>[] cellGroup) {
		
		ArrayList<String> missingCA = new ArrayList<String>();
		
		for (int i = 1; i < scriptName.size(); i++) {
			String currentCell = scriptName.get(i);
			String pairCell = "";
			int currentnum = 0;
			int pairnum = 0;
			
			//look for each cell's CA pair if its CA flag is on
			
			/*CAflag's position HARD-CODED : 6*/
			if (cellsInfo[cellsInfoStart+i-1].get(6).equalsIgnoreCase("yes")) {
				currentnum = Integer.parseInt(currentCell);
				
				int groupNum = checkGroup(currentnum,cellGroup);
				//if it's in L18
				if (groupNum == 0) {
					
					//and its pair is also inside, then ignore(continue)
					for (int j = 1; j < scriptName.size(); j++){
						pairCell = scriptName.get(j);
						pairnum = Integer.parseInt(pairCell);
						if (pairnum == currentnum + 3) break;
					}
					if (pairnum == currentnum + 3) continue;
					else missingCA.add(Integer.toString(currentnum + 3));
				}
				//if it's in L26
				else if (groupNum == 1){
					for (int j = 1; j < scriptName.size(); j++){
						pairCell = scriptName.get(j);
						pairnum = Integer.parseInt(pairCell);
						if (pairnum == currentnum - 3) break;
					}
					if (pairnum == currentnum - 3) continue;
					else missingCA.add(Integer.toString(currentnum - 3));
				}
			
			}
		}
			
		
		
		
		return missingCA;
	}
	
	
	private ArrayList<Integer> writeDeadCode(Sheet ws, int row, int cellScriptCodeColPos, BufferedWriter output ) {
		
		ImportReportInterfaceImpl importInterfaceImpl = new ImportReportInterfaceImpl();
		Row rowInstance;
		ArrayList<Integer> result = new ArrayList<Integer>();
		result.add(0);
		result.add(0);
		try {
			//output Cell-level dead Code
			do{} while (ws.getRow(++row) == null);
			rowInstance = ws.getRow(row);
			row--;
			
			do {
				
				row++; 
				rowInstance = ws.getRow(row);
				if (rowInstance == null) {
					do{
						rowInstance = ws.getRow(++row);
					} while (rowInstance == null);
				}
				// if meet "end"
				if (importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).equalsIgnoreCase("end")) { 
					result.set(1, 1);
					result.set(0, row);
					return result;
				}
				
				output.write(importInterfaceImpl.returnCellValue(
						rowInstance.getCell(cellScriptCodeColPos +
									Integer.parseInt(ResourceUtil.getCommonProperty("file.cellScriptCodeCol")))) + "\n");
				

			} while (!importInterfaceImpl.returnCellValue(rowInstance.getCell(0)).matches("\\d+\\..+"));
			
		} catch (Exception e) {
			System.out.println("error when: row = " + row);
			e.printStackTrace();
		}
		
		result.set(1, 0);
		result.set(0, row);
		
		return result;
	}

// get the cell script code column position by checking in coresponding cell group
	private int getCellScriptCodePos(int i, List<String>[] cellGroup) {
		
		int offset = 0;
		int groupNum = checkGroup(i,cellGroup);
		//e.g. if 1,2,3,7,8,9(L18) => 0,1,2,3,4,5
		//e.g. if 4,5,6,10,11,12 => 3,4,5,9,10,11
		
		if (groupNum != -1) {
			
			for(int j=0; j < cellGroup[groupNum].size(); j++) {
				if (Integer.parseInt(cellGroup[groupNum].get(j)) == i)
						break;
				else offset++;
			}
			
			return offset;
		
		}
		else return -1;
	}


//	public ArrayList<String>[][] validateGroup(ArrayList<String>[][] sitesInfo) {
//
//	
//	ArrayList<String>[][] newSitesInfo = new ArrayList[sitesInfo.length][sitesInfo[0].length];
//		
//	for (int j = 0; j<sitesInfo.length; j++) {
//			for (int i = 0; i<sitesInfo[j].length;i++) {
//				newSitesInfo[j][i] = new ArrayList<String>();
//				for (int k = 0; k<sitesInfo[j][i].size(); k++) {
//					newSitesInfo[j][i].add("");
//					
////					System.out.println("k = " + k +", sitesInfo: " + sitesInfo[j][i].get(k));
//					if ((!sitesInfo[j][i].get(k).equalsIgnoreCase("")) && checkGroup(k+1)) {
////						System.out.println("checkGroup: "+checkGroup(k));
////						System.out.println("k = " + k +", sitesInfo: " + sitesInfo[j][i].get(k));
//						newSitesInfo[j][i].set(k-3,sitesInfo[j][i].get(k));
//					}
//					else newSitesInfo[j][i].set(k, sitesInfo[j][i].get(k));
//				}
//			}
//		}
//	
//	return newSitesInfo;
//	}

	
	//Match the cellID to the given cellID Groups, return the groupnum, return -1 if not belongs to any group
	public int checkGroup(int k, List<String>[] cellGroup) {

		for (int i=0; i<cellGroup.length; i++){
			for (int j=0; j<cellGroup[i].size(); j++){
				if (Integer.toString(k).equalsIgnoreCase(cellGroup[i].get(j)))
						return i;
			}
		}
		
		return -1;
	}
	
//	public boolean checkGroup(int k) {
//
//		if (k%6 == 0 || k%6 == 5 || k%6 == 4)
//			return true;
//		
//		return false;
//	}

	private ArrayList<String>[][] readSitesInfo(Sheet ws, int rowNum, ArrayList<String>[] siteProperties) throws NumberFormatException, Exception {

//		System.out.println("Start to readSitesInfo..");
		ImportReportInterfaceImpl importInterfaceImpl = new ImportReportInterfaceImpl();
		if (rowNum % 3 != 0) throw new NumberFormatException();

		
		int rowStartCount = Integer.parseInt(ResourceUtil.getCommonProperty("file.scriptInputHeadersRownum"));
		int siteCount = 0;
		//get siteCount
		for (int i = rowStartCount; i < rowNum + rowStartCount; i++) {
			if (i == rowStartCount) {
				siteCount++;
			}
			else if (importInterfaceImpl.returnCellValue(ws.getRow(i-1).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.eNodeBIDCol")))).equalsIgnoreCase(
					importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.eNodeBIDCol")))))) continue;
			else siteCount++;
				
		}

		//get sitesInfo
		ArrayList<String>[][] siteInfo = new ArrayList[siteProperties.length][siteCount];
		siteCount = 0;
		int cellID = 0;
		String cellValue = "";
		
		
		for (int j = 0; j < siteProperties.length; j++) {
			for (int i = rowStartCount; i < rowNum + rowStartCount; i++) {
				
				cellValue =	importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(siteProperties[j].get(0))));
				cellID = Integer.parseInt(importInterfaceImpl.returnCellValue(
						ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.CellIDCol")))));

				//specially dealing with the first instance
				if (i == rowStartCount) {
					siteInfo[j][siteCount] = new ArrayList<String>();
					
					//minus the predata length at head
					for (int k = 0; k<Integer.parseInt(ResourceUtil.getCommonProperty("file.CellIDSize")); k++)
						siteInfo[j][siteCount].add("");
					
										
					siteInfo[j][siteCount].set(cellID - 1,cellValue);

				}
			
				//skip complete duplicate data
				else if (importInterfaceImpl.returnCellValue(ws.getRow(i-1).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.eNodeBIDCol")))).equalsIgnoreCase(
					importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.eNodeBIDCol")))))
					&&
					importInterfaceImpl.returnCellValue(ws.getRow(i-1).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.CellIDCol")))).equalsIgnoreCase(
							importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.CellIDCol"))))))  
						
						continue;
				//deal with different cells of same site
				else if (importInterfaceImpl.returnCellValue(ws.getRow(i-1).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.eNodeBIDCol")))).equalsIgnoreCase(
						importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.eNodeBIDCol")))))) {
					
						
						siteInfo[j][siteCount].set(cellID - 1,cellValue);
						
				}
				//deal with next site
				else {
					
					siteCount++;
					siteInfo[j][siteCount] = new ArrayList<String>();
					
					for (int k = 0; k<Integer.parseInt(ResourceUtil.getCommonProperty("file.CellIDSize")); k++)
						siteInfo[j][siteCount].add("");
				
					siteInfo[j][siteCount].set(cellID - 1,cellValue);
				}
			}
		}
//		System.out.println("End of readSitesInfo..");
		return siteInfo;
	}
	
	public ArrayList<String>[] readSiteProperties(List<SiteLevelRule> list){

		ArrayList<String>[] siteProperties = new ArrayList[list.size()];
		
		for(int i=0; i<list.size(); i++){
			siteProperties[i] = new ArrayList<String>();
			SiteLevelRule listItem = list.get(i);
			siteProperties[i].add(String.valueOf(Integer.parseInt((listItem.column))-1));
			siteProperties[i].add("4");
			siteProperties[i].add(listItem.attribute);
			siteProperties[i].add(listItem.KeyValue);
			
			List<String> det = listItem.determinants;
			for (int j=0; j<listItem.determinants.size(); j++){
				siteProperties[i].add(det.get(j));
			}
		}
		return siteProperties;
	}
	

	public ArrayList<String>[] readSiteProperties(Sheet ws, int row) {
//		System.out.println("Start to readSiteProperties..");
		int propertiesNum = row/2;
//		System.out.println("Properties Num: " + propertiesNum);
		ImportReportInterfaceImpl importInterfaceImpl = new ImportReportInterfaceImpl();
		ArrayList<String>[] siteProperties = new ArrayList[propertiesNum];
		
		for (int i = 0; i < propertiesNum; i++) {

			siteProperties[i] = new ArrayList<String>();
			for (int j = 0; j < ws.getRow(i*2).getLastCellNum(); j++)
//				siteProperties[i].add(ws.getRow(i).getCell(j).getStringCellValue());
				siteProperties[i].add(importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(j)));
			
			int k = 0;
			while (!importInterfaceImpl.returnCellValue(ws.getRow(i+1).getCell(k)).equalsIgnoreCase("")) {
				siteProperties[i].add(importInterfaceImpl.returnCellValue(ws.getRow(i+1).getCell(k)));
				k++;
			}
		}
//		System.out.println("End of readSiteProperties..");
		return siteProperties;
	}

	public ArrayList<String>[] readCellProperties(List<CellLevelRule> list){
		
		ArrayList<String>[] cellProperties = new ArrayList[list.size()];
		
		for(int i=0; i<list.size(); i++){
			CellLevelRule listItem = list.get(i);
			cellProperties[i] = new ArrayList<String>();
			cellProperties[i].add(String.valueOf(Integer.parseInt(listItem.column)-1));
			cellProperties[i].add(listItem.attribute);

			List<String> det = listItem.determinants;
			for(int j=0; j<det.size(); j++){
				cellProperties[i].add(det.get(j));
			}
		}
		
		
		return cellProperties;
	}

	public ArrayList<String>[] readCellProperties(Sheet ws, int row) {
//		System.out.println("Start to readCellProperties..");
		ImportReportInterfaceImpl importInterfaceImpl = new ImportReportInterfaceImpl();
		
		ArrayList<String>[] properties = new ArrayList[row-1];
		for (int i = 1; i < row; i++) {
			properties[i-1] = new ArrayList<String>();
			int colNum = ws.getRow(i).getLastCellNum();
			for (int j = 0; j < colNum; j++)
				properties[i-1].add(importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(j)));
		}
//		System.out.println("End of readCellProperties..");
		return properties;
	}
	
	public ArrayList<String>[] readCellInfo(Sheet ws, int row, ArrayList<String>[] properties) throws NumberFormatException, Exception {
		
//		System.out.println("Start to readCellInfo..");
		ImportReportInterfaceImpl importInterfaceImpl = new ImportReportInterfaceImpl();
		if (row % 3 != 0) throw new NumberFormatException();

		ArrayList<String>[] cellInfo = new ArrayList[row/3];
		int count = 0;
		int HeaderRowNum = Integer.parseInt(ResourceUtil.getCommonProperty("file.scriptInputHeadersRownum"));
			//skip duplicate data
		for (int i = HeaderRowNum; i < row + HeaderRowNum; i++) {
			
			if (ws.getRow(i+1)!=null) { 
				if (importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.eNodeBIDCol")))).equalsIgnoreCase(
						importInterfaceImpl.returnCellValue(ws.getRow(i+1).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.eNodeBIDCol")))))
						&& 
						importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.CellIDCol")))).equalsIgnoreCase(
								importInterfaceImpl.returnCellValue(ws.getRow(i+1).getCell(Integer.parseInt(ResourceUtil.getCommonProperty("file.CellIDCol")))))) {
					continue;
				}
			}
			
			cellInfo[count] = new ArrayList<String>();
			for (int j = 0; j < properties.length; j++) {
				
				try {
					if (j == 2 || j == 3) cellInfo[count].add(String.valueOf(ws.getRow(i).getCell(Integer.parseInt(properties[j].get(0))).getNumericCellValue()));
					else cellInfo[count].add(importInterfaceImpl.returnCellValue(ws.getRow(i).getCell(Integer.parseInt(properties[j].get(0)))));
				} catch (Exception e) {
//					System.out.println("Which cell: " + Integer.parseInt(properties[j].get(0)));
					
					System.out.println("error when  i = " + i + "\n\tj= " + j);
					e.printStackTrace();
				}
				
			}
			count++;
		}
//		System.out.println("End of readCellInfo..");
		return cellInfo;
		}
	
	public int getSitePropertiesLength(int cellidLength) {
		
		int cellgroup = cellidLength/6;
		int remainder = cellidLength - cellgroup*6;
		
		return 3*cellgroup + remainder + 4;

	}
	
	public ArrayList<String>[] getSiteName(ArrayList<String>[] cellsInfo, int siteNum) {
		
//		System.out.println("Start to getSiteName..");
		
//		System.out.println("siteNum: " + siteNum);
		ArrayList<String>[] siteNames = new ArrayList[siteNum];
		
		int siteCount = 0;
		
		for (int i = 0; i < cellsInfo.length; i++){
			
			//compare eNodeBID
			if (i == 0) {
				siteNames[0] = new ArrayList<String>();
				siteNames[0].add(cellsInfo[i].get(0));
				siteNames[0].add(cellsInfo[i].get(1));
				siteCount++;
			}
			else if  (cellsInfo[i-1].get(0).equalsIgnoreCase(cellsInfo[i].get(0))) {
				siteNames[siteCount-1].add(cellsInfo[i].get(1));
				continue;
			}
			else {
				siteNames[siteCount] = new ArrayList<String>();
				siteNames[siteCount].add(cellsInfo[i].get(0));
				siteNames[siteCount].add(cellsInfo[i].get(1));
				siteCount++;
			}
			
		}
//		System.out.println("End of getSiteName..");
		return siteNames;
	}
	
	public void printArrayOfArrayList(ArrayList<String>[] properties, BufferedWriter log) {
		
		System.out.println("=================PropertiesList=================");
		try {
			log.write("\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int i=0,j=0;
		try {
			for ( i = 0; i<properties.length; i++) {
				for ( j = 0; j<properties[i].size(); j++) {
					if (properties[i].get(j).equalsIgnoreCase("")) {
						System.out.print("Empty\t");
						log.write("Empty\t");
					}
					else {
						System.out.print(properties[i].get(j) + "\t\t");					
						log.write(properties[i].get(j) + "\t\t");
					}
				}
				System.out.println("");
				log.write("\n");
			}
		} catch (Exception e) {
			System.out.println("i,j = " + i + ", " + j);
			e.printStackTrace();
		}
		System.out.println("=================PropertiesEnd===================");
		try {
			log.write("\n=================PropertiesEnd===================");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
public void printArrayOfArrayList(ArrayList<String>[] properties) {
		
		System.out.println("=================PropertiesList=================");
		int i=0,j=0;
		try {
			for ( i = 0; i<properties.length; i++) {
				for ( j = 0; j<properties[i].size(); j++) {
					if (properties[i].get(j).equalsIgnoreCase("")) {
						System.out.print("Empty\t");
					}
					else {
						System.out.print(properties[i].get(j) + "\t\t");					
					}
				}
				System.out.println("");
			}
		} catch (Exception e) {
			System.out.println("i,j = " + i + ", " + j);
			e.printStackTrace();
		}
		System.out.println("=================PropertiesEnd===================");
		return;
	}
}