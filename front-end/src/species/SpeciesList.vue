<script>
import { mapActions } from 'vuex'
import { Hub } from 'aws-amplify'

export default {
  name: 'species-list',
  data () {
    return {
      species: [],
      signedIn: false,
      error: null,
      loading: true
    }
  },
  created () {
    Hub.listen('auth', this.authStateChanged)

    this.currentSession()
      .then(session => (this.signedIn = !!session))
  },
  beforeDestroy () {
    Hub.remove('auth', this.authStateChanged)
  },
  mounted () {
    this.loadAllSpecies()
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
    ...mapActions('auth', ['currentSession']),
    ...mapActions('species', ['fetchAllSpecies', 'invalidateCache']),
    authStateChanged (data) {
      if (data.payload.event === 'signOut') {
        this.invalidateCache()
        this.loadAllSpecies()
        this.signedIn = false
      }
    },
    loadAllSpecies () {
      this.loading = true
      this.fetchAllSpecies()
        .then(species => {
          this.species = species
          this.loading = false
        })
        .catch(console.error)
    },
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
      this.$router.push({ name: 'add-species' })
    }
  }
}
</script>

<template>
  <el-container direction="vertical">
    <el-row  class="intro" v-if="!signedIn">
      <p><em>Flower Companion</em> helps you manage your plant collection!</p>
      <p>Take a look at the example list of plants below, or <strong>Sign In</strong> to add your own plants.</p>
    </el-row >
    <el-row class="buttons">
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
        <template slot="empty">
          Click <strong>Add plant</strong> to add a new plant.
        </template>
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

.intro {
  text-align: center;
}

.buttons {
  text-align: right;
}
</style>
