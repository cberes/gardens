<script>
import { mapActions } from 'vuex'
import moment from 'moment'
import authService from '@/auth/auth-service'
import plantService from './plant-service'

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
  calculated: {
    hintVisible () {
      return this.newGarden.length > 0 && this.resultsFound === 0
    }
  },
  mounted () {
    this.loadGardens()
  },
  methods: {
    async loadGardens() {
      const session = await authService.currentSession()
      const authToken = session.getIdToken().getJwtToken()
      plantService.getGardens(authToken)
        .then(result => {
          this.gardens = result.data.gardens
        })
    },
    querySearch(queryString, cb) {
      const queryStringLower = queryString.toLowerCase()
      const results = this.gardens
        .filter(it => it.toLowerCase().indexOf(queryStringLower) === 0)
      this.resultsFound = results.length
      cb(results)
    },
    handleSelect({ value: garden }) {
      this.plants.push({
        key: Date.now().toString(),
        garden
      })

      this.$emit('input', this.plants)
      this.clearGarden()
    },
    clearGarden() {
      this.newGarden = ''
    },
    deletePlant(event) {
      const elem = event.target.parentElement
      const plantKey = elem && elem.dataset['plantKey']
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
        trigger="manual"
        content="Press enter to add a new garden"
        v-model="hintVisible">
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
        :data-plant-key="plant.key"
        closable
        @close="deletePlant"
        type="success"
        effect="plain">
        {{ plant.garden }}
      </el-tag>
    </el-row>
  </el-container>
</template>

<style scoped>
</style>
