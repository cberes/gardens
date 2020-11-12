import Vue from 'vue'
import Vuex from 'vuex'
import authModule from '@/auth/auth-store'
import speciesModule from '@/species/species-store'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
  },
  mutations: {
  },
  actions: {
  },
  modules: {
    auth: authModule,
    species: speciesModule
  }
})
