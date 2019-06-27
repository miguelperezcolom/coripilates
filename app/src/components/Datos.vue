<template>
  <div class="content">

    <h1>Hola  {{ info.nombre }} ;)</h1>

    <p v-if="info.saldo > 0">Tu saldo es de {{info.saldo}} euros.</p>
    <p v-if="info.saldo < 0">Tu saldo es de {{info.saldo}} euros. Por favor procede a pagar lo antes posible.</p>
    <p v-if="info.saldo == 0">Estás al corriente de pago.</p>

    <div class="buttons">
      <router-link to="cambiarpassword" class="button is-warning">Cambiar el password</router-link>
    </div>
  </div>
</template>

<script>
    import axios from 'axios';

    export default {
        name: 'Datos',
        props: {
            idUsuario: String,
        },
        data() {
            return {
                info: null,
            }
        },
        mounted () {
            axios
                .get(baseUrl + '/datos/' + this.idUsuario)
                .then(response => (this.info = response.data))
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
