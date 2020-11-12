import axios from 'axios'

export default {
  get (url, headers) {
    const options = headers && { headers }
    return axios.get(url, options)
  },
  post (url, request, headers) {
    const options = headers && { headers }
    return axios.post(url, request, options)
  },
  delete (url, headers) {
    const options = headers && { headers }
    return axios.delete(url, options)
  }
}
