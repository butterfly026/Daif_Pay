<template>
  <el-card class="box-card">
    <template #header>
      <div class="card-header">
        <span>发起批量提现</span>
      </div>
    </template>

    <div class="upload-box">
      <div class="upload-box-left">上传批量提现的表格:</div>
      <div class="upload-box-right">
        <div class="shili">
          <el-button size="mini" type="primary" @click="excelDownload"> 表格示例下载 </el-button>
        </div>
        <input ref="excelUploadInput" class="excel-upload-input" type="file" @change="handleClick" />
        <p style="color: red; margin-top: 5px">请上传csv或xlsx,xls的文件，上传笔数不超过100笔</p>
        <div class="drop" @drop="handleDrop" @dragover="handleDragover" @dragenter="handleDragover">
          <el-button :loading="loading" size="mini" type="primary" @click="handleUpload" :disabled="isDisabled"> 提交 </el-button>
        </div>
      </div>
    </div>
  </el-card>
  <el-card class="box-card" v-if="tableData.length > 0" style="margin-top: 10px">
    <el-table :data="tableData" border highlight-current-row style="width: 100%; margin-top: 20px">
      <el-table-column v-for="item of tableHeader" :key="item" :prop="item" :label="item" />
    </el-table>
  </el-card>
</template>
<script>
import { defineComponent, reactive, ref, toRefs } from 'vue';
import { ElMessage } from 'element-plus';
import XLSX from 'xlsx';
import { batchWithdrawal } from '../../http/apis/withdrawal';

export default defineComponent({
  props: {
    reset: {
      type: Function
    }
  },
  setup(props) {
    const state = reactive({
      tableData: [],
      tableHeader: [],
      tableList: [
        {
          amount: 1000,
          bank_name: '中国农业银行',
          bank_user: '阿猫',
          bank_card: '1231231',
          bank_open: '农行'
        },
        {
          amount: 990,
          bank_name: '中国工商银行',
          bank_user: '阿狗',
          bank_card: '1231231',
          bank_open: '工行'
        }
      ],
      isDisabled: false
    });

    // 点击表格示例下载
    const excelDownload = () => {
      loading.value = true;
      import('@/utils/Export2Excel').then(excel => {
        const tHeader = ['金额', '收款卡号', '收款姓名', '收款银行', '开户银行'];
        const filterVal = ['amount', 'bank_card', 'bank_user', 'bank_name', 'bank_open'];
        const data = formatJson(filterVal);
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: 'patchDemo'
        });
        loading.value = false;
      });
    };

    const formatJson = filterVal => {
      return state.tableList.map(v =>
        filterVal.map(j => {
          return v[j];
        })
      );
    };

    // 选择文件
    const handleClick = e => {
      const files = e.target.files;
      const rawFile = files[0]; // only use files[0]
      if (!rawFile) return;
      upload(rawFile);
    };

    const loading = ref(false);

    const handleDrop = e => {
      e.stopPropagation();
      e.preventDefault();
      if (loading.value) return;
      const files = e.dataTransfer.files;
      if (files.length !== 1) {
        ElMessage.error('只支持上传一个文件！');
        return;
      }
      const rawFile = files[0]; // only use files[0]

      if (!isExcel(rawFile)) {
        ElMessage.error('仅支持上载.xlsx、.xls、.csv后缀文件');
        return false;
      }
      upload(rawFile);
      e.stopPropagation();
      e.preventDefault();
    };

    const upload = rawFile => {
      excelUploadInput.value = null;
      if (!isExcel(rawFile)) {
        ElMessage.error('仅支持上载.xlsx、.xls、.csv后缀文件');
        return false;
      }

      if (!beforeUpload) {
        readerData(rawFile);
        return;
      }
      const before = beforeUpload(rawFile);
      if (before) {
        readerData(rawFile);
      }
    };

    const excelUploadInput = ref(null);

    const handleDragover = e => {
      e.stopPropagation();
      e.preventDefault();
      e.dataTransfer.dropEffect = 'copy';
    };

    const generateData = ({ header, results }) => {
      state.tableHeader = header;
      state.tableData = results;
    };

    // 处理表格数据
    const readerData = rawFile => {
      loading.value = true;
      // eslint-disable-next-line no-unused-vars
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = e => {
          const data = e.target.result;
          const workbook = XLSX.read(data, { type: 'array' });
          const firstSheetName = workbook.SheetNames[0];
          const worksheet = workbook.Sheets[firstSheetName];
          const header = getHeaderRow(worksheet);
          const results = XLSX.utils.sheet_to_json(worksheet);
          generateData({ header, results });
          loading.value = false;
          resolve();
        };
        reader.readAsArrayBuffer(rawFile);
      });
    };

    // 处理表头信息
    const getHeaderRow = sheet => {
      try {
        const headers = [];
        const range = XLSX.utils.decode_range(sheet['!ref']);
        let C;
        const R = range.s.r;
        /* start in the first row */
        for (C = range.s.c; C <= range.e.c; ++C) {
          /* walk every column in the range */
          const cell = sheet[XLSX.utils.encode_cell({ c: C, r: R })];
          /* find the cell in the first row */
          let hdr = 'UNKNOWN ' + C; // <-- replace with your desired default
          if (cell && cell.t) hdr = XLSX.utils.format_cell(cell);
          headers.push(hdr);
        }
        return headers;
      } catch (error) {
        ElMessage.error('上传的文件错误，请检查文件是否正确！！');
        return false;
      }
    };

    // 提交上传文件
    const handleUpload = () => {
      state.isDisabled = true;
      if (state.tableData.length > 0 && state.tableData.length < 101) {
        batchWithdrawal({ code: '0000', data: JSON.stringify(state.tableData) }).then(res => {
          if (res.status) {
            ElMessage.success('操作成功');
            state.tableData = [];
            props.reset();
            state.isDisabled = false;
          } else {
            props.reset();
            state.isDisabled = true;
            return false;
          }
        });
      } else {
        ElMessage.error('上传的文件不能为空，或是上传笔数超过100笔,金额为100-50000之间');
        props.reset();
      }
      excelUploadInput.value = null;
    };

    // 校验文件后缀
    const isExcel = file => {
      return /\.(xlsx|xls|csv)$/.test(file.name);
    };

    // 判断文件大小
    const beforeUpload = file => {
      const isLt1M = file.size / 1024 / 1024 < 1;
      if (isLt1M) {
        return true;
      }

      ElMessage({
        message: '文件大小不能超过1M',
        type: 'warning'
      });

      return false;
    };

    return {
      ...toRefs(state),
      loading,
      excelUploadInput,
      excelDownload,
      formatJson,
      handleClick,
      handleDrop,
      handleDragover,
      handleUpload,
      readerData,
      getHeaderRow,
      upload,
      beforeUpload,
      generateData,
      isExcel
    };
  }
});
</script>

<style scoped lang="less">
.upload-box {
  display: flex;
  .upload-box-left {
    width: 13%;
    display: flex;
    justify-items: center;
    align-items: center;
    padding-left: 20px;
    font-size: 18px;
    font-weight: 600;
  }
  .upload-box-right {
    .shili {
      margin: 8px 0;
    }
    .drop {
      margin-top: 10px;
    }
  }
}
</style>