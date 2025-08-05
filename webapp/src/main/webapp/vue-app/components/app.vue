<template>
  <div>
    <div v-if="loading" class="loader-container">
      <div class="loader"></div>
      <p>Chargement en cours...</p>
    </div>

    <component v-else :is="currentView" />
  </div>
</template>

<script>
import AlfrescoLogin from './alfrescoAppMain.vue';
import AlfrescoFilesList from './alfrescoAppFilesLists.vue';

export default {
  name: 'AlfrescoAppRoot',
  components: {
    AlfrescoLogin,
    AlfrescoFilesList
  },
  data() {
    return {
      currentView: null,
      loading: true
    };
  },
  methods: {
    onLoginSuccess() {
      this.currentView = 'AlfrescoFilesList';
      sessionStorage.setItem('alfrescoConnected', 'true');
    },
    checkSession() {
      fetch(`${eXo.env.portal.context}/rest/alfresco/documents/session`, {
        method: 'GET',
        credentials: 'include'
      })
        .then(resp => resp.json())
        .then(data => {
          this.currentView = data.authenticated ? 'AlfrescoFilesList' : 'AlfrescoLogin';
          if (data.authenticated) {
            sessionStorage.setItem('alfrescoConnected', 'true');
          } else {
            sessionStorage.removeItem('alfrescoConnected');
          }
        })
        .catch(() => {
          this.currentView = 'AlfrescoLogin';
        })
        .finally(() => {
          this.loading = false;
        });
    }
  },
  provide() {
    return {
      onLoginSuccess: this.onLoginSuccess
    };
  },
  created() {
    const alreadyConnected = sessionStorage.getItem('alfrescoConnected') === 'true';
    if (alreadyConnected) {
      this.checkSession();
    } else {
      this.currentView = 'AlfrescoLogin';
      this.loading = false;
    }
  }
};
</script>

<style scoped>
.loader-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.loader {
  border: 6px solid #f3f3f3;
  border-top: 6px solid #2c9f45;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
