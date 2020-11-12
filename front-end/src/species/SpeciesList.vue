<script>
import { mapActions } from 'vuex'
import speciesService from './species-service'

export default {
  name: 'species-list',
  data () {
    return {
      species: [],
      garden: new Set(),
      gardenFilters: [],
      error: null,
      loading: true
    }
  },
  mounted () {
    this.fetchAllSpecies()
      .then(species => {
        this.populateTable(species)
        return species
      })
      .then(species => {
        this.createGardenFilters(species)
        return species
      })
      .then(doneLoading)
  },
  methods: {
    ...mapActions('plants', ['fetchAllSpecies']),
    populateTable(species) {
      species.forEach(aSpecies => {
        const tableItem = {
          id: aSpecies.species.id,
          name: aSpecies.species.name,
          gardens: aSpecies.plants.map(it => it.garden)
        }
        this.species.push(tableItem)
      })
    },
    createGardenFilters(species) {
      species.forEach(aSpecies => {
        aSpecies.plants.map(it => it.garden).forEach(value => {
          if (!this.garden.has(value)) {
            this.garden.add(value)
            this.gardenFilters.push({ text: value, value })
          }
        })
      })
    },
    doneLoading() {
      this.loading = false
    },
    formatArray(row, column, value) {
      if (value.length === 0) {
        return ''
      } else if (value.length === 1) {
        return value[0]
      } else {
        return value[0] + ' and ' + (value.length - 1) + ' more'
      }
    },
    filterArray(value, row, column) {
      const property = column['property'];
      return row[property].indexOf(value) !== -1;
    },
    clearFilter() {
      this.$refs.species.clearFilter();
    },
    loadSpecies(id) {
      this.$router.push({ name: 'species', params: { id } })
    },
    addSpecies() {
      this.$router.push({ name: 'edit-species' })
    }
  }
}
</script>

<template>
  <el-container>
    <el-row>
      <el-button @click="clearFilter">Reset filters</el-button>
      <el-button @click="addSpecies">Add plant</el-button>
    </el-row>
    <el-row>
      <el-table
        ref="species"
        v-loading="loading"
        :data="species"
        style="width: 100%">
        <el-table-column
          prop="name"
          label="Plant"
          sortable
          column-key="name">
          <template slot-scope="scope">
            <el-button
              @click.native.prevent="loadSpecies(scope.row.id)"
              type="text">
              {{ scope.row.name }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column
          prop="gardens"
          label="Gardens"
          :formatter="formatArray"
          :filters="gardenFilters"
          :filter-method="filterArray">
          <template slot-scope="scope">
            {{ scope.row.gardens }}
            <el-tag
              :type="scope.row.tag === 'Home' ? 'primary' : 'success'"
              disable-transitions>{{scope.row.tag}}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-row>
  </el-container>
</template>

<style scoped>
.loading {
  color: #555555;
}

.error {
  color: #660000;
}
</style>
