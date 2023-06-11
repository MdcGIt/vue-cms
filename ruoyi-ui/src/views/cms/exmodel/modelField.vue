<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-button 
          plain
          type="info"
          icon="el-icon-back"
          size="mini"
          @click="handleGoBack">{{ $t('CMS.ExModel.GoBack') }}</el-button>
        <el-button 
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
        <el-button 
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleBatchDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <el-col :span="12">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input :placeholder="$t('CMS.ExModel.Placeholder.FieldQuery')" v-model="queryParams.query"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="el-icon-search"
                @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button 
                icon="el-icon-refresh"
                @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-row>
      <el-table v-loading="loading" :data="fieldList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
        <el-table-column :label="$t('CMS.ExModel.FieldName')" align="center" prop="name" />
        <el-table-column :label="$t('CMS.ExModel.FieldCode')" align="center" prop="code" />
        <el-table-column :label="$t('CMS.ExModel.FieldControlType')" align="center" prop="controlType">
          <template slot-scope="scope">
            {{ formatControlType(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.ExModel.FieldMappingName')" align="center" prop="fieldName" />
        <el-table-column :label="$t('CMS.ExModel.FieldMandatory')" align="center" prop="mandatoryFlag">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.YesOrNo" :value="scope.row.mandatoryFlag"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Common.Operation')" align="center" width="300" class-name="small-padding fixed-width">
          <template slot-scope="scope">
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
        v-show="fieldTotal>0"
        :total="fieldTotal"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadXModelFieldList" />
    </el-row>
    <!-- 添加/编辑弹窗 -->
    <el-dialog 
      :title="title"
      :visible.sync="open"
      width="600px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="el-form-dialog">
        <el-form-item :label="$t('CMS.ExModel.FieldName')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.ExModel.FieldCode')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item v-if="!isDefaultTable" :label="$t('CMS.ExModel.FieldMappingName')" prop="fieldName">
          <el-select v-model="form.fieldName">
            <el-option
              v-for="fieldName in tableFields"
              :key="fieldName"
              :label="fieldName"
              :value="fieldName"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isDefaultTable" :label="$t('CMS.ExModel.FieldType')" prop="fieldType">
          <el-select v-model="form.fieldType">
            <el-option
              v-for="dict in dict.type.MetaFieldType"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.ExModel.FieldControlType')" prop="controlType">
          <el-select v-model="form.controlType">
            <el-option
              v-for="control in controlOptions"
              :key="control.id"
              :label="control.name"
              :value="control.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.ExModel.FieldMandatory')" prop="mandatoryFlag">
          <el-select v-model="form.mandatoryFlag">
            <el-option
              v-for="dict in dict.type.YesOrNo"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.ExModel.FieldDefaultValue')" prop="defaultValue">
          <el-input v-model="form.defaultValue" />
        </el-form-item>
        <el-form-item v-if="showOptions" :label="$t('CMS.ExModel.FieldOptions')" prop="options">
          <div>
            <el-radio-group v-model="form.options.type">
              <el-radio label="text">{{ $t('CMS.ExModel.FieldOptionsInput') }}</el-radio>
              <el-radio label="dict">{{ $t('CMS.ExModel.FieldOptionsDict') }}</el-radio>
            </el-radio-group>
          </div>
          <div>
            <el-input v-if="form.options.type==='text'" type="textarea" v-model="form.options.value" />
            <el-input v-if="form.options.type==='dict'" v-model="form.options.value" />
          </div>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input type="textarea" v-model="form.remark" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="handleAddSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="closeDialog(false)">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { addXModelField, editXModelField, deleteXModelField, listXModelField, listXModelTableFields } from "@/api/contentcore/exmodel";
import { getControlOptions } from "@/api/meta/model"

export default {
  name: "CMSEXModelField",
  dicts: [ 'MetaFieldType', 'YesOrNo' ],
  data () {
    return {
      // 遮罩层
      loading: false,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      fieldList: undefined,
      fieldTotal: 0,
      selectedRows: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        modelId: this.$route.query.modelId,
        query: undefined
      },
      modelId: this.$route.query.modelId,
      isDefaultTable: this.$route.query.isDefaultTable === 'true',
      tableFields: [],
      controlOptions: [],
      // 表单参数
      form: {
        options:{ type: "text" }
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: this.$t('CMS.ExModel.RuleTips.FieldName'), trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]+$", message: this.$t('CMS.ExModel.RuleTips.FieldCode'), trigger: "blur" }
        ],
        controlType: [
          { required: true, message: this.$t('CMS.ExModel.RuleTips.FieldControlType'), trigger: "blur" }
        ],
        mandatoryFlag: [
          { required: true, message: this.$t('CMS.ExModel.RuleTips.FieldMandatory'), trigger: "blur" }
        ],
        fieldType: [
          { required: true, validator: (rule, value, callback) => {
                if (this.isDefaultTable && (!value || value == null || value == '')) {
                  return callback(new Error(this.$t('CMS.ExModel.RuleTips.FieldType')));
                }
                callback();
              }, trigger: "blur" }
        ],
        fieldName: [
          { required: true, validator: (rule, value, callback) => {
                if (!this.isDefaultTable) {
                  if (!value || value == null || value == '') {
                    return callback(new Error(this.$t('CMS.ExModel.RuleTips.FieldMappingName')));
                  }
                  for(let i = 0; i < this.fieldList.length; i++) {
                    if (this.fieldList[i].fieldName == value) {
                      return callback(new Error(this.$t('CMS.ExModel.RuleTips.FieldMappingUsed')));
                    }
                  }
                }
                callback();
              }, trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    showOptions() {
      return this.form.controlType==='select' || this.form.controlType==='radio' || this.form.controlType==='checkbox'
    }
  },
  created () {
    this.loadControlOptions();
    this.loadXModelFieldList();
    if (!this.isDefaultTable) {
      this.loadXmodelUsableFields();
    }
  },
  methods: {
    loadControlOptions() {
      getControlOptions().then(response => {
        this.controlOptions = response.data;
      })
    },
    formatControlType(row) {
      let control = this.controlOptions.find(item => item.id == row.controlType)
      return control ? control.name : row.controlType
    },
    loadXModelFieldList () {
      this.loading = true;
      listXModelField(this.queryParams).then(response => {
        this.fieldList = response.data.rows.map(item => {
          if (item.options == null) {
            item.options = { type: "text" };
          }
          return item;
        });
        this.fieldTotal = parseInt(response.data.total);
        this.loading = false;
      });
    },
    loadXmodelUsableFields () {
      const params = { modelId: this.modelId };
      listXModelTableFields(params).then(response => {
        this.tableFields = response.data.rows;
      });
    },
    handleSelectionChange (selection) {
      this.single = selection.length != 1
      this.multiple = !selection.length
      this.selectedRows = selection;
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadXModelFieldList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    closeDialog (loadList) {
      this.open = false;
      this.form = { options:{ type: "text" } };
      if (loadList) this.loadXModelFieldList();
    },
    handleAdd () {
      this.form = { options:{ type: "text" } };
      this.title = this.$t('CMS.ExModel.AddFieldTitle');
      this.open = true;
    },
    handleEdit (row) {
      this.form = row;
      this.title = this.$t('CMS.ExModel.EditFieldTitle');
      this.open = true;
    },
    handleAddSave () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.fieldId) {
            editXModelField(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.closeDialog(true);
            }); 
          } else {
            this.form.modelId = this.modelId;
            addXModelField(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.closeDialog(true);
            }); 
          }
        }
      });
    },
    handleDelete (row) {
      this.doDelete([ row ]);
    },
    handleBatchDelete () {
      if (this.selectedRows.length > 0) {
        this.doDelete(this.selectedRows);
      }
    },
    doDelete (fields) {
      const fieldIds = fields.map(f => f.fieldId);
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return deleteXModelField(fieldIds);
      }).then(() => {
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
        this.loadXModelFieldList();
      }).catch(function () { });
    },
    handleGoBack() {
      this.$router.push({ path: "/configs/exmodel" });
    }
  }
};
</script>