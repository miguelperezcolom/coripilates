<template>
  <div class="content">
    <h1>El acceso no es posible</h1>

    <p class="label is-error">{{mensaje}}</p>

  </div>
</template>

<script>
    import axios from 'axios';

    export default {
        name: 'ErrorAcceso',
        props: {
            mensaje: String
        },
        data() {
            return {
            }
        },
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
