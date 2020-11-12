<script>
import { mapActions } from 'vuex'
import moment from 'moment'
import authService from '@/auth/auth-service'
import speciesService from './species-service'

export default {
  name: 'edit-species',
  props: {
    speciesId: {
      type: String,
      required: false
    }
  },
  data () {
    return {
      species: null,
      loading: true
    }
  },
  mounted () {
    if (!this.speciesId) {
      this.species = {}
      this.loading = false
      return
    }

    this.fetchSpecies(this.speciesId)
      .then(species => {
        this.species = species
        this.loading = false
        return species
      })
  },
  methods: {
    ...mapActions('plants', ['fetchSpecies'])
  }
}
</script>

<template>
  <el-container v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="20">
        <h2>Plant</h2>
      </el-col>
      <el-col :span="4" v-if="signedIn">
        <el-button icon="el-icon-edit" circle @click="editSpecies"></el-button>
        <el-button icon="el-icon-delete" circle @click="confirmDeleteSpecies"></el-button>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      {{ species.name }}
    </el-row>
    <el-row :gutter="20" class="alt-name">
      {{ species.alternateName }}
    </el-row>
    <el-row :gutter="20">
      {{ species.moisture }}
    </el-row>
    <el-row :gutter="20">
      {{ species.light }}
    </el-row>
    <el-row :gutter="20">
      <h3>Planted</h3>
    </el-row>
    <el-row v-for="plant in species.plants" :key="plant.id" :gutter="20">
      <el-col :span="16">{{ plant.garden }}</el-col>
      <el-col :span="8">{{ plant.planted }}</el-col>
    </el-row>
  </el-container>
</template>

<style scoped>
.alt-name {
  font-style: italic;
}
</style>
