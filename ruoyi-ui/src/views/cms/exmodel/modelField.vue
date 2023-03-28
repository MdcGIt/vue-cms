<template>
  <div class="app-container">
    <el-row>
      <el-form 
        :model="queryParams"
        ref="queryForm"
        :inline="true"
        label-width="68px"
        class="el-form-search">
        <el-form-item prop="query">
          <el-input placeholder="数据字段名称/编码查询" v-model="queryParams.query" size="mini"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary"
            icon="el-icon-search"
            size="mini"
            @click="handleQuery">搜索</el-button>
          <el-button 
            icon="el-icon-refresh"
            size="mini"
            @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-row>
    <el-row class="mb10">
      <el-button 
        plain
        type="info"
        icon="el-icon-back"
        size="mini"
        @click="handleGoBack">返回模型列表</el-button>
      <el-button 
        plain
        type="primary"
        icon="el-icon-plus"
        size="mini"
        v-hasPermi="[ 'cms:exmodel:add', 'cms:exmodel:edit' ]"
        @click="handleAdd">新建</el-button>
      <el-button 
        type="danger"
        icon="el-icon-delete"
        size="mini"
        :disabled="multiple"
        v-hasPermi="[ 'cms:exmodel:add', 'cms:exmodel:edit' ]"
        @click="handleBatchDelete">删除</el-button>
    </el-row>
    <el-row>
      <el-table v-loading="loading" :data="fieldList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="名称" align="center" prop="name" />
        <el-table-column label="编码" align="center" prop="code" />
        <el-table-column label="控件类型" align="center" prop="controlType">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.MetaControlType" :value="scope.row.controlType"/>
          </template>
        </el-table-column>
        <el-table-column label="对应数据表字段" align="center" prop="fieldName" />
        <el-table-column label="是否必填" align="center" prop="mandatoryFlag">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.YesOrNo" :value="scope.row.mandatoryFlag"/>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="300" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              v-hasPermi="[ 'cms:exmodel:add', 'cms:exmodel:edit' ]"
              @click="handleEdit(scope.row)">修改</el-button>
            <el-button 
              size="mini"
              type="text"
              icon="el-icon-delete"
              v-hasPermi="[ 'cms:exmodel:add', 'cms:exmodel:edit' ]"
              @click="handleDelete(scope.row)">删除</el-button>
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
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" class="el-form-dialog">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item v-if="!isDefaultTable" label="数据表字段" prop="fieldName">
          <el-select v-model="form.fieldName" placeholder="请选择" size="small">
            <el-option
              v-for="fieldName in tableFields"
              :key="fieldName"
              :label="fieldName"
              :value="fieldName"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isDefaultTable" label="字段类型" prop="fieldType">
          <el-select v-model="form.fieldType" placeholder="请选择" size="small">
            <el-option
              v-for="dict in dict.type.MetaFieldType"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="控件类型" prop="controlType">
          <el-select v-model="form.controlType" placeholder="请选择" size="small">
            <el-option
              v-for="dict in dict.type.MetaControlType"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否必填" prop="mandatoryFlag">
          <el-select v-model="form.mandatoryFlag" placeholder="请选择" size="small">
            <el-option
              v-for="dict in dict.type.YesOrNo"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="默认值" prop="defaultValue">
          <el-input v-model="form.defaultValue" />
        </el-form-item>
        <el-form-item v-if="showOptions" label="可选项配置" prop="options">
          <div>
            <el-radio-group v-model="form.options.type">
              <el-radio label="text">手动输入</el-radio>
              <el-radio label="dict">字典数据</el-radio>
            </el-radio-group>
          </div>
          <div>
            <el-input v-if="form.options.type==='text'" type="textarea" v-model="form.options.value" />
            <el-input v-if="form.options.type==='dict'" v-model="form.options.value" />
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="handleAddSave">确 定</el-button>
        <el-button @click="closeDialog(false)">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
.el-form-item {
  margin-bottom: 18px;
}
</style>
<script>
import { addXModelField, editXModelField, deleteXModelField, listXModelField, listXModelTableFields } from "@/api/contentcore/exmodel";

export default {
  name: "CMSEXModelField",
  dicts: [ 'MetaFieldType', 'MetaControlType', 'YesOrNo' ],
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
      // 表单参数
      form: {
        options:{ type: "text" }
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]*$", message: "不能为空且只能使用字母、数字和下划线", trigger: "blur" }
        ],
        controlType: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        mandatoryFlag: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        fieldType: [
          { required: true, validator: (rule, value, callback) => {
                if (this.isDefaultTable && (!value || value == null || value == '')) {
                  return callback(new Error("字段类型不能为空"));
                }
                callback();
              }, trigger: "blur" }
        ],
        fieldName: [
          { required: true, validator: (rule, value, callback) => {
                if (!this.isDefaultTable) {
                  if (!value || value == null || value == '') {
                    return callback(new Error("数据表字段不能为空"));
                  }
                  for(let i = 0; i < this.fieldList.length; i++) {
                    if (this.fieldList[i].fieldName == value) {
                      return callback(new Error("数据表字段已占用"));
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
    this.loadXModelFieldList();
    if (!this.isDefaultTable) {
      this.loadXmodelUsableFields();
    }
  },
  methods: {
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
      this.title = "添加字段";
      this.open = true;
    },
    handleEdit (row) {
      this.form = row;
      this.title = "编辑字段信息";
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
      this.$modal.confirm("确认删除？").then(function () {
        return deleteXModelField(fieldIds);
      }).then(() => {
        this.$modal.msgSuccess("删除成功");
        this.loadXModelFieldList();
      }).catch(function () { });
    },
    handleGoBack() {
      this.$router.push({ path: "/configs/exmodel" });
    }
  }
};
</script>