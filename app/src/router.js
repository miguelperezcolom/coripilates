import Vue from 'vue'
import Router from 'vue-router'
import Home from './views/Home.vue'
import Marco from './components/Marco.vue'
import Error from './components/Error.vue'
import Autenticado from './components/Autenticado.vue'
import Login from './components/Login.vue'
import Usuario from './components/Usuario.vue'
import Clase from './components/Clase.vue'
import FechasAlternativas from './components/FechasAlternativas.vue'
import ClasesAlternativas from './components/ClasesAlternativas.vue'
import WhoAreYou from './components/WhoAreYou.vue'
import SetPassword from './components/SetPassword.vue'
import ResetPassword from './components/ResetPassword.vue'
import PwdResetSent from './components/PwdResetSent.vue'

Vue.use(Router)

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      name: 'home',
      component: WhoAreYou
    },
      {
          path: '/pwdresetsent',
          name: 'home',
          component: PwdResetSent
      },
      {
          path: '/:idUsuario',
          name: 'privado',
          component: Marco,
          props: true,
          children: [
              {path: 'autenticado', component:Autenticado, props: true,
                  children: [
                      {path: 'clases/:idClase/alternativas', component:FechasAlternativas, props: true,},
                      {path: 'clases/:idClase/alternativas/:fecha', component:ClasesAlternativas, props: true,},
                      {path: 'clases/:idClase', component:Clase, props: true,},
                      {path: 'cambiarpassword', component:SetPassword, props: true,},
                      {path: '*', component:Usuario, props: true,},
                      {path: '', component:Usuario, props: true,},
                  ]
              },
              {path: 'reset/:resetkey', component:ResetPassword, props: true,},
              {path: '*', component:Login, props: true,},
              {path: '', component:Login, props: true,},
          ]
      },
      {
          path: '/*',
          name: 'who',
          component: WhoAreYou
      }
  ]
})
