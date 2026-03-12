  <template>
  <v-btn
    class="ms-auto my-0 pt-1 icon-default-color"
    small
    icon
    @click="openSettingsDrawer">
    <v-icon
      size="20">
      fas fa-cog
    </v-icon>
  </v-btn>
</template>

<script>
export default {

  methods: {
    openSettingsDrawer() {
      this.$emit('open-settings-drawer');
    },
  }
};
</script>
