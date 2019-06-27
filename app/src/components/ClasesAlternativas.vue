<template>
  <div class="content">
    <router-link to=".">< Volver a la fecha</router-link>
    <h1>Hay plazas en estas clases</h1>
      <li v-for="clase in alternativas" class="box">
        <h2>{{ clase.hora }}</h2>
        <p>{{clase.actividad}}</p>
        <p>{{clase.nivel}}</p>

        <button @click="cambiar(clase.id)" class="button is-primary">Cambiar a esta clase</button>
      </li>
  </div>
</template>

<script>

    import axios from 'axios';

    export default {
        name: 'ClasesAlternativas',
        props: {
            idUsuario: String,
            idClase: String,
            fecha: String,
        },
        data() {
            return {
                alternativas: [],
            }
        },
        methods: {
          cambiar: function(cid) {
              console.log('cambiar(' + cid + ')');
              axios
                  .post(baseUrl + '/clases/' + this.idUsuario + '/' + this.idClase, {
                      nuevaClase: cid,
                  })
                  .then(response => (
                      //this.info = response.data
                      this.$router.go(-2)
                  ))
          }
        },
        mounted () {
            axios
                .get(baseUrl + '/alternativas/' + this.idUsuario + '/' + this.idClase + '/' + this.fecha)
                .then(response => (this.alternativas = response.data))
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
