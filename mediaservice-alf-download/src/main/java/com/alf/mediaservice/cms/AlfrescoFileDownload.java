package com.alf.mediaservice.cms;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class AlfrescoFileDownload implements IAlfrescoExport{

	static FileReader reader = null;
	static Properties p=new Properties();
	static List<AlfrescoFile> af = new ArrayList<AlfrescoFile>();
	static HashMap<String,String> pMap = new LinkedHashMap<String,String>();
	static {
		
		try {
			reader = new FileReader(confFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			p.load(reader);
			} catch (IOException e) {
			e.printStackTrace();
		}
		af = readCSVfile(p.getProperty("csvpath"));
	}
	
	public void fileDownload(String ticket) throws ClientProtocolException, IOException{
		String ticketURL = "?ticket=" + ticket;
		String defaultURL = null;
      	defaultURL = defaultURL+ticketURL;
     	HttpGet httpget = new HttpGet(defaultURL);
		org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
		long len = entity.getContentLength();
		InputStream inputStream = entity.getContent();
		
		}
	}
	
	 private static String sUserName;
	    private static String sPassword;
	    private static String SERVER_URL;
	    private String ticket;
	    private static String filepath = p.getProperty("filepath");
	    Authenticator basicAuth = null;
	
public String getAlfLoginTkt() throws IOException {
		

    	InputStream responseStr = null;
		String serviceLoginUrl = p.getProperty("serviceLoginUrl");
		HttpMethod method = null;
		sUserName = p.getProperty("sUserName");
    	sPassword =  p.getProperty("sPassword");
    	if(SERVER_URL==null)
    	SERVER_URL=p.getProperty("SERVER_URL");
		try {
			HttpClient httpclient = new HttpClient();
			String url = SERVER_URL + serviceLoginUrl;
			method = new GetMethod(url);
			method.setFollowRedirects(true);
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));
			List params = new ArrayList();
			NameValuePair nv = new NameValuePair("u", sUserName);
			NameValuePair nv2 = new NameValuePair("pw", sPassword);
			
			NameValuePair[] parameters = new NameValuePair[2];
			parameters[0] = nv;
			parameters[1] = nv2;
			params.add(nv);
			params.add(nv2);
			method.setQueryString(parameters);
			System.out.println("method is "+method.toString());
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(10000);
			httpclient.executeMethod(method);
			responseStr = method.getResponseBodyAsStream();
			InputStreamReader is = new InputStreamReader(responseStr);
			BufferedReader br = new BufferedReader(is);
			String read = br.readLine();
			StringBuffer sb = new StringBuffer(read);
			while (read != null) {
				if (!sb.toString().contains("ticket>")) {
					read = br.readLine();
					sb.append(read);
				} else
					break;
			}
			String response = sb.toString();
			Pattern pattern = Pattern.compile("ticket>.*?<");
			Matcher m = pattern.matcher(response);
			StringBuffer text = new StringBuffer();
			while (m.find()) {
				  text.append( m.group().replace("ticket>", "").replace("<", "").trim()  );
				}
			this.setTicket(text.toString());
			
			System.out.println("Alfresco Ticket is : " + ticket);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
		return ticket;
	}

public String getTicket() {
	return ticket;
}

public void setTicket(String ticket) {
	this.ticket = ticket;
}
 public static void main (String s[]) throws IOException{
	 AlfrescoFileDownload afd = new AlfrescoFileDownload();
	 System.out.println(afd.getAlfLoginTkt());
	 
	 String loginticket = afd.getAlfLoginTkt();
	 af.forEach(alfrescoFile->{
		 InputStream fileStream = null;
		 fileStream =  afd.getAlfrescoInputStream(alfrescoFile.getNodeID(),loginticket);
		 try {
			if(fileStream.read() != 60){
			 File targetFile = null;
			 if(p.getProperty("addnodeidwithfilename").equalsIgnoreCase("Y"))
			 targetFile = new File(filepath+"["+alfrescoFile.getNodeID()+"]"+alfrescoFile.getFileName());
			 else
				 targetFile = new File(filepath+alfrescoFile.getFileName());	 
			 try {
				FileUtils.copyInputStreamToFile(fileStream, targetFile);
			} catch (Exception e) {
					e.printStackTrace();
			}
 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 });
	 AppZip.createZipFile();
 }
 
 public  InputStream getAlfrescoInputStream(String nodeId,String ticket){
		
	  HttpClient httpclient = new HttpClient();
       String responseStr = null;
       HttpMethod method = null;
       String viewUrl = null;
       String serviceUrl = p.getProperty("serviceUrl");
       String url = null ;
       InputStream fileStream = null;
       try {
           if(null != ticket)
           url = SERVER_URL+serviceUrl+nodeId;
           method = new GetMethod(url);
           method.setFollowRedirects(true);
           method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                               new DefaultHttpMethodRetryHandler(3, false));
           NameValuePair[] parameters = {new NameValuePair("nodeId",nodeId),
                                   new NameValuePair("alf_ticket",this.ticket)};
           
           method.setQueryString(parameters);
           httpclient.getHttpConnectionManager().getParams()
						.setConnectionTimeout(10000);
           httpclient.executeMethod(method);
           
           fileStream=new BufferedInputStream(method.getResponseBodyAsStream());
           return fileStream;
       }catch(Exception e) {
    
            new Exception(e.getMessage(),e);
            return null;
       }
        
           
}

 private static List<AlfrescoFile> readCSVfile(String csvfilepath){
	 List<AlfrescoFile> afileList = new ArrayList<AlfrescoFile>();
     String line = "";
     String cvsSplitBy = ",";

     try (BufferedReader br = new BufferedReader(new FileReader(csvfilepath))) {

         while ((line = br.readLine()) != null) {

      
             String[] entityline = line.split(cvsSplitBy);
             afileList.add(new AlfrescoFile(entityline[0],entityline[1],entityline[2],entityline[3]));
         }

     } catch (IOException e) {
         e.printStackTrace();
     }
     return afileList;
 }
 
 
}

class AlfrescoFile {
	 
	String nodeID;
	 String fileName;
	 String attachmentId;
	 String title;
	 
 public String getNodeID() {
	return nodeID;
}
public void setNodeID(String nodeID) {
	this.nodeID = nodeID;
}
public String getFileName() {
	return fileName;
}
public void setFileName(String fileName) {
	this.fileName = fileName;
}

public AlfrescoFile(String nodeID, String fileName,String attachmentId,String title) {
	super();
	this.nodeID = nodeID;
	this.fileName = fileName;
	this.attachmentId = attachmentId;
	this.title = title;
}
public String getAttachmentId() {
	return attachmentId;
}
public void setAttachmentId(String attachmentId) {
	this.attachmentId = attachmentId;
}
public String getTitle() {
	return title;
}
public void setTitle(String title) {
	this.title = title;
}


}

