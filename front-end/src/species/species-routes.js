export default [
  {
    path: '/',
    name: 'home',
    component: require('./species-list-view').default
  },
  {
    path: '/',
    name: 'species-list',
    component: require('./species-list-view').default
  },
  {
    path: '/plant/:id',
    name: 'species',
    component: require('./species-view').default,
    props: true
  },
  {
    path: '/plant/edit/:id',
    name: 'edit-species',
    component: require('./species-edit-view').default,
    props: true
  }
]
