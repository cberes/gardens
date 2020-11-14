<script>
import Layout from './main'

export default {
  page: {
    title: '404',
    meta: [{ name: 'description', content: '404' }]
  },
  components: { Layout },
  props: {
    resource: {
      type: String,
      default: ''
    }
  },
  computed: {
    title () {
      return '404 ' + (this.resource || '') + ' Not Found'
    }
  }
}
</script>

<template>
  <Layout :title="title">
    <p>We can't find what you're looking for.</p>
    <p>
      Return to the <router-link to="/">plant list</router-link>.
    </p>
  </Layout>
</template>

<style module>
</style>
