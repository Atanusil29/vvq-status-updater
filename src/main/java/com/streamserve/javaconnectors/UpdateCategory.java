package com.streamserve.javaconnectors;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import streamserve.connector.StrsConfigVals;
import streamserve.connector.StrsConnectable;
import streamserve.connector.StrsServiceable;

public class UpdateCategory implements StrsConnectable{
	
	static final String CONNECTOR_NAME = "Update Category output connector";
	static final String PROPERTYNAME_WEBREPORT = "Web report";
	static final String PROPERTYNAME_BARCODE = "Bar code";
	static final String PROPERTYNAME_STATUS = "Status";
	static final String PROPERTYNAME_CSURL = "Content Server URL";
	static final String PROPERTYNAME_USERNAME = "User name";
	static final String PROPERTYNAME_PASSWORD = "Password";

	String m_webReport;
	String m_barCode;
	String m_status;
	String m_csUrl;
	String m_userName;
	String m_password;
	String CS_Ticket = null;
	//File m_outFile;
	//OutputStream m_outStream;

	StrsServiceable m_service;
	
	public UpdateCategory() {
	}
	
	public void readConfigVals(StrsConfigVals configVals) {
		String webReport = configVals.getValue(PROPERTYNAME_WEBREPORT);
		if (webReport.length() > 0) {
			m_webReport = webReport;
		}
		
		String barCode = configVals.getValue(PROPERTYNAME_BARCODE);
		if (barCode.length() > 0) {
			m_barCode = barCode;
		}
		String status = configVals.getValue(PROPERTYNAME_STATUS);
		if (status.length() > 0) {
			m_status = status;
		}
		String csUrl = configVals.getValue(PROPERTYNAME_CSURL);
		if (csUrl.length() > 0) {
			m_csUrl = csUrl;
		}
		String userName = configVals.getValue(PROPERTYNAME_USERNAME);
		if (userName.length() > 0) {
			m_userName = userName;
		}
		String password = configVals.getValue(PROPERTYNAME_PASSWORD);
		if (password.length() > 0) {
			m_password = password;
		}
		if (m_service == null) {
			m_service = configVals.getStrsService();
		}
		
	}
	
	
	/**
	 * StrsConnectable implementation
	 * 
	 *  The StreamServer calls this method at the end of the Process, Document or Job. 
	 *  use this method to performed the final delivery.
	 *  If the connector supports runtime properties, these are passed in the ConfigVals object. 
	 */
	public boolean strsoClose(StrsConfigVals configVals) throws RemoteException {
		return true;
	}
	
	/**
	 * StrsConnectable implementation
	 * 
	 * 	The StreamServer calls this method each time it starts processing output data.
	 *  can be used to initialize resources according to connector properties set in Design Center.
	 *  The properties are passed in the ConfigVals object and can be accessed with getValue method.
	*/
	public boolean strsoOpen(StrsConfigVals configVals) throws RemoteException {
		try {
			readConfigVals(configVals);
			log(StrsServiceable.MSG_ERROR, 1, m_userName);
			log(StrsServiceable.MSG_ERROR, 1, m_password);
			log(StrsServiceable.MSG_ERROR, 1, m_webReport);
			log(StrsServiceable.MSG_ERROR, 1, m_csUrl);
			log(StrsServiceable.MSG_ERROR, 1, m_status);
			// Code to update category goes here
			JSONParser jsonParser = new JSONParser();
			HttpClient httpclient = HttpClients.createDefault();
			log(StrsServiceable.MSG_ERROR, 1, "OTCSTicket : HTTP Connection instance created");
			HttpPost httppost = new HttpPost(m_csUrl + "api/v1/auth");
			//System.out.println("CS_GetOTCSTicket : Post setup : added url : " + m_csUrl + "/api/v1/auth");
			log(StrsServiceable.MSG_ERROR, 1, "OTCSTicket : " + m_csUrl + "api/v1/auth");
			
			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("username", m_userName));
			params.add(new BasicNameValuePair("password", m_password));

			try {
				httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			//Execute and get the response.
			HttpResponse response;
			
			try {	
				System.out.println("OTCSTicket : Posting HTTP...");
				response = httpclient.execute(httppost);
				
				HttpEntity entity = (HttpEntity) response.getEntity();
				
				if (entity != null) {
					
				   InputStream instream = ((org.apache.http.HttpEntity) entity).getContent();
				   
				   // NB: does not close inputStream, you can use IOUtils.closeQuietly for that
				   String theString = IOUtils.toString(instream, "UTF-8"); 
				 
				   //Parse it to JSON for ease of reading
				  
				   JSONObject jsonObject = (JSONObject)jsonParser.parse(theString);
				   
				   try {				   	
					   //Try to obtain the ticket
				        CS_Ticket = (String) jsonObject.get("ticket");	
				        log(StrsServiceable.MSG_ERROR, 1, "OTCSTicket : Response received");
				        if (CS_Ticket.isEmpty()) {
				        	log(StrsServiceable.MSG_ERROR, 1,"Error: OTCS Ticket could not be parsed");
				        	//return "CS_GetOTCSTicket : Error : OTCS Ticket could not be parsed";
				        }
				        //System.out.println("OTCS Ticket: " + jsonObject.get("ticket"));
				        log(StrsServiceable.MSG_ERROR, 1, "OTCSTicket : Response received: "+ jsonObject.get("ticket"));
				    } 
				   finally {
				        instream.close();   
				    }	
				   //return "0";
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				//return "CS_GetOTCSTicket : Error : IO Exception";
				log(StrsServiceable.MSG_ERROR, 1, e.getLocalizedMessage());
			}
			//return "CS_GetOTCSTicket : Error : Could not connect, invalid credentials or response";
			//log(StrsServiceable.MSG_ERROR, 1, "GetOTCSTicket: Could not connect, invalid credentials or response");
			
			
			//Call web report
			String dl= m_csUrl + "api/v1/webreports/"+ m_webReport+ "?format=webreport&GUIDID=" + m_barCode + "&status=" + m_status;
			StringBuffer content = new StringBuffer();
						
			//Create the downloadURL
		    //System.out.println("CS_GetWebReport : added url :" + dl);
		    log(StrsServiceable.MSG_ERROR, 1, "WebReporturl:" + dl);
		    URL url;
		    
		    try {
		    
		        url = new URL(dl);
		        
		        //System.out.println("CS_GetWebReport : Posting HTTP...");
		        
		        //Open the connection
		        HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		        
		        //Set the request headers
		        connect.setRequestProperty("User-Agent","Mozilla/5.0");
		        connect.setRequestProperty("OTCSticket", CS_Ticket);  
		        
		        int status = connect.getResponseCode();
		        BufferedReader in = null;

		        if (status > 299) {
		        	in = new BufferedReader(new InputStreamReader(connect.getErrorStream()));
		        	//System.out.println("CS_GetWebReport : Retrieving the value did not succeed, url response code: "+connect.getResponseCode());
		        	//return "CS_GetWebReport : Error : Retrieving the value did not succeed, url response code: "+ connect.toString(); 
		            
		        } else {
		        	//System.out.println("CS_GetWebReport : Response received");
		        	in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
		        } 
		        
		        String inputLine;

				while ((inputLine = in.readLine()) != null) {
					   content.append(inputLine);
				}
				
				in.close();
			    connect.disconnect();

			    //System.out.println("CS_GetWebReport : Web Report : " + content.toString());
			    m_webReport = content.toString();
			    
			    //return "0";
			      
			    } catch (Exception ex) {
			    	//return "CS_GetWebReport : Exception : Connection did not succeed";
			    	//log(StrsServiceable.MSG_ERROR, 1, ex.getStackTrace().toString());
			    }
			
			
		} catch (Exception e) {
			//log(StrsServiceable.MSG_ERROR, 1, e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	protected void log(int msgType, int loglevel, String message)
			throws RemoteException {
		if (m_service != null) {
			m_service.writeMsg(msgType, loglevel, CONNECTOR_NAME + ": "
					+ message);
		}
	}
	
	/**
	 * StrsConnectable implementation
	 * 
	 * 	The StreamServer calls this method directly after the connector has been created.
	 *  Use this method to initialize resources according to the connector properties set in Design Center.
	 *  The properties are passed in the ConfigVals object and can be accessed with getValue method.
	*/
	public boolean strsoStartJob(StrsConfigVals configVals)
			throws RemoteException {
		try {
			readConfigVals(configVals);
		} catch (Exception e) {
			log(StrsServiceable.MSG_ERROR, 1, e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * StrsConnectable implementation
	 * 
	 *  This method is called between a pair of strsoOpen() and strsoClose() calls. It can be called several times or only once,
	 *  depending on the amount of data to be written. Each strsoWrite() call provides buffered output data.
	 */
	public boolean strsoWrite(byte[] bytes) throws RemoteException {
		return true;
	}
	
	/**
	 * StrsConnectable implementation
	 * 
	 *  The StreamServer calls this method when all data has been delivered by the output connector and before
	 *  the connector is removed. Use this method to release the resources used by the connector.
	 */
	public boolean strsoEndJob() throws RemoteException {
		// TODO Auto-generated method stub
		return true;
	}

}
