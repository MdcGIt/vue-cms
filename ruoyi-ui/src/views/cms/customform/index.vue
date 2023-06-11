<template>
  <!-- 自定义表单管理页 -->
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleEdit">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="loadCustomFormList"></right-toolbar>
    </el-row>
    <el-row>
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true" v-show="showSearch">
        <el-form-item prop="query">
          <el-input
            v-model="queryParams.query"
            clearable
            placeholder="名称"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="status">
          <el-select 
            v-model="queryParams.status"
            clearable
            placeholder="状态"
            style="width: 110px">
            <el-option 
              v-for="dict in dict.type.CustomFormStatus"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>

    </el-row>

    <el-table 
      v-loading="loading"
      :data="dataList"
      @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column :label="$t('CMS.CustomForm.Name')">
        <template slot-scope="scope">
          <el-link type="primary" @click="handleViewData(scope.row)" class="link-type">
            <span>{{ scope.row.name }}</span>
          </el-link>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('CMS.CustomForm.Code')"
        align="center"
        prop="code" />
      <el-table-column :label="$t('CMS.CustomForm.Status')" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.CustomFormStatus" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column 
        label="创建时间"
        align="center"
        prop="createTime"
        width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column  
        :label="$t('Common.Operation')"
        align="center"
        width="220" 
        class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-setting"
            @click="handleFields(scope.row)">{{ $t("CMS.CustomForm.Fields") }}</el-button>
          <el-button 
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleEdit(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadCustomFormList" />

    <el-dialog 
      :title="title" 
      :visible.sync="open"
      :close-on-click-modal="false" 
      width="600px" 
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" :maxlength="30" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" :maxlength="30" />
        </el-form-item>
        <el-form-item v-if="!form.formId || form.formId == 0" :label="$t('CMS.CustomForm.TableName')" prop="tableName">
          <el-select v-model="form.tableName" filterable placeholder="请选择">
            <el-option
              v-for="item in xmodelDataTableList"
              :key="item"
              :label="item"
              :value="item">
            </el-option>
          </el-select>
        </el-form-item>
        <div v-if="form.formId && form.formId > 0">
          <el-form-item 
            v-for="item in form.templates" 
            :label="item.name" 
            :key="item.code">
            <el-input v-model="item.template">
              <el-button slot="append" icon="el-icon-folder-opened" @click="handleSelectTemplate(item.code)"></el-button>
            </el-input>
          </el-form-item>
        </div>
        <el-form-item :label="$t('Common.Remark')">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleSubmitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="handleCancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      :open="openTemplateSelector" 
      :publishPipeCode="publishPipe" 
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
  </div>
</template>
<script>
import { listModelDataTable } from "@/api/meta/model";
import { listCustomForms, getCustomForm, addCustomForm, editCustomForm, deleteCustomForms } from "@/api/customform/customform";

import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';

export default {
  name: "CustomFormList",
  dicts: [ 'CustomFormStatus' ],
  components: {
    'cms-template-selector': CMSTemplateSelector,
  },
  data () {
    return {
      loading: true,
      showSearch: true,
      openTemplateSelector: false,
      publishPipe: "",
      xmodelDataTableList: [],
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      dataList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      form: {
        status: '0'
      },
      rules: {
        name: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]+$", message: this.$t('Common.RuleTips.Code'), trigger: "blur" }
        ],
        tableName: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
        ]
      }
    };
  },
  created () {
    this.loadXModelDataTableList();
    this.loadCustomFormList();
  },
  methods: {
    loadXModelDataTableList() {
      listModelDataTable().then(response => {
        this.xmodelDataTableList = response.data.rows;
      });
    },
    loadCustomFormList () {
      this.loading = true;
      listCustomForms(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.loadCustomFormList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.formId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    reset () {
      this.resetForm("form");
      this.form = { status: '0' };
    },
    handleSelectTemplate(publishPipeCode) {
      this.publishPipe = publishPipeCode;
      this.$nextTick(() => {
        this.openTemplateSelector = true;
      })
    },
    handleTemplateSelected (template) {
      this.form.templates.some(item => {
        if (item.code == this.publishPipe) {
            item.template = template;
            return true;
        }
        return false;
      });
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel () {
      this.openTemplateSelector = false;
    },
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = this.$t('CMS.CustomForm.AddTitle');
    },
    handleEdit (row) {
      this.reset();
      const formId = row.formId ? row.formId : this.ids[0];
      getCustomForm(formId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = this.$t('CMS.CustomForm.EditTitle');
      });
    },
    handleCancel () {
      this.open = false;
      this.reset();
    },
    handleSubmitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.formId != undefined) {
            editCustomForm(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
              this.open = false;
              this.loadCustomFormList();
            });
          } else {
            addCustomForm(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.AddSuccess'));
              this.open = false;
              this.loadCustomFormList();
            });
          }
        }
      });
    },
    handleDelete (row) {
      const formIds = row.formId ? [ row.formId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return deleteCustomForms(formIds);
      }).then(() => {
        this.loadCustomFormList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(function () { });
    },
    handleFields(row) {
      this.$router.push({ 
        path: "/operations/customform/fields", 
        query: { 
          modelId: row.modelId
        } 
      });
    },
    handleViewData(row) {
      
    }
  }
};
</script>