<%@page import="javax.json.JsonNumber"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonValue"%>

<%@ page import="fr.gkuhmel.jahia.modules.googleplus.*" %>
<%@ page import="org.jahia.services.SpringContextSingleton" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gpUtil" uri="http://www.jahia.org/tags/googleplus" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="currentAliasUser" type="org.jahia.services.usermanager.JahiaUser"--%>

<c:if test="${renderContext.loggedIn && currentAliasUser.username != 'guest'}">
    <c:if test="${renderContext.user.providerName == 'googleplus'}">
    	 <h3>Your public activity&nbsp;</h3>
    	 <c:set var="accessToken" value="${renderContext.user.properties['access_token']}" scope="request"/>
    	 
         <%        
         String accessToken = (String) request.getAttribute("accessToken");
         GoogleAPIClient client = (GoogleAPIClient) SpringContextSingleton.getModuleBean("GoogleAPIClient");
         client.setAccessToken(accessToken);
        
         JsonObject object = client.getActivities("me", "public");        
         object.toString();
         
      	 //reading arrays from json
         JsonArray jsonArray = object.getJsonArray("items");
 
         for(JsonValue value : jsonArray){
        	StringBuffer bf = new StringBuffer();      	 
          	bf.append("<p>");
         	
          	JsonObject item = (JsonObject) value;
         	JsonObject itemObject = item.getJsonObject("object");
         	String itemContent = itemObject.getString("content");
         	JsonObject replies = itemObject.getJsonObject("replies");
         	JsonNumber nbReplies = replies.getJsonNumber("totalItems");
         	JsonObject plusoners = itemObject.getJsonObject("plusoners");
         	JsonNumber nbPlus = replies.getJsonNumber("totalItems");    
         	
         	bf.append(itemContent + " (" + nbReplies.toString() + " replies, " + nbPlus.toString() + " plus)");
         	
         	String urlPhoto = "";
         	JsonArray attachments = itemObject.getJsonArray("attachments");
         	if (attachments != null) {
	         	for(JsonValue valueAtt : attachments){
	         		JsonObject attachement = (JsonObject) valueAtt;
		         	if (attachement != null) {
		         		String objectType = attachement.getString("objectType");	
		         		if (objectType.equalsIgnoreCase("photo")) {
		         			JsonObject image = attachement.getJsonObject("image");	
		         			urlPhoto = image.getString("url");
		         			bf.append("<p align='center'><img width='100px' src='" + urlPhoto + "'/></p>");
		         		}
		         	}
	         	}
         	}
         	bf.append("</p>");
         	out.println(bf.toString());
         }         
         %>
    </c:if>
    <c:if test="${!renderContext.liveMode}">
        Google+ Activities
    </c:if>   
</c:if>
