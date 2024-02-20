<template>
  <div>
    <div class="betting-cont">
      <el-dialog title="title" :width="540" v-model="dialogVisable" @close="onClose" :close-on-click-modal="false">
        <div class="active-cont">
          <el-form :inline="false" ref="elForm" :rules="rules" :model="formInline" class="demo-form-inline" label-width="120px">
            <el-form-item label="Activity name" prop="name">
              <el-input v-model="formInline.name"></el-input>
            </el-form-item>
            <el-form-item label="Activity form" prop="desc">
              <el-input v-model="formInline.desc" type="textarea"></el-input>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisable = false">取消</el-button>
            <el-button type="primary" @click="submitmBtn">确认</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue';
import { ElButton, ElForm, ElFormItem, ElInput, ElOption, ElSelect, ElDialog, ElMessageBox, ElMessage } from 'element-plus';
export default defineComponent({
  components: {
    [ElButton.name]: ElButton,
    [ElDialog.name]: ElDialog,
    [ElForm.name]: ElForm,
    [ElFormItem.name]: ElFormItem,
    [ElInput.name]: ElInput,
    [ElOption.name]: ElOption,
    [ElSelect.name]: ElSelect,
    [ElMessageBox.name]: ElMessageBox,
    [ElMessage.name]: ElMessage
  },
  setup(props, context) {
    const formInline = ref({});
    const elForm = ref(null);
    const showForm = data => {
      dialogVisable.value = true;
    };
    const dialogVisable = ref(false);
    const rules = ref({
      name: [
        {
          required: true,
          message: 'Please input Activity name',
          trigger: 'change'
        },
        {
          min: 3,
          max: 5,
          message: 'Length should be 3 to 5',
          trigger: 'change'
        }
      ],
      desc: [
        {
          required: true,
          message: 'Please input activity form',
          trigger: 'change'
        }
      ]
    });
    const submitmBtn = () => {
      if (elForm.value) {
        elForm.value.validate(valid => {
          if (valid) {
            console.log('submit!!');
            ElMessageBox.confirm('proxy will permanently delete the file. Continue?', 'Warning', {
              confirmButtonText: 'OK',
              cancelButtonText: 'Cancel',
              type: 'warning'
            })
              .then(() => {
                ElMessage({
                  type: 'success',
                  message: 'Delete completed'
                });
              })
              .catch(() => {
                ElMessage({
                  type: 'info',
                  message: 'Delete canceled'
                });
              });
          } else {
            console.log('error submit!!');
            return false;
          }
        });
      }
    };
    const onClose = () => {
      if (elForm.value) {
        elForm.value.clearValidate();
      }
    };
    function cancal() {
      dialogVisable.value = false;
    }
    return {
      onClose,
      cancal,
      formInline,
      submitmBtn,
      elForm,
      showForm,
      dialogVisable,
      rules
    };
  }
});
</script>