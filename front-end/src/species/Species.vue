<script>
import { mapActions } from 'vuex'
import PlantList from '@/plant/PlantList'

export default {
  name: 'species',
  components: { PlantList },
  props: {
    speciesId: {
      type: String,
      required: true
    }
  },
  data () {
    return {
      species: null,
      plants: null,
      loading: true,
      signedIn: null
    }
  },
  created () {
    this.currentSession()
      .then(session => (this.signedIn = !!session))
  },
  mounted () {
    this.fetchSpecies(this.speciesId)
      .then(result => {
        this.species = result.species
        this.plants = result.plants
        this.loading = false
      })
  },
  methods: {
    ...mapActions('auth', ['currentSession']),
    ...mapActions('species', ['deleteSpecies', 'fetchSpecies']),
    editSpecies () {
      this.$router.push({ name: 'edit-species', params: { id: this.speciesId } })
    },
    deleteSpeciesIfConfirmed () {
      this.confirmDeleteSpecies()
        .then(this.doDeleteSpecies)
        .catch(() => console.log('canceled delete'))
    },
    confirmDeleteSpecies () {
      return this.$confirm('This will permanently delete the plant. Continue?', 'Warning', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning'
      })
    },
    doDeleteSpecies () {
      console.log(`deleting species ${this.speciesId}`)
      this.deleteSpecies(this.speciesId)
        .then(() => {
          this.notifyDeleteSucceeded()
          this.$router.push({ name: 'species-list' })
        })
        .catch(this.notifyDeleteFailed)
    },
    notifyDeleteSucceeded () {
      this.$message({
        type: 'success',
        message: 'Plant deleted successfully'
      })
    },
    notifyDeleteFailed () {
      this.$message({
        type: 'danger',
        message: 'Delete failed'
      })
    },
    formatEnum (s) {
      if (s) {
        return s.charAt(0).toUpperCase() + s.slice(1).toLowerCase().replace('_', ' ')
      } else {
        return ''
      }
    }
  }
}
</script>

<template>
  <el-container direction="vertical" v-loading="loading" v-if="species">
    <el-row>
      <el-col :span="20">
        <h2>Plant</h2>
      </el-col>
      <el-col :span="4" v-if="signedIn">
        <el-button icon="el-icon-edit" circle @click="editSpecies"></el-button>
        <el-button icon="el-icon-delete" circle @click="deleteSpeciesIfConfirmed"></el-button>
      </el-col>
    </el-row>
    <el-row>
      {{ species.name }}
    </el-row>
    <el-row class="alt-name">
      {{ species.alternateName }}
    </el-row>
    <el-row>
      {{ formatEnum(species.moisture) }}
    </el-row>
    <el-row>
      {{ formatEnum(species.light) }}
    </el-row>
    <el-row>
      <h3>Planted</h3>
    </el-row>
    <PlantList v-if="plants" :plants="plants"></PlantList>
  </el-container>
</template>

<style scoped>
.alt-name {
  font-style: italic;
}
</style>
