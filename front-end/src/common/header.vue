<script>

export default {
  props: {
    title: {
      type: String,
      default: ''
    }
  }
}
</script>

<template>
  <h1 v-if="title">{{ title }}</h1>
  <h1 v-else>Your plants</h1>
</template>

<style scoped>
</style>
