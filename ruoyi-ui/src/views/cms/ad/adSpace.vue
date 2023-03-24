<template>
  <div class="app-container">
    <el-row :gutter="24"
            class="mb8">
      <el-col :span="12">
        <el-button plain
                   type="primary"
                   icon="el-icon-plus"
                   size="mini"
                   @click="handleAdd">新建</el-button>
        <el-button plain
                   type="danger"
                   icon="el-icon-plus"
                   size="mini"
                   :disabled="selectedRows.length===0"
                   @click="handleDelete">删除</el-button>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <el-input placeholder="广告位名称" v-model="queryParams.name" size="mini" class="mr10" style="width: 200px;"></el-input>
        <el-button 
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="loadAdSpaceList">搜索</el-button>
        <el-button 
          icon="el-icon-refresh"
          size="mini"
          @click="resetQuery">重置</el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-table v-loading="loading"
            :data="dataList"
            @selection-change="handleSelectionChange"
            @row-dblclick="handleEdit">
        <el-table-column type="selection"
                        width="50"
                        align="center" />
        <el-table-column label="名称">
          <template slot-scope="scope">
            <el-link type="primary"
                      @click="handleEdit(scope.row)"
                      class="link-type">
              <span>{{ scope.row.name }}</span>
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="编码"
                          prop="code" />
        <el-table-column label="发布通道"
                          width="100"
                          prop="publishPipeCode" />
        <el-table-column label="操作"
                          align="center"
                          width="300" 
                          class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button size="mini"
                        type="text"
                        icon="el-icon-s-promotion"
                        @click="handlePublish(scope.row)">发布</el-button>
            <el-button size="mini"
                        type="text"
                        @click="handlePreview(scope.row)"><svg-icon icon-class="eye-open" class="mr1"></svg-icon>预览</el-button>
            <el-button size="mini"
                        type="text"
                        icon="el-icon-edit"
                        @click="handleEdit(scope.row)">修改</el-button>
            <el-button size="mini"
                        type="text"
                        icon="el-icon-delete"
                        @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total>0"
                :total="total"
                :page.sync="queryParams.pageNum"
                :limit.sync="queryParams.pageSize"
                @pagination="loadAdSpaceList" />
    </el-row>
    <!-- 表单对话框 -->
    <el-dialog :title="dialogTitle"
               :visible.sync="dialogVisible"
               :close-on-click-modal="false"
               width="600px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="80px">
        <el-form-item label="名称"
                      prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编码"
                      prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="发布通道"
                      prop="publishPipeCode">
          <el-select v-model="form.publishPipeCode">
            <el-option
              v-for="pp in publishPipes"
              :key="pp.pipeCode"
              :label="pp.pipeName"
              :value="pp.pipeCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发布目录"
                      prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="模板"
                      prop="template">
          <el-input v-model="form.template">
            <el-button 
              slot="append"
              type="primary"
              @click="handleSelectTemplate()">选择</el-button>
          </el-input>
        </el-form-item>
        <el-form-item label="备注"
                      prop="remark">
          <el-input v-model="form.remark"
                    type="textarea"
                    maxlength="100" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary"
                   @click="handleDialogOk">确 定</el-button>
        <el-button @click="handleDialogClose">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      :open="openTemplateSelector" 
      :publishPipeCode="form.publishPipeCode" 
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
  </div>
</template>
<style scoped>
.el-input, .el-select, .el-textarea {
  width: 300px;
}
</style>
<script>
import { codeValidator } from '@/utils/validate'
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { listAdSpaces, addAdSpace, editAdSpace, deleteAdSpace, publishAdSpace } from "@/api/advertisement/advertisement";
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';

export default {
  name: "CMSAdSpace",
  components: {
    'cms-template-selector': CMSTemplateSelector
  },
  data () {
    return {
      loading: true,
      dataList: undefined,
      total: 0,
      selectedRows: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined
      },
      dialogTitle: "",
      dialogVisible: false,
      form: {
        path: 'include/ad/',
        publishPipeCode: ''
      },
      rules: {
        type: [
          { required: true, message: "类型不能为空", trigger: "blur" }
        ],
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        code: [
          { required: true, message: "编码不能为空", trigger: "blur" },
          { trigger: "blur", validator: codeValidator }
        ],
        publishPipeCode: [
          { required: true, message: "发布通道不能为空", trigger: "blur" }
        ],
        path: [
          { required: true, message: "目录不能为空", trigger: "blur" }
        ],
        template: [
          { required: true, message: "模板不能为空", trigger: "blur" }
        ]
      },
      publishPipes: [],
      openTemplateSelector: false
    };
  },
  created () {
    this.loadPublishPipes();
    this.loadAdSpaceList();
  },
  methods: {
    loadPublishPipes() {
      getPublishPipeSelectData().then(response => {
        if (response.code == 200) {
          this.publishPipes = response.data.rows;
        }
      });
    },
    loadAdSpaceList () {
      this.loading = true;
      listAdSpaces(this.queryParams).then(response => {
        if (response.code == 200) {
          this.dataList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      });
    },
    resetQuery () {
      this.queryParams.pageNum = 1;
      this.queryParams.name = undefined;
      this.loadAdSpaceList();
    },
    handleSelectionChange(selection) {
      this.selectedRows = selection.map(item => item);
    },
    handleAdd() {
      this.dialogTitle = "添加广告位";
      this.form = { path: 'include/ad/' };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.$router.push({ path: "/cms/adspace/editor", query: { id: row.pageWidgetId } });
    },
    handleDialogClose(reload) {
      this.dialogVisible = false;
      if (reload) {
        this.loadAdSpaceList();
      }
    },
    handleDialogOk () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          addAdSpace(this.form).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess(response.msg);
              this.handleDialogClose(true);
            }
          });
        }
      });
    },
    handleDelete(row) {
      const pageWidgetIds = row.pageWidgetId ? [ row.pageWidgetId ] : this.selectedRows.map(item => item.pageWidgetId);
      if (pageWidgetIds.length == 0) {
        return;
      }
      this.$modal.confirm("确认删除？").then(function() {
        return deleteAdSpace(pageWidgetIds);
      }).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadAdSpaceList();
        }
      }).catch(() => {});
    },
    handlePublish(row) {
      const pageWidgetIds = [ row.pageWidgetId ];
      publishAdSpace(pageWidgetIds).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadAdSpaceList();
        }
      });
    },
    handlePreview(row) {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "pagewidget", dataId: row.pageWidgetId },
      });
      window.open(routeData.href, '_blank');
    },
    handleSelectTemplate () {
      this.openTemplateSelector = true;
    },
    handleTemplateSelected (template) {
      this.form.template = template;
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel () {
      this.openTemplateSelector = false;
    },
  }
};
</script>