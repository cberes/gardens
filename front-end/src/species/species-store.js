import Vue from 'vue'
import authService from '@/auth/auth-service'
import speciesService from './species-service'

const state = {
  allSpecies: null
}

const mutations = {
  SET_ALL_SPECIES (state, newValue) {
    Vue.set(state, 'allSpecies', newValue)
  },
  ADD_SPECIES (state, newValue) {
    if (!state.allSpecies) {
      Vue.set(state, 'allSpecies', [])
    }
    state.allSpecies.push(newValue)
  },
  DELETE_SPECIES (state, id) {
    if (state.allSpecies) {
      const index = state.allSpecies.findIndex(it => it.id === id)
      if (index !== -1) {
        state.allSpecies.splice(index, 1)
      }
    }
  },
  INVALIDATE_CACHE (state) {
    Vue.set(state, 'allSpecies', null)
  },
}

const actions = {
  async fetchAllSpecies ({ commit, state, rootState }) {
    if (state.allSpecies) {
      return Promise.resolve(state.allSpecies)
    }

    const session = await authService.currentSession()
    const authToken = sesstion && session.getIdToken().getJwtToken()

    return speciesService.getAllSpecies().then(result => {
      commit('SET_ALL_SPECIES', result.data.results)
      return result.data.results || []
    })
  },
  async fetchSpecies ({ commit, state, rootState }, id) {
    const found = (state.allSpecies || []).find(it => it.id === id)

    if (found) {
      return Promise.resolve(found)
    }

    const session = await authService.currentSession()
    const authToken = sesstion && session.getIdToken().getJwtToken()

    return speciesService.getSpecies(id).then(result => {
      commit('ADD_SPECIES', result.data.result)
      return result.data.result
    })
  },
  async deleteSpecies ({ commit, state, rootState }, id) {
    commit('DELETE_SPECIES', id)
    const session = await authService.currentSession()
    const authToken = session.getIdToken().getJwtToken()
    return speciesService.deleteSpecies(id, authToken)
  },
  invalidateCache ({ commit }) {
    commit('INVALIDATE_CACHE')
  }
}

export default {
  namespaced: true,
  state: state,
  mutations: mutations,
  actions: actions
}
