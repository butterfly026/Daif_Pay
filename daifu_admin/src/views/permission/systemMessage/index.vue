<template>
  <div class="container">
    <el-dialog title="" v-model="colors.dialogVisible">
      <div class="announ">
        <div class="announcont">
          <el-button>{{t('routets.system.back')}}</el-button>
          <el-button>{{t('routets.system.next')}}</el-button>
        </div>
        <div class="announ-msg">
          <h4>【雷火电竞注单取消通知】</h4>
          <p>
            公告内容详情公告内容详情公告内容详情公告内容详情
            公告内容详情公告内容详情公告内容详情公告内容详情
            公告内容详情公告内容详情公告内容详情公告内容详情
            公告内容详情公告内容详情公告内容详情公告内容详情
            公告内容详情公告内容详情公告内容详情公告内容详情
            公告内容详情公告内容详情公告内容详情公告内容详情
            公告内容详情公告内容详情公告内容详情公告内容详情
            公告内容详情公告内容详情公告内容详情公告内容详情
            公告内容详情公告内容详情公告内容详情公告内容详情
          </p>
          <div>2021-03-505 08：12：38</div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="colors.dialogVisible = false"
            >{{ t('golbal.sure') }}</el-button
          >
        </span>
      </template>
    </el-dialog>
    <page-table
      :FormDataList="FormDataList"
      ref="pageTable"
      url=""
      :listHanlder="currentHanlder"
      :showsummary="false"
      :special_params="{ uid }"
    >
      <el-table-column
        type="index"
        :label="t('routes.system.sort_number')"
        align="center"
        width="80"
      ></el-table-column>
      <el-table-column align="center" prop="a" :label="t('routes.system.msg_subject')" width="300">
        <template #default="scope">
          <span class="cursor" @click="selectDetail(scope.row)">{{
            scope.row.a
          }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="b" :label="t('routes.system.msg_content')">
        <template #default="scope">
          <el-popover placement="top" :width="260" trigger="hover">
            <template #reference>
              <span class="cursor">{{ scope.row.b }}</span>
            </template>
            <div
              class="popover-content"
              v-html="scope.row.b"
              @click="selectDetail(scope.row)"
            ></div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="c" :label="t('routes.system.send_time')" width="150">
      </el-table-column>
    </page-table>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, unref, inject, reactive, toRefs } from 'vue';
import useSearchPlaceholder from './search';
import PageTable from '@/components/page-table/index.vue';
import { ElTable, ElMessage, ElMessageBox, ElForm } from 'element-plus';
import { addType } from '@/utils/selectOptions';
import { useI18n } from 'vue-i18n';
export default defineComponent({
  name: 'SystemMessages',
  components: { PageTable },
  setup() {
    const {t} =useI18n()
    const { FormDataList } = useSearchPlaceholder();
    const uid = inject('') || '';
    const pageTable = ref<typeof ElTable | null>(null);
    const elForm = ref<typeof ElForm | null>(null);
    const colors = ref({
      isActive: true,
      dialogVisible: false
    });
    const currentHanlder = (tableData: any) => {
      return tableData.map((item: any) => {
        return {
          ...item
        };
      });
    };
    const selectDetail = () => {
      colors.value.dialogVisible = true;
    };
    return {
      elForm,
      FormDataList,
      currentHanlder,
      uid,
      pageTable,
      close,
      addType,
      colors,
      selectDetail,
      t
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
  .active {
    color: blue;
  }
  .announ {
    margin: 0 auto;
    padding: 0 10px;
    background: white;
    border: 1px solid #e6dddd;
    z-index: 66666;
    .announcont {
      height: 60px;
      line-height: 60px;
      text-align: right;
      border-bottom: 1px solid #e0e0e0;
    }
    .announ-msg {
      > h4 {
        margin-top: 15px;
      }
      > p {
        margin: 20px;
        font-size: 14px;
        color: #675a5a;
        text-indent: 28px;
        text-align: justify;
        line-height: 30px;
      }
      > div {
        padding: 0 20px 20px 0;
        text-align: right;
        font-size: 14px;
        color: #777070;
      }
    }
  }
  .cursor {
    cursor: pointer;
  }
  ::v-deep(.el-table__body tr:hover > td) {
    background: #e6f7ff;
  }
}
</style>
