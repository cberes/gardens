import { expect } from 'chai'
import { mount, createLocalVue } from '@vue/test-utils'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import Vuex from 'vuex'
import EditSpecies from '@/species/EditSpecies.vue'

const localVue = createLocalVue()

localVue.use(ElementUI, { locale })
localVue.use(Vuex)

describe('Species editor', () => {
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

  const mockStore = (species, saveSpecies) => {
    const plant = {
      namespaced: true,
      actions: {
        fetchGardens: () => []
      }
    }

    const speciesModule = {
      namespaced: true,
      actions: {
        fetchSpecies: () => species,
        saveSpecies: (unused, request) => {
          saveSpecies && saveSpecies(request)
          return true
        }
      }
    }

    return new Vuex.Store({
      modules: {
        plant,
        species: speciesModule
      }
    })
  }

  const factory = async (store, speciesId) => {
    const wrapper = mount(EditSpecies, {
      propsData: { speciesId },
      mocks: { $router: { push: () => {} } },
      store,
      localVue
    })

    await localVue.nextTick()
    await localVue.nextTick()

    return wrapper
  }

  const allErrors = wrapper => {
    const errors = []
    const errorElems = wrapper.findAll('.el-form-item__error')
    for (let i = 0; i < errorElems.length; ++i) {
      errors.push(errorElems.at(i).text())
    }
    return errors
  }

  const textMaxLength = () => 'abcdefghij'.repeat(10)

  it('shows no errors when all fields are valid', async () => {
    const store = mockStore(createTestData())
    const wrapper = await factory(store, 'test species id')

    wrapper.vm.onSubmit('form')
    await localVue.nextTick()

    expect(allErrors(wrapper).length).to.equal(0)
  })

  it('sends save request when all fields are valid', async () => {
    let resolveFunc
    const saveCalled = new Promise((resolve, reject) => (resolveFunc = resolve))

    const store = mockStore(createTestData(), resolveFunc)
    const wrapper = await factory(store, 'test species id')

    wrapper.vm.onSubmit('form')

    await saveCalled
  })

  it('shows errors when required elements are empty', async () => {
    const store = mockStore(createTestData())
    const wrapper = await factory(store, 'test species id')
    wrapper.setData({ species: { name: '', light: null, moisture: null } })

    wrapper.vm.onSubmit('form')
    await localVue.nextTick()

    expect(allErrors(wrapper)).to.deep.equal([
      'Please enter a name',
      'Light preference is required',
      'Soil moisture preference is required'
    ])
  })

  it('shows error when name is too long', async () => {
    const store = mockStore(createTestData())
    const wrapper = await factory(store, 'test species id')
    wrapper.setData({
      species: Object.assign(createTestData().species, { name: textMaxLength() + 'k' })
    })

    wrapper.vm.onSubmit('form')
    await localVue.nextTick()

    expect(allErrors(wrapper)).to.deep.equal([
      'Name is too long'
    ])
  })

  it('shows error when alternate name is too long', async () => {
    const store = mockStore(createTestData())
    const wrapper = await factory(store, 'test species id')
    wrapper.setData({
      species: Object.assign(createTestData().species, { alternateName: textMaxLength() + 'k' })
    })

    wrapper.vm.onSubmit('form')
    await localVue.nextTick()

    expect(allErrors(wrapper)).to.deep.equal([
      'Alternate name is too long'
    ])
  })
})
