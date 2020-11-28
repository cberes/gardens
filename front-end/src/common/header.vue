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
  <h1>
    <img src="/img/logo.png"/>
    <router-link to="/">{{ titleOrDefault }}</router-link>
  </h1>
</template>

<style scoped>
h1 {
  margin: 0;
}
img {
  padding-right: 1em;
  vertical-align: middle;
}
</style>
