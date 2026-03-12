<template>
  <exo-drawer
    ref="alfrescoSettingsDrawer"
    allow-expand
    right>
    <template slot="title">
      <span class="text-color">
        {{ $t('alfresco.create.connection.message') }}
      </span>
    </template>
    <template slot="content">
      <div class="pa-4">
        <div class="text-color">
          <p>
            {{ $t('alfresco.enable.users.connection.message') }}
          </p>
          <p>
            {{ $t('alfresco.users.connection.todo.message') }}
          </p>
        </div>
        <div class="mt-5">
          <v-form
            v-model="valid"
            ref="settingsForm">
            <v-label
              for="serverApiUrl">
              <span class="text-subtitle-2 mt-5 mb-3">
                {{ $t('alfresco.settings.server.api.url.label') }}
              </span>
            </v-label>
            <v-text-field
              v-model="alfrescoSettings.serverApiUrl"
              :rules="[rules.required]"
              name="serverApiUrl"
              class="mt-n3 mb-2"
              :placeholder="$t('alfresco.settings.server.api.url.placeholder')"
              dense
              outlined />
            <v-label
              for="appToken">
              <span class="text-subtitle-2 mt-5 mb-3">
                {{ $t('alfresco.settings.app.token.label') }}
              </span>
            </v-label>
            <v-text-field
              v-model="alfrescoSettings.appToken"
              :rules="[rules.required]"
              name="appToken"
              class="mt-n3 mb-2"
              :placeholder="$t('alfresco.settings.app.token.placeholder')"
              dense
              outlined />
            <v-label
              for="maxTicketsToDisplay">
              <span class="text-subtitle-2 mt-5 mb-3">
                {{ $t('alfresco.settings.max.tickets.display.label') }}
              </span>
            </v-label>
          </v-form>
        </div>
      </div>
    </template>
    <template slot="footer">
      <div class="ms-auto d-flex width-fit-content">
        <v-btn
          @click="closeDrawer"
          class="btn me-4">
          {{ $t('alfresco.settings.cancel.label') }}
        </v-btn>
        <v-btn
          :loading="isSavingSettings"
          :disabled="!valid"
          class="btn btn-primary"
          @click="addAlfrescoSettings">
          {{ $t('glpi.settings.validate.label') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>

export default {
  props: {
    isSavingSettings: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      valid: false,
      alfrescoSettings: {},
      rules: {
        required: v => !!v || this.$t('alfresco.settings.form.required.error.message'),
      }
    };
  },
  created() {
    this.$root.$on('open-alfresco-settings-drawer', this.openDrawer);
  },
  methods: {
    openDrawer(alfrescoSettings) {
      if (alfrescoSettings) {
        this.alfrescoSettings = alfrescoSettings;
      }
      this.$refs.alfrescoSettingsDrawer.open();
    },
    closeDrawer() {
      this.$refs.settingsForm.reset();
      this.$refs.alfrescoSettingsDrawer.close();
    },
    addAlfrescoSettings() {
      this.$emit('save-alfresco-settings', this.alfrescoSettings);
    }
  }
};
</script>
