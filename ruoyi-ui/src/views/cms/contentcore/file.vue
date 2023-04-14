<template>
  <div class="app-container cms-file">
    <el-container>
      <el-aside width="220">
        <el-scrollbar :style="treeSideStyle">
          <el-tree :data="directoryTreeData" 
                  :props="defaultProps" 
                  :expand-on-click-node="false"
                  :filter-node-method="filterNode"
                  v-loading="treeLoading"
                  node-key="id"
                  ref="tree"
                  highlight-current
                  @node-click="handleNodeClick">
          </el-tree>
        </el-scrollbar>
      </el-aside>
      <el-container>
        <el-header style="height: 70px;">
          <div class="btn-toolbar">
            <el-button 
              type="primary"
              size="mini"
              plain
              icon="el-icon-plus"
              :disabled="disableAdd"
              @click="handleAdd">新建</el-button>
            <el-button 
              type="primary"
              size="mini"
              plain
              icon="el-icon-upload2"
              :disabled="disableAdd"
              @click="handleUpload">上传</el-button>
            <el-button 
              type="danger"
              size="mini"
              plain
              icon="el-icon-delete"
              :disabled="disableAdd||multiple"
              @click="handleDelete()">删除</el-button>
          </div>
          <el-card shadow='hover' class="directory-toolbar" style="padding: 0, 10px">
            <span class="span-path">
              <el-button type="text" icon="el-icon-folder" @click="handlePathClick(-1)"></el-button>
              <span class="path-spliter">/</span>
            </span>
            <span class="span-path" v-for="(item, index) in pathArray" :key="item" >
              <span v-if="index" class="path-spliter">/</span>
              <el-button type="text" @click="handlePathClick(index)" style="margin-left:0;">{{item}}</el-button>
            </span>
          </el-card>
        </el-header>
        <el-main>
          <el-table v-loading="loading"
                  :data="fileList"
                  @selection-change="handleSelectionChange">
            <el-table-column type="selection"
                            width="50"
                            align="center" />
            <el-table-column label="文件名"
                            align="left"
                            prop="fileName">
              <template slot-scope="scope">
                <i v-if="scope.row.isDirectory" class="el-icon-folder"></i>
                <el-button v-if="scope.row.isDirectory" type="text" @click="handleDirectoryClick(scope.row)">{{ scope.row.fileName }}</el-button>
                <span v-else>{{ scope.row.fileName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="大小"
                            align="center"
                            width="160" 
                            prop="fileSize"/>
            <el-table-column label="最后修改时间"
                            align="center"
                            width="200" 
                            prop="modifyTime"/>
            <el-table-column label="操作"
                            align="center"
                            width="180" 
                            class-name="small-padding fixed-width">
              <template slot-scope="scope">
                <el-popover style="margin-right:10px;"
                  v-if="!disableAdd"
                  placement="top"
                  width="200"
                  v-model="scope.row.showRename">
                  <el-input v-model="scope.row.rename" size="mini" placeholder="请输入文件名" />
                  <div style="text-align: right; margin-top: 5px;">
                    <el-button size="mini" type="text" @click="scope.row.showRename = false">取消</el-button>
                    <el-button type="primary" size="mini" @click="handleRename(scope.row)">确定</el-button>
                  </div>
                  <el-button 
                    slot="reference" 
                    size="mini"
                    type="text"
                    icon="el-icon-edit"
                  >重命名</el-button>
                </el-popover>
                <el-button size="mini"
                          v-if="scope.row.canEdit"
                          type="text"
                          icon="el-icon-edit"
                          @click="handleEdit(scope.row)">编辑</el-button>
                <el-button size="mini"
                          v-if="scope.row.isImage"
                          type="text"
                          icon="el-icon-crop"
                          @click="handleCrop(scope.row)">裁剪</el-button>
                <el-button size="mini"
                          v-if="!disableAdd"
                          type="text"
                          icon="el-icon-delete"
                          @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-main>
      </el-container>
    </el-container>
    <!-- 添加文件或目录 -->
    <el-dialog title="新建文件/目录"
               :visible.sync="openAddDialog"
               width="500px"
               append-to-body>
      <el-form ref="addForm"
               :model="addForm"
               :rules="rules"
               label-width="80px">
        <el-form-item label="类型"
                      prop="isDirectory">
          <el-radio-group v-model="addForm.isDirectory" size="medium">
            <el-radio-button :label="false">文件</el-radio-button>
            <el-radio-button :label="true">目录</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="名称"
                      prop="name">
          <el-input v-model="addForm.fileName" size="medium" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="handleAddSubmit">确 定</el-button>
        <el-button @click="handleAddClose">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 上传文件对话框 -->
    <el-dialog title="上传文件"
               :visible.sync="openUploadDialog"
               width="400px"
               append-to-body>
      <el-form ref="uploadForm"
               :model="uploadForm"
               label-width="0">
        <el-form-item>
          <el-upload 
            ref="upload"
            drag
            :data="uploadForm"
            :action="upload.url"
            :headers="upload.headers"
            :file-list="upload.fileList"
            :on-progress="handleFileUploadProgress"
            :on-success="handleFileSuccess"
            :auto-upload="false"
            :limit="1">
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" :loading="upload.isUploading" @click="handleUploadSubmit">确 定</el-button>
        <el-button @click="handleUploadClose">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getToken } from "@/utils/auth";
import { isImage } from "@/utils/ruoyi";
import { getDirectoryTreeData, getFileList, renameFile, addFile, editFile, deleteFile } from "@/api/contentcore/file";

export default {
  name: "CMSFile",
  data () {
    return {
      treeSideHeight: 600,
      // 遮罩层
      treeLoading: false,
      loading: false,
      // 选中数组
      selectedRows: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 目录树
      directoryTreeData: [],
      selectedDirectory: "/",
      pathArr: [],
      // 文件列表
      fileList: [],
      // 搜索文件名
      fileName: "",
      // 新建文件/目录弹窗
      openAddDialog: false,
      addForm: {},
      // 表单校验
      rules: {
        filePath: [
          { required: true, pattern: "^[A-Za-z0-9_]*$", message: "不能为空且只能使用字母、数字和下划线", trigger: "blur" }
        ]
      },
      openUploadDialog: false,
      uploadForm: {},
      // 上传参数
      upload: {
        // 是否禁用上传
        isUploading: false,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/cms/file/upload",
        // 上传的文件列表
        fileList: []
      },
    };
  },
  computed: {
    treeSideStyle() {
      return { height: this.treeSideHeight + 'px' };
    },
    pathArray() {
      if (this.selectedDirectory == "/") {
        return [];
      }
      return this.selectedDirectory.split("/");
    },
    disableAdd() {
      return this.selectedDirectory == "" || this.selectedDirectory == "/"
    }
  },
  watch: {
    selectedDirectory(newVal) {
      this.pathArr = newVal.split("/");
    }
  },
  created () {
    let height = document.body.offsetHeight;
    this.treeSideHeight = height - 150;
    this.loadDirectory();
    this.loadFileList();
  },
  methods: {
    filterNode (value, data) {
      if (!value) return true;
      return data.label.indexOf(value) > -1;
    },
    handleNodeClick (data) {
      this.selectedDirectory = data.id;
      this.loadFileList();
    },
    handleDirectoryClick(row) {
      this.selectedDirectory = row.filePath + "/";
      this.loadFileList();
    },
    handlePathClick (index) {
      if (index == -1) {
        this.selectedDirectory = "/";
      } else {
        this.selectedDirectory = this.pathArray.slice(0, index + 1).join("/") + "/";
      }
      this.loadFileList();
    },
    loadDirectory() {
      this.treeLoading = true;
      getDirectoryTreeData().then(response => {
        this.directoryTreeData = response.data;
        this.treeLoading = false;
      });
    },
    loadFileList() {
      if (this.selectedDirectory === '') {
        this.$modal.msgError("请先选择一个目录");
        return;
      }
      this.loading = true;
      getFileList(this.selectedDirectory, this.fileName).then(response => {
        if (response.code == 200) {
          this.fileList = response.data;
          this.fileList.forEach(f => {
              f.isImage = isImage(f.fileName);
          });
        }
        this.loading = false;
      });
    },
    handleQuery () {
      this.loadFileList();
    },
    // 多选框选中数据
    handleSelectionChange (selection) {
      this.selectedRows = selection.map(item => item);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleAdd () {
      this.addForm = { isDirectory: false, filePath: "" };
      this.openAddDialog = true;
    },
    handleAddSubmit () {
      const params = {
        dir: this.selectedDirectory,
        fileName: this.addForm.fileName,
        isDirectory: this.addForm.isDirectory
      };
      addFile(params).then(response => {
        this.$modal.msgSuccess("操作成功");
        this.openAddDialog = false;
        this.loadFileList();
      });
    },
    handleAddClose () {
      this.openAddDialog = false;
    },
    handleRename (row) {
      const params = {
        filePath: row.filePath,
        rename: row.rename,
        isDirectory: row.isDirectory
      };
      renameFile(params).then(response => {
        this.$modal.msgSuccess("操作成功");
        row.showRename = false;
        this.loadFileList();
      });
    },
    handleEdit (row) {
      this.$router.push({ 
        path: "/cms/file/editor", 
        query: { 
          filePath: row.filePath 
        } 
      });
    },
    // TODO 裁剪图片
    handleCrop (row) {

    },
    handleDelete (row) {
      const rows = row ? [ row ] : this.selectedRows
      this.$modal.confirm("是否确认删除？").then(function () {
        return deleteFile(rows);
      }).then(() => {
        this.$modal.msgSuccess("操作成功");
        this.loadFileList();
      }).catch(function () { });
    },
    handleUpload () {
      this.openUploadDialog = true;
    },
    handleUploadClose () {
      this.openUploadDialog = false;
    },
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    handleFileSuccess(response, file, fileList) {
      this.upload.isUploading = false;
      if (response.code === 200) {
        this.$modal.msgSuccess("上传成功");
        this.open = false;
        this.loadFileList();
      } else {
        this.$modal.msgError(response.msg);
      }
      this.$refs.upload.clearFiles();
      this.resetForm("uploadForm");
    },
    handleUploadSubmit () {
      this.$refs["uploadForm"].validate(valid => {
        if (valid) {
          this.uploadForm.dir = this.selectedDirectory;
          this.$refs.upload.submit();
        }
      });
    }
  }
};
</script>
<style>
.cms-file .el-card__body {
  padding: 0 10px;
}
.cms-file .el-aside {
  width: 220px;
  padding: 0;
  border-radius: 4px;
  border: 1px solid #e6ebf5;
  background-color: #fff;
}
.cms-file .btn-toolbar {
  margin-bottom: 10px;
}
.cms-file .btn-toolbar .el-button {
  margin-right: 10px;
  margin-left: 0;
}
.cms-file .el-scrollbar__wrap {
  overflow-x: hidden;
}
.cms-file .span-path {
  line-height: 30px;
}
.cms-file .path-spliter {
  display: inline-block;
  padding: 0 5px;
}
</style>