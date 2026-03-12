export function saveAlfrescoSettings(AlfrescoSetting) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/alfresco/settings/save`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(AlfrescoSetting, (key, value) => {
      if (value !== null) {
        return value;
      }
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error while saving Alfresco Settings');
    } else {
      return resp.json();
    }
  });
}

export function getAlfrescoSettings() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/alfresco/settings`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
  }).then(resp => {
    if (!resp?.ok && resp?.status !== 404) {
      throw resp;
    } else {
      return resp.json();
    }
  });
}

export function saveUserToken(token) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/alfresco/settings/token`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: token,
  }).then(resp => {
    if (!resp?.ok) {
      throw resp;
    } else {
      return resp.text();
    }
  });
}

export function removeUserToken() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/alfresco/settings/remove/token`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw resp;
    } else {
      return resp.text();
    }
  });
}
