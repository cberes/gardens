import { expect } from 'chai'
import { mount, createLocalVue } from '@vue/test-utils'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import Vuex from 'vuex'
import Species from '@/species/Species.vue'

const localVue = createLocalVue()

localVue.use(ElementUI, { locale })
localVue.use(Vuex)

describe('Species component', () => {
  const createTestData = () => ({
    species: {
      id: '14ef5fe2-a1db-42cb-9394-ff46efba8070',
      name: 'Early Sunflower',
      alternateName: 'Heliopsis helianthoides',
      light: 'FULL',
      moisture: 'MEDIUM_WET'
    },
    plants: [{ garden: 'Front' }, { garden: 'Side' }]
  })

  const mockStore = (species, signedIn) => {
    const actions = {
      fetchSpecies: () => species,
      deleteSpecies: (unused, id) => `deleted ${id}`
    }

    return new Vuex.Store({
      modules: {
        auth: {
          namespaced: true,
          actions: {
            currentSession: () => signedIn
          }
        },
        species: {
          namespaced: true,
          actions
        }
      }
    })
  }

  const factory = async (store, speciesId) => {
    const wrapper = mount(Species, { propsData: { speciesId }, store, localVue })

    await localVue.nextTick()
    await localVue.nextTick()

    return wrapper
  }

  it('renders species info', async () => {
    const store = mockStore(createTestData())
    const wrapper = await factory(store, 'test species id')

    expect(wrapper.text()).to.include('Early Sunflower')
    expect(wrapper.text()).to.include('Heliopsis helianthoides')
    expect(wrapper.text()).to.include('Full')
    expect(wrapper.text()).to.include('Medium wet')
  })

  it('does not render edit/delete buttons if no user', async () => {
    const store = mockStore(createTestData())
    const wrapper = await factory(store, 'test species id')

    expect(wrapper.find('.el-button').exists()).to.equal(false)
  })

  it('renders edit/delete buttons if user', async () => {
    const store = mockStore(createTestData(), true)
    const wrapper = await factory(store, 'test species id')

    expect(wrapper.find('.el-button').exists()).to.equal(true)
  })

  it('prompts for confirmation before deleting a species', async () => {
    const store = mockStore(createTestData(), true)
    const wrapper = await factory(store, 'test species id')

    const { element: button } = wrapper.findAll('.el-button').at(1)

    button.click()

    await localVue.nextTick()

    // the message box is created outside the component, so we can't use the wrapper
    const messageBoxes = document.getElementsByClassName('el-message-box')
    expect(messageBoxes.length).to.equal(1)
    expect(messageBoxes[0].textContent).to.include('permanently delete')
  })
})
