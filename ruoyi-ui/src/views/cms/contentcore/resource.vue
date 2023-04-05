<template>
  <div class="app-container">
    <el-form :model="queryParams"
             ref="queryForm"
             :inline="true"
             label-width="68px"
             class="el-form-search">
      <el-form-item prop="name" style="padding:2px;">
        <el-input placeholder="资源名称" v-model="queryParams.name" size="small">
          <el-select v-model="queryParams.resourceType" slot="prepend" placeholder="类型" size="small" style="width:80px;">
            <el-option
              v-for="rt in resourceTypes"
              :key="rt.id"
              :label="rt.name"
              :value="rt.id"
            />
          </el-select>
        </el-input>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRange"
                        size="small"
                        style="width: 240px"
                        value-format="yyyy-MM-dd"
                        type="daterange"
                        range-separator="-"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"></el-date-picker>
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

    <el-row :gutter="10"
            class="mb8">
      <el-col :span="1.5">
        <el-button type="primary"
                   icon="el-icon-plus"
                   size="mini"
                   @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success"
                   icon="el-icon-edit"
                   size="mini"
                   :disabled="single"
                   @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger"
                   icon="el-icon-delete"
                   size="mini"
                   :disabled="multiple"
                   @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading"
              :data="resourceList"
              @selection-change="handleSelectionChange">
      <el-table-column type="selection"
                       width="55"
                       align="center" />
      <el-table-column label="ID"
                       align="center"
                       prop="resourceId"
                       width="180" />
      <el-table-column prop="resourceTypeName" label="类型" width="80" />
      <el-table-column label="名称"
                       align="left"
                       prop="name">
        <template slot-scope="scope">
          <el-link 
            type="primary"
            target="_blank"
            :href="scope.row.src">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="存储方式"
                       align="center"
                       width="80"
                       prop="storageType" />
      <el-table-column label="大小"
                       align="center"
                       width="80"
                       prop="fileSizeName" />
      <el-table-column label="创建时间"
                       align="center"
                       prop="createTime"
                       width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        label="操作"
        align="center"
        width="180" 
        class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)">修改</el-button>
          <el-button 
            size="mini"
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
                @pagination="getList" />

    <!-- 添加或修改资源对话框 -->
    <el-dialog :title="title"
               :visible.sync="open"
               width="500px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="80px">
        <el-form-item label="上传资源">
          <el-upload 
            ref="upload"
            drag
            :data="form"
            :action="upload.url"
            :headers="upload.headers"
            :file-list="upload.fileList"
            :on-progress="handleFileUploadProgress"
            :on-success="handleFileSuccess"
            :auto-upload="false"
            :before-upload="handleBeforeUpload"
            :on-change="handleUploadChange"
            :limit="1">
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
              <div class="el-upload__tip" slot="tip">只能上传jpg/png文件，且不超过500kb</div>
            </el-upload>
        </el-form-item>
        <el-form-item label="资源名称"
                      prop="name">
          <el-input v-model="form.name"
                    placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="备注"
                      prop="remark">
          <el-input v-model="form.remark"
                    type="textarea"
                    placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" :loading="upload.isUploading" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
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
<script>
import { getToken } from "@/utils/auth";
import { getResourceTypes, getResrouceList, getResourceDetail, delResource } from "@/api/contentcore/resource";

export default {
  name: "CmsResource",
  data () {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 资源表格数据
      resourceList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        resourceType: undefined,
        name: undefined,
        beginTime: undefined,
        endTime: undefined
      },
      resourceTypes: [],
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "资源名称不能为空", trigger: "blur" }
        ]
      },
      // 上传参数
      upload: {
        // 是否禁用上传
        isUploading: false,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/cms/resource",
        // 上传的文件列表
        fileList: []
      },
    };
  },
  created () {
    this.loadResourceTypes();
    this.getList();
  },
  methods: {
    loadResourceTypes() {
      getResourceTypes().then(response => {
        this.resourceTypes = response.data;
      });
    },
    /** 查询资源列表 */
    getList () {
      this.loading = true;
      if (this.dateRange.length == 2) {
        this.queryParams.beginTime = this.dateRange[0];
        this.queryParams.endTime = this.dateRange[1];
      }
      getResrouceList(this.queryParams).then(response => {
        this.resourceList = response.data.rows;
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
      this.form = {
        name: undefined,
        remark: undefined
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm");
      this.queryParams.resourceType = undefined;
      this.dateRange = [];
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.resourceId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = "添加资源";
    },
    /** 修改按钮操作 */
    handleUpdate (row) {
      this.reset();
      const resourceId = row.resourceId || this.ids
      getResourceDetail(resourceId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改资源";
      });
    },
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    handleFileSuccess(response, file, fileList) {
      this.upload.isUploading = false;
      if (response.code === 200) {
        this.$modal.msgSuccess(this.form.resourceId != undefined ? "修改成功" : "添加成功");
        this.open = false;
        this.getList();
      } else {
        this.$modal.msgError(response.msg);
      }
      this.$refs.upload.clearFiles();
      this.resetForm("form");
    },
    handleUploadChange(file) {
      file.name = file.name.toLowerCase();
      if (!file.name.endsWith(".png") && !file.name.endsWith(".jpg")) {
        this.$modal.msgError("文件格式错误，请上传图片类型,如：.jpg，.png后缀的文件。");
        this.upload.fileList = [];
        return;
      }
      this.form.name = file.name;
    },
    handleBeforeUpload(file) {
      return true;
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.$refs.upload.submit();
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete (row) {
      const resourceIds = row.resourceId || this.ids;
      this.$modal.confirm("是否确认删除？").then(function () {
        return delResource(resourceIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(function () { });
    }
  }
};
</script>