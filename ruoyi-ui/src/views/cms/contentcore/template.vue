<template>
  <div class="app-container">
    <el-row>
      <el-col>
        <el-form :model="queryParams"
                ref="queryForm"
                :inline="true"
                label-width="68px"
                class="el-form-search">
          <el-form-item label="发布通道" prop="publishPipeCode">
            <el-select v-model="queryParams.publishPipeCode" size="small" style="width:120px;">
              <el-option
                v-for="pp in publishPipes"
                :key="pp.pipeCode"
                :label="pp.pipeName"
                :value="pp.pipeCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="模板名称" prop="filename">
            <el-input v-model="queryParams.filename" size="small">
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary"
                      icon="el-icon-search"
                      size="small"
                      @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh"
                      size="small"
                      @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-row :gutter="10"
            class="mb8">
      <el-col :span="1.5">
        <el-button type="primary"
                   icon="el-icon-plus"
                   size="mini"
                   plain
                   @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success"
                   icon="el-icon-edit"
                   size="mini"
                   plain
                   :disabled="single"
                   @click="handleEdit">编辑</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger"
                   icon="el-icon-delete"
                   size="mini"
                   plain
                   :disabled="multiple"
                   @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-row>
      <el-col>
        <el-table v-loading="loading"
                  :data="templateList"
                  @selection-change="handleSelectionChange"
                  @row-dblclick="handleEdit">
          <el-table-column type="selection"
                          width="50"
                          align="center" />
          <el-table-column type="index"
                          label="序号"
                          align="center"
                          width="50" />
          <el-table-column label="发布通道"
                            align="center"
                            width="120"
                            prop="publishPipeCode" />
          <el-table-column label="模板名称"
                            align="left"
                            :show-overflow-tooltip="true"
                            prop="path">
          </el-table-column>
          <el-table-column label="备注"
                            align="left"
                            :show-overflow-tooltip="true"
                            prop="remark">
          </el-table-column>
          <el-table-column label="大小"
                            align="right"
                            width="160"
                            prop="filesizeName" />
          <el-table-column label="更新时间"
                            align="center"
                            width="160"
                            prop="modifyTime" />
          <el-table-column label="操作"
                          align="center"
                          width="180" 
                          class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button size="mini"
                        type="text"
                        icon="el-icon-edit"
                        @click="handleRename(scope.row)">重命名</el-button>
              <el-button size="mini"
                        type="text"
                        icon="el-icon-edit"
                        @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="mini"
                        type="text"
                        icon="el-icon-delete"
                        @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="getList"
        />
      </el-col> 
    </el-row>
    <!-- 添加或修改模板文件对话框 -->
    <el-dialog :title="title"
               :visible.sync="open"
               width="500px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="80px">
        <el-form-item v-if="!form.templateId||form.templateId==0" label="发布通道" prop="publishPipeCode">
          <el-select v-model="form.publishPipeCode" >
            <el-option
              v-for="pp in publishPipes"
              :key="pp.pipeCode"
              :label="pp.pipeName"
              :value="pp.pipeCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"
                      prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="备注"
                      prop="remark">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { getConfigKey } from "@/api/system/config"
import { getTemplateList, getTemplateDetail, renameTemplate, addTemplate, delTemplate } from "@/api/contentcore/template";

export default {
  name: "CmsTemplate",
  data () {
    var validatePath = (rule, value, callback) => {
        if (value === '' || !value.endsWith(this.templateSuffix)) {
          callback(new Error("模板文件名称不能为空且后缀必须为：" + this.templateSuffix));
        } else {
          callback();
        }
      };
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      selectedIds: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 资源表格数据
      templateList: [],
      total: 0,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      publishPipes: [],
      // 查询参数
      queryParams: {
        publishPipeCode: undefined,
        filename: undefined
      },
      templateSuffix: ".template.html",
      isRename: false,
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        path: [
          { trigger: "blur", validator: validatePath }
        ]
      }
    };
  },
  created () {
    getPublishPipeSelectData().then(response => {
        this.publishPipes = response.data.rows;
      });
    getConfigKey("CMSTemplateSuffix").then(response => {
      this.templateSuffix = response.data;
    })
    this.getList();
  },
  methods: {
    /** 查询资源列表 */
    getList () {
      this.loading = true;
      getTemplateList(this.queryParams).then(response => {
        this.templateList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    // 取消按钮
    cancel () {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset () {
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange (selection) {
      this.selectedIds = selection.map(item => item.templateId);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.form = {};
      this.open = true;
      this.title = "添加模板文件";
    },
    /** 修改按钮操作 */
    handleRename (row) {
      this.form = {};
      getTemplateDetail(row.templateId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改模板文件名";
      });
    },
    handleEdit (row) {
      this.$router.push({ 
        path: "/cms/template/editor", 
        query: { 
          id: row.templateId
        } 
      });
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.templateId) {
            renameTemplate(this.form).then(response => {
              this.$modal.msgSuccess("操作成功");
              this.open = false;
              this.getList();
            }); 
          } else {
            addTemplate(this.form).then(response => {
              this.$modal.msgSuccess("操作成功");
              this.open = false;
              this.getList();
            }); 
          }
          
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete (row) {
      const templateIds = row ? [ row.templateId ] : this.selectedIds
      this.$modal.confirm("是否确认删除？").then(function () {
        return delTemplate(templateIds);
      }).then(() => {
        this.$modal.msgSuccess("操作成功");
        this.getList();
      }).catch(function () { });
    }
  }
};
</script>
<style scoped>
.time {
  font-size: 13px;
  color: #999;
}
.el-card {
  margin-bottom: 10px;
  padding: 10px;
}
.r-image {
  width: 130px;
}
.el-form-search {
  width: 100%;
}
</style>