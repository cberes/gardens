import { Auth } from 'aws-amplify'

export default {
  currentSession () {
    return Auth.currentSession()
      .catch(err => console.log(err))
  },
  signOut () {
    return Auth.signOut()
  }
}
