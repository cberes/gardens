import Vue from 'vue'
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
  }
}

const actions = {
  fetchAllSpecies ({ commit, state, rootState }) {
    if (state.allSpecies) {
      return Promise.resolve(state.allSpecies)
    }

    return speciesService.getAllSpecies().then(result => {
      commit('SET_ALL_SPECIES', result.data.results)
      return result.data.results || []
    })
  },
  fetchSpecies ({ commit, state, rootState }, id) {
    const found = (state.allSpecies || []).find(it => it.id === id)

    if (found) {
      return Promise.resolve(found)
    }

    return speciesService.getSpecies(id).then(result => {
      commit('ADD_SPECIES', result.data.result)
      return result.data.result
    })
  }
}

export default {
  namespaced: true,
  state: state,
  mutations: mutations,
  actions: actions
}
