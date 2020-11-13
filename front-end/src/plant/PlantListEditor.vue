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
      plants: this.value,
      gardens: null,
      newGarden: '',
      hintVisible: false
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
      this.hintVisible = results.length === 0
      cb(results)
    },
    handleSelect(item) {
      // TODO figure this out
      console.log(item);
      this.plants.push({
        // TODO need to set a key
        garden: item
      })

      this.$emit('input', this.plants)

      this.newGarden = ''
      this.hintVisible = false
    },
    deletePlant(x, y, z) {
      // TODO figure this out
      console.log(x)
      console.log(y)
      console.log(z)

      if (x.id) {
        this.$emit('delete', x)
      }

      const index = this.plants.findIndex(it => it.id === x.id)
      if (index !== -1) {
        this.plants.splice(index, 1)
        this.$emit('input', this.plants)
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
          v-on:keyup.enter="enterPressed"
          :fetch-suggestions="querySearch"
          placeholder="Please input"
          @select="handleSelect"
        ></el-autocomplete>
      </el-popover>
    </el-row>
    <el-row>
      <el-tag
        v-for="plant in plants"
        :key="plant.id"
        closable
        @close="deletePlant">
        {{ plant.garden }}
      </el-tag>
    </el-row>
  </el-container>
</template>

<style scoped>
</style>
