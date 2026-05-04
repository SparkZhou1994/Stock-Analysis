<template>
  <div class="app">
    <Header />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import Header from './components/layout/Header.vue';
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f7fa;
  color: #303133;
  line-height: 1.5;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-top: 60px;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Element Plus 全局样式调整 */
.el-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
}

.el-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.12);
}

.el-button {
  border-radius: 6px;
}

.el-input__wrapper {
  border-radius: 6px;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-empty {
  padding: 60px 0;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .container {
    padding-left: 12px !important;
    padding-right: 12px !important;
  }

  .grid-cols-1 {
    grid-template-columns: repeat(1, minmax(0, 1fr)) !important;
  }

  .grid-cols-2 {
    grid-template-columns: repeat(1, minmax(0, 1fr)) !important;
  }

  .grid-cols-3 {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  }

  .grid-cols-4 {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  }

  .el-header .container {
    padding-left: 12px !important;
    padding-right: 12px !important;
  }

  .el-header h1 {
    font-size: 16px !important;
  }

  .el-button {
    padding: 0 10px !important;
    font-size: 13px !important;
  }

  .el-button .el-icon {
    margin-right: 4px !important;
  }
}

@media (max-width: 640px) {
  .grid-cols-3 {
    grid-template-columns: repeat(1, minmax(0, 1fr)) !important;
  }

  .grid-cols-4 {
    grid-template-columns: repeat(1, minmax(0, 1fr)) !important;
  }

  .el-tabs__nav-wrap {
    padding: 0 12px !important;
  }

  .el-tab-pane {
    padding: 16px 0 !important;
  }

  .el-form-item {
    margin-bottom: 20px !important;
  }

  .el-form-item__label {
    font-size: 14px !important;
  }
}
</style>
