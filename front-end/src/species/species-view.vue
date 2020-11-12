<script>
import Layout from '@/common/main'
import Species from './Species'

export default {
  components: { Layout, Species }
}
</script>

<template>
  <Layout>
    <Species :speciesId="$route.params.id"></Species>
  </Layout>
</template>

<style scoped>
</style>
