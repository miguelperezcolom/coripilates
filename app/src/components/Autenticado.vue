<template>
  <div class="content">

    <router-view></router-view>

  </div>
</template>

<script>

  import axios from 'axios';
  import datos from './Datos.vue';
  import clases from './Clases.vue';

export default {
  name: 'Autenticado',
  props: {
    idUsuario: String,
  },
    data() {
      console.log('hola desde autenticado ' + this.idUsuario);
      return {
        datos: {
            nombre: '',
        }
      }
    }
    ,
    mounted () {
        axios
            .get(baseUrl + '/datos/' + this.idUsuario)
            .then(response => (this.datos = response.data))
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
