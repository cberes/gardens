<script>
import { components } from 'aws-amplify-vue'

export default {
  name: 'app',
  components: {
    ...components
  }
}
</script>

<template>
  <div id="app">
    <router-view :key="$route.fullPath"/>
  </div>
</template>

<style>
#app {
  font-family: "Helvetica Neue",Helvetica,Arial,sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
