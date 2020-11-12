<script>
import Layout from '@/common/main'
import SpeciesList from './SpeciesList'

export default {
  components: { Layout, SpeciesList }
}
</script>

<template>
  <Layout>
    <div class="container">
      <h2>Plant list</h2>
      <SpeciesList></SpeciesList>
    </div>
  </Layout>
</template>

<style scoped>
</style>
