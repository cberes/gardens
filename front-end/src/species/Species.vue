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
  computed: {
    moisturePercent () {
      switch (this.species && this.species.moisture) {
        case 'WET':
          return 100
        case 'MEDIUM_WET':
          return 80
        case 'MEDIUM':
          return 60
        case 'MEDIUM_DRY':
          return 40
        case 'DRY':
        default:
          return 20
      }
    },
    lightPercent () {
      switch (this.species && this.species.light) {
        case 'FULL':
          return 100
        case 'PARTIAL':
          return 66
        case 'SHADE':
        default:
          return 33
      }
    }
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
      <h2>{{ species.name }}</h2>
      <div id="edit-buttons" v-if="signedIn">
        <el-button icon="el-icon-edit" circle @click="editSpecies"></el-button>
        <el-button icon="el-icon-delete" circle @click="deleteSpeciesIfConfirmed"></el-button>
      </div>
    </el-row>
    <el-row :gutter="10">
      <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
        <el-card shadow="hover" class="box-card">
          <div slot="header" class="clearfix card-header">Also known as</div>
          <div class="card-body">
            {{ species.alternateName }}
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
        <el-card shadow="hover" class="box-card">
          <div slot="header" class="clearfix card-header">Soil moisture</div>
          <div class="card-body percent">
            <el-progress type="circle" :percentage="moisturePercent" :show-text="false"></el-progress>
            <div>{{ formatEnum(species.moisture) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
        <el-card shadow="hover" class="box-card">
          <div slot="header" class="clearfix card-header">Sun</div>
          <div class="card-body percent">
            <el-progress type="circle" :percentage="lightPercent" status="warning" :show-text="false"></el-progress>
            <div>{{ formatEnum(species.light) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
        <el-card shadow="hover" class="box-card">
          <div slot="header" class="clearfix card-header">Planted</div>
          <PlantList v-if="plants" :plants="plants" class="card-body"></PlantList>
        </el-card>
      </el-col>
    </el-row>
  </el-container>
</template>

<style scoped>
.alt-name {
  font-style: italic;
  padding-left: 2em;
}
#edit-buttons {
  text-align: right;
  float: right;
}
.el-card {
  margin-bottom: 1vh;
}
.card-header {
  font-weight: bold;
}
.clearfix:before, .clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}
.card-body {
  font-size: 18px;
}
.percent {
  text-align: center;
}
</style>
