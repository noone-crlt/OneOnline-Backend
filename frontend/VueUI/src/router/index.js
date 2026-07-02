import { createRouter, createWebHashHistory } from 'vue-router'

import { authUser, ensureAdminAccess, hydrateSession } from '../stores/auth'
import AdminLayout from '../components/admin/AdminLayout.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminBooksView from '../views/admin/AdminBooksView.vue'
import AdminCategoriesView from '../views/admin/AdminCategoriesView.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'
import AdminAuthorsView from '../views/admin/AdminAuthorsView.vue'
import Homepage from '../components/Homepage.vue'
import LibraryView from '../components/Library.vue'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import BookDetail from '../views/BookDetail.vue'
import BookReaderView from '../views/BookReaderView.vue'
import CartView from '../views/CartView.vue'
import PaymentResultView from '../views/PaymentResultView.vue'
import UserProfileView from '../views/UserProfileView.vue'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/profile',
      name: 'profile',
      component: UserProfileView,
      meta: { title: 'Tài khoản & Thư viện', requiresAuth: true },
    },
    {
      path: '/cart',
      name: 'cart',
      component: CartView,
      meta: { title: 'Giỏ hàng', requiresAuth: true },
    },
    {
      path: '/payment-result',
      name: 'payment-result',
      component: PaymentResultView,
      meta: { title: 'Kết quả thanh toán' },
    },
    {
      path: '/',
      name: 'home',
      component: Homepage,
      meta: {
        title: 'One Online',
      },
    },
    {
      path: '/home',
      redirect: { name: 'home' },
    },
    {
      path: '/library',
      name: 'library',
      component: LibraryView,
      meta: {
        title: 'Thư viện',
      },
    },
    {
      path: '/book/:slug',
      name: 'book-detail',
      component: BookDetail,
      meta: {
        title: 'Chi tiết sách',
      },
    },
    {
      path: '/read/:slug',
      name: 'book-reader',
      component: BookReaderView,
      meta: {
        title: 'Đọc sách',
        requiresAuth: true,
      },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        title: 'Đăng nhập',
        guestOnly: true,
      },
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: {
        title: 'Đăng ký',
        guestOnly: true,
      },
    },
    {
      path: '/admin/login',
      redirect: { name: 'login' },
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: {
        requiresAdmin: true,
      },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: AdminDashboardView,
          meta: { title: 'Bảng điều khiển quản trị' },
        },
        {
          path: 'books',
          name: 'admin-books',
          component: AdminBooksView,
          meta: { title: 'Quản lý sách' },
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: AdminCategoriesView,
          meta: { title: 'Quản lý danh mục' },
        },
        {
          path: 'users',
          name: 'admin-users',
          component: AdminUsersView,
          meta: { title: 'Quản lý người dùng' },
        },
        {
          path: 'authors',
          name: 'admin-authors',
          component: AdminAuthorsView,
          meta: { title: 'Quản lý tác giả' },
        }
      ]
    },
    {
      path: '/dashboard',
      redirect: { name: 'admin-dashboard' },
    },
  ],
})

router.beforeEach(async (to) => {
  hydrateSession()

  const currentUser = authUser.value

  if (to.meta.requiresAuth && !currentUser) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.guestOnly && currentUser) {
    const hasAdminAccess = await ensureAdminAccess()
    return hasAdminAccess ? { name: 'admin-dashboard' } : { name: 'library' }
  }

  if (to.meta.requiresAdmin) {
    if (!currentUser) {
      return {
        name: 'login',
        query: { redirect: to.fullPath },
      }
    }

    const hasAccess = await ensureAdminAccess()
    if (!hasAccess) {
      return authUser.value ? { name: 'library' } : { name: 'login', query: { redirect: to.fullPath } }
    }
  }

  return true
})

router.afterEach((to) => {
  const title = to.meta.title
  document.title = title ? `${title} | One Online` : 'One Online'
})

export default router
