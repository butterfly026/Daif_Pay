<template>
  <IndexComponent :key="updateKey" /> 
</template>
<script>
import { defineComponent, provide, ref } from 'vue'
import IndexComponent from './indexComponent.vue'
export default defineComponent({
  name: 'RolePermission',
  components: {
    IndexComponent
  },
  setup() {
    const updateKey = ref(0);
    const updateHanlder = () => {
      updateKey.value++;
    }
    provide('updateHanlder', updateHanlder)
    return {
      updateKey
    }
  } 
})
</script>