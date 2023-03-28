<template>
  <div class="app-container">
    <el-row>
      <el-col>
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" size="small" placeholder="输入友链名称/链接查询">
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary"
              icon="el-icon-search"
              size="small"
              @click="handleQuery">搜索</el-button>
            <el-button 
              icon="el-icon-refresh"
              size="small"
              @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-row :gutter="10"
            class="mb8">
      <el-col :span="1.5">
        <el-button 
          type="info"
          icon="el-icon-back"
          size="mini"
          plain
          @click="handleGoBack">返回</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="el-icon-plus"
          size="mini"
          plain
          v-hasPermi="[ 'cms:friendlink:add' ]"
          @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="success"
          icon="el-icon-edit"
          size="mini"
          plain
          :disabled="single"
          v-hasPermi="[ 'cms:friendlink:add', 'cms:friendlink:edit' ]"
          @click="handleEdit">编辑</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="danger"
          icon="el-icon-delete"
          size="mini"
          plain
          :disabled="multiple"
          v-hasPermi="[ 'cms:friendlink:delete' ]"
          @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-row>
      <el-col>
        <el-table 
          v-loading="loading"
          :data="linkList"
          @selection-change="handleSelectionChange">
          <el-table-column 
            type="selection"
            width="50"
            align="center" />
          <el-table-column 
            type="index"
            label="序号"
            align="center"
            width="50" />
          <el-table-column 
            label="Logo"
            align="left"
            width="100"
            prop="name">
            <template slot-scope="scope">
              <el-image v-if="scope.row.src!=null&&scope.row.src!=''" :src="scope.row.src"
                style="height: 60px;"
                fit="scale-down">
              </el-image>
            </template>
          </el-table-column>
          <el-table-column 
            label="名称"
            align="left"
            width="300"
            prop="name">
          </el-table-column>
          <el-table-column 
            label="链接"
            align="left"
            prop="url"/>
          <el-table-column 
            label="最近修改时间"
            align="center"
            width="160">
            <template slot-scope="scope">
              <span v-if="scope.row.updateTime!=null">{{ parseTime(scope.row.updateTime) }}</span>
              <span v-else>{{ parseTime(scope.row.createTime) }}</span>
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
                v-hasPermi="[ 'cms:friendlink:add', 'cms:friendlink:edit' ]"
                @click="handleEdit(scope.row)">编辑</el-button>
              <el-button 
                size="mini"
                type="text"
                icon="el-icon-delete"
                v-hasPermi="[ 'cms:friendlink:delete' ]"
                @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col> 
    </el-row>
    <!-- 添加或修改弹窗 -->
    <el-dialog 
      :title="title"
      :visible.sync="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="链接" prop="url">
          <el-input v-model="form.url" placeholder="http(s)://" />
        </el-form-item>
        <el-form-item label="Logo" prop="logo">
          <cms-logo-view 
            v-model="form.logo" 
            :src="form.src"
            :width="218" 
            :height="150">
          </cms-logo-view>
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
<style scoped>
</style>
<script>
import { urlValidator } from '@/utils/validate'
import { getLinkList, addLink, editLink, deleteLink } from "@/api/link/link";
import CMSLogoView from '@/views/cms/components/LogoView';

export default {
  name: "CmsLink",
  components: {
    "cms-logo-view": CMSLogoView
  },
  data () {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      selectedRows: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 资源表格数据
      linkList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        query: undefined,
        groupId: this.$route.query.groupId,
        pageSize: 20,
        pageNo: 1
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        url: [
          { required: true, message: "链接不能为空", trigger: "blur" },
          { trigger: "blur", validator: urlValidator }
        ]
      }
    };
  },
  created () {
    this.loadListData();
  },
  methods: {
    loadListData () {
      this.loading = true;
      getLinkList(this.queryParams).then(response => {
        this.linkList = response.data.rows;
        this.loading = false;
      });
    },
    cancel () {
      this.open = false;
      this.reset();
    },
    reset () {
      this.form = {};
    },
    handleQuery () {
      this.queryParams.pageNo = 1;
      this.loadListData();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.selectedRows = selection.map(item => item);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleAdd () {
      this.reset();
      this.title = "添加友情链接";
      this.open = true;
    },
    handleEdit (row) {
      this.reset();
      this.title = "编辑友情链接信息";
      this.form = row;
      this.open = true;
    },
    /** 提交按钮 */
    submitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.groupId = this.queryParams.groupId;
          if (this.form.linkId) {
            editLink(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.open = false;
              this.loadListData();
            }); 
          } else {
            addLink(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.open = false;
              this.loadListData();
            }); 
          }
        }
      });
    },
    handleDelete (row) {
      const rows = row.linkId ? [{ linkId: row.linkId }] : this.selectedRows
      this.$modal.confirm('是否确认删除选中的友链?').then(function() {
        return deleteLink(rows);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.loadListData();
      }).catch(() => {});
    },
    handleGoBack() {
      const obj = { path: "/interact/link" };
      this.$tab.closeOpenPage(obj);
    },
  }
};
</script>