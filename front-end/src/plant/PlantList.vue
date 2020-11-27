<script>
import { format, parseISO } from 'date-fns'

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
      return plant.planted ? format(parseISO(plant.planted), 'yyyy/MM/dd') : ''
    }
  }
}
</script>

<template>
  <el-container direction="vertical" v-if="plants.length > 0">
    <el-row v-for="plant in plants" :key="plant.id">
      <el-col :span="16">{{ plant.garden }}</el-col>
      <el-col :span="8" class="plant-date">{{ plantDate(plant) }}</el-col>
    </el-row>
  </el-container>
  <el-row v-else>
    None
  </el-row>
</template>

<style scoped>
.plant-date {
  font-style: italic;
}
</style>
