import authService from '@/auth/auth-service'

export default router => {
  router.beforeEach((to, from, next) => {
    if (to.name === 'authenticate') {
      return guardAuthenticate(router, next)
    } else if (to.name === 'signout') {
      return guardSignOut(router, next)
    } else if (isAuthenticationRequired(to)) {
      return guardDefault(router, to, next)
    } else {
      next()
    }
  })
}

function isAuthenticationRequired (to) {
  return ['edit-species'].includes(to.name)
}

async function guardAuthenticate (router, next) {
  if (await authService.currentSession()) {
    next({ name: 'home' })
    return
  }

  next()
}

async function guardSignOut (router, next) {
  if (await authService.currentSession()) {
    next()
    return
  }

  next({ name: 'home' })
}

async function guardDefault (router, to, next) {
  if (!await authService.currentSession()) {
    router.app.$store.dispatch('auth/setRedirect', {
      name: to.name,
      params: to.params,
      query: to.query
    })

    next({ name: 'authenticate' })
  } else {
    next()
  }
}
