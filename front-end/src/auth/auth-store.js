import authService from './auth-service'

const state = {
  authToken: null,
  clientId: '',
  userPoolId: '',
  redirectFrom: null
}

const mutations = {
  SET_AUTH_TOKEN (state, value) {
    state.authToken = value
  },
  SET_REDIRECT_FROM (state, value) {
    state.redirectFrom = value
  }
}

const actions = {
  setAuthToken ({ commit }, value) {
    commit('SET_AUTH_TOKEN', value)
  },
  setRedirect ({ commit }, value) {
    commit('SET_REDIRECT_FROM', value)
  },
  clearRedirect ({ commit }) {
    commit('SET_REDIRECT_FROM', null)
  },
  currentSession () {
    return authService.currentSession()
  }
}

export default {
  namespaced: true,
  state: state,
  mutations: mutations,
  actions: actions
}
