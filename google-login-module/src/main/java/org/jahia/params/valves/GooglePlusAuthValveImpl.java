package org.jahia.params.valves;

import javax.servlet.http.HttpServletRequest;

import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerGooglePlusProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.gkuhmel.jahia.modules.googleplus.GoogleAPIClient;

/**
 * Valve dedicated to log the user with Google+ credentials
 * @author gkuhmel
 */
public class GooglePlusAuthValveImpl extends AutoRegisteredBaseAuthValve {

    private static final transient Logger logger = LoggerFactory
            .getLogger(GooglePlusAuthValveImpl.class);

    // Invoke method
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        // Retrieve the context, the current request and the get parameter (code)
        AuthValveContext authContext = (AuthValveContext) context;
        HttpServletRequest request = authContext.getRequest();
        String gpCode = request.getParameter("code");

        // If we have the code from Google, we can start to work
        logger.debug("Gp code = " + gpCode);
        if (gpCode != null) {
            try {
                JahiaUser jgpu = null;
                
                GoogleAPIClient gApi = (GoogleAPIClient) SpringContextSingleton.getModuleBean("GoogleAPIClient");              
                String gpToken = gApi.getAccessToken(gpCode);
                if (gpToken != null) {                
                    JahiaUserManagerGooglePlusProvider provider = (JahiaUserManagerGooglePlusProvider) ServicesRegistry
                            .getInstance().getJahiaUserManagerService().getProvider("googleplus");
                    jgpu = provider.lookupUserByAccessToken(gpToken);                 
                }

                if (jgpu != null) {
                    authContext.getSessionFactory().setCurrentUser(jgpu);
                    request.getSession().setAttribute(ProcessingContext.SESSION_USER, jgpu);
                } else {
                    throw new RuntimeException("Cannot retrieve user from access token");
                }
                return;
            } catch (Exception e) {
                logger.warn("Error authenticating the user via GooglePlus", e);
            }
        }
        valveContext.invokeNext(context);
    }

}
