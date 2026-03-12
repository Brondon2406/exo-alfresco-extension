package org.exoplatform.alfresco.rest.api;

import org.exoplatform.alfresco.rest.model.AlfrescoSettingsEntity;

public interface AlfrescoSettingService {

    /**
     * Saves GLPI settings
     *
     * @param serverApiUrl       server api url
     * @param appToken           app token generated in api client
     * @return {@link AlfrescoSettingsEntity}
     */
    AlfrescoSettingsEntity saveAlfrescoSettings(String serverApiUrl, String appToken);

    /**
     * Retrieves saved GLPI settings
     * @return {@link AlfrescoSettingsEntity}
     */
    AlfrescoSettingsEntity getAlfrescoSettings();


    /**
     * Save user token
     *
     * @param userToken user token
     * @param userIdentityId user identity id
     * @return saved token
     */
    String saveUserToken(String userToken, String userIdentityId);

    /**
     * Retrieves saved user token
     *
     * @param userIdentityId user identity id
     * @return saved token
     */
    String getUserToken(String userIdentityId);

    /**
     * Checks if user token valid
     *
     * @param userToken user token
     * @return true if token valid or false if else
     */
    boolean isUserTokenValid(String userToken);


    /**
     * Removes saved user GLPI token
     *
     * @param userIdentityId user identity id
     */
    void removeUserToken(String userIdentityId);
}
