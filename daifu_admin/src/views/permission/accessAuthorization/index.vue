<template>
  <div class="container">
    <page-table :FormDataList="FormDataList" ref="pageTable" url="" :listHanlder="currentHanlder" :showsummary="false" :special_params="{ uid }">
      <template #batch>
        <el-button type="primary" @click="addMessage">{{t('global.increment')}}</el-button>
      </template>
      <el-table-column type="index" label="序号" align="center" width="80"></el-table-column>
      <el-table-column align="center" prop="a" label="授权类型" width="180"> </el-table-column>
      <el-table-column align="center" prop="b" label="授权对象"> </el-table-column>
      <el-table-column align="center" prop="c" label="描述"> </el-table-column>
      <el-table-column align="center" prop="d" label="编辑时间"> </el-table-column>
      <el-table-column align="center" prop="e" label="operating_people"> </el-table-column>
      <el-table-column align="center" prop="f" label="状态">
        <el-switch v-model="btnColor" active-color="#409EFF" inactive-color="#e8e8e8"> > </el-switch>
      </el-table-column>
      <el-table-column align="center" prop="i" width="150" label="操作">
        <template v-slot="scope">
          <span class="blue-cursor" @click="deleteAccess(scope.row)">删除</span>
          <el-divider direction="vertical"></el-divider>
          <span class="blue-cursor" @click="editAccess(scope.row)">编辑</span>
        </template>
      </el-table-column>
    </page-table>
    <el-dialog :title="addDialog.title + '授权对象'" v-model="addDialog.dialogAddEdit" width="540px">
      <el-form ref="elForm" :model="formData" :rules="rules" size="medium" label-width="auto">
        <el-form-item label="添加方式" prop="a" v-if="addDialog.title == '新增'">
          <el-radio-group v-model="formData.a" size="medium">
            <el-radio v-for="(item, index) in addType" :key="index" :label="item.value" :disabled="item.disabled">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="授权对象" prop="b" v-if="formData.a == '1'">
          <el-input v-model="formData.b" placeholder="请输入精准授权IP地址" clearable :style="{ width: '100%' }"> </el-input>
        </el-form-item>
        <div class="input-width" v-if="formData.a == '2'">
          <el-form-item label="授权对象:" prop="b">
            <el-input v-model="formData.b" placeholder="请输入精准授权IP地址" clearable :style="{ width: '53%' }"> </el-input>
            <span class="span">至</span>
            <el-input v-model="formData.b" placeholder="第4段IP" clearable :style="{ width: '33%' }"> </el-input>
          </el-form-item>
        </div>
        <el-form-item label="备注：" prop="c">
          <el-input v-model="formData.c" type="textarea" placeholder="如有需要请填写，不超过50个字" :autosize="{ minRows: 4, maxRows: 4 }" :style="{ width: '100%' }"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <div class="btn">
            <el-button @click="close">取消</el-button>
            <el-button type="primary" @click="submitMessage">确定</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, inject, reactive } from 'vue';
import PageTable from '../../../components/page-table/index.vue';
import { ElTable, ElMessage, ElMessageBox, ElForm, ElButton, ElTableColumn, ElSwitch, ElDivider, ElDialog, ElFormItem, ElRadio, ElRadioGroup, ElInput, } from 'element-plus';

export default defineComponent({
  name: 'accessAuth',
  components: { 
    PageTable,
    [ElForm.name]: ElForm,
    [ElButton.name]: ElButton,
    [ElTableColumn.name]: ElTableColumn,
    [ElSwitch.name]: ElSwitch,
    [ElDivider.name]: ElDivider,
    [ElDialog.name]: ElDialog,
    [ElFormItem.name]: ElFormItem,
    [ElRadio.name]: ElRadio,
    [ElRadioGroup.name]: ElRadioGroup,
    [ElInput.name]: ElInput,
  },
  setup() {
    const FormDataList = ref([
      {
        label: "授权对象",
        model: 'a',
        placeholder: ''
      }
    ]);
    const uid = inject('') || '';
    const pageTable = ref<typeof ElTable | null>(null);
    const elForm = ref<typeof ElForm | null>(null);
    const btnColor = ref({
      value: true
    });
    const addType = [
      {
        label: "单个IP",
        value: 1
      },
      {
        label: "IP段",
        value: 2
      }
    ]
    const rules = reactive({
      a: [
        {
          required: true,
          message: "添加方式:不能为空",
          trigger: 'change'
        }
      ],
      b: [
        {
          required: true,
          message: "请输入精准授权IP地址",
          trigger: 'blur'
        }
      ],
      c: [
        {
          required: true,
          message: "如有需要请填写，不超过50个字",
          trigger: 'blur'
        }
      ]
    });
    const addDialog = ref({
      dialogAddEdit: false,
      title: ''
    });
    const formData = ref({});
    const currentHanlder = (tableData: any) => {
      return tableData.map((item: any) => {
        return {
          ...item
        };
      });
    };
    const deleteSystemContext = () => {
      ElMessageBox.confirm("您确定要删除此条数据吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "关闭",
        type: 'warning'
      })
        .then(() => {
          ElMessage.success({
            message: "已删除",
            type: 'success'
          });
        })
        .catch(() => {
          ElMessage.success({
            message: "已取消",
            type: 'success'
          });
        });
    };
    const editAccess = (row: any) => {
      formData.value = Object.assign({}, row);
      addDialog.value.title = "编辑";
      addDialog.value.dialogAddEdit = true;
    };
    const deleteAccess = () => {
      ElMessageBox.confirm("您确定要删除此条数据吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "关闭",
        type: 'warning'
      })
        .then(() => {
          ElMessage.success({
            message: "已删除",
            type: 'success'
          });
        })
        .catch(() => {
          ElMessage.success({
            message: "已取消",
            type: 'success'
          });
        });
    };
    const submitMessage = () => {
      if (elForm.value) {
        elForm.value.validate((valid: boolean) => {
          if (valid) {
            if (addDialog.value.title === "新增") {
              ElMessage.success({
                message: "添加成功",
                type: 'success'
              });
            } else {
              ElMessage.success({
                message: "编辑成功",
                type: 'success'
              });
            }
          } else {
            ElMessage.error({
              message: "请完善信息",
              type: 'error'
            });
            return false;
          }
        });
        elForm.value.clearValidate();
      }
    };
    const close = () => {
      if (elForm.value) {
        elForm.value.clearValidate();
      }
      addDialog.value.dialogAddEdit = false;
    };
    const addMessage = () => {
      addDialog.value.title = "新增";
      addDialog.value.dialogAddEdit = true;
    };
    return {
      addDialog,
      btnColor,
      elForm,
      FormDataList,
      currentHanlder,
      uid,
      pageTable,
      deleteSystemContext,
      editAccess,
      deleteAccess,
      submitMessage,
      close,
      rules,
      addType,
      formData,
      addMessage,
    };
  }
});
</script>
<style lang="scss" scoped>
.container {
  padding: 20px 20px 20px 20px;
  .blue-cursor {
    color: #409eff;
    cursor: pointer;
  }
  .input-width {
    .span {
      padding: 0 15px;
    }
  }
}
</style>
