<script>
import { mapActions, mapState } from 'vuex'
import { Hub } from 'aws-amplify'
import _ from 'lodash'

export default {
  name: 'authenticate',
  data () {
    return {
      authConfig: {
        usernameAttributes: 'email'
      }
    }
  },
  created () {
    Hub.listen('auth', this.authStateChanged)
  },
  beforeDestroy () {
    Hub.remove('auth', this.authStateChanged)
  },
  computed: {
    ...mapState('auth', ['redirectFrom'])
  },
  methods: {
    ...mapActions('auth', ['clearRedirect']),
    ...mapActions('species', ['invalidateCache']),
    authStateChanged (data) {
      if (data.payload.event === 'signIn') {
        this.invalidateCache()
        this.redirectAfterLogin()
      }
    },
    redirectAfterLogin () {
      if (this.redirectFrom) {
        const next = _.cloneDeep(this.redirectFrom)
        this.clearRedirect()
        this.$router.push(next)
      } else {
        this.$router.push({ name: 'home' })
      }
    }
  }
}
</script>

<template>
  <div class="container">
    <amplify-authenticator :authConfig="authConfig"></amplify-authenticator>
  </div>
</template>

<style scoped>
</style>
