import authService from './auth-service'
import plantService from './plant-service'

const state = {
}

const mutations = {
}

const actions = {
  async fetchGardens ({ commit }) {
    const session = await authService.currentSession()
    const authToken = session.getIdToken().getJwtToken()
    const result = await plantService.getGardens(authToken)
    return result.data.gardens || []
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
