package org.exoplatform.alfresco.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlfrescoSettingsEntity implements Serializable {
    private String serverApiUrl;
    private String appToken;
}
