<template>
  <div class="content">
    <router-link to=".">< Volver a la lista de clases</router-link>
    <h1>Clase {{info.hora}}</h1>
    <h2>Fecha: {{info.fecha}}</h2>
    <p>Actividad: {{info.actividad}}</p>
    <p>Nivel: {{info.nivel}}</p>
    <p v-if="info.activa">Asistiré</p>
    <p v-if="!info.activa">No asistiré</p>
    <p v-if="info.cambio">Es un cambio</p>
    <div class="buttons">
    <button @click="noasistire" v-if="info.activa" class="button is-danger">No asistiré</button>
    <button @click="asistire" v-if="!info.activa" class="button is-success">Asistiré</button>
    <router-link :to="link(info.id)" class="button is-warning">Cambiar</router-link>
    </div>
  </div>
</template>

<script>
    import axios from 'axios';

    export default {
        name: 'Clase',
        props: {
            idUsuario: String,
            idClase: String,
        },
        data() {
            return {
                info: {
                    activa: false,
                },
            }
        },
        methods: {
          asistire: function() {
              axios
                  .put(baseUrl + '/clases/' + this.idUsuario + '/' + this.idClase)
                  .then(response => (this.info = response.data))
          },
          noasistire: function() {
              axios
                  .delete(baseUrl + '/clases/' + this.idUsuario + '/' + this.idClase)
                  .then(response => (this.info = response.data))
          },
            link: function(cid) {
              return '/' + this.idUsuario + '/autenticado/clases/' + cid + '/alternativas';
            }
        },
        mounted () {

            axios
                .get(baseUrl + '/clases/' + this.idUsuario + '/' + this.idClase)
                .then(response => (this.info = response.data))

        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
