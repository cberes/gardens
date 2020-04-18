import httpService from './HttpService'

const baseUrl = '/api/campgrounds'

export default {
  getCampground (id) {
    const url = baseUrl + '/' + id
    return httpService.get(url)
  },

  getCampgrounds () {
    return httpService.get(baseUrl)
  },

  getCampsites (id) {
    const url = baseUrl + '/' + id + '/campsites'
    return httpService.get(url)
  }
}
