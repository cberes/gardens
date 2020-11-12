import httpService from '@/common/http-service'
import apiConfig from '@/config/api'

const baseUrl = apiConfig.url + '/api/gardens'

export default {
  getGardens () {
    const url = baseUrl
    return httpService.get(url)
  }
}
