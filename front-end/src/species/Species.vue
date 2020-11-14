<script>
import { mapActions } from 'vuex'
import moment from 'moment'
import authService from '@/auth/auth-service'

export default {
  name: 'species',
  props: {
    speciesId: {
      type: String,
      required: true
    }
  },
  data () {
    return {
      species: null,
      loading: true,
      signedIn: null
    }
  },
  created () {
    authService.currentSession()
      .then(session => (this.signedIn = !!session))
  },
  mounted () {
    this.fetchSpecies(this.speciesId)
      .then(species => {
        this.species = species
        this.loading = false
        return species
      })
  },
  methods: {
    ...mapActions('plants', ['deleteSpecies', 'fetchSpecies']),
    editSpecies () {
      this.$router.push({ name: 'edit-species', params: { id: this.speciesId } })
    },
    confirmDeleteSpecies () {
      this.$confirm('This will permanently delete the plant. Continue?', 'Warning', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        this.deleteSpecies(this.speciesId)
          .then(() => {
            this.$message({
              type: 'success',
              message: 'Plant deleted successfully'
            })

            this.$router.push({ name: 'species-list' })
          }).catch(() => {
            this.$message({
              type: 'danger',
              message: 'Delete failed'
            })
          })
      })
    },
    plantDate (plant) {
      return moment(plant.planted).format('YYYY/MM/DD')
    }
  }
}
</script>

<template>
  <el-container v-loading="loading" v-if="species">
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
      <el-col :span="8">{{ plantDate(plant) }}</el-col>
    </el-row>
  </el-container>
</template>

<style scoped>
.alt-name {
  font-style: italic;
}
</style>
