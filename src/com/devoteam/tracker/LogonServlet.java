package com.devoteam.tracker;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Date;
import java.util.Random;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.devoteam.tracker.model.SystemParameters;
import com.devoteam.tracker.model.User;
import com.devoteam.tracker.util.DBDeterminer;
import com.devoteam.tracker.util.ServletConstants;

import com.devoteam.tracker.util.Email;

public class LogonServlet extends HttpServlet {
	  /**
	 * 
	 */
	private static final long serialVersionUID = 2633490449348814829L;
	private String url;
	private User thisU;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
	    url = null;
	    thisU = null;
	    HttpSession session = null;
	    
	    url = DBDeterminer.determineURL();
	    /*if (url == null) {
	    	return;
	    }*/
	    RequestDispatcher dispatcher = null;
	    try {
			Random r = new Random();
			String ran = "?ran=" + String.valueOf(Math.abs(r.nextLong()));
	    	/*RequestDispatcher*/ dispatcher = getServletContext().getRequestDispatcher("/logon.jsp"+ran);
	    	if (url.startsWith("error:")) {
	    		throw new Exception(url);
	    	}
	    	Connection conn = DriverManager.getConnection(url);
	    	try {
	    		//String userId = req.getParameter("userId");
	    		String password = req.getParameter("password");
	    		String email = req.getParameter("email");
	    		String action = req.getParameter("action");
	    		if (action.equals("resetPwd")) {
	    			// first check that the user exists
	    			conn = null;
	    			boolean found = false;
			    	CallableStatement cstmt = null;
					long id = -999;
				    try {
				    	conn = DriverManager.getConnection(url);
				    	cstmt = conn.prepareCall("{call GetUserIdFromEmail(?)}");
						cstmt.setString(1, email);
						found = cstmt.execute();
						if (found) {
							ResultSet rs = cstmt.getResultSet();
							if (rs.next()) {
								id = rs.getLong(1);
							}
						}
						if ((id==-999)||(id==-99)) {
				        	req.setAttribute("userMessage", "Error: unable to determine if user exists");
						}
				    } catch (Exception ex) {
			        	req.setAttribute("userMessage", "Error: unable to reset user password, " + ex.getMessage());
			        } finally {
				    	try {
				    		if ((cstmt != null) && (!cstmt.isClosed()))	cstmt.close();
				    		if ((conn != null) && (!conn.isClosed())) conn.close();
					    } catch (SQLException ex) {
					    	ex.printStackTrace();
					    }
				    } 
				    if (id<0) {
				    	req.setAttribute("userMessage", 
						"Please enter valid email address");
				    } else if (id>0) {
					    try {
							conn = DriverManager.getConnection(url);
					    	cstmt = conn.prepareCall("{call GetSendGridAccountDetails()}");
							found = cstmt.execute();
							String sgAccount = "unavailable", sgPassword = "", sender = "", 
									endMessage = "", sgActive = "";
							if (found) {
								ResultSet rs = cstmt.getResultSet();
								if (rs.next()) {
									sgAccount = rs.getString(1);
									sgPassword = rs.getString(2);
									sender = rs.getString(3);
									endMessage = rs.getString(4);
									sgActive = rs.getString(5);
								}
							}
							if ((sgAccount.equals("unavailable")) || (sgAccount.equals("failed"))) {
					        	req.setAttribute("userMessage", "Error: unable to get SendGrid account details");
							} else {
								// Reset user password and send email
								String ret = null;
								try {
									conn = DriverManager.getConnection(url);
							    	cstmt = conn.prepareCall("{call ResetUserPassword(?,?)}");
									cstmt.setLong(1, id);
									cstmt.setString(2, "Password.Reset");
									found = cstmt.execute();
									if (found) {
										ResultSet rs = cstmt.getResultSet();
										if (rs.next()) {
											ret = rs.getString(1);
										}
									}
								}
								catch (Exception ex) {
							        	req.setAttribute("userMessage", "Error: unable to reset user password, " + ex.getMessage());
								}
								finally {
							    	try {
							    		if ((cstmt != null) && (!cstmt.isClosed()))	cstmt.close();
							    		if ((conn != null) && (!conn.isClosed())) conn.close();
									    } catch (SQLException ex) {
									    	ex.printStackTrace();
									    }
								}
								// SendGrid code
								//if (sgActive.equals("Y")) {
									Email e = new Email();
									String sgResult = e.sendResetEmail(email, ret, sgAccount, sgPassword, sender, endMessage);
									if (sgResult.equals("success")) {
										req.setAttribute(
												"userMessage", 
												"Password reset email has been sent to "+
												email +
												". <br>Please contact support if you don't receive this email.");
									} else {
										req.setAttribute("userMessage", sgResult );
									}
								/*} else {
									req.setAttribute("userMessage", "Password for "+email+" reset to "+ret);
								}*/	
							}
						    } catch (Exception ex) {
					        	req.setAttribute("userMessage", "Error: unable to get SendGrid account details, " + ex.getMessage());
						    } finally {
						    	try {
						    		if ((cstmt != null) && (!cstmt.isClosed()))	cstmt.close();
						    		if ((conn != null) && (!conn.isClosed())) conn.close();
							    } catch (SQLException ex) {
							    	ex.printStackTrace();
							    }
					    } 
				    }
	    		}
	    		else if (email == "" || password == "") {
	    			req.setAttribute("userMessage", 
					"Email and password cannot be blank! Try again!"+action);
	    		} else {
	    			/*long uId = 0;
	    			try {
	    				uId = Long.parseLong(userId);
	    			} catch (NumberFormatException nfe) {
	    				req.setAttribute("userMessage", 
			    			"User Id must be numeric! Try again!");
	    			}*/
	    			CallableStatement cstmt = conn.prepareCall("{call GetUserDets(?, ?)}");
	    			cstmt.setString(1, email);
	    			cstmt.setString(2, password);
	    			boolean found = cstmt.execute();
	    			if (found) {
	    				ResultSet rs = cstmt.getResultSet();
	    				if (rs.next()) {
	    					thisU = new User(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4),
	    						rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8),
	    						rs.getDate(9));
	    					if (thisU.getStatus().equalsIgnoreCase(User.STATUS_ACTIVE)) {
		    					if ((thisU.getUserType().equalsIgnoreCase(User.USER_TYPE_CUSTOMER)) ||
		    						(thisU.getUserType().equalsIgnoreCase(User.USER_TYPE_THIRD_PARTY))) {		    						
		    						getCustomers(conn);
		    					}
								session = req.getSession(false);
								if (session != null) {
									session.invalidate();
								}
								session = req.getSession(true);
								session.setAttribute(ServletConstants.DB_CONNECTION_URL_IN_SESSION, url);
								session.setAttribute(ServletConstants.USER_OBJECT_NAME_IN_SESSION, thisU);
								long passwordExpiry = rs.getLong(12);
								getSystemParameters(session, conn);
								Date rightNow = new Date();
								long diff = rightNow.getTime() - thisU.getChangeDT().getTime();
								if (diff > passwordExpiry) {
									req.setAttribute("userMessage", "Password for " + thisU.getFirstname() + 
										" " + thisU.getSurname() + " has expired. " +
										"Please enter old and new passwords.");
									session.setAttribute(ServletConstants.SCREEN_TITLE_IN_SESSION, ServletConstants.CHANGE_PASSWORD);
									dispatcher = getServletContext().getRequestDispatcher("/passwordChange.jsp"+ran);
									//dispatcher = getServletContext().getRequestDispatcher("/passwordChange");
								} else {
									req.setAttribute("userMessage",  "Logon successful");
									dispatcher = getServletContext().getRequestDispatcher("/userRole");
								}
		    				} else {
		    					if (thisU.getStatus().equalsIgnoreCase(User.STATUS_SUSPENDED)) {
		    						req.setAttribute("userMessage", 
						        			"This user is currently suspended. " + 
						        			"Please contact the system administrator.");
		    					} else {
									req.setAttribute("userMessage", 
											"Invalid logon details entered! Try again!");
		    					}
		    				}	
						} else {
							req.setAttribute("userMessage", 
								"Invalid logon details entered! Try again!");
						}
	    			} else {
					req.setAttribute("userMessage", 
						"Unable to retrieve logon details! Try again!");
			    	}
			    	cstmt.close();
			    }
	    	} catch (SQLException e) {
				req.setAttribute("userMessage", 
						"Error: " + e.getMessage());
			} finally {
			    conn.close();
				dispatcher.forward(req,resp);
			}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	private void getCustomers(Connection conn) throws SQLException {
		CallableStatement cstmt = null;
	    try {
			cstmt = conn.prepareCall("{call GetCustomersForUser(?)}");
			cstmt.setLong(1, thisU.getUserId());
			if (cstmt.execute()) {
				ResultSet rs = cstmt.getResultSet();
				while (rs.next()) {
					thisU.addCustomer(rs.getLong(1), rs.getString(2));
				}
			}	
		} catch (SQLException e) {
			throw new SQLException("calling GetCustomersForUser(), " + e.getMessage());
		} finally {
			cstmt.close();
		}
		
	}

    private void getSystemParameters(HttpSession session, Connection conn) 
        	throws SQLException {
		CallableStatement cstmt = null;
		boolean found = false;
		try {
			conn = DriverManager.getConnection(url);
			cstmt = conn.prepareCall("{call GetSystemParameters()}");
			if (cstmt.execute()) {
				ResultSet rs = cstmt.getResultSet();
				if (rs.next()) {
					SystemParameters sP = new SystemParameters(rs.getInt(1),
							rs.getInt(2), rs.getInt(3), rs.getInt(4), 
							rs.getInt(5), rs.getDate(6), rs.getString(7),
							rs.getInt(8));
					session.setAttribute(ServletConstants.SYSTEM_PARAMETERS_IN_SESSION, sP);
					found = true;
				}
			}
			if (!found) {
				throw new SQLException("no system parameters returned");
			}
		} catch (SQLException e) {
			throw new SQLException("calling GetSystemParameters(), " + e.getMessage());
		} finally {
			cstmt.close();
		}
    }
	
}	
