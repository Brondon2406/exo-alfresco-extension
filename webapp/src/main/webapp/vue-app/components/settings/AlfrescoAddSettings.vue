<template>
  <div class="mt-auto mb-auto align-center">
    <v-btn
      class="btn btn-primary"
      color="primary"
      variant="flat"
      outlined
      @click="openSettingsDrawer">
      {{ $t('alfresco.settings.label') }}
    </v-btn>
  </div>
</template>

<script>
export default {
  methods: {
    openSettingsDrawer() {
      this.$emit('open-settings-drawer');
    }
  }
};
</script>
