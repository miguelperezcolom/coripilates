<template>
  <div class="content">
    <router-link to=".">< Volver a la clase</router-link>
    <h1>Tenemos plazas en estas fechas</h1>
      <li v-for="clase in alternativas" class="box">
        <h2>{{ clase.texto }}</h2>
        <router-link :to="url(clase)" class="button is-primary">Mirar este día</router-link>
      </li>
  </div>
</template>

<script>

    import axios from 'axios';

    export default {
        name: 'FechasAlternativas',
        props: {
            idUsuario: String,
            idClase: String,
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
                      this.$router.go(-1)
                  ))
          },
            url: function(c) {
              return 'alternativas/' + c.fecha;
            }
        },
        mounted () {
            axios
                .get(baseUrl + '/alternativas/' + this.idUsuario + '/' + this.idClase)
                .then(response => (this.alternativas = response.data))
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
