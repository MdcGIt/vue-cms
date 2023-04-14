<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-row>
          <el-button 
            plain
            type="primary"
            icon="el-icon-plus"
            size="mini"
            @click="handleAdd">新建</el-button>
          <el-button 
            plain
            type="danger"
            icon="el-icon-delete"
            size="mini"
            :disabled="multiple"
            @click="handleBatchDelete">删除</el-button>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input placeholder="输入模型名称/编码查询" v-model="queryParams.query"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="el-icon-search"
                @click="handleQuery">搜索</el-button>
              <el-button 
                icon="el-icon-refresh"
                @click="resetQuery">重置</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-row>
      <el-table v-loading="loading" :data="xmodelList" @selection-change="handleSelectionChange">
      <el-table-column type="selection"
                       width="55"
                       align="center" />
        <el-table-column label="名称">
          <template slot-scope="scope">
            <el-link type="primary"
                      @click="handleOpenModelField(scope.row)"
                      class="link-type">
              <span>{{ scope.row.name }}</span>
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="编码" prop="code" />
        <el-table-column label="分类" prop="ownerType" />
        <el-table-column label="数据表" prop="tableName" />
        <el-table-column label="操作" align="center" width="300" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button 
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)">修改</el-button>
            <el-button 
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination 
        v-show="xmodelTotal>0"
        :total="xmodelTotal"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadXModelList" />
    </el-row>
    <!-- 添加/编辑模型弹窗 -->
    <el-dialog 
      :title="title"
      :visible.sync="open"
      width="600px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form 
        ref="form"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="el-form-dialog">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="数据存储表" prop="tableName">
          <el-select v-model="form.tableName" filterable placeholder="请选择">
            <el-option
              v-for="item in xmodelDataTableList"
              :key="item"
              :label="item"
              :value="item">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleAddSave">确 定</el-button>
        <el-button @click="closeDialog(false)">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { addXModel, editXModel, deleteXModel, listXModel, listXModelDataTable } from "@/api/contentcore/exmodel";

export default {
  name: "CMSEXmodel",
  data () {
    return {
      // 遮罩层
      loading: false,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      xmodelDataTableList: [],
      xmodelList: undefined,
      xmodelTotal: 0,
      selectedRows: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        query: undefined
      },
      // 表单参数
      form: {
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]*$", message: "不能为空且只能使用字母、数字和下划线", trigger: "blur" }
        ],
        tableName: [
          { required: true, message: "数据表不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created () {
    this.loadXModelDataTableList();
    this.loadXModelList();
  },
  methods: {
    loadXModelList () {
      this.loading = true;
      listXModel(this.queryParams).then(response => {
        this.xmodelList = response.data.rows;
        this.xmodelTotal = parseInt(response.data.total);
        this.loading = false;
      });
    },
    loadXModelDataTableList() {
      listXModelDataTable().then(response => {
        this.xmodelDataTableList = response.data.rows;
      });
    },
    handleSelectionChange (selection) {
      this.single = selection.length != 1
      this.multiple = !selection.length
      this.selectedRows = selection;
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadXModelList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    closeDialog (loadList) {
      this.open = false;
      this.form = {};
      if (loadList) this.loadXModelList();
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.form = {};
      this.title = "新建模型";
      this.open = true;
    },
    handleEdit (row) {
      this.form = row;
      this.title = "编辑模型";
      this.open = true;
    },
    handleAddSave () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.modelId) {
            editXModel(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.closeDialog(true);
            }); 
          } else {
            addXModel(this.form).then(response => {
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
    doDelete (xmodels) {
      this.$modal.confirm("是否确认删除？").then(function () {
        return deleteXModel(xmodels);
      }).then(() => {
        this.$modal.msgSuccess("删除成功");
        this.loadXModelList();
      }).catch(function () { });
    },
    handleOpenModelField (row) {
      this.$router.push({ path: "/cms/exmodel/field", query: { modelId: row.modelId, isDefaultTable: row.tableName == 'x_model_data' } });
    }
  }
};
</script>