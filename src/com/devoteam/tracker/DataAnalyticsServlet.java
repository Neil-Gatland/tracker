package com.devoteam.tracker;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.devoteam.tracker.model.User;
import com.devoteam.tracker.util.ServletConstants;
import com.devoteam.tracker.util.UtilBean;
import com.devoteam.tracker.util.DataAnalytics;

public class DataAnalyticsServlet extends HttpServlet {

	private static final long serialVersionUID = 3042616730721566442L;
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String destination = "/dataAnalytics.jsp";
		HttpSession session = req.getSession(false);
		Random r = new Random();
		String ran = "?ran=" + String.valueOf(Math.abs(r.nextLong()));
		if (session == null) {
			destination = "/logon.jsp";
			session = req.getSession(true);
			session.setAttribute("userMessage", "Please enter a valid email address and password");
		} else {
			session.setAttribute("prevScreen", "dataAnalytics");
			String dataSourceName = req.getParameter("selectDataSourceName");
			String dataTemplateName = req.getParameter("selectDataTemplateName");
			String action = req.getParameter("selectedAction");
			String chartData = req.getParameter("chartData");
			String reportSQL = "";
			String reportName = "";
			String [] parameterArray = { "", "", "", "", "", "", "", "", "", "" };
			int parameterPos = 0;
			if ((action.equals("createChart"))||
				(action.equals("downloadRawData"))||
				(action.equals("downloadChartData"))) {
				String selectSQL = "", fromSQL = "", whereSQL = "", groupBySQL = "", orderBySQL = "";
				String chartDataNames = "";
				User thisU = (User)session.getAttribute(ServletConstants.USER_OBJECT_NAME_IN_SESSION);
				String url = (String)session.getAttribute(ServletConstants.DB_CONNECTION_URL_IN_SESSION);
				UtilBean uB = new UtilBean(thisU, destination.substring(1), url);
				DataAnalytics dA = new DataAnalytics(thisU,url);
				String message = null;
		    	Connection conn = null;
		    	CallableStatement cstmt = null;
		    	// Build SELECT, GROUP BY and ORDER BY SQL
		    	selectSQL = "SELECT ";
		    	groupBySQL = "GROUP BY ";
		    	orderBySQL = "ORDER BY ";
		    	try {
			    	conn = DriverManager.getConnection(url);
			    	cstmt = conn.prepareCall("{call GetDataTemplateTotals(?)}");
			    	cstmt.setString(1, dataTemplateName);
					boolean found = cstmt.execute();
					if (found) {
						ResultSet rs = cstmt.getResultSet();
						while (rs.next()) {
							String columnDBName = rs.getString(1);
							String totalType = rs.getString(2);
							String columnDisplayName = rs.getString(3);
							selectSQL = selectSQL+totalType+"("+columnDBName+") AS "+
							(totalType.equals("COUNT")?"No":"Total")+
							", "+columnDBName+" ";
							groupBySQL = groupBySQL+" "+columnDBName+" ";
							orderBySQL = orderBySQL+" "+columnDBName+" ";
							chartDataNames = chartDataNames+"data.addColumn('string', '"+columnDisplayName+"'); ";
						}
					}
			    } catch (Exception ex) {
			    	message = "Error in GetDataTemplateTotals(): " + ex.getMessage();
			    	ex.printStackTrace();
			    } finally {
			    	try {
			    		if ((cstmt != null) && (!cstmt.isClosed()))	cstmt.close();
			    		if ((conn != null) && (!conn.isClosed())) conn.close();
				    } catch (SQLException ex) {
				    	ex.printStackTrace();
				    }
			    } 
		    	// Build FROM SQL
		    	fromSQL = "FROM "+dA.GetDataSourceView(dataSourceName)+" ";
				// Get control names for parameters and build WHERE SQL
			    try {
			    	conn = DriverManager.getConnection(url);
			    	cstmt = conn.prepareCall("{call GetDataTemplateParameterControls(?)}");
			    	cstmt.setString(1, dataTemplateName);
					boolean found = cstmt.execute();
					if (found) {
						ResultSet rs = cstmt.getResultSet();
						boolean whereUsed = false;
						while (rs.next()) {
							String control = rs.getString(1);
							String controlValue = req.getParameter(control);
							String originalControlValue = controlValue;
							if ((control.equals("selectStartDate"))||(control.equals("selectEndDate"))) {
								if ((controlValue.equals(""))&&(control.equals("selectStartDate"))) {
									controlValue = "2010-01-01";
								} else if ((controlValue.equals(""))&&(control.equals("selectEndDate"))) {
									controlValue = "2030-12-31";
								} else {
									controlValue=
										controlValue.substring(6, 10)+"-"+
										controlValue.substring(3, 5)+"-"+
										controlValue.substring(0, 2);
								}
							}
							String columnDBName = rs.getString(2);
							String expressionCondition = rs.getString(3);
							if (whereUsed) {
								whereSQL=whereSQL+"AND ";
							}
							else {
								whereSQL = whereSQL+"WHERE ";
								whereUsed = true;
							}
							whereSQL = whereSQL+columnDBName+" "+expressionCondition+" '"+controlValue+"' ";
							controlValue = originalControlValue;
							parameterArray[parameterPos] = controlValue;
							parameterPos++;
						}
					}
			    } catch (Exception ex) {
			    	message = "Error in GetDataTemplateParameterControls(): " + ex.getMessage();
			    	ex.printStackTrace();
			    } finally {
			    	try {
			    		if ((cstmt != null) && (!cstmt.isClosed()))	cstmt.close();
			    		if ((conn != null) && (!conn.isClosed())) conn.close();
				    } catch (SQLException ex) {
				    	ex.printStackTrace();
				    }
			    } 
				String finalSQL = selectSQL+fromSQL+whereSQL+groupBySQL+orderBySQL;
				// Use SQL to build chart data
				chartData = chartDataNames +
						"data.addColumn('number', 'No.'); "+
						"data.addRows([ ";
				PreparedStatement pstmt = null;	
				try {
			    	conn = DriverManager.getConnection(url);
			    	pstmt = conn.prepareStatement(finalSQL);
					boolean found = pstmt.execute();
					if (found) {
						ResultSet rs = pstmt.getResultSet();
						while (rs.next()) {
							chartData=chartData+"['"+rs.getString(2)+"', "+rs.getString(1)+"], ";
						}
					}
			    } catch (Exception ex) {
			    	message = "Error in prepared SQL for chart: " + ex.getMessage();
			    	ex.printStackTrace();
			    } finally {
			    	try {
			    		if ((cstmt != null) && (!cstmt.isClosed()))	cstmt.close();
			    		if ((conn != null) && (!conn.isClosed())) conn.close();
				    } catch (SQLException ex) {
				    	ex.printStackTrace();
				    }
			    } 
    			chartData = chartData+"]);";
    			// amend SELECT and ORDER BY SQL to create report SQL for raw data CSV
    			if ((action.equals("downloadRawData"))) {
    				selectSQL = "SELECT * ";
    				orderBySQL = "ORDER BY ";
    				try {
    			    	conn = DriverManager.getConnection(url);
    			    	cstmt = conn.prepareCall("{call GetDataTemplateSorts(?)}");
    			    	cstmt.setString(1, dataTemplateName);
    					boolean found = cstmt.execute();
    					if (found) {
    						ResultSet rs = cstmt.getResultSet();
    						boolean whereUsed = false;
    						while (rs.next()) {
    							String columnDBName = rs.getString(1);
    							String sortType = rs.getString(2);
    							orderBySQL = orderBySQL+columnDBName+" "+sortType+", ";    						}
    					}
    			    } catch (Exception ex) {
    			    	message = "Error in GetDataTemplateParameterControls(): " + ex.getMessage();
    			    	ex.printStackTrace();
    			    } finally {
    			    	try {
    			    		if ((cstmt != null) && (!cstmt.isClosed()))	cstmt.close();
    			    		if ((conn != null) && (!conn.isClosed())) conn.close();
    				    } catch (SQLException ex) {
    				    	ex.printStackTrace();
    				    }
    			    }
    				orderBySQL = orderBySQL.substring(0,orderBySQL.length()-2);
    				reportSQL = selectSQL+fromSQL+whereSQL+orderBySQL;
    				reportName = 
						dataTemplateName.replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", "")+
						(parameterArray[0].equals("")?
								"":"_"+parameterArray[0].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[1].equals("")?
								"":"_"+parameterArray[1].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[2].equals("")?
								"":"_"+parameterArray[2].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[3].equals("")?
								"":"_"+parameterArray[3].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[4].equals("")?
								"":"_"+parameterArray[4].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[5].equals("")?
								"":"_"+parameterArray[5].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[6].equals("")?
								"":"_"+parameterArray[6].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[7].equals("")?
								"":"_"+parameterArray[7].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[8].equals("")?
								"":"_"+parameterArray[8].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[9].equals("")?
								"":"_"+parameterArray[9].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						"_RawData";   				
    			}
    			// ue existing SQL to download chart data as a CSV
    			if (action.equals("downloadChartData")) {
    				reportSQL = finalSQL;
    				reportName = 
						dataTemplateName.replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", "")+
						(parameterArray[0].equals("")?
								"":"_"+parameterArray[0].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[1].equals("")?
								"":"_"+parameterArray[1].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[2].equals("")?
								"":"_"+parameterArray[2].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[3].equals("")?
								"":"_"+parameterArray[3].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[4].equals("")?
								"":"_"+parameterArray[4].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[5].equals("")?
								"":"_"+parameterArray[5].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[6].equals("")?
								"":"_"+parameterArray[6].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[7].equals("")?
								"":"_"+parameterArray[7].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[8].equals("")?
								"":"_"+parameterArray[8].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						(parameterArray[9].equals("")?
								"":"_"+parameterArray[9].replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", ""))+
						"_ChartData";   				 
    			}
			} 			
			req.setAttribute("dataSourceName", dataSourceName);
			req.setAttribute("dataTemplateName", dataTemplateName);
			req.setAttribute("chartData", chartData);
			session.setAttribute("chartData", chartData);
			req.setAttribute("parameter0", parameterArray[0]);
			req.setAttribute("parameter1", parameterArray[1]);
			req.setAttribute("parameter2", parameterArray[2]);
			req.setAttribute("parameter3", parameterArray[3]);
			req.setAttribute("parameter4", parameterArray[4]);
			req.setAttribute("parameter5", parameterArray[5]);
			req.setAttribute("parameter6", parameterArray[6]);
			req.setAttribute("parameter7", parameterArray[7]);
			req.setAttribute("parameter8", parameterArray[8]);
			req.setAttribute("parameter9", parameterArray[9]);
			session.setAttribute("parameter0", parameterArray[0]);
			session.setAttribute("parameter1", parameterArray[1]);
			session.setAttribute("parameter2", parameterArray[2]);
			session.setAttribute("parameter3", parameterArray[3]);
			session.setAttribute("parameter4", parameterArray[4]);
			session.setAttribute("parameter5", parameterArray[5]);
			session.setAttribute("parameter6", parameterArray[6]);
			session.setAttribute("parameter7", parameterArray[7]);
			session.setAttribute("parameter8", parameterArray[8]);
			session.setAttribute("parameter9", parameterArray[9]);
			req.setAttribute("reportSQL", reportSQL);
			req.setAttribute("reportName", reportName);
			session.setAttribute("reportSQL", reportSQL);
			session.setAttribute("reportName", reportName);
		}
		  	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(destination+ran);
		  	dispatcher.forward(req,resp);
	    }
	
}