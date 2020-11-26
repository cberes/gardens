<script>
import { mapActions } from 'vuex'

export default {
  name: 'authenticate-button',
  data () {
    return {
      signedIn: false
    }
  },
  created () {
    this.currentSession()
      .then(session => (this.signedIn = !!session))
  },
  methods: {
    ...mapActions('auth', ['currentSession', 'signOut']),
    ...mapActions('species', ['invalidateCache']),
    doSignOut () {
      this.invalidateCache() // TODO would be nice if this happened after sign out but before the event
      this.signOut()
        .then(this.afterSignOut)
        .catch(console.error)
    },
    afterSignOut () {
      this.signedIn = false
      this.goHome()
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
