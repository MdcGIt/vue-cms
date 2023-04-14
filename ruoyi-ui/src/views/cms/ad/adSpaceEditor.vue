<template>
  <div class="app-container adspace-editor-container">
    <div class="mb12">
      <el-button 
        plain
        type="success"
        icon="el-icon-edit"
        size="mini"
        @click="handleSave">保存</el-button>
      <el-button 
        plain
        type="primary"
        icon="el-icon-s-promotion"
        size="mini"
        @click="handlePublish">发布</el-button>
      <el-button 
        plain
        type="primary"
        icon="el-icon-view"
        size="mini"
        @click="handlePreview">预览</el-button>
    </div>
    <el-form 
      ref="form"
      :model="form"
      :rules="rules"
      label-width="80px">
      <el-card shadow="never">
        <div slot="header" class="clearfix">
          <span>基础属性</span>
        </div>
        <div class="form-col">
          <el-form-item label="名称"
                        prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="编码"
                        prop="code">
            <el-input v-model="form.code" />
          </el-form-item>
          <el-form-item label="发布目录"
                        prop="path">
            <el-input v-model="form.path" />
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
          <el-form-item label="模板"
                        prop="template">
            <el-input v-model="form.template" :disabled="templateDisabled" >
              <el-button 
                slot="append"
                type="primary"
                :disabled="templateDisabled"
                @click="handleSelectTemplate()">选择</el-button>
            </el-input>
          </el-form-item>
          <el-form-item label="备注"
                        prop="remark">
            <el-input v-model="form.remark" />
          </el-form-item>
        </div>
      </el-card>
    </el-form>
    <el-card class="mt10" shadow="never">
      <div slot="header" class="clearfix">
        <span>广告列表</span>
      </div>
      <el-row :gutter="24" class="mb12">
        <el-col :span="12">
          <el-button 
            plain
            type="primary"
            icon="el-icon-plus"
            size="mini"
            @click="handleAddAdvertisement">新建</el-button>
          <el-button 
            plain
            type="success"
            icon="el-icon-edit"
            size="mini"
            :disabled="selectedRows.length!==1"
            @click="handleEditAdvertisement">编辑</el-button>
          <el-button 
            plain
            type="danger"
            icon="el-icon-plus"
            size="mini"
            :disabled="selectedRows.length===0"
            @click="handleDeleteAdvertisements">删除</el-button>
        </el-col>
        <el-col :span="12" style="text-align: right">
          <el-input placeholder="广告名称" v-model="queryParams.name" size="mini" style="width: 200px;" class="mr10"></el-input>
          <el-button 
            type="primary"
            icon="el-icon-search"
            size="mini"
            @click="handleQuery">搜索</el-button>
          <el-button 
            icon="el-icon-refresh"
            size="mini"
            @click="resetQuery">重置</el-button>
        </el-col>
      </el-row>
      <el-row>
        <el-table 
          v-loading="loading"
          :data="dataList"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleEditAdvertisement">
          <el-table-column type="selection"
                          width="50"
                          align="center" />
          <el-table-column label="名称"
                          prop="name">
          </el-table-column>
          <el-table-column label="类型"
                            width="100"
                            align="center"
                            prop="typeName">
          </el-table-column>
          <el-table-column label="权重"
                            width="100"
                            align="center"
                            prop="weight" />
          <el-table-column label="状态"
                            width="100"
                            align="center"
                            prop="state">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.EnableOrDisable" :value="scope.row.state"/>
            </template>
          </el-table-column>
          <el-table-column label="上线时间"
                            align="center"
                            prop="onlineDate"
                            width="160">
            <template slot-scope="scope">
              <span>{{ scope.row.onlineDate }}</span>
            </template>
          </el-table-column>
          <el-table-column label="下线时间"
                            align="center"
                            prop="offlineDate"
                            width="160">
            <template slot-scope="scope">
              <span>{{ scope.row.offlineDate }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作"
                            align="center"
                            width="300" 
                            class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button 
                v-if="scope.row.state==='1'"
                size="mini"
                type="text"
                icon="el-icon-switch-button"
                @click="handleEnableAdvertisements(scope.row)">启用</el-button>
              <el-button 
                v-if="scope.row.state==='0'"
                size="mini"
                type="text"
                icon="el-icon-switch-button"
                @click="handleDisableAdvertisements(scope.row)">停用</el-button>
              <el-button 
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="handleEditAdvertisement(scope.row)">修改</el-button>
              <el-button 
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDeleteAdvertisements(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total>0"
                  :total="total"
                  :page.sync="queryParams.pageNum"
                  :limit.sync="queryParams.pageSize"
                  @pagination="loadAdvertisementList" />
      </el-row>
    </el-card>
    <!-- 模板选择组件 -->
    <cms-template-selector :open="openTemplateSelector" 
                       :publishPipeCode="form.publishPipeCode"
                       @ok="handleTemplateSelected"
                       @cancel="handleTemplateSelectorCancel" />
  </div>
</template>
<script>
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { getPageWidget, addPageWidget, editPageWidget, publishPageWidgets } from "@/api/contentcore/pagewidget";
import { listAdvertisements, listAdvertisementTypes, deleteAdvertisement, enableAdvertisement, disableAdvertisement } from "@/api/advertisement/advertisement";
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';

export default {
  name: "CMSAdSpaceEditor",
  components: {
    'cms-template-selector': CMSTemplateSelector,
  },
  dicts: [ 'EnableOrDisable' ],
  data () {
    return {
      loading: true,
      pageWidgetId: this.$route.query.id,
      publishPipes: [],
      pageWidgetTypes: [],
      form: {
        publishPipeCode: '',
        content: {}
      },
      rules: {
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        code: [
          { required: true, message: "编码不能为空", trigger: "blur" }
        ],
        publishPipeCode: [
          { required: true, message: "发布通道不能为空", trigger: "blur" }
        ],
        path: [
          { required: true, message: "目录不能为空", trigger: "blur" }
        ]
      },
      openTemplateSelector: false,
      dataList: undefined,
      total: 0,
      selectedRows: [],
      adSpaceId: this.$route.query.id,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        adSpaceId: this.$route.query.id,
        name: undefined
      }
    };
  },
  computed: {
    templateDisabled() {
      return !this.form.publishPipeCode || this.form.publishPipeCode == null || this.form.publishPipeCode === '';
    }
  },
  created () {
    if (this.pageWidgetId) {
      this.loadPublishPipes();
      this.loadPageWidgetInfo();
      this.loadAdvertisementList();
    } else {
      this.$modal.msgError("参数异常");
    }
  },
  methods: {
    loadPublishPipes() {
      getPublishPipeSelectData().then(response => {
        this.publishPipes = response.data.rows;
      });
    },
    loadPageWidgetInfo() {
      getPageWidget(this.pageWidgetId).then(response => {
        if (response.code == 200) {
          this.form = response.data;
          this.dataList = this.form.content ? JSON.parse(this.form.content) : [];
        }
      });
    },
    handleSave () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.catalogId = this.catalogId;
          this.form.content = JSON.stringify(this.dataList);
          if (this.pageWidgetId) {
            editPageWidget(this.form).then(response => {
              if (response.code === 200) {
                this.$modal.msgSuccess(response.msg);
              }
            });
          } else {
            addPageWidget(this.form).then(response => {
              if (response.code === 200) {
                this.$modal.msgSuccess(response.msg);
                this.$router.push({ path: form.route, query: { id: response.data } });
              }
            });
          }
        }
      });
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
    handlePublish() {
      const pageWidgetIds = [ this.pageWidgetId ];
      publishPageWidgets(pageWidgetIds).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess(response.msg);
        }
      });
    },
    handlePreview() {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "pagewidget", dataId: this.pageWidgetId },
      });
      window.open(routeData.href, '_blank');
    },
    loadAdvertisementList () {
      this.loading = true;
      listAdvertisements(this.queryParams).then(response => {
        if (response.code == 200) {
          this.dataList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      });
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadAdvertisementList();
    },
    resetQuery () {
      this.queryParams.name = undefined;
      this.queryParams.pageNum = 1;
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.selectedRows = selection.map(item => item);
    },
    handleAddAdvertisement() {
      this.$router.push({ path: "/cms/ad/editor", query: { adSpaceId: this.adSpaceId } });
    },
    handleEditAdvertisement(row) {
      const advertisementId = row.advertisementId || this.selectedRows[0].advertisementId;
      this.$router.push({ path: "/cms/ad/editor", query: { adSpaceId: this.adSpaceId, id: advertisementId } });
    },
    handleDeleteAdvertisements(row) {
      const advertisementIds = row.advertisementId ? [ row.advertisementId ] : this.selectedRows.map(item => item.advertisementId);
      if (advertisementIds.length == 0) {
        return;
      }
      this.$modal.confirm("确认删除？").then(function() {
        return deleteAdvertisement(advertisementIds);
      }).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadAdvertisementList();
        }
      }).catch(() => {});
    },
    handleEnableAdvertisements(row) {
      const advertisementIds = row.advertisementId ? [ row.advertisementId ] : this.selectedRows.map(item => item.advertisementId);
      enableAdvertisement(advertisementIds).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadAdvertisementList();
        }
      });
    },
    handleDisableAdvertisements(row) {
      const advertisementIds = row.advertisementId ? [ row.advertisementId ] : this.selectedRows.map(item => item.advertisementId);
      disableAdvertisement(advertisementIds).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadAdvertisementList();
        }
      });
    }
  }
};
</script>
<style scoped>
.adspace-editor-container .el-input, .el-select, .el-textarea {
  width: 400px;
}
.adspace-editor-container .form-row {
  width: 100%;
  display: inline-block;
}
.adspace-editor-container .el-form-item {
  width: 500px;
  float: left;
}
</style>