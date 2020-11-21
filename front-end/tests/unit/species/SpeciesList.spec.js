import { expect } from 'chai'
import { mount, createLocalVue } from '@vue/test-utils'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import Vuex from 'vuex'
import SpeciesList from '@/species/SpeciesList.vue'

const localVue = createLocalVue()

localVue.use(ElementUI, { locale })
localVue.use(Vuex)

describe('Species List', () => {
  function mockStore (species) {
    const actions = {
      fetchAllSpecies: () => Promise.resolve(species)
    }
    return new Vuex.Store({
      modules: {
        species: {
          namespaced: true,
          actions
        }
      }
    })
  }

  function createTestData () {
    return [
      {
        species: {
          id: '14ef5fe2-a1db-42cb-9394-ff46efba8070',
          name: 'Early Sunflower'
        },
        plants: [{ garden: 'Front' }, { garden: 'Side' }]
      },
      {
        species: {
          id: '34e05180-65fa-47c7-8132-f79e128ac245',
          name: 'Purple Coneflower'
        },
        plants: [{ garden: 'Side' }, { garden: 'Back' }]
      },
      {
        species: {
          id: 'a335a859-2de7-43fa-8a6f-0456dc52dc3c',
          name: 'Wild Bergamot'
        },
        plants: [{ garden: 'Front' }, { garden: 'Side' }, { garden: 'Back' }]
      },
      {
        species: {
          id: 'd7623484-7b0b-46f1-b2fc-cb785f11dfd7',
          name: 'Yarrow'
        },
        plants: [{ garden: 'Side' }]
      }
    ]
  }

  it('renders species table with plants', async () => {
    // const wrapper = shallowMount(SpeciesList, {
    //   propsData: { msg }
    // })
    const store = mockStore(createTestData())
    const wrapper = mount(SpeciesList, { store, localVue })
    // wrapper.setData({ species: createTestData(), loading: false })

    await localVue.nextTick()
    await localVue.nextTick()

    expect(wrapper.find('.el-table').exists()).to.equal(true)

    expect(wrapper.text()).to.include('Early Sunflower')
    expect(wrapper.text()).to.include('Purple Coneflower')
    expect(wrapper.text()).to.include('Wild Bergamot')
    expect(wrapper.text()).to.include('Yarrow')
  })
})
