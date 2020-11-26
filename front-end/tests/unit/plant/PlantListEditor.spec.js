import { expect } from 'chai'
import { mount, createLocalVue } from '@vue/test-utils'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import Vuex from 'vuex'
import PlantListEditor from '@/plant/PlantListEditor.vue'

const localVue = createLocalVue()

localVue.use(ElementUI, { locale })
localVue.use(Vuex)

describe('Plant List Editor', () => {
  const createTestData = () => [
    {
      id: '7cd7ff88-1429-407a-a830-05f0c9fbbbe1',
      garden: 'Front'
    },
    {
      id: '7b79bc5d-3b95-4764-baeb-f821c06bd30d',
      garden: 'Side'
    }
  ]

  const mockStore = (gardens) => {
    const actions = {
      fetchGardens: () => gardens.map(name => ({ name }))
    }

    return new Vuex.Store({
      modules: {
        plant: {
          namespaced: true,
          actions
        }
      }
    })
  }

  const factory = async (plants, gardens = ['Front', 'Back', 'Side', 'Back corner']) => {
    const store = mockStore(gardens)
    const wrapper = mount(PlantListEditor, { propsData: { value: plants }, store, localVue })

    await localVue.nextTick()

    return wrapper
  }

  const closeTag = tag => {
    const closeButton = tag.find('.el-icon-close')
    closeButton.trigger('click')
  }

  const closeTagAt = (wrapper, index) => {
    const tags = wrapper.findAll('.el-tag')
    closeTag(tags.at(index))
  }

  const addPlant = async (garden) => {
    const wrapper = await factory(createTestData())

    wrapper.vm.handleSelect({ value: garden })

    await localVue.nextTick()

    return wrapper
  }

  it('renders plant list', async () => {
    const wrapper = await factory(createTestData())

    const tags = wrapper.findAll('.el-tag')

    expect(tags.length).to.equal(2)
    expect(tags.at(0).text()).to.equal('Front')
    expect(tags.at(1).text()).to.equal('Side')
  })

  it('sends delete event when saved plant is deleted', async () => {
    const wrapper = await factory(createTestData())

    const tags = wrapper.findAll('.el-tag')
    closeTag(tags.at(1))

    expect(wrapper.emitted('delete').length).to.equal(1)
    expect(wrapper.emitted('delete')[0]).to.deep.equal([{
      id: '7b79bc5d-3b95-4764-baeb-f821c06bd30d',
      key: '7b79bc5d-3b95-4764-baeb-f821c06bd30d',
      garden: 'Side'
    }])
  })

  it('sends input event when saved plant is deleted', async () => {
    const wrapper = await factory(createTestData())

    const tags = wrapper.findAll('.el-tag')
    closeTag(tags.at(1))

    expect(wrapper.emitted('input').length).to.equal(1)
    expect(wrapper.emitted('input')[0]).to.deep.equal([
      [
        {
          id: '7cd7ff88-1429-407a-a830-05f0c9fbbbe1',
          key: '7cd7ff88-1429-407a-a830-05f0c9fbbbe1',
          garden: 'Front'
        }
      ]
    ])
  })

  it('removes plant tag when saved plant is deleted', async () => {
    const wrapper = await factory(createTestData())

    const tags = wrapper.findAll('.el-tag')
    closeTag(tags.at(1))

    await localVue.nextTick()

    const tagsAfterDelete = wrapper.findAll('.el-tag')
    expect(tagsAfterDelete.length).to.equal(1)
    expect(tagsAfterDelete.at(0).text()).to.equal('Front')
  })

  it('finds suggestions based on input', (done) => {
    factory(createTestData())
      .then(wrapper => {
        localVue.nextTick().then(() => {
          // TODO can I test this without calling the method?
          wrapper.vm.querySearch('Ba', results => {
            expect(results).to.deep.equal([{ value: 'Back' }, { value: 'Back corner' }])
            done()
          })
        })
      })
  })

  it('adds tag when a plant is added', async () => {
    const wrapper = await addPlant('Back')

    const tags = wrapper.findAll('.el-tag')
    expect(tags.length).to.equal(3)
    expect(tags.at(0).text()).to.equal('Front')
    expect(tags.at(1).text()).to.equal('Side')
    expect(tags.at(2).text()).to.equal('Back')
  })

  it('sends input event when a plant is added', async () => {
    const wrapper = await addPlant('Back')

    expect(wrapper.emitted('input').length).to.equal(1)
    expect(wrapper.emitted('input')[0][0].length).to.equal(3)
    expect(wrapper.emitted('input')[0][0][2].garden).to.equal('Back')
  })

  it('does not send delete event when a new plant is deleted', async () => {
    const wrapper = await addPlant('Back')

    closeTagAt(wrapper, 2)

    expect(wrapper.emitted('delete')).to.equal(undefined)
  })

  it('send input event when a new plant is deleted', async () => {
    const wrapper = await addPlant('Back')

    closeTagAt(wrapper, 2)

    expect(wrapper.emitted('input').length).to.equal(2)
    expect(wrapper.emitted('input')[1][0].length).to.equal(2)
  })
})
