<template>
  <div class="content">
    <div class="field">
      <label for="password" class="label">Tu password:</label>
      <input type="password" id="password" v-model="password" class="input"/>
      <p class="help is-danger">{{error}}</p>
    </div>
    <div class="control">
    <button @click="comprobar" class="button is-primary">Entrar</button>
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
        },
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
