import './initComponents.js';
import './../css/alfrescoApp.css';

const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
const bundle = 'locale.portlet.alfresco';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/${bundle}-${lang}.json`;
const appId = 'AlfrescoApplication';

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

export function init() {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      Vue.createApp({
        template: `<alfresco-app id="${appId}" />`,
        vuetify,
        i18n,
      }, `#${appId}`, 'Alfresco Application');
    })
    .catch(() => {
      // Monte l'app même sans i18n pour ne pas bloquer
      Vue.createApp({
        template: `<alfresco-app id="${appId}" />`,
        vuetify,
      }, `#${appId}`, 'Alfresco Application');
    });
}
