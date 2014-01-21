package fr.gkuhmel.jahia.modules.googleplus;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * THis class handles the communication between Jahia Server and Google.
 * @author gkuhmel
 */
public class GoogleAPIClient {

    private static Logger logger = LoggerFactory.getLogger(GoogleAPIClient.class);
    
	private String accessToken;
	
    /*
     * Proxy settings.
     */
    private Boolean proxy;    
    private String httpProxyURL;
    private int httpProxyPort;
    private String httpProxyLogin;
    private String httpProxyPassword;
    
    private String clientId;
    private String clientSecret;
    private String redirectUri;

    private String permissionList;
   
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public JsonObject getActivities(String userId, String collection) {
		
		JsonObject obj = null;
		
        try {     	 
    		 CloseableHttpClient httpclient = null;
    		 HttpGet httpget = null;
    		 URI uri = null;
    		 
        	 if (this.proxy) {
    			 CredentialsProvider credsProvider = new BasicCredentialsProvider();
    		     credsProvider.setCredentials(
    		                new AuthScope(this.httpProxyURL, this.httpProxyPort),
    		                new UsernamePasswordCredentials(this.httpProxyLogin, this.httpProxyPassword));
    		    httpclient = HttpClients.custom()
    		    		.setDefaultCredentialsProvider(credsProvider).build();
	
	            HttpHost proxy = new HttpHost(this.httpProxyURL, this.httpProxyPort);

	            RequestConfig config = RequestConfig.custom()
	                .setProxy(proxy)
	                .build();
	            
	            uri = new URIBuilder()
	            .setScheme("https")
	            .setHost("www.googleapis.com")
	            .setPort(443)
	            .setPath("/plus/v1/people/" + userId + "/activities/" + collection)
	            .setParameter("access_token", this.accessToken)
	            .build();
	            
	            httpget = new HttpGet(uri);
	            httpget.setConfig(config);
	            
    		 } else {
    			 httpclient = HttpClients.custom().build();
	
    	         uri = new URIBuilder()
    	            .setScheme("https")
    	            .setHost("www.googleapis.com")
    	            .setPort(443)
    	            .setPath("/plus/v1/people/" + userId + "/activities/" + collection)
    	            .setParameter("access_token", this.accessToken)
    	            .build();
    	         httpget = new HttpGet(uri);       	         
    		 }
        	
            logger.debug("executing request: " + httpget.getRequestLine());
            logger.debug("to target: " + uri.getHost());

            CloseableHttpResponse response = httpclient.execute(httpget);

            HttpEntity entity = response.getEntity();
            logger.debug(response.getStatusLine().toString());
            if (entity != null) {
            	logger.debug("Response content length: " + entity.getContentLength());
                String jsonResponse = EntityUtils.toString(entity);
                
                JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
                obj = jsonReader.readObject();                
            }
            EntityUtils.consume(entity);

        } catch (ClientProtocolException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
        finally {
            	
        }     
        return obj;   
	}
	
	/**
     * This method retrieves the access token from the gpCode, using a POST request
     * to GG API
     * @param gpCode
     * @return
     * @throws IOException
     */
    public String getAccessToken(String gpCode)
            throws IOException {
    		 
    		 String token = "";
    		 HttpHost targetHost = null;
    		 CloseableHttpClient httpclient = null;
    		 HttpPost httpPostToken = null;
    		 
    		 if (this.proxy) {
    			 CredentialsProvider credsProvider = new BasicCredentialsProvider();
    		     credsProvider.setCredentials(
    		                new AuthScope(this.httpProxyURL, this.httpProxyPort),
    		                new UsernamePasswordCredentials(this.httpProxyLogin, this.httpProxyPassword));
    		    httpclient = HttpClients.custom()
    		    		.setDefaultCredentialsProvider(credsProvider).build();

    		    targetHost =  new HttpHost("accounts.google.com",443,"https");
	            HttpHost proxy = new HttpHost(this.httpProxyURL, this.httpProxyPort);

	            RequestConfig config = RequestConfig.custom()
	                .setProxy(proxy)
	                .build();
	            
	             httpPostToken = new HttpPost("/o/oauth2/token");
	             httpPostToken.setConfig(config);
    		 } else {
    			 httpclient = HttpClients.custom().build();
    			 
    			 targetHost =  new HttpHost("accounts.google.com",443,"https");
                 httpPostToken = new HttpPost("/o/oauth2/token");
    		 }
    		
             
             List <NameValuePair> nvps = new ArrayList <NameValuePair>();                      
             nvps.add(new BasicNameValuePair("client_id", this.clientId));
             nvps.add(new BasicNameValuePair("client_secret", this.clientSecret));
             nvps.add(new BasicNameValuePair("code", gpCode));
             nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
             nvps.add(new BasicNameValuePair("redirect_uri", this.redirectUri));
             
             try {
	             httpPostToken.setEntity(new UrlEncodedFormEntity(nvps));
	
	             logger.debug("executing request: " + httpPostToken.getRequestLine());        
	             logger.debug("to target: " + targetHost);
	
	             HttpResponse responseToken = httpclient.execute(targetHost, httpPostToken);         		            		            		            	
	             HttpEntity entity = responseToken.getEntity();

	             logger.debug(responseToken.getStatusLine().toString());
                 if (entity != null) {
                	 logger.debug("Response content length: " + entity.getContentLength());
                     String responseBody = EntityUtils.toString(entity);
                     logger.debug("Response : " + responseBody);
                     
                     JsonReader jsonReader = Json.createReader(new StringReader(responseBody));
                     JsonObject obj = jsonReader.readObject();
                     token = obj.getString("access_token");
                     
                     logger.debug("Access token : " + token);                                                
                 }      
                          
             } catch (UnsupportedEncodingException e1) {
    			e1.printStackTrace();
    			logger.error(e1.getMessage());
    		} catch (ClientProtocolException e1) {
    			e1.printStackTrace();
    			logger.error(e1.getMessage());
    		} catch (IOException e1) {
    			e1.printStackTrace();
    			logger.error(e1.getMessage());
    		}  
             
            return token;                     
    }
    
    /**
     * This methode request google api to get user info
     * @param gpToken
     * @return
     */
    public JsonObject getUserInfo(String gpToken) {
		JsonObject obj = null;
        try {     	 
    		 CloseableHttpClient httpclient = null;
    		 HttpGet httpget = null;
    		 URI uri = null;
    		 
        	 if (this.proxy) {
    			 CredentialsProvider credsProvider = new BasicCredentialsProvider();
    		     credsProvider.setCredentials(
    		                new AuthScope(this.httpProxyURL, this.httpProxyPort),
    		                new UsernamePasswordCredentials(this.httpProxyLogin, this.httpProxyPassword));
    		    httpclient = HttpClients.custom()
    		    		.setDefaultCredentialsProvider(credsProvider).build();
	
	            HttpHost proxy = new HttpHost(this.httpProxyURL, this.httpProxyPort);

	            RequestConfig config = RequestConfig.custom()
	                .setProxy(proxy)
	                .build();
	            
	            uri = new URIBuilder()
	            .setScheme("https")
	            .setHost("www.googleapis.com")
	            .setPort(443)
	            .setPath("/userinfo/v2/me")
	            .setParameter("access_token", gpToken)
	            .build();
	            httpget = new HttpGet(uri);
	            httpget.setConfig(config);
	            
    		 } else {
    			 httpclient = HttpClients.custom().build();
	
    	         uri = new URIBuilder()
    	            .setScheme("https")
    	            .setHost("www.googleapis.com")
    	            .setPort(443)
    	            .setPath("/userinfo/v2/me")
    	            .setParameter("access_token", gpToken)
    	            .build();
    	         httpget = new HttpGet(uri);       	         
    		 }
        	
        	logger.debug("executing request: " + httpget.getRequestLine());
        	logger.debug("to target: " + uri.getHost());

            CloseableHttpResponse response = httpclient.execute(httpget);

            HttpEntity entity = response.getEntity();

            logger.debug(response.getStatusLine().toString());
            if (entity != null) {
            	logger.debug("Response content length: " + entity.getContentLength());
                String jsonResponse = EntityUtils.toString(entity);
                
                JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
                obj = jsonReader.readObject();
  
            }
            EntityUtils.consume(entity);

        } catch (ClientProtocolException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
        finally {
            	
        }	        
        return obj;
	}

	public Boolean getProxy() {
		return proxy;
	}

	public void setProxy(Boolean proxy) {
		this.proxy = proxy;
	}

	public String getHttpProxyURL() {
		return httpProxyURL;
	}

	public void setHttpProxyURL(String httpProxyURL) {
		this.httpProxyURL = httpProxyURL;
	}

	public int getHttpProxyPort() {
		return httpProxyPort;
	}

	public void setHttpProxyPort(int httpProxyPort) {
		this.httpProxyPort = httpProxyPort;
	}

	public String getHttpProxyLogin() {
		return httpProxyLogin;
	}

	public void setHttpProxyLogin(String httpProxyLogin) {
		this.httpProxyLogin = httpProxyLogin;
	}

	public String getHttpProxyPassword() {
		return httpProxyPassword;
	}

	public void setHttpProxyPassword(String httpProxyPassword) {
		this.httpProxyPassword = httpProxyPassword;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getPermissionList() {
		return permissionList;
	}

	public void setPermissionList(String permissionList) {
		this.permissionList = permissionList;
	}

}
