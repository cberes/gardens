import Vue from 'vue'

// Element
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import 'element-ui/lib/theme-chalk/display.css'
import locale from 'element-ui/lib/locale/lang/en'

// Amplify
import Amplify, * as AmplifyModules from 'aws-amplify'
import { AmplifyPlugin } from 'aws-amplify-vue'
import { aws as awsConfig } from './config/config'

// this app ...
import App from './App.vue'
import router from './router'
import store from './store'
import navigationGuards from './common/navigation-guards'

require('normalize-css')

Vue.config.productionTip = false
Vue.use(ElementUI, { locale })

Amplify.configure(awsConfig)
Vue.use(AmplifyPlugin, AmplifyModules)

navigationGuards(router)

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
