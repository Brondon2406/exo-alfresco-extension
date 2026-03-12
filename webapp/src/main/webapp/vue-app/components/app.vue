<template>
  <v-app>
    <v-card
        min-width="100%"
        max-width="100%"
        min-height="200"
        class="d-flex border-box-sizing flex-column pa-5 overflow-hidden position-relative card-border-radius"
        :loading="loading"
        loader-height="2"
        flat>

      <!-- État de chargement initial -->
      <div v-if="loading" class="loader-container">
        <div class="loader"></div>
        <p>Chargement en cours...</p>
      </div>

      <template v-else>

        <!-- Cas 1 : Pas de settings configurés -->
        <template v-if="!hasAlfrescoSettings">
          <!-- Admin : interface de configuration -->
          <alfresco-add-settings
              v-if="isAdmin"
              @open-settings-drawer="openSettingsDrawer" />
          <!-- Non-admin : message d'attente -->
          <div
              v-else
              class="not-configured-message d-flex flex-column align-center justify-center pa-8">
            <v-icon size="48" color="grey lighten-1">
              fas fa-plug
            </v-icon>
            <p class="text-subtitle-1 grey--text mt-4">
              {{ $t('alfresco.not.configured.message') }}
            </p>
          </div>
        </template>

        <!-- Cas 2 : Settings présents -->
        <template v-else>
          <!-- Non connecté : formulaire de login -->
          <alfresco-app-main
              v-if="!isConnected"
              @login-success="onLoginSuccess" />
          <!-- Connecté : liste des fichiers -->
          <alfresco-app-files-lists
              v-else
              @logout-success="onLogoutSuccess" />
        </template>

      </template>
    </v-card>

    <!-- Drawer settings (accessible uniquement à l'admin) -->
    <alfresco-settings-drawer
        v-if="isAdmin"
        ref="settingsDrawer"
        :is-saving-settings="isSavingSettings"
        @save-alfresco-settings="saveAlfrescoSettings" />
  </v-app>
</template>

<script>
export default {
  name: 'AlfrescoAppRoot',

  data() {
    return {
      loading: true,
      isAdmin: false,
      isConnected: false,
      alfrescoSettings: null,
      isSavingSettings: false,
    };
  },

  computed: {
    hasAlfrescoSettings() {
      return !!this.alfrescoSettings;
    },
  },

  created() {
    this.initApp();
  },

  methods: {

    /**
     * Point d'entrée : charge les settings ET vérifie la session en parallèle.
     */
    initApp() {
      this.loading = true;
      Promise.all([
        this.loadSettings(),
        this.checkSession(),
      ]).finally(() => {
        this.loading = false;
      });
    },

    /**
     * Charge les settings Alfresco depuis le backend.
     * La réponse doit contenir : { alfrescoSettings, admin }
     */
    loadSettings() {
      return this.$alfrescoService.getAlfrescoSettings()
          .then(response => {
            this.alfrescoSettings = response?.alfrescoSettings || null;
            this.isAdmin = response?.admin || true;
          })
          .catch(() => {
            this.alfrescoSettings = null;
            this.isAdmin = true;
          });
    },

    /**
     * Vérifie si une session Alfresco est déjà active (cookie/session serveur).
     */
    checkSession() {
      return fetch(`${eXo.env.portal.context}/rest/alfresco/documents/session`, {
        method: 'GET',
        credentials: 'include',
      })
          .then(resp => resp.json())
          .then(data => {
            this.isConnected = !!data?.authenticated;
            if (this.isConnected) {
              sessionStorage.setItem('alfrescoConnected', 'true');
            } else {
              sessionStorage.removeItem('alfrescoConnected');
            }
          })
          .catch(() => {
            this.isConnected = false;
            sessionStorage.removeItem('alfrescoConnected');
          });
    },

    /**
     * Appelé par AlfrescoAppMain après un login réussi.
     */
    onLoginSuccess() {
      this.isConnected = true;
      sessionStorage.setItem('alfrescoConnected', 'true');
    },

    /**
     * Appelé par AlfrescoAppFilesLists après une déconnexion.
     */
    onLogoutSuccess() {
      this.isConnected = false;
      sessionStorage.removeItem('alfrescoConnected');
    },

    /**
     * Ouvre le drawer de configuration (admin uniquement).
     */
    openSettingsDrawer() {
      this.$root.$emit('open-alfresco-settings-drawer', this.alfrescoSettings);
    },

    /**
     * Sauvegarde les settings et met à jour l'état local.
     */
    saveAlfrescoSettings(settings) {
      this.isSavingSettings = true;
      return this.$alfrescoService.saveAlfrescoSettings(settings)
          .then(savedSettings => {
            this.alfrescoSettings = savedSettings;
            this.$root.$emit(
                'alert-message',
                this.$t('alfresco.settings.saved.success.message'),
                'success'
            );
            this.$refs.settingsDrawer.closeDrawer();
          })
          .catch(() => {
            this.$root.$emit(
                'alert-message',
                this.$t('alfresco.settings.saved.error.message'),
                'error'
            );
          })
          .finally(() => {
            this.isSavingSettings = false;
          });
    },
  },
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

.not-configured-message {
  min-height: 200px;
  text-align: center;
}
</style>
