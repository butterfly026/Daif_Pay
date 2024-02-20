
import { defineComponent, reactive, ref, toRefs } from 'vue';
import { getChannelList, addChannel, delChannel, editChannel, closeAllChannel, getChannelBalance } from '@/http/apis/channel'
import { ElTable, ElTableColumn, ElInput, ElForm, ElFormItem, ElSelect, ElButton, ElOption, ElDialog, ElSwitch, ElPagination, ElMessage, ElMessageBox } from 'element-plus';
export default defineComponent({
  components: { ElTable, ElTableColumn, ElInput, ElForm, ElFormItem, ElSelect, ElButton, ElOption, ElDialog, ElSwitch, ElPagination, ElMessage },
  setup() {
    const closeCashPay = reactive({
      state: 0,
    });
    const formInline = reactive({
      name: null,
      state: null,
      page: 1,
      page_size: 10
    });
    const total = ref(0);
    const form = reactive({
      name: '',
      shortname:'',
      account: '',
      ppk: '',
      gateway: '',
      notify_url: '',
      proxy: '',
      rate: '',
      other_info: '',
      selfcharge: 'false',
      state: null
    });
    const isLoading = ref(false);
    const addform = ref(null);
    const showDialog = ref(false);
    const dialogType = ref(null);
    const delDialog = ref(false);
    const ChannelId = ref(null);
    const tableData = ref([]);
    const isClose = ref(0);
    const rules = reactive({
      name: [{
        required: true,
        message: '请输入渠道名称',
        trigger: 'blur',
      }],
      shortname: [{
        required: true,
        message: '请输入短名称',
        trigger: 'blur',
      }],
      account: [{
        required: true,
        message: '请输入商户号',
        trigger: 'blur',
      }],
      ppk: [{
        required: true,
        message: '请输入商户秘钥',
        trigger: 'blur',
      }],
      gateway: [{
        required: true,
        message: '请输入渠道网关',
        trigger: 'blur',
      }],
      notify_url: [{
        required: true,
        message: '请输入回调地址',
        trigger: 'blur',
      }],
      rate: [{
        required: true,
        message: '请输入费率',
        trigger: ['blur', 'change'],
      }],
      state: [{
        required: true,
        message: '请选择状态',
        trigger: 'change',
      }]
    })
    // 获取列表数据
    const getChannel = () => {
      // console.log('getChannel');
      isLoading.value = true;
      getChannelList(formInline).then(res => {
        // console.log(res)
        isLoading.value = false;
        if (res.status) {
          if (formInline.page == 1) {
            total.value = Number(res.data.total) || 0
          }
          if (res.data && res.data.d && Array.isArray(res.data.d)) {
            tableData.value = res.data.d;
          }
        }
      }).catch(err => {
        // console.log(err);
        isLoading.value = false;
      })
    }
    const gotoPage = (current, type) => {
      type === 'size' ? formInline.page_size = current : formInline.page = current;
      getChannel();
    }
    const reset = () => {
      formInline.name = null;
      formInline.state = null;
      formInline.page = 1;
      formInline.page_size = 10;
      getChannel();
    }
    // 新增弹框
    const openDialog = () => {
      showDialog.value = true;
      dialogType.value = 'add'
      addform.value && addform.value.resetFields()
      // addChannel({
      //     name:'test1',
      //     account: '14544',
      //     ppk:'44544',
      //     gateway:'192.168.1.1',
      //     notify_url:'www.bigbet88.com',
      //     state:1
      // }).then(res =>{
      //     if(res.status){
      //     console.log(res);
      //      onCancel();
      //     } else {
      //      alert(res.data);
      //     }
      //   })
    }

    // 开启 关闭
    const handleState = (index, row) => {
      // console.log(index, row)
    }

    // 编辑
    const handleEdit = (index, row) => {
      form.id = row.id;
      form.name = row.name;
      form.shortname = row.shortname;
      form.account = row.account;
      form.ppk = row.ppk;
      form.gateway = row.gateway;
      form.notify_url = row.notify_url;
      form.other_info = row.other_info;
      form.rate = row.rate;
      form.state = row.state;
      form.proxy = row.proxy;
      form.selfcharge = (row.selfcharge=="true");
      showDialog.value = true;
      dialogType.value = 'edit'
    }
    // 删除
    const showDelDialog = (index, row) => {
      ChannelId.value = row.id
      delDialog.value = true
    }

    const handleDelChannel = () => {
      delChannel({ id: ChannelId.value }).then(res => {
        if (res.status) {
          ElMessage({
            showClose: true,
            message: '删除成功',
            type: 'success',
          })
          delDialog.value = false
          getChannel();
        } else {
          alert(res.data);
          delDialog.value = false
        }
      })
    }

    // 获取列表
    getChannel();
    
    // 提交
    const onSubmit = () => {
      if (dialogType.value === 'add') {        
        addform.value.validate((valid) => {
          if (valid && JSON.parse(form.other_info)) {            
            addChannel(form).then(res => {
              if (res.status) {
                ElMessage({
                  showClose: true,
                  message: '提交成功',
                  type: 'success',
                })
                onCancel();
                getChannel();
              } else {
                return false
              }
            })
          }
        })
      } else {
        addform.value.validate((valid) => {
          if (valid) {
            editChannel(form).then(res => {
              if (res.status) {
                ElMessage({
                  showClose: true,
                  message: '提交成功',
                  type: 'success',
                })
                onCancel();
                getChannel();
              } else {
                ElMessage.warning('修改失败')
                return false
              }
            })
          }
        })
      }

    }
    // 取消
    const onCancel = () => {
      addform.value.resetFields();
      showDialog.value = false;
    }

    const closeAll = () => {
      ElMessageBox.confirm(`你确定要${['关闭','开启'][isClose.value]}所有进单吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let params = {};
          params.state = 1;

        closeAllChannel(params).then(res => {
          if (res.status) {
            ElMessage.success('操作成功');
            isClose.value = res.data;
          }
        });
      });
    }

    closeAllChannel(closeCashPay).then(res => {
      if (res.status) {
        isClose.value = res.data;
      }
    });

    const handleShowMoney = (row) => {
      // console.log(row.id);
      // console.log(row.name);
      getChannelBalance({ id: row.id }).then(res => {
        // console.log(res);
        if (res.status) {
          ElMessageBox.alert(row.name + '余额为：' + res.data + '元', '提示', {
            confirmButtonText: '确定',
            type: 'info',
          })
        } else {
          ElMessage.warning('查询失败');
        }
      })

    }

    return {
      formInline,
      total,
      ...toRefs(formInline),
      tableData,
      isLoading,
      showDialog,
      dialogType,
      delDialog,
      isClose,
      openDialog,
      onSubmit,
      getChannel,
      reset,
      gotoPage,
      handleState,
      handleEdit,
      showDelDialog,
      handleDelChannel,
      form,
      addform,
      rules,
      ChannelId,
      onCancel,
      closeAll,
      handleShowMoney,
      closeAllChannel
    }
  }
})
