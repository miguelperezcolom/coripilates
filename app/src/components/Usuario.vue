<template>
  <div class="content">

    <datos v-bind:id-usuario="idUsuario"></datos>

    <hr>

    <clases v-bind:id-usuario="idUsuario"></clases>

  </div>
</template>

<script>

  import datos from './Datos.vue';
  import clases from './Clases.vue';

export default {
  name: 'Usuario',
    components: {
      datos,
        clases
    },
  props: {
    idUsuario: String,
  },
    data() {
      return {
      }
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
