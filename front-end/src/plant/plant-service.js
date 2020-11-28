import httpService from '@/common/http-service'
import { web as webConfig } from '@/config/config'

const baseUrl = webConfig.apiUrl + '/api/gardens'

export default {
  getGardens (authToken) {
    const url = baseUrl
    return httpService.get(url, authToken && { Authorization: authToken })
  }
}
