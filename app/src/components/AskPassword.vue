<template>
  <div class="content">
    <div class="field">
      <label for="password" class="label">Tu password:</label>
      <input type="password" id="password" v-model="password" class="input"/>
      <p class="help is-danger">{{error}}</p>
    </div>
    <div class="control">
      <div class="buttons">
        <button @click="comprobar" class="button is-primary">Entrar</button>
        <button @click="resetpassword" class="button is-warning">Solicitar reset password</button>
      </div>
    </div>

  </div>
</template>

<script>
    import axios from 'axios';

    export default {
        name: 'AskPassword',
        props: {
            idUsuario: String
        },
        data() {
            return {
                password: '',
                error: null,
            }
        },
        methods: {
            comprobar: function() {
                axios.post(baseUrl + '/check/' + this.idUsuario, {
                        password: this.password,
                })
                    .then(response => {
                        if (response.data.result == 'ok') {
                            this.$router.push(oldTo?oldTo:'/' + this.idUsuario + '/autenticado/');
                        } else {
                            this.error = response.data.msg;
                        }
                    })
                    .catch(e => {
                        console.log(e)
                    })
            },
            resetpassword: function() {
                axios.post(baseUrl + '/resetpassword', {
                    idUsuario: this.idUsuario,
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
