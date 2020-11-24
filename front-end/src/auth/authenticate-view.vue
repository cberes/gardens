<script>
import Layout from '@/common/main'
import Authenticate from './Authenticate'

export default {
  components: { Layout, Authenticate }
}
</script>

<template>
  <Layout :includeAuthButton="false">
    <Authenticate></Authenticate>
  </Layout>
</template>

<style scoped>
</style>
