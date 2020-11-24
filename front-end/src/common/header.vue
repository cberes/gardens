<script>

export default {
  props: {
    title: {
      type: String,
      default: ''
    }
  },
  computed: {
    titleOrDefault () {
      return this.title || 'Your plants'
    }
  }
}
</script>

<template>
  <h1><router-link to="/">{{ titleOrDefault }}</router-link></h1>
</template>

<style scoped>
</style>
