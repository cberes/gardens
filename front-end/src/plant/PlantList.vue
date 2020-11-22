<script>
import moment from 'moment'

export default {
  name: 'plant-list',
  props: {
    plants: {
      type: Array,
      required: true
    }
  },
  methods: {
    plantDate (plant) {
      return moment(plant.planted).format('YYYY/MM/DD')
    }
  }
}
</script>

<template>
  <el-container v-if="plants.length > 0">
    <el-row v-for="plant in plants" :key="plant.id" :gutter="20">
      <el-col :span="16">{{ plant.garden }}</el-col>
      <el-col :span="8">{{ plantDate(plant) }}</el-col>
    </el-row>
  </el-container>
  <el-row v-else>
    None
  </el-row>
</template>

<style scoped>
</style>
