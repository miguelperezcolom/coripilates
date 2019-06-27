<template>
  <div class="content">
    <h2>Tus clases</h2>
      <li v-for="clase in clases" :class="estilo(clase)">
        <h2>{{ clase.hora }}</h2>
        <p>Fecha: {{clase.fecha}}</p>
        <p>Actividad: {{clase.actividad}}</p>
        <p>Nivel: {{clase.nivel}}</p>
        <p v-if="clase.modificable && clase.activa">Asistiré</p>
        <p v-if="clase.modificable && !clase.activa">No asistiré</p>
        <p v-if="clase.modificable && clase.cambio">Es un cambio</p>

        <router-link :to="classurl(clase.id)" v-if="clase.modificable" class="button is-primary">Modificar</router-link>
      </li>
  </div>
</template>

<script>

    import axios from 'axios';

    export default {
        name: 'Clases',
        props: {
            idUsuario: String
        },
        data() {
            return {
                clases: [],
            }
        },
        methods: {
          classurl: function(cid) {
              return '/' + this.idUsuario + '/autenticado/clases/' + cid;
          },
          estilo: function(c) {
              var css = 'box';
              if (!c.modificable) css += ' pasado';
              return css;
          }
        },
        mounted () {
            axios
                .get(baseUrl + '/clases/' + this.idUsuario)
                .then(response => (this.clases = response.data))
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

  .pasado {
    opacity: 0.5;
  }

</style>
