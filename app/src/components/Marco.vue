<template>
  <div id="marco">
    <router-view/>
  </div>
</template>

<script>

    export default {
        name: 'Marco',
        props: {
            idUsuario: String
        },
        data() {
            return {

            }
        },

        beforeRouteEnter (to, from, next) {
            // called before the route that renders this component is confirmed.
            // does NOT have access to `this` component instance,
            // because it has not been created yet when this guard is called!
            console.log('autenticado: ' + autenticado);
            if (!autenticado && to.path.indexOf('autenticado') > 0) {
                oldTo = to;
                next(to.path.substring(0, to.path.indexOf('autenticado')) + '/login');
            } else {
                next();
            }

        },
    }

</script>

<style>
</style>
