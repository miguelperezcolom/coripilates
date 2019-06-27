<template>
  <div class="content">
    <h1>Hola {{ status.nombre }}!</h1>

    <setpwd v-if="status.result == 'primeracceso'" v-bind:id-usuario="idUsuario"></setpwd>
    <askpwd v-if="status.result == 'ok'" v-bind:id-usuario="idUsuario"></askpwd>
    <error v-if="status.result == 'error'" v-bind:mensaje="status.msg"></error>

  </div>
</template>

<script>
    import axios from 'axios';
    import setpwd from './SetPassword.vue';
    import askpwd from './AskPassword.vue';
    import error from './ErrorAcceso.vue';

    export default {
        name: 'Login',
        components: {
            setpwd, askpwd, error
        },
        props: {
            idUsuario: String
        },
        data() {
            return {
                status: {
                    nombre: '',
                    result: '',
                    msg: '',
                },
            }
        },
        mounted () {
            axios
                .get(baseUrl + '/status/' + this.idUsuario)
                .then(response => (this.status = response.data))
                .catch(e => {
                    this.status.result = 'error'; this.status.msg = e;
                })
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
