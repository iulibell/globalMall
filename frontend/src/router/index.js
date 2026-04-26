import { createRouter, createWebHistory } from 'vue-router'
import MallHome from '@/components/MallHome.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import ProfileView from '@/views/ProfileView.vue'
import GoodsDetailView from '@/views/GoodsDetailView.vue'
import UserOrderPayView from '@/views/UserOrderPayView.vue'
import MerchantApplyGoodsView from '@/views/MerchantApplyGoodsView.vue'
import MerchantApplyPayView from '@/views/MerchantApplyPayView.vue'
import MerchantOffShelfPayView from '@/views/MerchantOffShelfPayView.vue'
import SearchResultsView from '@/views/SearchResultsView.vue'
import CartView from '@/views/CartView.vue'

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: MallHome,
      meta: { layout: 'mall' },
    },
    {
      path: '/goods/:goodsId',
      name: 'goods-detail',
      component: GoodsDetailView,
      meta: { layout: 'mall' },
    },
    {
      path: '/search',
      name: 'search',
      component: SearchResultsView,
      meta: { layout: 'mall' },
    },
    {
      path: '/cart',
      name: 'cart',
      component: CartView,
      meta: { layout: 'mall' },
    },
    {
      path: '/order/pay',
      name: 'order-pay',
      component: UserOrderPayView,
      meta: { layout: 'mall' },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { layout: 'auth' },
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { layout: 'auth' },
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
      meta: { layout: 'profile' },
    },
    {
      path: '/merchant/apply-goods',
      name: 'merchant-apply-goods',
      component: MerchantApplyGoodsView,
      meta: { layout: 'profile' },
    },
    {
      path: '/merchant/apply-pay',
      name: 'merchant-apply-pay',
      component: MerchantApplyPayView,
      meta: { layout: 'profile' },
    },
    {
      path: '/merchant/off-shelf-pay',
      name: 'merchant-off-shelf-pay',
      component: MerchantOffShelfPayView,
      meta: { layout: 'profile' },
    },
  ],
})
