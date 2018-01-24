package com.huawei.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class uploadServlet
 */
@WebServlet("/UploadTemplateServlet")
@MultipartConfig
public class UploadTemplateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadTemplateServlet() {
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

		String path = this.getClass().getClassLoader().getResource("").getPath();
		path = URLDecoder.decode(path,"UTF-8");
		String[] pathArr = path.split("/WEB-INF/classes/");
		File rootDir = new File(pathArr[0] + "/resource");
		System.out.println("rootDir: " + rootDir);
		if(!rootDir.exists()) rootDir.mkdirs();
	
	    Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
	    System.out.println(filePart==null);
	    String fileName = getSubmittedFileName(filePart); // MSIE fix.
	    System.out.println("fileName: " + fileName);
	    // ... (do your job here)
	    File file = new File(rootDir, fileName);

	    try (InputStream input = filePart.getInputStream()) {
	        Files.copy(input, file.toPath());
	    }
		
		//Upload Operation
//		FileItemFactory factory = new DiskFileItemFactory();
//		ServletFileUpload upload = new ServletFileUpload(factory);
//		List<FileItem> items = null;
//		try {
//			items = upload.parseRequest(request);
//			
//
//		} catch (Exception e) {
//			e.printStackTrace();	
//		}
//		
//		//file has been uploaded to the temp file
//		for(FileItem item : items) {	
//			
//			String fileName = item.getName();
//			System.out.println(fileName);
//			File newFile = new File(rootDir + "/" + fileName);
//			try {
//				item.write(newFile);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			System.out.println("Have successfully uploaded the File: " + newFile);
//			//redirect/refresh the page
//			response.sendRedirect("currentTemplate.jsp");
//		}
		response.sendRedirect("currentTemplate.jsp");
	}
	
	
	private static String getSubmittedFileName(Part part) {
	    for (String cd : part.getHeader("content-disposition").split(";")) {
	        if (cd.trim().startsWith("filename")) {
	            String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	            return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // MSIE fix.
	        }
	    }
	    return null;
	}
}
