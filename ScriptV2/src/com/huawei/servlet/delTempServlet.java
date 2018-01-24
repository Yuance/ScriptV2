package com.huawei.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class delTempServlet
 */
@WebServlet("/delTempServlet")
public class delTempServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public delTempServlet() {
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
		boolean flag = false;
		System.out.println("rootDir: " + rootDir);
		if(!rootDir.exists()) rootDir.mkdirs();
		//delete the existing one if exists
		if (rootDir.listFiles() != null) {
			for(File file : rootDir.listFiles()) {
				if (file.getName().endsWith(".xlsx")) {
					try {
						Files.delete(file.toPath());
						System.out.println("Successfully delete: " + file);
						flag = true;
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (flag) {
			response.getWriter().write("success");
		}
		else response.getWriter().write("fail");
	}

}
