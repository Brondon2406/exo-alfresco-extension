import alfrescoApp from './components/app.vue';
import alfrescoAppMain from './components/alfrescoAppMain.vue';
import alfrescoAppFilesLists from './components/alfrescoAppFilesLists.vue';
import AlfrescoAddSettings from './components/settings/AlfrescoAddSettings.vue';
import AlfrescoSettingsDrawer from './components/settings/AlfrescoSettingsDrawer.vue';
import AlfrescoSettingsButton from './components/AlfrescoSettingsButton.vue';
import * as alfrescoService from './js/AlfrescoService';

const components = {
  'alfresco-app': alfrescoApp,                        // ← manquait
  'alfrescoApp-main': alfrescoAppMain,
  'alfrescoApp-files-lists': alfrescoAppFilesLists,
  'alfresco-add-settings': AlfrescoAddSettings,
  'alfresco-settings-drawer': AlfrescoSettingsDrawer,
  'alfresco-settings-button': AlfrescoSettingsButton,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (!Vue.prototype.$alfrescoService) {
  window.Object.defineProperty(Vue.prototype, '$alfrescoService', {
    value: alfrescoService,
  });
}
