import { expect } from 'chai'
import { mount, createLocalVue } from '@vue/test-utils'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import Vuex from 'vuex'
import AuthenticateButton from '@/auth/AuthenticateButton.vue'

const localVue = createLocalVue()

localVue.use(ElementUI, { locale })
localVue.use(Vuex)

describe('Authenticate Button', () => {
  const mockStore = (signedIn, signOut) => {
    const auth = {
      namespaced: true,
      actions: {
        currentSession: () => signedIn,
        signOut: (unused) => {
          signOut && signOut()
        }
      }
    }

    const species = {
      namespaced: true,
      actions: {
        invalidateCache: () => {}
      }
    }

    return new Vuex.Store({
      modules: {
        auth,
        species
      }
    })
  }

  const factory = async (store) => {
    const wrapper = mount(AuthenticateButton, { store, localVue })
    wrapper.vm.$router = {
      push: () => {}
    }

    await localVue.nextTick()

    return wrapper
  }

  const clickButton = wrapper => {
    wrapper.find('.el-button').trigger('click')
  }

  it('renders Sign In button when signed out', async () => {
    const wrapper = await factory(mockStore(false))

    expect(wrapper.text()).to.equal('Sign In')
  })

  it('renders Sign Out button when signed in', async () => {
    const wrapper = await factory(mockStore(true))

    expect(wrapper.text()).to.equal('Sign Out')
  })

  it('invokes Sign Out action when button clicked', async () => {
    let resolveFunc
    const signOutCalled = new Promise((resolve, reject) => (resolveFunc = resolve))
    const wrapper = await factory(mockStore(true, resolveFunc))

    clickButton(wrapper)

    await signOutCalled
  })

  it('changes button to Sign In after signing out', async () => {
    const wrapper = await factory(mockStore(true))

    clickButton(wrapper)

    await localVue.nextTick()
    await localVue.nextTick()
    expect(wrapper.text()).to.equal('Sign In')
  })
})
