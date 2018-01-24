package com.huawei.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
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
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.huawei.service.ScriptBuilderInterface;
import com.huawei.service.ScriptBuilderInterfaceImpl;

/**
 * Servlet implementation class UploadDataServlet
 */
@WebServlet("/UploadDataServlet")
@MultipartConfig
public class UploadDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadDataServlet() {
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
		File rootDir = new File(pathArr[0] + "/data");
		System.out.println("rootDir: " + rootDir);
		if(!rootDir.exists()) rootDir.mkdirs();
		//delete the existing one if exists
		if (rootDir.listFiles() != null) {
			for(File file : rootDir.listFiles()) {
				System.out.println("Delete: " + file.delete());
			}
		}
		
		
	
//	    Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
//	    System.out.println(filePart==null);
//	    String fileName = getSubmittedFileName(filePart); // MSIE fix.
//	    System.out.println("fileName: " + fileName);
//	    // ... (do your job here)
//	    File file = new File(rootDir, fileName);
//
//	    try (InputStream input = filePart.getInputStream()) {
//	        Files.copy(input, file.toPath());
//	    } catch(Exception e) {
//	    	e.printStackTrace();
//	    }
		
		//Upload Operation
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = null;
		try {
			items = upload.parseRequest(request);
			

		} catch (Exception e) {
			e.printStackTrace();	
		}
		
		//file has been uploaded to the temp file
		for(FileItem item : items) {	
			
			String fileName = item.getName();
			System.out.println(fileName);
			File newFile = new File(rootDir + "/" + fileName);
			try {
				item.write(newFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Have successfully uploaded the File: " + newFile);
			//redirect/refresh the page
		}
		//Start the generation
		System.out.println("Start:");
		ScriptBuilderInterface scriptBuilder = new ScriptBuilderInterfaceImpl();
		scriptBuilder.startThread();
		System.out.println("End.");
		
		response.sendRedirect("End.html");
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
