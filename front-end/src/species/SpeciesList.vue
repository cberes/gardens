<script>
import { mapActions } from 'vuex'

export default {
  name: 'species-list',
  data () {
    return {
      species: [],
      error: null,
      loading: true
    }
  },
  mounted () {
    this.fetchAllSpecies()
      .then(species => {
        this.species = species
        this.loading = false
      })
  },
  computed: {
    gardenFilters () {
      const filters = []
      const garden = new Set()

      this.species.forEach(theSpecies => {
        theSpecies.plants.map(it => it.garden).forEach(value => {
          if (!garden.has(value)) {
            garden.add(value)
            filters.push({ text: value, value })
          }
        })
      })

      return filters
    },
    tableRows () {
      return this.species.map(theSpecies => {
        return {
          id: theSpecies.species.id,
          name: theSpecies.species.name,
          gardens: theSpecies.plants.map(it => it.garden)
        }
      })
    }
  },
  methods: {
    ...mapActions('species', ['fetchAllSpecies']),
    formatArray (row, column, value) {
      if (value.length === 0) {
        return ''
      } else if (value.length === 1) {
        return value[0]
      } else {
        return value[0] + ' and ' + (value.length - 1) + ' more'
      }
    },
    filterArray (value, row, column) {
      const property = column.property
      return row[property].indexOf(value) !== -1
    },
    clearFilter () {
      this.$refs.species.clearFilter()
    },
    loadSpecies (id) {
      this.$router.push({ name: 'species', params: { id } })
    },
    addSpecies () {
      this.$router.push({ name: 'edit-species' })
    }
  }
}
</script>

<template>
  <el-container direction="vertical">
    <el-row>
      <el-button @click="clearFilter">Reset filters</el-button>
      <el-button @click="addSpecies">Add plant</el-button>
    </el-row>
    <el-row>
      <el-table
        ref="species"
        v-loading="loading"
        :data="tableRows"
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
