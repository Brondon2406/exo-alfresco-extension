import './initComponents.js';
import app from './components/app.vue';
import './../css/alfrescoApp.css';

const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
const bundle = 'locale.portlet.alfrescoApp';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/${bundle}-${lang}.json`;

exoi18n.loadLanguageAsync(lang, url)
  .then(i18n => {
    new Vue({
      render: h => h(app),
      i18n
    }).$mount('#vue_webpack_alfresco');
  });
