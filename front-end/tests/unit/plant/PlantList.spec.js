import { expect } from 'chai'
import { mount, createLocalVue } from '@vue/test-utils'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import Vuex from 'vuex'
import PlantList from '@/plant/PlantList.vue'

const localVue = createLocalVue()

localVue.use(ElementUI, { locale })
localVue.use(Vuex)

describe('Plant List', () => {
  const createTestData = () => [
    {
      garden: 'Front',
      planted: '2020-05-11T12:00:00Z'
    },
    {
      garden: 'Side',
      planted: '2019-06-03T00:05:07Z'
    },
    {
      garden: 'Back',
      planted: '2020-11-22T00:05:07Z'
    }
  ]

  const factory = async (plants) => {
    const wrapper = mount(PlantList, { propsData: { plants }, localVue })

    await localVue.nextTick()

    return wrapper
  }

  beforeEach(() => {

  })

  it('renders plant list', async () => {
    const wrapper = await factory(createTestData())

    // TODO the dates will be in the user's timezone; if I can mock that, it would be good to test
    expect(wrapper.text()).to.include('Front')
    expect(wrapper.text()).to.include('2020/05/')
    expect(wrapper.text()).to.include('Back')
    expect(wrapper.text()).to.include('2019/06/')
    expect(wrapper.text()).to.include('Side')
    expect(wrapper.text()).to.include('2020/11/')
  })

  it('displays a message when there are no plants', async () => {
    const wrapper = await factory([])

    expect(wrapper.text()).to.include('None')
  })
})
