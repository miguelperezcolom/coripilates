<template>
  <div class="content">
    <h1>CoriPilates</h1>
    <div class="field">
      <label for="email" class="label">Tu email:</label>
      <input type="email" id="email" v-model="email" class="input"/>
    </div>
    <div class="field">
      <label for="password" class="label">Tu password:</label>
      <input type="password" id="password" v-model="password" class="input"/>
      <p class="help is-danger">{{error}}</p>
    </div>
    <div class="control">
      <div class="buttons">
        <button @click="comprobar" class="button is-primary">Entrar</button>
        <button @click="resetpassword" class="button is-warning">Solicitar password</button>
      </div>
    </div>

  </div>
</template>

<script>
    import axios from 'axios';

    export default {
        name: 'WhoAreYou',
        data() {
            return {
                email: '',
                password: '',
                error: null,
            }
        },
        methods: {
            comprobar: function() {
                axios.post(baseUrl + '/login', {
                    email: this.email,
                        password: this.password,
                })
                    .then(response => {
                        if (response.data.result == 'ok') {
                            autenticado = true;
                            this.$router.push(response.data.url);
                        } else {
                            this.error = response.data.msg;
                        }
                    })
                    .catch(e => {
                        this.error = e;
                    })
            },
            resetpassword: function() {
                axios.post(baseUrl + '/resetpassword', {
                    email: this.email,
                })
                    .then(response => {
                        if (response.data.result == 'ok') {
                            autenticado = true;
                            this.$router.push(response.data.url);
                        } else {
                            this.error = response.data.msg;
                        }
                    })
                    .catch(e => {
                        this.error = e;
                    })
            },
        },
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
