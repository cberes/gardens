import httpService from '@/common/http-service'
import { web as webConfig } from '@/config/config'

const baseUrl = webConfig.apiUrl + '/api/species'
const baseUrlPublic = webConfig.apiUrl + '/api/public/species'

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
  }
}
