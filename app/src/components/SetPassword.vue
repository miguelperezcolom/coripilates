<template>
  <div class="content">
    <h1>Por favor dinos que password quieres utilizar.</h1>

    <div class="field">
      <label for="password" class="label">Tu nuevo password:</label>
      <input type="password" id="password" v-model="password" class="input"/>
    </div>
    <div class="field">
      <label for="password2" class="label">Repite el nuevo password:</label>
      <input type="password" id="password2" v-model="password2" class="input"/>
      <p class="help is-danger">{{error}}</p>
    </div>
    <div class="control">
      <button @click="comprobar" class="button is-primary" :disabled="!password || password != password2">Entrar</button>
    </div>

  </div>
</template>

<script>
    import axios from 'axios';

    export default {
        name: 'SetPassword',
        props: {
            idUsuario: String
        },
        data() {
            return {
                password: null,
                password2: null,
                error: null,
            }
        },
        watch: {
            password: function (val) {
                this.error = val == this.password2?null:'Los 2 passwords deben coincidir';
            },
            password2: function (val) {
                this.error = val == this.password?null:'Los 2 passwords deben coincidir';
            },
        },
        methods: {
            comprobar: function() {
                axios.put(baseUrl + '/password/' + this.idUsuario, {
                    password: this.password,
                    password2: this.password2,
                })
                    .then(response => {
                        this.$router.push(oldTo?oldTo:'/' + this.idUsuario + '/autenticado/');
                    })
                    .catch(e => {
                        console.log(e)
                    })
            },
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
