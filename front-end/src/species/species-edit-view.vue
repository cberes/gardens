<script>
import Layout from '@/common/main'
import EditSpecies from './EditSpecies'

export default {
  components: { Layout, EditSpecies }
}
</script>

<template>
  <Layout>
    <EditSpecies :speciesId="$route.params.id"></EditSpecies>
  </Layout>
</template>

<style scoped>
</style>
