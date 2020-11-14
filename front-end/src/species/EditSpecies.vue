<script>
import { mapActions } from 'vuex'
import authService from '@/auth/auth-service'
import speciesService from './species-service'
import PlantListEditor from '@/plant/PlantListEditor'

export default {
  name: 'edit-species',
  components: { PlantListEditor },
  props: {
    speciesId: {
      type: String,
      required: false
    }
  },
  data () {
    return {
      species: null,
      loading: true,
      rules: {
        name: [
          { required: true, message: 'Please enter a name', trigger: 'blur' },
          { max: 100, message: 'Name is too long', trigger: 'blur' }
        ],
        alternateName: [
          { max: 100, message: 'Alternate name is too long', trigger: 'blur' }
        ],
        light: [
          { required: true, message: 'Light preference is required', trigger: 'blur' }
        ],
        moisture: [
          { required: true, message: 'Soil moisture preference is required', trigger: 'blur' }
        ]
      }
    }
  },
  mounted () {
    if (!this.speciesId) {
      this.species = {
        light: 'FULL',
        moisture: 'MEDIUM',
        plants: [],
        plantsToDelete: []
      }
      this.loading = false
      return
    }

    this.fetchSpecies(this.speciesId)
      .then(species => {
        this.species = species
        this.species.plantsToDelete = []
        this.loading = false
        return species
      })
  },
  methods: {
    ...mapActions('plants', ['fetchSpecies', 'invalidateCache']),
    plantDeleted (plant) {
      this.plantsToDelete.push(plant)
    },
    onSubmit (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.doSubmit()
        } else {
          this.$message.error('Please correct any errors')
          return false
        }
      })
    },
    async doSubmit () {
      const session = await authService.currentSession()
      const authToken = session.getIdToken().getJwtToken()
      speciesService.updateSpeciesAndPlants(this.species, authToken)
        .then(() => {
          this.$message({
            type: 'success',
            message: 'Plant saved successfully'
          })

          this.invalidateCache()
          this.$router.push({ name: 'species-list' })
        })
        .catch(error => {
          console.error('Error saving plant')
          console.error(error)

          this.$message.error('Uh oh, there was an error.')
        })
    },
    onCancel () {
      this.$router.go(-1)
    }
  }
}
</script>

<template>
  <el-form ref="form" :model="species" :rules="rules" label-width="120px">
    <el-form-item label="Plant name" prop="name">
      <el-input v-model="species.name" placeholder="plant's most common name"></el-input>
    </el-form-item>
    <el-form-item label="Plant name alternate" prop="alternateName">
      <el-input v-model="species.alternateName" placeholder="scientific name or common name"></el-input>
    </el-form-item>
    <el-form-item label="Light preference" prop="light">
      <el-select v-model="species.light">
        <el-option label="Full sun" value="FULL"></el-option>
        <el-option label="Part sun" value="PARTIAL"></el-option>
        <el-option label="Shade" value="SHADE"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="Soil moisture" prop="moisture">
      <el-select v-model="species.moisture">
        <el-option label="Wet" value="WET"></el-option>
        <el-option label="Medium-wet" value="MEDIUM_WET"></el-option>
        <el-option label="Medium" value="MEDIUM"></el-option>
        <el-option label="Medium-dry" value="MEDIUM_DRY"></el-option>
        <el-option label="Dry" value="DRY"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="Gardens" prop="plants">
      <PlantListEditor v-model="species.plants" @delete="deletePlant"></PlantListEditor>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit">Create</el-button>
      <el-button @click="onCancel">Cancel</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>
