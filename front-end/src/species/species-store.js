import Vue from 'vue'
import authService from '@/auth/auth-service'
import speciesService from './species-service'

const TTL_MINUTES = 10

const expireTime = () => {
  const date = new Date()
  date.setMinutes(date.getMinutes() + TTL_MINUTES)
  return date
}

const getAuthToken = async () => {
  const session = await authService.currentSession()
  return session && session.getIdToken().getJwtToken()
}

const state = {
  allSpecies: null,
  allSpeciesExpires: Date.now(),
  cache: {}
}

const mutations = {
  SET_ALL_SPECIES (state, newValue) {
    Vue.set(state, 'allSpecies', newValue)
    Vue.set(state, 'allSpeciesExpires', expireTime())
  },
  ADD_SPECIES (state, newValue) {
    Vue.set(state.cache, newValue.species.id, newValue)
  },
  DELETE_SPECIES (state, id) {
    Vue.delete(state.cache, id)
  },
  DELETE_FROM_ALL_SPECIES (state, id) {
    if (state.allSpecies) {
      const index = state.allSpecies.findIndex(it => it.species.id === id)
      if (index !== -1) {
        state.allSpecies.splice(index, 1)
      }
    }
  },
  INVALIDATE_CACHE (state) {
    Vue.set(state, 'allSpecies', null)
  }
}

const actions = {
  async fetchAllSpecies ({ commit, state, rootState }) {
    if (state.allSpecies && Date.now() <= state.allSpeciesExpires) {
      return Promise.resolve(state.allSpecies)
    }

    const authToken = await getAuthToken()

    return speciesService.getAllSpecies(authToken)
      .then(result => result.data.results || [])
      .then(results => {
        commit('SET_ALL_SPECIES', results)
        results.forEach(it => commit('ADD_SPECIES', it))
        return results
      })
      .catch(error => {
        console.error('Failed to get all species', error)
        return []
      })
  },
  async fetchSpecies ({ commit, state, rootState }, id) {
    const found = state.cache[id]

    if (found) {
      return Promise.resolve(found)
    }

    const authToken = await getAuthToken()

    return speciesService.getSpecies(id, authToken)
      .then(result => {
        commit('ADD_SPECIES', result.data.result)
        return result.data.result
      })
  },
  async saveSpecies ({ commit }, speciesAndPlants) {
    commit('INVALIDATE_CACHE')
    const authToken = await getAuthToken()
    return speciesService.updateSpeciesAndPlants(speciesAndPlants, authToken)
  },
  async deleteSpecies ({ commit }, id) {
    commit('DELETE_SPECIES', id)
    commit('DELETE_FROM_ALL_SPECIES', id)
    const authToken = await getAuthToken()
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
