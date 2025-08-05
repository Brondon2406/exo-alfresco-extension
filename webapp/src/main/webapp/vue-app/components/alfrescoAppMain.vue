<template>
  <div id="vue_webpack_alfresco">
    <h2 class="vue_webpack_alfresco-app-title">Alfresco Login</h2>
    <img
      src="/images/alfresco-logo.png"
      alt="Alfresco Logo"
      style="width: 120px; margin-bottom: 20px"
    />

    <div class="login-form">
      <input type="text" v-model="username" placeholder="Nom d'utilisateur Alfresco" />
      <input type="password" v-model="password" placeholder="Mot de passe Alfresco" />
      <button @click="login">Connexion</button>
    </div>

    <p class="alert-error" v-if="errorMessage">{{ errorMessage }}</p>
  </div>
</template>

<script>
export default {
  inject: ['onLoginSuccess'],
  data() {
    return {
      username: '',
      password: '',
      errorMessage: '',
    };
  },
  methods: {
    login() {
      const formData = new URLSearchParams();
      formData.append('alfrescoUsername', this.username);
      formData.append('alfrescoPass', this.password);

      fetch(`${eXo.env.portal.context}/rest/alfresco/documents/login`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData,
      })
        .then((resp) => {
          if (!resp.ok) {
            throw resp;
          }
          return resp.json();
        })
        .then(() => {
          this.onLoginSuccess();
        })
        .catch(async (err) => {
          const msg = await err.text();
          this.errorMessage = msg || 'Échec de la connexion à Alfresco';
          setTimeout(() => {
            this.errorMessage = '';
          }, 5000);
        });
    },
  },
};
</script>

<style scoped>
.login-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 300px;
}
.login-form input {
  padding: 8px;
}
.login-form button {
  padding: 8px;
  background-color: #2c9f45;
  color: white;
  border: none;
  cursor: pointer;
}
.alert-error {
  color: red;
}
</style>
