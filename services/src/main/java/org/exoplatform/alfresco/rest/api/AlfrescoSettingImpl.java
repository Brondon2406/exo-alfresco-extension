package org.exoplatform.alfresco.rest.api;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.exoplatform.alfresco.rest.model.AlfrescoSettingsEntity;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONObject;

import java.net.URI;

public class AlfrescoSettingImpl implements  AlfrescoSettingService {

    private static final Log LOG = ExoLogger.getLogger(AlfrescoSettingImpl.class);
    private final SettingService settingService;
    private final HttpClient httpClient;
    private AlfrescoSettingsEntity alfrescoSettings = null;

    private static final int DEFAULT_POOL_CONNECTION = 100;
    private static final Context ALFRESCO_INTEGRATION_SETTING_CONTEXT = Context.GLOBAL.id("alfresco-extension-webapp");
    private static final Scope ALFRESCO_INTEGRATION_SETTING_SCOPE = Scope.APPLICATION.id("alfresco-extension-webapp");
    private static final String  ALFRESCO_INTEGRATION_SERVER_API_URL_SETTING_KEY = "ALFRESCOIntegrationServerApiUrlSetting";
    private static final String  ALFRESCO_INTEGRATION_APP_TOKEN_SETTING_KEY = "ALFRESCOIntegrationAppTokenSetting";
    private static final String  ALFRESCO_USER_TOKEN_SETTING_KEY = "ALFRESCOUserToken";
    private static final String  ALFRESCO_SERVICE_API = "alfresco-service-api";

    public AlfrescoSettingImpl(SettingService settingsService) {
        this.settingService = settingsService;
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(DEFAULT_POOL_CONNECTION);
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
            .setMaxConnPerRoute(DEFAULT_POOL_CONNECTION);
        this.httpClient = httpClientBuilder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlfrescoSettingsEntity saveAlfrescoSettings(String serverApiUrl, String appToken) {
        if (StringUtils.isBlank(serverApiUrl)) {
            throw new IllegalArgumentException("ALFRESCO serverApiUrl is mandatory");
        }
        if (StringUtils.isBlank(appToken)) {
            throw new IllegalArgumentException("ALFRESCO app token is mandatory");
        }
        this.settingService.set(ALFRESCO_INTEGRATION_SETTING_CONTEXT,
            ALFRESCO_INTEGRATION_SETTING_SCOPE,
            ALFRESCO_INTEGRATION_SERVER_API_URL_SETTING_KEY,
            SettingValue.create(serverApiUrl));
        this.settingService.set(ALFRESCO_INTEGRATION_SETTING_CONTEXT,
            ALFRESCO_INTEGRATION_SETTING_SCOPE,
            ALFRESCO_INTEGRATION_APP_TOKEN_SETTING_KEY,
            SettingValue.create(appToken));

        alfrescoSettings = new AlfrescoSettingsEntity(serverApiUrl, appToken);
        return alfrescoSettings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlfrescoSettingsEntity getAlfrescoSettings() {
        SettingValue<?> serverApiUrlSettingValue = settingService.get(ALFRESCO_INTEGRATION_SETTING_CONTEXT,
            ALFRESCO_INTEGRATION_SETTING_SCOPE,
            ALFRESCO_INTEGRATION_SERVER_API_URL_SETTING_KEY);
        SettingValue<?> appTokenSettingValue = settingService.get(ALFRESCO_INTEGRATION_SETTING_CONTEXT,
            ALFRESCO_INTEGRATION_SETTING_SCOPE,
            ALFRESCO_INTEGRATION_APP_TOKEN_SETTING_KEY);

        if (serverApiUrlSettingValue == null || appTokenSettingValue == null) {
            return null;
        }
        return new AlfrescoSettingsEntity(serverApiUrlSettingValue.getValue().toString(),
            appTokenSettingValue.getValue().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String saveUserToken(String userToken, String userIdentityId) {
        if (StringUtils.isBlank(userToken)) {
            throw new IllegalArgumentException("ALFRESCO user token is mandatory");
        }
        if (StringUtils.isBlank(userIdentityId)) {
            throw new IllegalArgumentException("userIdentityId is mandatory");
        }
        this.settingService.set(Context.USER.id(userIdentityId),
            ALFRESCO_INTEGRATION_SETTING_SCOPE,
            ALFRESCO_USER_TOKEN_SETTING_KEY,
            SettingValue.create(userToken));
        return userToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserToken(String userIdentityId) {
        SettingValue<?> userTokenSettingValue = this.settingService.get(Context.USER.id(userIdentityId),
            ALFRESCO_INTEGRATION_SETTING_SCOPE,
            ALFRESCO_USER_TOKEN_SETTING_KEY);

        if (userTokenSettingValue == null) {
            return null;
        }
        return userTokenSettingValue.getValue().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserTokenValid(String userToken) {
        if (userToken == null) {
            return false;
        }
        String sessionToken = initSession(userToken);
        if (sessionToken != null) {
            killSession(sessionToken);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeUserToken(String userIdentityId) {
        if (userIdentityId == null) {
            throw new IllegalArgumentException("user identity id is mandatory");
        }
        settingService.remove(Context.USER.id(userIdentityId),
            ALFRESCO_INTEGRATION_SETTING_SCOPE,
            ALFRESCO_USER_TOKEN_SETTING_KEY);
    }

    private String initSession(String userToken) {
        long startTime = System.currentTimeMillis();
        AlfrescoSettingsEntity settings = getCurrentAlfrescoSettings();
        if (settings == null || userToken == null) {
            return null;
        }
        try {
            HttpGet httpTypeRequest = new HttpGet(settings.getServerApiUrl() + "/initSession");
            httpTypeRequest.setHeader("Authorization", "user_token " + userToken);
            httpTypeRequest.setHeader("App-Token", settings.getAppToken());
            URI uri = new URIBuilder(httpTypeRequest.getURI()).addParameter("get_full_session", "true").build();
            httpTypeRequest.setURI(uri);
            HttpResponse httpResponse = httpClient.execute(httpTypeRequest);
            String responseString = new BasicResponseHandler().handleResponse(httpResponse);
            EntityUtils.consume(httpResponse.getEntity());
            JSONObject jsonResponse = new JSONObject(responseString);
            return jsonResponse.getString("session_token");
        } catch (HttpResponseException e) {
            LOG.error("remote_service={} operation={} parameters=\"user token:{}, status=ko "
                    + "duration_ms={} error_msg=\"{}, status : {} \"",
                ALFRESCO_SERVICE_API,
                "ALFRESCO initSession",
                userToken,
                System.currentTimeMillis() - startTime,
                e.getReasonPhrase(),
                e.getStatusCode());
        } catch (Exception e) {
            LOG.error("Error while init ALFRESCO session", e);
        }
        return null;
    }

    private AlfrescoSettingsEntity getCurrentAlfrescoSettings() {
        if (alfrescoSettings == null) {
            alfrescoSettings = getAlfrescoSettings();
        }
        return alfrescoSettings;
    }

    private HttpGet initHttpGet(String sessionToken) {
        AlfrescoSettingsEntity settings = getCurrentAlfrescoSettings();
        if (settings == null || sessionToken == null) {
            return null;
        }
        HttpGet httpTypeRequest = new HttpGet(settings.getServerApiUrl() + "/killSession");
        httpTypeRequest.setHeader("Session-Token", sessionToken);
        httpTypeRequest.setHeader("App-Token", settings.getAppToken());
        return httpTypeRequest;
    }

    private void killSession(String sessionToken) {
        long startTime = System.currentTimeMillis();
        HttpGet httpGet = initHttpGet(sessionToken);
        if (httpGet == null) {
            return;
        }
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            EntityUtils.consume(httpResponse.getEntity());
            httpResponse.getStatusLine().getStatusCode();
        } catch (HttpResponseException e) {
            LOG.error("remote_service={} operation={} parameters=\"session token:{}, status=ko "
                    + "duration_ms={} error_msg=\"{}, status : {} \"",
                ALFRESCO_SERVICE_API,
                "ALFRESCO killSession",
                sessionToken,
                System.currentTimeMillis() - startTime,
                e.getReasonPhrase(),
                e.getStatusCode());
        } catch (Exception e) {
            LOG.error("while destroying ALFRESCO session", e);
        }
    }
}
