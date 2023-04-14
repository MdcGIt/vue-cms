<template>
  <div class="app-container">
    <el-dialog :title="title"
               :visible.sync="visible"
               width="700px"
               :close-on-click-modal="false"
               append-to-body>
      <el-form :model="queryParams"
              ref="queryForm"
              :inline="true"
              label-width="68px"
              class="el-form-search">
        <el-form-item label="模板名称" prop="filename">
          <el-input v-model="queryParams.filename" size="small">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary"
                    icon="el-icon-search"
                    size="small"
                    @click="handleQuery">{{ $t("Common.Search") }}</el-button>
          <el-button icon="el-icon-refresh"
                    size="small"
                    @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
        </el-form-item>
      </el-form>
      <el-table 
        v-loading="loading"
        :height="435"
        :data="templateList"
        highlight-current-row
        @current-change="handleSelectionChange">
        <el-table-column type="index"
                        label="序号"
                        align="center"
                        width="50" />
        <el-table-column label="模板名称"
                        align="left"
                        prop="path"/>
      </el-table>
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
      <div slot="footer"
            class="dialog-footer">
        <el-button type="primary" 
                    :disabled="okBtnDisabled"
                    @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { getTemplateList } from "@/api/contentcore/template";

export default {
  name: "CMSTemplateSelector",
  props: {
    title: {
      type: String,
      default: "选择模板",
      required: false
    },
    publishPipeCode: {
      type: String,
      required: true
    },
    open: {
      type: Boolean,
      default: false,
      required: true
    }
  },
  watch: {
    open () {
      this.visible = this.open;
    },
    visible (newVal) {
      if (!newVal) {
        this.handleCancel();
      } else {
        this.getList();
      }
    },
    publishPipeCode () {
      this.queryParams.publishPipeCode = this.publishPipeCode;
    }
  },
  computed: {
    okBtnDisabled() {
      return this.selectedTemplate == undefined;
    }
  },
  data () {
    return {
      // 遮罩层
      loading: false,
      visible: this.open,
      // 选中数组
      selectedTemplate: undefined,
      // 资源表格数据
      templateList: [],
      total: 0,
      // 查询参数
      queryParams: {
        publishPipeCode: this.publishPipeCode,
        filename: undefined,
        pageSize: 8
      }
    };
  },
  methods: {
    /** 查询资源列表 */
    getList () {
      if (!this.visible) {
        return;
      }
      this.loading = true;
      getTemplateList(this.queryParams).then(response => {
        this.templateList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.selectedTemplate = undefined;
        this.loading = false;
      });
    },
    handleSelectionChange (selection) {
      if (selection) {
        this.selectedTemplate = selection.path;
      }
    },
    handleOk () {
      this.$emit("ok", this.selectedTemplate);
    },
    // 取消按钮
    handleCancel () {
      this.$emit("cancel");
      this.queryParams.filename = undefined;
      this.selectedTemplate = undefined;
      this.templateList = [];
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.queryParams.filename = undefined;
      this.handleQuery();
    }
  }
};
</script>