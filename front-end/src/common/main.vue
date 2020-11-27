<script>
import Footer from './footer'
import Header from './header'
import AuthenticateButton from '@/auth/AuthenticateButton'

export default {
  components: { Footer, Header, AuthenticateButton },
  props: {
    title: {
      type: String,
      default: ''
    },
    includeAuthButton: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      menuOpen: false
    }
  }
}
</script>

<template>
  <el-container class="main">
    <el-container direction="horizontal">
      <el-header>
        <Header :title="title"></Header>
      </el-header>
      <div class="aside-auth">
        <AuthenticateButton v-if="includeAuthButton"></AuthenticateButton>
      </div>
    </el-container>
    <el-main>
      <slot/>
    </el-main>
    <el-footer>
      <Footer></Footer>
    </el-footer>
  </el-container>
</template>

<style scoped>
.main {
  max-width: 60em;
  margin: 2vh auto 2vh;
}
.aside-auth {
  text-align: right;
  margin: 2vh 2vh 0 auto;
  float: right;
}
.el-footer {
  margin-top: 8vh;
}
</style>
