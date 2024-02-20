<template>
  <div class="page_table_layout">
    <formSerch :FormDataList="FormDataList" @onSubmit="onSubmit" :firstload="firstload" class="mgt-10">
      <template #csv>
        <slot name="csv"></slot>
        <slot name="add"></slot>
      </template>
    </formSerch>
    <div class="line_content" v-if="$slots.batch">
      <slot name="batch"></slot>
    </div>
    <el-table :data="tableData" @selection-change="selectionLineChangeHandle" ref="tebleRef" align="center" border :summary-method="getSummaries(summaries, cloumBai, allNum)" :show-summary="showsummary" style="width: 100%" :max-height="globalTableHeight">
      <el-table-column v-if="isselection" align="center" type="selection" width="55"> </el-table-column>
      <slot></slot>
    </el-table>
    <Pagination v-show="total > 0" :total="total" v-model:page="pageQuery.page" v-model:limit="pageQuery.limit" @pagination="pagination"></Pagination>
  </div>
</template>

<script>
import { defineComponent, ref, PropType, unref } from 'vue';
import { ElTable, ElTableColumn } from 'element-plus';
import formSerch from '../serch-form/index.vue';
import usePaginationHanlder from '../pagination/pagination';
import { getSummaries } from '../../utils/table';
import Pagination from '..//pagination/index.vue';
import http from '../../http/request';

export default defineComponent({
  name: 'PageTable',
  components: { 
    formSerch, Pagination,
    [ElTable.name]: ElTable,
    [ElTableColumn.name]: ElTableColumn,
  },
  props: {
    // 搜索框数据
    FormDataList: {
      type: Array,
      default: []
    },
    // 是否第一次加载
    firstload: {
      type: Boolean,
      default: true
    },
    // 页面请求url地址
    url: {
      type: String,
      required: true
    },
    // 请求方法
    methods: {
      type: String,
      default: 'get'
    },
    // 列表数据如需特殊处理的执行函数
    listHanlder: {
      type: Function
    },
    // 其它请求数据，列如uid，username，通过provide注入之类的
    special_params: {
      type: Object,
      default: {}
    },
    // 移除表格底部计算总数的prop字段
    summaries: {
      type: Array,
      default: []
    },
    // 是否支持多选，如果支持多选，那么使用ref调用getSelectLineData可以获取返回选中的数据
    isselection: {
      type: Boolean,
      default: false
    },
    cloumBai: {
      type: Array,
      default: []
    },
    allNum: {
      type: Array,
      default: []
    },
    showsummary: {
      type: Boolean,
      default: true
    }
  },
  emits: [],
  setup(props, ctx) {
    // 控制分页
    const pageQuery = ref({});
    // 控制搜索框数据
    const listQuery = ref({});
    // 表格数据
    const tableData = ref([
      // {
      //   id: '123123',
      //   amount: '3423',
      //   cid: 1,
      //   state: '361'
      // },
      // {
      //   id: 'ASDFASDF',
      //   amount: '342334',
      //   cid: 1,
      //   state: '362'
      // }
    ]);
    const totalCount = ref();
    // 分页功能
    const { total, pagination } = usePaginationHanlder({
      pageQuery: pageQuery.value,
      hander: getList
    });
    // 表格选中数据
    let tableSelectData = [];
    const tebleRef = ref<typeof ElTable | null>(null);
    if (tebleRef.value) {
      tebleRef.value.doLayout();
    }
    const selectionLineChangeHandle = (val) => {
      tableSelectData = val;
    };
    // 返回选中的数据
    const getSelectLineData = () => {
      return tableSelectData;
    };
    // 设置表格的行尾总结栏目style
    function getList() {
      let params = {
        ...unref(pageQuery.value),
        ...unref(listQuery.value),
        ...props.special_params
      };
      if (props.methods === 'get') {
        params = {
          params: params
        };
      }
      http[props.methods](props.url, params).then(res => {
        // tableData.value = [];
        // total.value = 0;
        // tableData.value = [];
        if (res.status) {
          if (props.methods === 'get') {
            if (params.params.page == 1) {
              total.value = res.data.t;
            }
          } else {
            if (params.page == 1) {
              total.value = res.data.t;
            }
          }

          if (!res.data.d) {
            tableData.value = [];
            return;
          }
          if (res.data.d) {
            if (props.listHanlder) {
              tableData.value = props.listHanlder(res.data.d, res.data) || [];
            } else {
              tableData.value = res.data.d;
            }
          } else {
            if (props.listHanlder) {
              tableData.value = props.listHanlder(res.data.d, res.data) || [];
            }
          }
        } else {
          tableData.value = [];
        }
      });
    }

    const onSubmit = (data) => {
      pageQuery.value.page = 1;
      listQuery.value = data;
      getList();
    };
    return {
      pageQuery,
      listQuery,
      tableData,
      total,
      pagination,
      onSubmit,
      getSummaries,
      selectionLineChangeHandle,
      getSelectLineData,
      tebleRef,
      getList
    };
  }
});
</script>
<style lang="scss" scoped>
.line_content {
  margin-bottom: 20px;
}
</style>
