import httpService from '@/common/http-service'
import apiConfig from '@/config/api'

const baseUrl = apiConfig.url + '/api/species'
const baseUrlPublic = apiConfig.url + '/api/public/species'

export default {
  getAllSpecies (authToken) {
    const url = authToken ? baseUrl : baseUrlPublic
    return httpService.get(url, authToken && { Authorization: authToken })
  },

  getSpecies (id, authToken) {
    const url = (authToken ? baseUrl : baseUrlPublic) + '/' + id
    return httpService.get(url, authToken && { Authorization: authToken })
  },

  updateSpeciesAndPlants (request, authToken) {
    const url = baseUrl
    return httpService.post(url, request, { Authorization: authToken })
  },

  deleteSpecies (id, authToken) {
    const url = baseUrl + '/' + id
    return httpService.delete(url, { Authorization: authToken })
  },
}
