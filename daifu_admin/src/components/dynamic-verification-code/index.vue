<template>
  <div class="dialogcontent">
    <el-dialog :title="dialogContent.title" v-model="dialogFormVisible" :width="350" :close-on-click-modal="false">
      <div class="active-cont">
        <p class="titleT pad" v-show="dialogContent.content">{{ dialogContent.content }}</p>
        <el-form ref="elForm" @submit.native.prevent :model="form" class="demo-form-inline" label-width="auto">
          <el-form-item label="动态验证码" prop="code" :rules="[
              {
                required: true,
                message: '请输入动态验证码',
                trigger: 'blur'
              },
              {
                pattern: dynamic,
                message: dynamicMsg
              }
            ]">
            <el-input v-model.trim="form.code" @keyup.enter="submitmBtn" placeholder="请输入动态验证码"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <slot name="vit"></slot>

        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent, reactive, ref, watch } from 'vue';
import { ElForm } from 'element-plus';
import { dynamic, dynamicMsg } from '../../utils/expressions';

export default defineComponent({
  name: 'DynamicVerificationCode',
  setup() {
    const elForm = ref(null);
    const dialogFormVisible = ref(false);
    const form = ref({});
    const isItverified = ref(false);
    // 打开验证组件
    const dialogContent = reactive({
      title: "动态验证码"
    });
    const showForm = (bool, dialog = {}) => {
      form.value.code = '';
      Object.assign(dialogContent, dialog);
      dialogFormVisible.value = bool;
    };

    // 提交方法
    const submitmBtn = (states) => {
      return new Promise(resolve => {
        if (elForm.value) {
          elForm.value.validate((valid) => {
            if (valid) {
              resolve(form.value.code);
            } else {
              console.log('error submit!!');
              return false;
            }
          });
        }
      });
    };

    function cancel() {
      dialogFormVisible.value = false;
      form.value.code = '';
      (elForm.value).resetFields();
    }
    return {
      dialogFormVisible,
      form,
      showForm,
      submitmBtn,
      elForm,
      dialogContent,
      cancel,
      dynamic,
      dynamicMsg
    };
  }
});
</script>
<style lang="less" scoped>
.titleT {
  margin-bottom: 12px;
  color: #ff4d4f;
  font-weight: bold;
}
.pad {
  padding-left: 15px;
}
.dialogcontent ::v-deep(.el-dialog__body) {
  padding: 20px 20px 0;
}
</style>
