<script>
import authService from '@/auth/auth-service'

export default {
  data () {
    return {
      signedIn: null
    }
  },
  created () {
    authService.currentSession()
      .then(session => (this.signedIn = !!session))
  }
}
</script>

<template>
  <el-menu :router="true">
    <el-menu-item index="/">
      <i class="el-icon-location"></i> Home
    </el-menu-item>
    <el-menu-item index="/signout" v-if="signedIn">
      <i class="el-icon-user"></i> Sign out
    </el-menu-item>
    <el-menu-item index="/authenticate" v-if="!signedIn">
      <i class="el-icon-user-solid"></i> Sign in
    </el-menu-item>
  </el-menu>
</template>

<style module>
</style>
