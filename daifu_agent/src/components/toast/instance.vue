<template>
  <teleport to="#app">
    <transition name="fade-toast" mode="out-in" appear @after-leave="handleAfterLeave">
      <div v-if="show" class="toast-wrap" @click="show = false" :style="{ zIndex }">
        <p>{{ message }}</p>
      </div>
    </transition>
  </teleport>
</template>
<script src="./instance.js"></script>
<style lang="less" scoped>
.toast-wrap {
  position: fixed;
  top: 20%;
  left: 50%;
  transform: translate(-50%);
  z-index: 2021;
  p {
    background: rgba(0, 0, 0, 0.69);
    text-align: center;
    font-size: 16px;
    color: #fff;
    border-radius: 2px;
    padding: 15px 25px;
    max-width: 365px;
  }
}
.fade-toast-enter-active,
.fade-toast-leave-active {
  transition: all 0.3s ease-out;
}

.fade-toast-enter-from {
  top: 15%;
  opacity: 0;
}
.fade-toast-leave-to {
  top: 15%;
  opacity: 0;
}
</style>
