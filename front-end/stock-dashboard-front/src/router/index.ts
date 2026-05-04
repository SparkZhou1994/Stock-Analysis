import { createRouter, createWebHistory } from 'vue-router';
import DashboardView from '../views/DashboardView.vue';
import DetailView from '../views/DetailView.vue';
import WatchlistView from '../views/WatchlistView.vue';
import SearchView from '../views/SearchView.vue';
import SettingsView from '../views/SettingsView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView,
      meta: {
        title: '股票基金看板',
      },
    },
    {
      path: '/detail/:type/:code',
      name: 'detail',
      component: DetailView,
      meta: {
        title: '详情',
      },
    },
    {
      path: '/watchlist',
      name: 'watchlist',
      component: WatchlistView,
      meta: {
        title: '自选',
      },
    },
    {
      path: '/search',
      name: 'search',
      component: SearchView,
      meta: {
        title: '搜索',
      },
    },
    {
      path: '/settings',
      name: 'settings',
      component: SettingsView,
      meta: {
        title: '设置',
      },
    },
  ],
});

// 路由守卫，设置页面标题
router.beforeEach((to, _, next) => {
  const title = to.meta.title as string;
  if (title) {
    document.title = title;
  }
  next();
});

export default router;
