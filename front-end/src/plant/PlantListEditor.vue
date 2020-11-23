<script>
import { mapActions } from 'vuex'

export default {
  name: 'plant-list-editor',
  props: {
    value: {
      type: Array,
      required: false
    }
  },
  data () {
    return {
      plants: this.value.map(plant => {
        return {
          ...plant,
          key: plant.id
        }
      }),
      gardens: null,
      newGarden: '',
      resultsFound: 0
    }
  },
  computed: {
    hintVisible () {
      return this.newGarden.length > 0 && this.resultsFound === 0
    }
  },
  mounted () {
    this.loadGardens()
  },
  methods: {
    ...mapActions('plant', ['fetchGardens']),
    loadGardens () {
      this.fetchGardens()
        .catch(error => {
          console.error('Failed to get gardens', error)
          return []
        })
        .then(gardenList => (this.gardens = gardenList.map(garden => ({
          name: garden,
          key: garden.toLowerCase()
        }))))
    },
    querySearch (queryString, cb) {
      const queryStringLower = queryString.toLowerCase()
      const results = this.gardens
        .filter(garden => garden.key.indexOf(queryStringLower) === 0)
        .map(garden => garden.name)
      this.resultsFound = results.length
      cb(results)
    },
    handleSelect ({ value: garden }) {
      this.plants.push({
        key: Date.now().toString(),
        garden
      })

      this.$emit('input', this.plants)
      this.clearGarden()
    },
    clearGarden () {
      this.newGarden = ''
    },
    deletePlant (plantKey) {
      const index = this.plants.findIndex(it => it.key === plantKey)
      const plant = index !== -1 ? this.plants[index] : null

      if (index !== -1) {
        this.plants.splice(index, 1)
        this.$emit('input', this.plants)
      }

      if (plant && plant.id) {
        this.$emit('delete', plant.id)
      }
    }
  }
}
</script>

<template>
  <el-container>
    <el-row>
      <el-popover
        placement="top"
        width="200"
        content="Press enter to add a new garden"
        trigger="manual"
        :value="hintVisible">
        <el-autocomplete
          v-model="newGarden"
          placeholder="Please input"
          v-on:keyup.enter="enterPressed"
          :fetch-suggestions="querySearch"
          :trigger-on-focus="false"
          :select-when-unmatched="true"
          @select="handleSelect"
        ></el-autocomplete>
      </el-popover>
    </el-row>
    <el-row>
      <el-tag
        v-for="plant in plants"
        :key="plant.key"
        closable
        @close="deletePlant(plant.key)"
        type="success"
        effect="plain">
        {{ plant.garden }}
      </el-tag>
    </el-row>
  </el-container>
</template>

<style scoped>
</style>
