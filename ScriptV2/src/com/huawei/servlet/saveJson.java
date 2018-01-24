package com.huawei.servlet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class saveJson
 */
@WebServlet("/saveJsonServlet")
public class saveJson extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public saveJson() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//get This json
		String data = request.getParameter("setting");
		String location = request.getParameter("location");
		System.out.println(data);
		System.out.println(location);
		
		//rewrite to the file
		File jsonFile = null;
		BufferedWriter jsonOutput = null;
		
		try {
			String path = this.getClass().getClassLoader().getResource("").getPath();
			path = URLDecoder.decode(path,"UTF-8");
			String[] pathArr = path.split("/WEB-INF/classes/");
			path = pathArr[0];
			System.out.println(path);
			// 3 cases of save json
			if (location.equalsIgnoreCase("site")) {
				jsonFile = new File(path + "/RuleFiles/siteLevelRule.json");

				jsonOutput = new BufferedWriter(new FileWriter(jsonFile));
				jsonOutput.write("{\"setting\":" + data +"}");
			}
			else if (location.equalsIgnoreCase("cell")) {
				jsonFile = new File(path + "/RuleFiles/cellLevelRule.json");

				jsonOutput = new BufferedWriter(new FileWriter(jsonFile));
				jsonOutput.write("{\"setting\":" + data +"}");
			}
			else if (location.equalsIgnoreCase("template")){
				jsonFile = new File(path + "/resource/sheetNames.json");
				jsonOutput = new BufferedWriter(new FileWriter(jsonFile));
				jsonOutput.write(data);
			}
			else if (location.equalsIgnoreCase("cellGroup")) {
				jsonFile = new File(path + "/resource/cellGroup.json");
				jsonOutput = new BufferedWriter(new FileWriter(jsonFile));
				jsonOutput.write(data);
			}
			else throw new IOException("Wrong data format from ajax");
			
			System.out.println(jsonFile);
			
			jsonOutput.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		//if success(no exception) return the success msg
		response.getWriter().write("success");
	}

}
