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
        <div>
        	<c:if test="${not empty renderContext.user.properties['picture']}" var="pictureAvailable">
        
	            <div class='image'>
	                <div class='itemImage itemImageRight'>
	                    <img src="${renderContext.user.properties['picture']}" alt="" border="0"/>
	                </div>
	            </div>
            
            </c:if>
            <p>
            	<fmt:message key="label.loggedAs"/>&nbsp;          
	            <c:if test="${not empty renderContext.user.properties['link']}" var="linkAvailable">
	            	<a href="${renderContext.user.properties['link']}" target="_blank">${renderContext.user.properties["j:firstName"]}&nbsp;${renderContext.user.properties["j:lastName"]}<c:if test="${!empty currentAliasUser}"> (as ${currentAliasUser.username})</c:if></a>
	            </c:if>
	            <c:if test="${not linkAvailable}">
	            	${renderContext.user.properties["j:firstName"]}&nbsp;${renderContext.user.properties["j:lastName"]}<c:if test="${!empty currentAliasUser}"> (as ${currentAliasUser.username})</c:if>
	            </c:if>
            </p>
            <p>
                Email:&nbsp;${renderContext.user.properties["j:email"]}
            </p>        
        </div>
    </c:if>
    <c:if test="${!renderContext.liveMode}">
        Google+ Infos
    </c:if>   
</c:if>
