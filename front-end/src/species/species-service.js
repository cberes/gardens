import httpService from '@/common/http-service'
import apiConfig from '@/config/api'

const baseUrl = apiConfig.url + '/api/species'

export default {
  getAllSpecies () {
    const url = baseUrl
    return httpService.get(url)
  },

  getSpecies (id) {
    const url = baseUrl + '/' + id
    return httpService.get(url)
  },

  updateSpeciesAndPlants (species, plants, plantsToDelete) {
    const url = baseUrl
    const request = {
        species,
        plants,
        plantsToDelete
    }
    return httpService.post(url, request)
  },

  deleteSpecies (id) {
    const url = baseUrl + '/' + id
    return httpService.delete(url)
  },
}
