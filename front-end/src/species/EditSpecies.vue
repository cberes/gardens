<script>
import { mapActions } from 'vuex'
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
      species: {
        light: 'FULL',
        moisture: 'MEDIUM'
      },
      plants: [],
      plantsToDelete: [],
      loading: !!this.speciesId,
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
      return
    }

    this.fetchSpecies(this.speciesId)
      .then(result => {
        this.species = result.species
        this.plants.push.apply(this.plants, result.plants || [])
        this.loading = false
      })
  },
  computed: {
    submitText () {
      return this.species && this.species.id ? 'Update' : 'Create'
    }
  },
  methods: {
    ...mapActions('species', ['fetchSpecies', 'saveSpecies']),
    deletePlant (plant) {
      this.plantsToDelete.push(plant)
    },
    onSubmit (formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.doSubmit()
        } else {
          this.$message.error('Please correct any errors')
          return false
        }
      })
    },
    doSubmit () {
      this.loading = true
      this.saveSpecies(this.buildRequest())
        .then(() => {
          this.notifySaveSucceeded()
          this.$router.push({ name: 'species-list' })
        })
        .catch(this.notifySaveFailed)
        .finally(() => (this.loading = false))
    },
    buildRequest () {
      return {
        species: this.species,
        plants: this.plants,
        plantsToDelete: this.plantsToDelete
      }
    },
    notifySaveSucceeded () {
      this.$message({
        type: 'success',
        message: 'Plant saved successfully'
      })
    },
    notifySaveFailed (error) {
      console.error('Error saving plant', error)
      this.$message.error('Uh oh, there was an error.')
    },
    onCancel () {
      this.$router.go(-1)
    }
  }
}
</script>

<template>
  <el-form ref="form" :model="species" :rules="rules" label-width="200px">
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
      <PlantListEditor v-model="plants" @delete="deletePlant"></PlantListEditor>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit('form')">{{ submitText }}</el-button>
      <el-button @click="onCancel">Cancel</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>
