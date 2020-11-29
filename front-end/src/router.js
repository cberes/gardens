import Vue from 'vue'
import VueRouter from 'vue-router'
import authRoutes from '@/auth/auth-routes'
import { web as webConfig } from '@/config/config'
import speciesRoutes from '@/species/species-routes'

Vue.use(VueRouter)

const router = new VueRouter({
  mode: 'history',
  base: webConfig.baseUrl,
  routes: [
    ...authRoutes,
    ...speciesRoutes,
    {
      path: '/404',
      name: '404',
      component: require('./common/404').default,
      props: true
    },
    {
      path: '*',
      redirect: '404'
    }
  ]
})

export default router
