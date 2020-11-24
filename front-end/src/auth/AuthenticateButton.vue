<script>
import { mapActions } from 'vuex'
import { AmplifyEventBus } from 'aws-amplify-vue'

export default {
  name: 'authenticate',
  data () {
    return {
      signedIn: false
    }
  },
  created () {
    this.currentSession()
      .then(session => (this.signedIn = !!session))

    AmplifyEventBus.$on('authState', info => {
      if (info === 'signedOut') {
        this.signedIn = false
        this.goHome()
      }
    })
  },
  beforeDestroy () {
    AmplifyEventBus.$off('authState')
  },
  methods: {
    ...mapActions('auth', ['currentSession', 'signOut']),
    doSignOut () {
      this.signOut()
        .then(() => {
          this.signedIn = false
          this.goHome()
        })
        .catch(console.error)
    },
    goHome () {
      this.$router.push({ name: 'home' })
    },
    goAuth () {
      this.$router.push({ name: 'authenticate' })
    }
  }
}
</script>

<template>
  <el-button v-if="signedIn" @click="doSignOut">Sign Out</el-button>
  <el-button v-else @click="goAuth">Sign In</el-button>
</template>

<style scoped>
</style>
