package org.jahia.services.usermanager;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.json.JsonObject;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.gkuhmel.jahia.modules.googleplus.GoogleAPIClient;


/**
 * Handles the Google plus User
 * @author gkuhmel
 *
 */
public class JahiaUserManagerGooglePlusProvider extends JahiaUserManagerProvider {

    // Caches name
    public static final String LDAP_USER_CACHE = "GooglePlusUsersCache";

    private static Logger logger = LoggerFactory.getLogger(JahiaUserManagerGooglePlusProvider.class);
    public static final String PROVIDER_NAME = "googleplus";

    public static final String PROVIDERS_USER_CACHE = "ProvidersUsersCache";
    private CacheService cacheService = null;

    private Map<String, String> googleProperties = null;

    private JCRUserManagerProvider jcrUserManagerProvider;
    private Map<String, String> mappedProperties = null;

    private Cache<String, JahiaUser> mProvidersUserCache;
    private Cache<String, Serializable> mUserCache;

    private Map<String, String> permissions = null;
    
	
    public CacheService getCacheService() {
		return cacheService;
	}

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	
	private class GooglePropertiesMapping {

        private UserProperties props;

        private GooglePropertiesMapping(DefaultGooglePlusUser gUser, String token) {
            super();
            this.props = new UserProperties();
            props.setUserProperty("access_token", new UserProperty("access_token", token, true));                        
            props.setUserProperty("j:firstName", new UserProperty("j:firstName", gUser.getGiven_name(), true));
            props.setUserProperty("j:lastName", new UserProperty("j:lastName", gUser.getFamily_name(), true));
            props.setUserProperty("j:gender", new UserProperty("j:gender", gUser.getGender(), true));
            props.setUserProperty("j:email", new UserProperty("j:email", gUser.getEmail(), true));
            props.setUserProperty("j:googleID", new UserProperty("j:googleID", gUser.getId(), true));
            props.setUserProperty("preferredLanguage", new UserProperty("preferredLanguage", gUser.getLocale(), true));            
            props.setUserProperty("link", new UserProperty("link", gUser.getLink(), true));
            props.setUserProperty("name", new UserProperty("name", gUser.getName(), true));
            props.setUserProperty("picture", new UserProperty("picture", gUser.getPicture(), true));            
        }

        UserProperties getUserProperties() {
            return props;
        }
    }
	
	// Class to wrapp the user in the internal cache
    public static class JahiaUserWrapper implements Serializable {

        private static final long serialVersionUID = -2955706620534674310L;

        // the internal user, only defined when creating object
        private JahiaUser user;

        /**
         * Constructor.
         * @param ju
         *            JahiaUser, a user from a provider.
         */
        public JahiaUserWrapper(JahiaUser ju) {
            user = ju;
        }

        /**
         * Get the internal user.
         * @return JahiaUser, the internal user.
         */
        public JahiaUser getUser() {
            return user;
        }
    }
	
	@Override
	public JahiaUser createUser(String arg0, String arg1, Properties arg2) {
		return null;
	}

	@Override
	public boolean deleteUser(JahiaUser arg0) {
		return false;
	}

	@Override
	public int getNbUsers() {
		return -1;
	}

	@Override
	public List<String> getUserList() {
        return Collections.emptyList();
	}

	@Override
	public List<String> getUsernameList() {
        return Collections.emptyList();
	}

	@Override
	public boolean login(String arg0, String arg1) {
		return true;
	}

	@Override
	public JahiaUser lookupUser(String name) {
		JahiaGooglePlusUser jahiaGoogleUser = null;

		jahiaGoogleUser = (JahiaGooglePlusUser) mProvidersUserCache.get("n" + name);

        if (jahiaGoogleUser == null) {
            JCRUser jcrUser = jcrUserManagerProvider.lookupExternalUser(name);

            if (jcrUser != null) {
                String access_token = jcrUser.getProperty("access_token");

                if (access_token != null) {                  
                    DefaultGooglePlusUser user = this.getGooglePlusUserByAccessToken(access_token);
                    JahiaGooglePlusUser jgu = googleToJahiaUser(user, access_token);
                    return jgu;
                } else
                    return null;
            } else
                return null;
        }

        return jahiaGoogleUser;
	}

	@Override
	public JahiaUser lookupUserByKey(String userKey) {
        JahiaGooglePlusUser jahiaGoogleUser = null;
        jahiaGoogleUser = (JahiaGooglePlusUser) mProvidersUserCache.get("k" + userKey);

        if (jahiaGoogleUser == null) {
            String name = removeKeyPrefix(userKey);
            jahiaGoogleUser = (JahiaGooglePlusUser) lookupUser(name);
        }
        return jahiaGoogleUser;
	}

	private String removeKeyPrefix(String userKey) {
        if (userKey.startsWith("{" + getKey() + "}")) {
            return userKey.substring(getKey().length() + 2);
        } else {
            return userKey;
        }
	}

	@Override
	public Set<JahiaUser> searchUsers(Properties arg0) {
        return Collections.emptySet();
	}

	@Override
    public void updateCache(JahiaUser jahiaUser) {
        mUserCache.put("k" + jahiaUser.getUserKey(), new JahiaUserWrapper(jahiaUser));
        mProvidersUserCache.put("k" + jahiaUser.getUserKey(), jahiaUser);
        mUserCache.put("n" + jahiaUser.getUsername(), new JahiaUserWrapper(jahiaUser));
        mProvidersUserCache.put("n" + jahiaUser.getUsername(), jahiaUser);
    }


	@Override
	public boolean userExists(String arg0) {
		return false;
	}

	@Override
	public void start() throws JahiaInitializationException {
        mUserCache = cacheService.getCache(LDAP_USER_CACHE
                + (PROVIDER_NAME.equals(getKey()) ? "" : "-" + getKey()), true);
        mProvidersUserCache = cacheService.getCache(PROVIDERS_USER_CACHE, true);
        logger.debug("JahiaGoogleProvider Initialized");
	}

	@Override
	public void stop() throws JahiaException {
		
	}

	public Map<String, String> getPermissionsMap() {
		return this.permissions;
	}
	
	// Method called by the valve to get the JahiaGooglePlusUser - Provided paramas : gp token
    public JahiaGooglePlusUser lookupUserByAccessToken(String gpToken) {
        DefaultGooglePlusUser gpUser = this.getGooglePlusUserByAccessToken(gpToken);
        JahiaGooglePlusUser jgu = googleToJahiaUser(gpUser, gpToken);
        return jgu;
    }
    
	
	private JahiaGooglePlusUser googleToJahiaUser(DefaultGooglePlusUser gpUser, String token) {
		JahiaGooglePlusUser jgu = null;

        UserProperties userProps = new GooglePropertiesMapping(gpUser, token).getUserProperties();

        jgu = new JahiaGooglePlusUser(JahiaUserManagerGooglePlusProvider.PROVIDER_NAME,
                gpUser.getId(), gpUser.getId(), userProps);

        if (jgu != null) {
            mProvidersUserCache.put("k" + jgu.getUserKey(), jgu);
            mUserCache.put("n" + jgu.getUsername(), new JahiaUserWrapper(jgu));
            mProvidersUserCache.put("n" + jgu.getUsername(), jgu);
        }

        mUserCache.put("k" + jgu.getUserKey(), new JahiaUserWrapper(jgu));
        JCRUser jcrUser = (JCRUser) jcrUserManagerProvider.lookupExternalUser(jgu);

        if (jcrUser == null) {
            try {
                JCRStoreService.getInstance().deployExternalUser(jgu);
                jcrUser = (JCRUser) jcrUserManagerProvider.lookupExternalUser(jgu);
            } catch (RepositoryException e) {
                logger.error(
                        "Error deploying external user '" + jgu.getUsername() + "' for provider '"
                                + PROVIDER_NAME + "' into JCR repository. Cause: " + e.getMessage(),
                        e);
            }
        }
        return jgu;
	}

	private DefaultGooglePlusUser getGooglePlusUserByAccessToken(String gpToken) {
		DefaultGooglePlusUser googleUser = new DefaultGooglePlusUser();
		GoogleAPIClient gApi = (GoogleAPIClient) SpringContextSingleton.getModuleBean("GoogleAPIClient");
				
		JsonObject obj = gApi.getUserInfo(gpToken);       
        googleUser.setId(obj.getString("id"));
        googleUser.setEmail(obj.getString("email"));
        googleUser.setFamily_name(obj.getString("family_name"));
        googleUser.setGender(obj.getString("gender"));
        googleUser.setGiven_name(obj.getString("given_name"));
        googleUser.setLink(obj.getString("link"));
        googleUser.setLocale(obj.getString("locale"));
        googleUser.setName(obj.getString("name"));
        googleUser.setPicture(obj.getString("picture"));
        googleUser.setVerifiedEmail(obj.getBoolean("verified_email"));
                           
        return googleUser;
	}

	public void setGoogleProperties(Map<String, String> googleProperties) {
        this.googleProperties = new HashMap<String, String>();
        this.mappedProperties = new HashMap<String, String>();
        for (Object key : googleProperties.keySet()) {
        	String keyString = key.toString();
            String value = googleProperties.get(keyString);
            this.googleProperties.put(keyString, value);
            if (keyString.endsWith(".attribute.map") && value.equalsIgnoreCase("true")) {
                this.mappedProperties.put(
                        StringUtils.substringBeforeLast(keyString, ".attribute.map"), value);
            } else if (keyString.endsWith(".permission") && value.equalsIgnoreCase("true")) {;
                String permKey = StringUtils.substringBeforeLast(keyString, ".permission");
                this.permissions.put(permKey, value);
            }
        }       
    }

    public void setJcrUserManagerProvider(JCRUserManagerProvider jcrUserManagerProvider) {
        this.jcrUserManagerProvider = jcrUserManagerProvider;
    }

}
