<template>
  <div id="vue_webpack_alfresco-list">
    <h2 class="vue_webpack_alfresco-app-title">Liste des fichiers Alfresco</h2>
    <div class="upload-zone" @dragover.prevent @drop.prevent="handleDrop">
      <p>Déposez votre fichier ici ou cliquez pour sélectionner</p>
      <p v-if="fileToUpload"><strong>Fichier sélectionné :</strong> {{ fileToUpload.name }}</p>
      <input
        type="file"
        ref="fileInput"
        @change="handleFileChange"
        accept="*/*"
        style="display: none"
      />
      <button @click="triggerFileInput">Choisir un fichier</button>
      <button @click="uploadFile" :disabled="!fileToUpload || isUploading">
        {{ isUploading ? 'Envoi en cours...' : 'Déposer' }}
      </button>
    </div>

    <p class="alert-error" v-if="errorMessage">{{ errorMessage }}</p>
    <p class="alert-success" v-if="successMessage">{{ successMessage }}</p>

    <table v-if="files.length">
      <thead>
        <tr>
          <th>#</th>
          <th>Nom</th>
          <th>Créé le</th>
          <th>Modifié le</th>
          <th>Action</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(file, index) in paginatedFiles" :key="file.id">
          <td>{{ index + 1 + (page - 1) * pageSize }}</td>
          <td>{{ file.name }}</td>
          <td>{{ file.createdAt }}</td>
          <td>{{ file.modifiedAt }}</td>
          <td><a :href="downloadLink(file.id)">Télécharger</a></td>
        </tr>
      </tbody>
    </table>

    <div v-if="totalPages > 1" class="pagination">
      <button @click="page--" :disabled="page === 1">Précédent</button>
      <span>Page {{ page }} / {{ totalPages }}</span>
      <button @click="page++" :disabled="page >= totalPages">Suivant</button>
    </div>
  </div>
</template>

<script>
export default {
  inject: ['onLoginSuccess'],
  data() {
    return {
      files: [],
      fileToUpload: null,
      parentNodeId: '-my-', // ✅ Plus sûr que "root" selon ton backend
      errorMessage: '',
      successMessage: '',
      page: 1,
      pageSize: 10,
      isUploading: false,
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.files.length / this.pageSize);
    },
    paginatedFiles() {
      const start = (this.page - 1) * this.pageSize;
      return this.files.slice(start, start + this.pageSize);
    },
  },
  created() {
    this.fetchFiles();
  },
  methods: {
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    handleFileChange(event) {
      const file = event.target.files[0];
      if (!file) {
        return;
      }
      this.validateAndSetFile(file);
    },
    handleDrop(event) {
      const file = event.dataTransfer.files[0];
      if (!file) {
        return;
      }

      this.validateAndSetFile(file);
    },
    validateAndSetFile(file) {
      const maxSize = 10 * 1024 * 1024; // 10 MB

      if (file.size > maxSize) {
        this.errorMessage = 'Le fichier dépasse la taille autorisée (10MB).';
        this.fileToUpload = null;
        return;
      }

      this.fileToUpload = file;
      this.errorMessage = '';
    },
    fetchFiles() {
      fetch(`${eXo.env.portal.context}/rest/alfresco/documents/files`, {
        method: 'GET',
        credentials: 'include',
      })
        .then((resp) => {
          if (!resp.ok) {
            throw resp;
          }
          return resp.json();
        })
        .then((data) => {
          if (!Array.isArray(data)) {
            throw new Error('Données invalides');
          }
          this.files = data;
        })
        .catch(async (err) => {
          const msg = await err.text();
          this.errorMessage = msg || 'Erreur lors du chargement des fichiers';
          this.resetMessageAfterDelay();
        });
    },
    uploadFile() {
      if (!this.fileToUpload) {
        this.errorMessage = 'Aucun fichier sélectionné';
        return;
      }

      const formData = new FormData();
      formData.append('file', this.fileToUpload);

      this.isUploading = true;
      fetch(`${eXo.env.portal.context}/rest/alfresco/documents/upload/${this.parentNodeId}`, {
        method: 'POST',
        body: formData,
        credentials: 'include',
      })
        .then((resp) => {
          if (!resp.ok) {
            this.fileToUpload = null;
            this.$refs.fileInput.value = null;
            this.fetchFiles();
            this.resetMessageAfterDelay();
            throw resp;
          }
          return resp.json();
        })
        .then(() => {
          this.successMessage = 'Fichier déposé avec succès';
          this.fileToUpload = null;
          this.$refs.fileInput.value = null;
          this.fetchFiles();
          this.resetMessageAfterDelay();
        })
        .catch(async (err) => {
          const msg = await err.text();
          this.errorMessage = msg || "Échec de l'upload du fichier";
          this.resetMessageAfterDelay();
        })
        .finally(() => {
          this.isUploading = false;
        });
    },
    downloadLink(id) {
      return `${eXo.env.portal.context}/rest/alfresco/documents/download/${id}`;
    },
    logout() {
      fetch(`${eXo.env.portal.context}/rest/alfresco/documents/logout`, {
        method: 'POST',
        credentials: 'include',
      })
        .then((resp) => {
          if (!resp.ok) {
            throw resp;
          }
          sessionStorage.removeItem('alfrescoConnected');
        })
        .then(() => this.onLoginSuccess())
        .catch(async (err) => {
          const msg = await err.text();
          this.errorMessage = msg || 'Échec de la déconnexion';
          this.resetMessageAfterDelay();
        });
    },
    resetMessageAfterDelay() {
      setTimeout(() => {
        this.errorMessage = '';
        this.successMessage = '';
      }, 5000);
    },
  },
};
</script>
