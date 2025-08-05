import alfrescoAppMain from './components/alfrescoAppMain.vue';
import alfrescoAppFilesLists from './components/alfrescoAppFilesLists.vue';

const components = {
  'alfrescoApp-main': alfrescoAppMain,
  'alfrescoApp-files-lists': alfrescoAppFilesLists,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
